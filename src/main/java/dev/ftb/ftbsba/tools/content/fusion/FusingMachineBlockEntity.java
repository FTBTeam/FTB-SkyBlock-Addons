package dev.ftb.ftbsba.tools.content.fusion;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.content.core.*;
import dev.ftb.ftbsba.tools.recipies.FusingMachineRecipe;
import dev.ftb.ftbsba.tools.recipies.SuperCoolerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

public class FusingMachineBlockEntity extends BlockEntity implements MenuProvider, FluidEnergyProvider, ProgressProvider {
    public LazyOptional<EmittingEnergy> energy = LazyOptional.of(() -> new EmittingEnergy(100000, 10000, 10000, (energy) -> this.setChanged()));
    public LazyOptional<ExtractOnlyFluidTank> tank = LazyOptional.of(() -> new ExtractOnlyFluidTank(10000, (tank) -> this.setChanged()));
    public LazyOptional<EmittingStackHandler> input = LazyOptional.of(() -> new EmittingStackHandler(2, (contents) -> {
        this.setChanged();
        this.progress = 0;
    }));

    public int progress = 0;
    public int progressRequired = 0;

    public FusingMachineRecipe currentRecipe = null;

    FluidEnergyProcessorContainerData containerData = new FluidEnergyProcessorContainerData(this, this);

