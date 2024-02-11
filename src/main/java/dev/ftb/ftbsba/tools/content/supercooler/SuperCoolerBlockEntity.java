package dev.ftb.ftbsba.tools.content.supercooler;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.content.core.*;
import dev.ftb.ftbsba.tools.recipies.SuperCoolerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuperCoolerBlockEntity extends BlockEntity implements MenuProvider, ProgressProvider, FluidEnergyProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuperCoolerBlockEntity.class);

    public LazyOptional<EmittingEnergy> energy = LazyOptional.of(() -> new EmittingEnergy(100000, 10000, 10000, (energy) -> this.setChanged()));
    public LazyOptional<IFluidTank> tank = LazyOptional.of(() -> new EmittingFluidTank(10000, (tank) -> this.setChanged()));
    public LazyOptional<IOStackHandler> ioWrapper = LazyOptional.of(() -> new IOStackHandler(3, 1, (container, ioType) -> {
        setChanged();
        if (ioType == IOStackHandler.IO.INPUT) {
            if (this.progress > 0) {
                this.progress = 0;
            }
        }
    }));

    FluidEnergyProcessorContainerData containerData = new FluidEnergyProcessorContainerData(this, this);

    int progress = 0;
    int progressRequired = 0;
    SuperCoolerRecipe processingRecipe = null;
    boolean tickLock = false;

    public SuperCoolerBlockEntity(BlockPos pos, BlockState state) {
        super(ToolsRegistry.SUPER_COOLER_BLOCK_ENTITY.get(), pos, state);
    }

    public static <T extends BlockEntity> void ticker(Level level, BlockPos pos, BlockState state, T t) {
        if (!(t instanceof SuperCoolerBlockEntity entity)) {
            return;
        }

        if (level.isClientSide || entity.tickLock) {
            return;
        }

        if (!entity.hasEnergy() || !entity.hasFluid() || !entity.hasItemInAnySlot()) {
            entity.progress = 0;
            return;
        }

        // We should test for the recipe here
        if (entity.progress == 0) {
            SuperCoolerRecipe recipe = entity.testForRecipe();
            if (recipe == null) {
                return;
            }

            if (!entity.canAcceptOutput(recipe)) {
                return;
            }

            entity.progress = 1;
            entity.processingRecipe = recipe;
            entity.progressRequired = recipe.energyComponent.ticksToProcess();
        }

        if (entity.processingRecipe != null) {
            if (entity.progress == entity.progressRequired) {
                entity.executeRecipe();
            } else {
                // Use energy
                entity.useEnergy();
                entity.progress++;
            }
        }
    }

    public void executeRecipe() {
        if (this.processingRecipe == null) {
            breakProgress();
            return;
        }

        // This shouldn't be possible, but we'll check because we have to
        var tank = this.tank.orElseThrow(RuntimeException::new);
        var ioWrapper = this.ioWrapper.orElseThrow(RuntimeException::new);

        // Extract the fluid
        int requiredFluid = this.processingRecipe.fluidIngredient.getAmount();
        var fluidResult = tank.drain(requiredFluid, IFluidHandler.FluidAction.SIMULATE);
        if (fluidResult.isEmpty() || fluidResult.getAmount() < requiredFluid) {
            breakProgress();
            return;
        }

        // Extract the items
        // First test if we can extract the items by simulating and validating the result
        var requiredItems = this.processingRecipe.ingredients;
        for (var ingredient : requiredItems) {
            for (int i = 0; i < ioWrapper.getInput().getSlots(); i++) {
                var stack = ioWrapper.getInput().getStackInSlot(i);
                if (ingredient.test(stack)) {
                    var result = ioWrapper.getInput().extractItem(i, 1, true);
                    if (result.isEmpty()) {
                        breakProgress();
                        return;
                    }
                }
            }
        }

        // Do everything
        tank.drain(requiredFluid, IFluidHandler.FluidAction.EXECUTE);
        for (var ingredient : requiredItems) {
            for (int i = 0; i < ioWrapper.getInput().getSlots(); i++) {
                var stack = ioWrapper.getInput().getStackInSlot(i);
                if (ingredient.test(stack)) {
                    // This logically can't be false due to the simulation above
                    ioWrapper.getInput().extractItem(i, 1, false);
                }
            }
        }

        // Produce the result
        ioWrapper.getOutput().insertItem(0, this.processingRecipe.result.copy(), false);
        breakProgress();
    }

    private void useEnergy() {
        if (this.processingRecipe == null) {
            return;
        }

        if (!this.energy.isPresent()) {
            breakProgress();
            return;
        }

        EmittingEnergy emittingEnergy = this.energy.orElseThrow(RuntimeException::new);
        var result = emittingEnergy.extractEnergy(this.processingRecipe.energyComponent.fePerTick(), true);
        if (result < this.processingRecipe.energyComponent.fePerTick()) {
            breakProgress();
            return;
        }

        emittingEnergy.extractEnergy(this.processingRecipe.energyComponent.fePerTick(), false);
    }

    /**
     * This will always force us back to the start of the recipe
     */
    private void breakProgress() {
        this.progress = 0;
        this.progressRequired = 0;
        this.processingRecipe = null;
        this.tickLock = false;
    }

    public boolean canAcceptOutput(SuperCoolerRecipe recipe) {
        var output = this.ioWrapper.orElseThrow(RuntimeException::new).getOutput();
        var outputSlot = output.getStackInSlot(0);

        if (outputSlot.isEmpty()) {
            return true;
        }

        // Do we have room for the result?
        if (outputSlot.getCount() >= outputSlot.getMaxStackSize()) {
            return false;
        }

        // Are the items the same?
        return outputSlot.sameItem(recipe.result);
    }

    private boolean hasFluid() {
        return tank.map(IFluidTank::getFluid).map(e -> !e.isEmpty()).orElse(false);
    }

    private boolean hasEnergy() {
        return energy.map(IEnergyStorage::getEnergyStored).orElse(0) > 0;
    }

    private boolean hasItemInAnySlot() {
        if (!ioWrapper.isPresent()) {
            return true;
        }

        var input = ioWrapper.orElseThrow(RuntimeException::new).getInput();
        for (int i = 0; i < input.getSlots(); i++) {
            if (!input.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Concerned this is too complex for a per tick operation.
     * @return The recipe to process, or null if no recipe can be processed
     */
    @Nullable
    private SuperCoolerRecipe testForRecipe() {
        LazyOptional<IOStackHandler> ioWrapper = this.ioWrapper;
        if (!ioWrapper.isPresent()) {
            return null;
        }

        // Does this cache? I haven't looked yet
        var recipes = this.level.getServer().getRecipeManager().getAllRecipesFor(ToolsRegistry.SUPER_COOLER_RECIPE_TYPE.get());
        if (recipes.isEmpty()) {
            return null;
        }

        if (!this.tank.isPresent()) {
            return null;
        }

        if (!this.ioWrapper.isPresent()) {
            return null;
        }

        var io = ioWrapper.orElseThrow(RuntimeException::new);
        boolean hasOccupiedSlot = false;
        for (int i = 0; i < io.getInput().getSlots(); i++) {
            if (!io.getInput().getStackInSlot(i).isEmpty()) {
                hasOccupiedSlot = true;
                break;
            }
        }

        // No items in the input slots = no recipe
        if (!hasOccupiedSlot) {
            return null;
        }

        var recipesForFluid = recipes.stream()
                .filter(e -> !e.fluidIngredient.isEmpty())
                .filter(e -> e.fluidIngredient.isFluidEqual(this.tank.orElseThrow(RuntimeException::new).getFluid()))
                .filter(e -> e.fluidIngredient.getAmount() <= this.tank.orElseThrow(RuntimeException::new).getFluidAmount())
                .toList();

        for (var recipe : recipesForFluid) {
            ItemStackHandler input = io.getInput();

            var filledIngredients = recipe.ingredients.stream().filter(e -> !e.isEmpty()).toList();
            NonNullList<Ingredient> foundIngredients = NonNullList.create();

            for (int i = 0; i < input.getSlots(); i++) {
                var stack = input.getStackInSlot(i);
                if (stack.isEmpty()) {
                    continue;
                }

                for (var ingredient : filledIngredients) {
                    if (ingredient.test(stack)) {
                        foundIngredients.add(ingredient);
                    }
                }
            }

            // Compare the found ingredients to the recipe ingredients
            for (var ingredient : recipe.ingredients) {
                if (!foundIngredients.contains(ingredient)) {
                    return null;
                }
            }

            return recipe;
        }

        return null;
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
        return new SuperCoolerContainer(i, containerData, arg, arg2, this);
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

        ioWrapper.ifPresent(wrapper -> {
            wrapper.getInput().deserializeNBT(arg.getCompound("input"));
            wrapper.getInput().deserializeNBT(arg.getCompound("output"));
        });
        energy.ifPresent(storage -> storage.deserializeNBT(arg.get("energy")));
        tank.ifPresent(tank -> ((FluidTank) tank).readFromNBT(arg.getCompound("fluid")));

        // Write the progress
        this.progress = arg.getInt("progress");
        this.progressRequired = arg.getInt("progressRequired");

        // Write the recipe id
        if (arg.contains("recipe")) {
            try {
                Recipe<?> parsedRecipe = this.level.getServer().getRecipeManager().byKey(new ResourceLocation(arg.getString("recipe"))).orElse(null);
                if (parsedRecipe == null) {
                    this.processingRecipe = null;
                    this.progress = 0;
                    this.progressRequired = 0;
                    return;
                }

                this.processingRecipe = (SuperCoolerRecipe) parsedRecipe; // Try catch just in case
            } catch (Exception e) {
                LOGGER.error("Failed to load recipe from NBT", e);
                this.processingRecipe = null;
                this.progress = 0;
                this.progressRequired = 0;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag arg) {
        super.saveAdditional(arg);

        ioWrapper.ifPresent(wrapper -> {
            arg.put("input", wrapper.getInput().serializeNBT());
            arg.put("output", wrapper.getInput().serializeNBT());
        });
        energy.ifPresent(storage -> arg.put("energy", storage.serializeNBT()));
        tank.ifPresent(tank -> arg.put("fluid", ((FluidTank) tank).writeToNBT(new CompoundTag())));

        // Write the progress
        arg.putInt("progress", this.progress);
        arg.putInt("progressRequired", this.progressRequired);

        // Write the recipe id
        if (this.processingRecipe != null) {
            arg.putString("recipe", this.processingRecipe.getId().toString());
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, entity -> this.getUpdateTag());
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    @Override
    public int getEnergy() {
        return energy.map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    @Override
    public int getMaxEnergy() {
        return energy.map(IEnergyStorage::getMaxEnergyStored).orElse(0);
    }

    @Override
    public int getFluid() {
        return tank.map(IFluidTank::getFluid).map(FluidStack::getAmount).orElse(0);
    }

    @Override
    public int getMaxFluid() {
        return tank.map(IFluidTank::getCapacity).orElse(0);
    }

    @Override
    public void setEnergy(int energy) {
        this.energy.ifPresent(e -> e.overrideEnergy(energy));
    }

    @Override
    public void setFluid(int fluid) {
        this.tank.ifPresent(t -> {
            var stack = new FluidStack(t.getFluid().getFluid(), fluid);
            if (stack.getAmount() > t.getFluidAmount()) {
                t.fill(stack, IFluidHandler.FluidAction.EXECUTE);
            } else {
                t.drain(t.getFluidAmount() - stack.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            }
        });
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
}
