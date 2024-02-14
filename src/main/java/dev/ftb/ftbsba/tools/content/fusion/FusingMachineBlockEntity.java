package dev.ftb.ftbsba.tools.content.fusion;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.content.core.*;
import dev.ftb.ftbsba.tools.recipies.FusingMachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class FusingMachineBlockEntity extends AbstractMachineBlockEntity {
    private final EmittingEnergy energyHandler = new EmittingEnergy(100000, 10000, 10000, (energy) -> this.setChanged());
    private final ExtractOnlyFluidTank fluidHandler = new ExtractOnlyFluidTank(10000, (tank) -> this.setChanged());
    private final EmittingStackHandler itemHandler = new EmittingStackHandler(2, (contents) -> {
        this.setChanged();
        this.progress = 0;
    });

    private final LazyOptional<EmittingEnergy> energy = LazyOptional.of(() -> energyHandler);
    private final LazyOptional<ExtractOnlyFluidTank> tank = LazyOptional.of(() -> fluidHandler);
    private final LazyOptional<EmittingStackHandler> input = LazyOptional.of(() -> itemHandler);

    private int progress = 0;
    private int progressRequired = 0;
    private FusingMachineRecipe currentRecipe = null;
    private final FluidEnergyProcessorContainerData containerData = new FluidEnergyProcessorContainerData(this, this);

    public FusingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ToolsRegistry.FUSING_MACHINE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void tickServer() {
        if (!hasEnergy() || !hasOccupiedInputSlots()) {
            return;
        }

        // We need to find the recipe before we can check the fluid tank
        if (progress == 0) {
            currentRecipe = RecipeCaches.FUSING_MACHINE.getCachedRecipe(this::findValidRecipe, itemHandler, null)
                    .orElse(null);

            if (currentRecipe != null) {
                // Test for tank validity
                // TODO: Add fluid check
                var tank = fluidHandler;
                if (!tank.isEmpty() && !tank.getFluid().isFluidEqual(currentRecipe.fluidResult)) {
                    setActive(false);
                    return;
                }

                // Otherwise the fluid is either empty or the same as the recipe
                if (!tank.isEmpty() && tank.getFluid().getAmount() + currentRecipe.fluidResult.getAmount() > tank.getCapacity()) {
                    // Don't allow the fluid to overflow
                    setActive(false);
                    return;
                }

                // Finally, we can start the process
                progressRequired = currentRecipe.energyComponent.ticksToProcess();
                progress = 1; // Start the progress
            } else {
                setActive(false);
            }
        }

        if (currentRecipe != null) {
            if (progress == progressRequired) {
                // We're done... Output the result
                extractRecipe();
                breakProgress();
            } else {
                setActive(true);
                useEnergy();
                progress ++;
            }
        }
    }

    //#region BlockEntity processing

    private void extractRecipe() {
        var inventory = itemHandler;

        var requiredItems = this.currentRecipe.ingredients;
        // Try and remove the items from the input slots
        for (var ingredient : requiredItems) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                if (ingredient.test(inventory.getStackInSlot(i))) {
                    if (inventory.extractItem(i, 1, true).isEmpty()) {
                        // this shouldn't happen, but let's be defensive
                        breakProgress();
                        currentRecipe = null;
                        return;
                    }
                }
            }
        }

        for (var ingredient : requiredItems) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                if (ingredient.test(inventory.getStackInSlot(i))) {
                    // This logically can't be false due to the simulation above
                    inventory.extractItem(i, 1, false);
                }
            }
        }

        fluidHandler.forceFill(this.currentRecipe.fluidResult, IFluidHandler.FluidAction.EXECUTE);
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

    private void breakProgress() {
        this.progress = 0;
        this.progressRequired = 0;
    }

    private boolean hasEnergy() {
        return energyHandler.getEnergyStored() > 0;
    }

    private boolean hasOccupiedInputSlots() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private Optional<FusingMachineRecipe> findValidRecipe() {
        return level.getRecipeManager().getAllRecipesFor(ToolsRegistry.FUSING_MACHINE_RECIPE_TYPE.get()).stream()
                .filter(this::recipeMatchesInput)
                .findFirst();
    }

    private boolean recipeMatchesInput(FusingMachineRecipe recipe) {
        NonNullList<Ingredient> foundIngredients = NonNullList.create();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                for (Ingredient ingredient : recipe.ingredients) {
                    if (ingredient.test(itemHandler.getStackInSlot(i))) {
                        foundIngredients.add(ingredient);
                    }
                }
            }
        }

        return foundIngredients.containsAll(recipe.ingredients);
    }

//#endregion

//#region BlockEntity setup and syncing

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ftbsba.fusing_machine");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return input.cast();
        } else if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return tank.cast();
        }

        return super.getCapability(cap, side);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory arg, Player arg2) {
        if (arg2 instanceof ServerPlayer sp) {
            fluidHandler.needSync(sp);
        }
        return new FusingMachineContainer(i, arg, getBlockPos());
    }

    @Override
    public void load(CompoundTag arg) {
        super.load(arg);

        itemHandler.deserializeNBT(arg.getCompound("input"));
        energyHandler.deserializeNBT(arg.get("energy"));
        fluidHandler.readFromNBT(arg.getCompound("fluid"));
    }

    @Override
    protected void saveAdditional(CompoundTag arg) {
        super.saveAdditional(arg);

        arg.put("input", itemHandler.serializeNBT());
        arg.put("energy", energyHandler.serializeNBT());
        arg.put("fluid", fluidHandler.writeToNBT(new CompoundTag()));
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        input.invalidate();
        energy.invalidate();
        tank.invalidate();
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

//#endregion

//#region Data Syncing helper methods

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
    public void setFluid(FluidStack fluid) {
        fluidHandler.overrideFluidStack(fluid);
    }

    @Override
    public void setEnergy(int energy) {
        energyHandler.overrideEnergy(energy);
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
    public EmittingStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public ContainerData getContainerData() {
        return containerData;
    }

    @Override
    public void syncFluidTank() {
        fluidHandler.sync(this);
    }

//#endregion

    public static class ExtractOnlyFluidTank extends EmittingFluidTank {
        public ExtractOnlyFluidTank(int capacity, Consumer<EmittingFluidTank> listener) {
            super(capacity, listener);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        public int forceFill(FluidStack resource, FluidAction action) {
            return super.fill(resource, action);
        }

        public void overrideFluidStack(FluidStack stack) {
            this.fluid = stack;
        }
    }
}
