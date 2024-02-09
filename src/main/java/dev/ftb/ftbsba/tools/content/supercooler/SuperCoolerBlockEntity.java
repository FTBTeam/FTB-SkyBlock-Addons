package dev.ftb.ftbsba.tools.content.supercooler;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.utils.IOStackWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
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
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
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
                tank.ifPresent(tank -> tank.fill(new FluidStack(tank.getFluid(), finalValue), IFluidHandler.FluidAction.EXECUTE));
            } else if (index == 3) {
                progress = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    int progress = 0;

    public SuperCoolerBlockEntity(BlockPos pos, BlockState state) {
        super(ToolsRegistry.SUPER_COOLER_BLOCK_ENTITY.get(), pos, state);
    }

    public static <T extends BlockEntity> void ticker(Level level, BlockPos pos, BlockState state, T t) {
        if (!(t instanceof SuperCoolerBlockEntity entity)) {
            return;
        }


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
