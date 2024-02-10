package dev.ftb.ftbsba.tools.content.supercooler;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.recipies.SuperCoolerRecipe;
import dev.ftb.ftbsba.tools.utils.IOStackWrapper;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuperCoolerBlockEntity extends BlockEntity implements MenuProvider {
    public ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (progress > 0) {
                progress = 0;
            }
        }
    };

    public ItemStackHandler output = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public CustomEnergy energy = new CustomEnergy(this);
    public LazyOptional<CustomEnergy> energyLazy = LazyOptional.of(() -> energy);

    public LazyOptional<IFluidTank> tank = LazyOptional.of(() -> new FluidTank(10000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    });

    public LazyOptional<IOStackWrapper> ioWrapper = LazyOptional.of(() -> new IOStackWrapper(inventory, output));

    /**
     * Mostly borrowed from https://github.com/desht/ModularRouters/blob/MC1.20.1-master/src/main/java/me/desht/modularrouters/block/tile/ModularRouterBlockEntity.java#L1103-L1129 with permission.
     * Thanks Desht!
     */
    ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            int result = 0;
            if (index == 0) {
                result = energy.getEnergyStored() & 0x0000FFFF;
            } else if (index == 1) {
                result = (energy.getEnergyStored() & 0xFFFF0000) >> 16;
            } else if (index == 2) {
                return tank.map(IFluidTank::getFluid).map(FluidStack::getAmount).orElse(0);
            } else if (index == 3) {
                return progress;
            } else if (index == 4) {
                return progressRequired;
            }

            return result;
        }

        @Override
        public void set(int index, int value) {
            if (value < 0) value += 65536;

            if (index == 0) {
                energy.setEnergy(energy.getEnergyStored() & 0xFFFF0000 | value, false);
            } else if (index == 1) {
                energy.setEnergy(energy.getEnergyStored() & 0x0000FFFF | (value << 16), false);
            } else if (index == 2) {
                final int finalValue = value;
                tank.ifPresent(tank -> {
                    // If the value is greater than the previous value, we're filling the tank
                    if (finalValue > tank.getFluidAmount()) {
                        tank.fill(new FluidStack(tank.getFluid(), finalValue - tank.getFluidAmount()), IFluidHandler.FluidAction.EXECUTE);
                    } else {
                        tank.drain(tank.getFluidAmount() - finalValue, IFluidHandler.FluidAction.EXECUTE);
                    }
                });
            } else if (index == 3) {
                progress = value;
            } else if (index == 4) {
                progressRequired = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    int progress = 0;
    int progressRequired = 0;
    SuperCoolerRecipe processingRecipe = null;

    public SuperCoolerBlockEntity(BlockPos pos, BlockState state) {
        super(ToolsRegistry.SUPER_COOLER_BLOCK_ENTITY.get(), pos, state);
    }

    public static <T extends BlockEntity> void ticker(Level level, BlockPos pos, BlockState state, T t) {
        if (!(t instanceof SuperCoolerBlockEntity entity)) {
            return;
        }

        if (level.isClientSide) {
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
        } else if (entity.progress >= entity.progressRequired) {
            entity.executeRecipe();
            entity.breakProgress();
        } else {
            // Use energy
            entity.useEnergy();
            entity.progress++;
        }
    }

    public void executeRecipe() {
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
    }

    private void useEnergy() {
        if (this.processingRecipe == null) {
            return;
        }

        var result = this.energy.extractEnergy(this.processingRecipe.energyComponent.fePerTick(), true);
        if (result < this.processingRecipe.energyComponent.fePerTick()) {
            breakProgress();
            return;
        }

        this.energy.extractEnergy(this.processingRecipe.energyComponent.fePerTick(), false);
    }

    /**
     * This will always force us back to the start of the recipe
     */
    private void breakProgress() {
        this.progress = 0;
        this.progressRequired = 0;
        this.processingRecipe = null;
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
        return energy.getEnergyStored() > 0;
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
        LazyOptional<IOStackWrapper> ioWrapper = this.ioWrapper;
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

        boolean hasOccupiedSlot = false;
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            if (!this.inventory.getStackInSlot(i).isEmpty()) {
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
            IOStackWrapper io = ioWrapper.orElseThrow(RuntimeException::new);
            ItemStackHandler input = io.getInput();

            var filledIngredients = recipe.ingredients.stream().filter(e -> !e.isEmpty()).toList();
            System.out.println(filledIngredients.size());
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
            return energyLazy.cast();
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return tank.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ftbsba.super_cooler"); // TODO: Add translation
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
        energyLazy.invalidate();
        tank.invalidate();
    }

    @Override
    public void load(CompoundTag arg) {
        super.load(arg);

        ioWrapper.ifPresent(wrapper -> {
            wrapper.getInput().deserializeNBT(arg.getCompound("input"));
            wrapper.getInput().deserializeNBT(arg.getCompound("output"));
        });
        energyLazy.ifPresent(storage -> storage.deserializeNBT(arg.get("energy")));
        tank.ifPresent(tank -> {
            ((FluidTank) tank).readFromNBT(arg.getCompound("fluid"));
        });
    }

    @Override
    protected void saveAdditional(CompoundTag arg) {
        super.saveAdditional(arg);

        ioWrapper.ifPresent(wrapper -> {
            arg.put("input", wrapper.getInput().serializeNBT());
            arg.put("output", wrapper.getInput().serializeNBT());
        });
        energyLazy.ifPresent(storage -> arg.put("energy", storage.serializeNBT()));
        tank.ifPresent(tank -> {
            arg.put("fluid", ((FluidTank) tank).writeToNBT(new CompoundTag()));
        });
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

    public static class CustomEnergy extends EnergyStorage {
        BlockEntity entity;

        public CustomEnergy(BlockEntity blockEntity) {
            super(100000, 1000, 1000);
            entity = blockEntity;

        }
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            var result = super.receiveEnergy(maxReceive, simulate);
            if (!simulate) {
                this.entity.setChanged();
            }
            return result;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            var result = super.extractEnergy(maxExtract, simulate);
            if (!simulate) {
                this.entity.setChanged();
            }
            return result;
        }

        public void setEnergy(int energy, boolean update) {
            this.energy = energy;
            if (update) {
                this.entity.setChanged();
            }
        }
    }
}
