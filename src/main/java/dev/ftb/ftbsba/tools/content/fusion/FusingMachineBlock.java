package dev.ftb.ftbsba.tools.content.fusion;

import dev.ftb.ftbsba.tools.content.core.AbstractMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FusingMachineBlock extends AbstractMachineBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FusingMachineBlockEntity(pos, state);
    }
}
