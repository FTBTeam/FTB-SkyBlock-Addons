package dev.ftb.ftbsba.tools.content.fusion;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class FusingMachineBlock extends Block implements EntityBlock {
    public FusingMachineBlock() {
        super(Properties.of(Material.STONE).strength(1F, 1F));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        NetworkHooks.openScreen((ServerPlayer) player, (FusingMachineBlockEntity) level.getBlockEntity(pos), pos);
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FusingMachineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return FusingMachineBlockEntity::ticker;
    }

    @Override
    public void onRemove(BlockState arg, Level arg2, BlockPos arg3, BlockState arg4, boolean bl) {
        super.onRemove(arg, arg2, arg3, arg4, bl);

        BlockEntity entity = arg2.getBlockEntity(arg3);
        if (!(entity instanceof FusingMachineBlockEntity fusingBlockEntity)) {
            return;
        }

        fusingBlockEntity.input.ifPresent(handler -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                Block.popResource(arg2, arg3, handler.getStackInSlot(i));
            }
        });
    }
}
