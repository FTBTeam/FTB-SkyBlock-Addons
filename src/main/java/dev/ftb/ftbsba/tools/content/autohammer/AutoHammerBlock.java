package dev.ftb.ftbsba.tools.content.autohammer;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.content.core.AbstractMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
import static net.minecraft.world.phys.shapes.BooleanOp.OR;

public class AutoHammerBlock extends Block implements EntityBlock {
    private final Supplier<Item> baseHammerItem;
    private final AutoHammerProperties props;

    public static final VoxelShape EAST_WEST = Stream.of(Block.box(1, 4, 1, 15, 14, 15),Block.box(0, 4, 0, 2, 14, 2),Block.box(0, 4, 14, 2, 14, 16),Block.box(14, 4, 0, 16, 14, 2),Block.box(14, 4, 14, 16, 14, 16),Block.box(0, 0, 0, 16, 4, 16),Block.box(0, 14, 0, 16, 16, 16),Block.box(4, 4, 0, 12, 12, 2),Block.box(4, 4, 14, 12, 12, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, OR)).get();
    public static final VoxelShape NORTH_SOUTH = Stream.of(Block.box(1, 4, 1, 15, 14, 15),Block.box(0, 4, 14, 2, 14, 16),Block.box(14, 4, 14, 16, 14, 16),Block.box(0, 4, 0, 2, 14, 2),Block.box(14, 4, 0, 16, 14, 2),Block.box(0, 0, 0, 16, 4, 16),Block.box(0, 14, 0, 16, 16, 16),Block.box(0, 4, 4, 2, 12, 12),Block.box(14, 4, 4, 16, 12, 12)).reduce((v1, v2) -> Shapes.join(v1, v2, OR)).get();

    public AutoHammerBlock(Supplier<Item> baseHammerItem, AutoHammerProperties properties) {
        super(Properties.of(Material.STONE).strength(1F, 1F));

        this.props = properties;
        this.baseHammerItem = baseHammerItem;

        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(AbstractMachineBlock.ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, AbstractMachineBlock.ACTIVE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    public VoxelShape getVisualShape(BlockState arg, BlockGetter arg2, BlockPos arg3, CollisionContext arg4) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            return NORTH_SOUTH;
        } else {
            return EAST_WEST;
        }
    }

    @Override
    @Deprecated
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity tileEntity = world.getBlockEntity(pos);

            if (tileEntity instanceof AutoHammerBlockEntity) {
                Direction dir = state.getValue(HORIZONTAL_FACING);
                AutoHammerBlockEntity autoHammer = (AutoHammerBlockEntity) tileEntity;

                autoHammer.getCapability(ForgeCapabilities.ITEM_HANDLER, AutoHammerBlockEntity.getInputDirection(dir)).ifPresent(e -> popResource(world, pos, e.getStackInSlot(0)));
                autoHammer.getCapability(ForgeCapabilities.ITEM_HANDLER, AutoHammerBlockEntity.getOutputDirection(dir)).ifPresent(e -> {
                    for (int i = 0; i < e.getSlots(); i++) {
                        popResource(world, pos, e.getStackInSlot(i));
                    }
                });

                world.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level arg, BlockState arg2, BlockEntityType<T> arg3) {
        return AutoHammerBlockEntity::ticker;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos arg, BlockState arg2) {
        if (this == ToolsRegistry.IRON_AUTO_HAMMER.get()) {
            return ToolsRegistry.IRON_AUTO_HAMMER_BLOCK_ENTITY.get().create(arg, arg2);
        } else if (this == ToolsRegistry.GOLD_AUTO_HAMMER.get()) {
            return ToolsRegistry.GOLD_AUTO_HAMMER_BLOCK_ENTITY.get().create(arg, arg2);
        } else if (this == ToolsRegistry.DIAMOND_AUTO_HAMMER.get()) {
            return ToolsRegistry.DIAMOND_AUTO_HAMMER_BLOCK_ENTITY.get().create(arg, arg2);
        }
        return ToolsRegistry.NETHERITE_AUTO_HAMMER_BLOCK_ENTITY.get().create(arg, arg2);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }
}
