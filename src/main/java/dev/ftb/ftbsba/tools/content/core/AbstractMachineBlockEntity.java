package dev.ftb.ftbsba.tools.content.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;

public abstract class AbstractMachineBlockEntity extends BlockEntity implements MenuProvider, FluidEnergyProvider, ProgressProvider {
    public AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // TODO there's a lot more we could move in here, e.g. progress ticking etc.

    public abstract ContainerData getContainerData();

    public abstract void syncFluidTank();

    public abstract IItemHandler getItemHandler();

    public void tickClient() {
        if (getBlockState().hasProperty(AbstractMachineBlock.ACTIVE)
                && getBlockState().getValue(AbstractMachineBlock.ACTIVE)
                && level.random.nextInt(5) == 0) {
            Vec3 vec = Vec3.upFromBottomCenterOf(getBlockPos(), 1.05);
            level.addParticle(ParticleTypes.SMOKE, vec.x, vec.y, vec.z, 0, 0, 0);
        }
    }

    public abstract void tickServer();

    public void dropItemContents() {
        IItemHandler handler = getItemHandler();
        for (int i = 0; i < handler.getSlots(); i++) {
            Block.popResource(level, getBlockPos(), handler.getStackInSlot(i));
        }
    }

    protected final void setActive(boolean active) {
        boolean curActive = getBlockState().getValue(AbstractMachineBlock.ACTIVE);
        if (active != curActive) {
            level.setBlock(getBlockPos(), getBlockState().setValue(AbstractMachineBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }
}
