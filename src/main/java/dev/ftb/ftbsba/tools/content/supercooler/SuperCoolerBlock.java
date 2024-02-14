package dev.ftb.ftbsba.tools.content.supercooler;

import dev.ftb.ftbsba.tools.content.core.AbstractMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SuperCoolerBlock extends AbstractMachineBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SuperCoolerBlockEntity(pos, state);
    }
}