    public FusingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ToolsRegistry.FUSING_MACHINE_BLOCK_ENTITY.get(), pos, state);
    }

    public static <T extends BlockEntity> void ticker(Level level, BlockPos pos, BlockState state, T t) {
        if (!(t instanceof FusingMachineBlockEntity entity)) {
            return;
        }

        if (level.isClientSide) {
            return;
        }

        // Requires input items, energy and the fluid tank to be not full and the correct fluid for the recipe
        if (!entity.hasEnergy() || !entity.hasOccupiedInputSlots()) {
            return;
        }

        // We need to find the recipe before we can check the fluid tank
        if (entity.progress == 0) {
            var recipe = entity.testForRecipe();
            if (recipe == null) {
                return;
            }

            // Test for tank validity
            // TODO: Add fluid check
            var tank = entity.tank.orElseThrow(RuntimeException::new);
            if (!tank.isEmpty() && !tank.getFluid().isFluidEqual(recipe.fluidResult)) {
                return;
            }

            // Otherwise the fluid is either empty or the same as the recipe
            if (!tank.isEmpty() && tank.getFluid().getAmount() + recipe.fluidResult.getAmount() > tank.getCapacity()) {
                // Don't allow the fluid to overflow
                return;
            }

            // Finally, we can start the process
            entity.progressRequired = recipe.energyComponent.ticksToProcess();
            entity.progress = 1; // Start the progress
            entity.currentRecipe = recipe;
        }

        if (entity.currentRecipe != null) {
            if (entity.progress == entity.progressRequired) {
                // We're done... Ouput the result
                entity.extractRecipe();
                entity.breakProgress();
            } else {
                entity.useEnergy();
                entity.progress ++;
            }
        }
    }

    //#region BlockEntity processing

    private void extractRecipe() {
        var inventory = input.orElseThrow(RuntimeException::new);
        var tank = this.tank.orElseThrow(RuntimeException::new);

        var requiredItems = this.currentRecipe.ingredients;
        // Try and remove the items from the input slots
        for (var ingredient : requiredItems) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                var stack = inventory.getStackInSlot(i);
                if (ingredient.test(stack)) {
                    var result = inventory.extractItem(i, 1, true);
                    if (result.isEmpty()) {
                        breakProgress();
                        return;
                    }
                }
            }
        }

        for (var ingredient : requiredItems) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                var stack = inventory.getStackInSlot(i);
                if (ingredient.test(stack)) {
                    // This logically can't be false due to the simulation above
                    inventory.extractItem(i, 1, false);
                }
            }
        }

        tank.forceFill(this.currentRecipe.fluidResult, IFluidHandler.FluidAction.EXECUTE);
    }

    private void useEnergy() {
        if (this.currentRecipe == null) {
            return;
        }

        if (!this.energy.isPresent()) {
            breakProgress();
            return;
        }

        EmittingEnergy emittingEnergy = this.energy.orElseThrow(RuntimeException::new);
        var result = emittingEnergy.extractEnergy(this.currentRecipe.energyComponent.fePerTick(), true);
        if (result < this.currentRecipe.energyComponent.fePerTick()) {
            breakProgress();
            return;
        }

        emittingEnergy.extractEnergy(this.currentRecipe.energyComponent.fePerTick(), false);
    }

    private void breakProgress() {
        this.progress = 0;
        this.progressRequired = 0;
        this.currentRecipe = null;
    }

    private boolean hasEnergy() {
        return energy.map(EmittingEnergy::getEnergyStored).orElse(0) > 0;
    }

    private boolean hasOccupiedInputSlots() {
        if (!input.isPresent()) {
            return false;
        }

        var hasItems = false;
        var handler = input.orElseThrow(RuntimeException::new);

        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                hasItems = true;
                break;
            }
        }

        return hasItems;
    }

    @Nullable
    private FusingMachineRecipe testForRecipe() {
        if (!input.isPresent()) {
            return null;
        }


        var handler = input.orElseThrow(RuntimeException::new);

        // This is an immutable hash map so the lookup is fast
        var recipes = level.getRecipeManager().getAllRecipesFor(ToolsRegistry.FUSING_MACHINE_RECIPE_TYPE.get());

        if (recipes.isEmpty()) {
            return null;
        }

        for (var recipe : recipes) {
            List<Ingredient> ingredients = recipe.ingredients.stream().filter(ingredient -> !ingredient.isEmpty()).toList();

            NonNullList<Ingredient> foundItems = NonNullList.create();
            for (int i = 0; i < handler.getSlots(); i++) {
                if (handler.getStackInSlot(i).isEmpty()) {
                    // Don't waste time checking empty slots
                    continue;
                }

                for (Ingredient ingredient : ingredients) {
                    if (ingredient.test(handler.getStackInSlot(i))) {
                        foundItems.add(ingredient);
                    }
                }
            }

            for (var ingredient : recipe.ingredients) {
                if (!foundItems.contains(ingredient)) {
                    return null;
                }
            }

            return recipe;
        }

        return null;
    }

    //#endregion

    //#region BlockEntity setup and syncing

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ftbsba.fusing_machine"); // TODO: Add translation
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
        return new FusingMachineContainer(i, containerData, arg, arg2, this);
    }

    @Override
    public void load(CompoundTag arg) {
        super.load(arg);

        input.ifPresent(input -> input.deserializeNBT(arg.getCompound("input")));
        energy.ifPresent(storage -> storage.deserializeNBT(arg.get("energy")));
        tank.ifPresent(tank -> tank.readFromNBT(arg.getCompound("fluid")));
    }

    @Override
    protected void saveAdditional(CompoundTag arg) {
        super.saveAdditional(arg);

        input.ifPresent(input -> arg.put("input",input.serializeNBT()));
        energy.ifPresent(storage -> arg.put("energy", storage.serializeNBT()));
        tank.ifPresent(tank -> arg.put("fluid", tank.writeToNBT(new CompoundTag())));
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        input.invalidate();
        energy.invalidate();
        tank.invalidate();
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

    //#endregion

    //#region Data Syncing helper methods

    @Override
    public int getEnergy() {
        return energy.map(EmittingEnergy::getEnergyStored).orElse(0);
    }

    @Override
    public int getMaxEnergy() {
        return energy.map(EmittingEnergy::getMaxEnergyStored).orElse(0);
    }

    @Override
    public int getFluid() {
        return tank.map(IFluidTank::getFluidAmount).orElse(0);
    }

    @Override
    public int getMaxFluid() {
        return tank.map(IFluidTank::getCapacity).orElse(0);
    }

    @Override
    public void setFluid(int fluid) {
        tank.ifPresent(t -> t.overrideFluidAmount(fluid));
    }

    @Override
    public void setEnergy(int energy) {
        this.energy.ifPresent(e -> e.overrideEnergy(energy));
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

        public void overrideFluidAmount(int amount) {
            if (this.fluid.isEmpty()) {
                return;
            }

            this.fluid.setAmount(amount);
        }
    }
}
