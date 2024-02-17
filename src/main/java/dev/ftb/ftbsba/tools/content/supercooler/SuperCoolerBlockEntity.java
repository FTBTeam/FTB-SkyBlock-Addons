package dev.ftb.ftbsba.tools.content.supercooler;

import com.google.common.collect.Sets;
import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.content.core.*;
import dev.ftb.ftbsba.tools.recipies.SuperCoolerRecipe;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public class SuperCoolerBlockEntity extends AbstractMachineBlockEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuperCoolerBlockEntity.class);

    private final EmittingEnergy energyHandler = new EmittingEnergy(100000, 10000, 10000, (energy) -> this.setChanged());
    private final EmittingFluidTank fluidHandler = new EmittingFluidTank(10000, (tank) -> this.setChanged());
    private final IOStackHandler itemHandler = new IOStackHandler(3, 1, (container, ioType) -> {
        setChanged();
        if (ioType == IOStackHandler.IO.INPUT) {
            if (this.progress > 0) {
                this.progress = 0;
            }
        }
    });

    private final LazyOptional<EmittingEnergy> energy = LazyOptional.of(() -> energyHandler);
    private final LazyOptional<IFluidTank> tank = LazyOptional.of(() -> fluidHandler);
    private final LazyOptional<IOStackHandler> ioWrapper = LazyOptional.of(() -> itemHandler);

    private final FluidEnergyProcessorContainerData containerData = new FluidEnergyProcessorContainerData(this, this);

    private int progress = 0;
    private int progressRequired = 0;
    private SuperCoolerRecipe currentRecipe = null;
    private ResourceLocation pendingRecipeId = null;  // set when loading from NBT
    boolean tickLock = false;

    public SuperCoolerBlockEntity(BlockPos pos, BlockState state) {
        super(ToolsRegistry.SUPER_COOLER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public IOStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public void tickServer() {
        if (tickLock) {
            return;
        }

        if (!hasEnoughEnergy() || !hasAnyFluid() || !hasItemInAnySlot()) {
            setActive(false);
            progress = 0;
            return;
        }

        if (pendingRecipeId != null) {
            level.getServer().getRecipeManager().byKey(pendingRecipeId).ifPresent(r -> {
                if (r instanceof SuperCoolerRecipe s) {
                    currentRecipe = s;
                }
            });
            pendingRecipeId = null;
        }

        if (progress == 0) {
            currentRecipe = RecipeCaches.SUPER_COOLER.getCachedRecipe(this::findValidRecipe, itemHandler, fluidHandler)
                    .orElse(null);

            ItemStack outputStack = itemHandler.getOutput().getStackInSlot(0);
            if (currentRecipe == null || !outputStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(currentRecipe.result, outputStack)) {
                setActive(false);
                return;
            }

            progress = 1;
            progressRequired = currentRecipe.energyComponent.ticksToProcess();
        }

        if (currentRecipe != null) {
            if (progress == progressRequired && canAcceptOutput(currentRecipe)) {
                executeRecipe();
            } else if (progress < progressRequired) {
                if (fluidHandler.getFluid().containsFluid(currentRecipe.fluidIngredient)) {
                    // Use energy
                    setActive(true);
                    useEnergy();
                    progress++;
                }
            }
        }
    }

    public void executeRecipe() {
        if (this.currentRecipe == null) {
            breakProgress();
            return;
        }

        // Ensure enough fluid
        if (!fluidHandler.getFluid().containsFluid(currentRecipe.fluidIngredient)) {
            breakProgress();
            return;
        }

        // Ensure the items are OK
        // First test if we can extract the items by simulating and validating the result
        Set<Ingredient> requiredItems = Sets.newIdentityHashSet();
        requiredItems.addAll(currentRecipe.ingredients);

        ItemStackHandler inputHandler = itemHandler.getInput();
        BitSet extractingSlots = new BitSet(inputHandler.getSlots());  // track which slots we need to extract from

        for (var ingredient : requiredItems) {
            for (int i = 0; i < inputHandler.getSlots(); i++) {
                if (!extractingSlots.get(i) && ingredient.test(inputHandler.getStackInSlot(i))) {
                    if (inputHandler.extractItem(i, 1, true).isEmpty()) {
                        // this shouldn't happen, but let's be defensive
                        breakProgress();
                        currentRecipe = null;
                        return;
                    }
                    extractingSlots.set(i);
                }
            }
        }

        // Consume inputs, produce output
        if (extractingSlots.cardinality() == currentRecipe.ingredients.size()) {
            fluidHandler.drain(currentRecipe.fluidIngredient.getAmount(), IFluidHandler.FluidAction.EXECUTE);

            for (int i = 0; i < inputHandler.getSlots(); i++) {
                if (extractingSlots.get(i)) {
                    inputHandler.extractItem(i, 1, false);
                }
            }

            itemHandler.getOutput().insertItem(0, this.currentRecipe.result.copy(), false);
            breakProgress();
        }
    }

    private void useEnergy() {
        if (this.currentRecipe == null) {
            return;
        }

        var result = energyHandler.extractEnergy(this.currentRecipe.energyComponent.fePerTick(), true);
        if (result < this.currentRecipe.energyComponent.fePerTick()) {
            breakProgress();
            return;
        }

        energyHandler.extractEnergy(this.currentRecipe.energyComponent.fePerTick(), false);
    }

    /**
     * This will always force us back to the start of the recipe
     */
    private void breakProgress() {
        this.progress = 0;
        this.progressRequired = 0;
        this.currentRecipe = null;
        this.tickLock = false;
    }

    public boolean canAcceptOutput(SuperCoolerRecipe recipe) {
        var outputSlot = itemHandler.getOutput().getStackInSlot(0);

        if (outputSlot.isEmpty()) {
            return true;
        }

        int nItems = currentRecipe == null ? 0 : currentRecipe.result.getCount();

        // Do we have room for the result?
        if (outputSlot.getCount() >= outputSlot.getMaxStackSize() - nItems) {
            return false;
        }

        // Are the items the same?
        return ItemHandlerHelper.canItemStacksStack(outputSlot, recipe.result);
    }

    private boolean hasAnyFluid() {
        return !fluidHandler.isEmpty();
    }

    private boolean hasEnoughEnergy() {
        return energyHandler.getEnergyStored() > (currentRecipe == null ? 0 : currentRecipe.energyComponent.fePerTick());
    }

    private boolean hasItemInAnySlot() {
        var input = itemHandler.getInput();
        for (int i = 0; i < input.getSlots(); i++) {
            if (!input.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private Optional<SuperCoolerRecipe> findValidRecipe() {
        return level.getRecipeManager().getAllRecipesFor(ToolsRegistry.SUPER_COOLER_RECIPE_TYPE.get()).stream()
                .sorted((a, b) -> b.ingredients.size() - a.ingredients.size())  // prioritise recipes with more ingredients
                .filter(this::recipeMatchesInput)
                .findFirst();
    }

    private boolean recipeMatchesInput(SuperCoolerRecipe recipe) {
        if (!recipe.fluidIngredient.isFluidEqual(fluidHandler.getFluid())) {
            return false;
        }

        Set<Ingredient> inputSet = Sets.newIdentityHashSet();
        inputSet.addAll(recipe.ingredients);

        int found = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                Iterator<Ingredient> iter = inputSet.iterator();
                while (iter.hasNext()) {
                    Ingredient ingr = iter.next();
                    if (ingr.test(itemHandler.getStackInSlot(i))) {
                        iter.remove();
                        found++;
                        break;
                    }
                }
                if (found == recipe.ingredients.size()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return ioWrapper.cast();
        } else if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return tank.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ftbsba.super_cooler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory arg, Player arg2) {
        return new SuperCoolerContainer(i, arg, getBlockPos());
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        ioWrapper.invalidate();
        energy.invalidate();
        tank.invalidate();
    }

    @Override
    public void load(CompoundTag arg) {
        super.load(arg);

        itemHandler.getInput().deserializeNBT(arg.getCompound("input"));
        itemHandler.getOutput().deserializeNBT(arg.getCompound("output"));
        energyHandler.deserializeNBT(arg.get("energy"));
        fluidHandler.readFromNBT(arg.getCompound("fluid"));

        // Write the progress
        this.progress = arg.getInt("progress");
        this.progressRequired = arg.getInt("progressRequired");

        // Write the recipe id
        if (arg.contains("recipe")) {
            try {
                pendingRecipeId = new ResourceLocation(arg.getString("recipe"));
            } catch (ResourceLocationException e) {
                pendingRecipeId = null;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag arg) {
        super.saveAdditional(arg);

        arg.put("input", itemHandler.getInput().serializeNBT());
        arg.put("output", itemHandler.getOutput().serializeNBT());
        arg.put("energy", energyHandler.serializeNBT());
        arg.put("fluid", fluidHandler.writeToNBT(new CompoundTag()));

        // Write the progress
        arg.putInt("progress", this.progress);
        arg.putInt("progressRequired", this.progressRequired);

        // Write the recipe id
        if (this.currentRecipe != null) {
            arg.putString("recipe", this.currentRecipe.getId().toString());
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compoundTag = new CompoundTag();
        saveAdditional(compoundTag);
        return compoundTag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public int getEnergy() {
        return energyHandler.getEnergyStored();
    }

    @Override
    public int getMaxEnergy() {
        return energyHandler.getMaxEnergyStored();
    }

    @Override
    public FluidStack getFluid() {
        return fluidHandler.getFluid();
    }

    @Override
    public int getMaxFluid() {
        return fluidHandler.getCapacity();
    }

    @Override
    public void setEnergy(int energy) {
        energyHandler.overrideEnergy(energy);
    }

    @Override
    public void setFluid(FluidStack fluid) {
        fluidHandler.setFluid(fluid);
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public int getMaxProgress() {
        return progressRequired;
    }

    @Override
    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public void setMaxProgress(int maxProgress) {
        this.progressRequired = maxProgress;
    }

    @Override
    public ContainerData getContainerData() {
        return containerData;
    }

    @Override
    public void syncFluidTank() {
        fluidHandler.sync(this);
    }
}
