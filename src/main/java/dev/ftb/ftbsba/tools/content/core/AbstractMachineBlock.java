package dev.ftb.ftbsba.tools.content.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractMachineBlock extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public AbstractMachineBlock() {
        super(Properties.of(Material.STONE).strength(1F, 1F));

        registerDefaultState(getStateDefinition().any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, ACTIVE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
        if (level != null) {
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if (tag != null) {
                int energy = tag.getInt("energy");
                if (energy > 0) {
                    list.add(Component.translatable("ftbsba.tooltip.energy", energy));
                }
                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tag.getCompound("fluid"));
                if (!fluidStack.isEmpty()) {
                    list.add(Component.translatable("ftbsba.tooltip.fluid", fluidStack.getAmount(), fluidStack.getDisplayName()));
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AbstractMachineBlockEntity machine) {
                // handle filling/emptying with bucket (or other fluid containing item)
                if (doFluidInteraction(machine, result.getDirection(), player, hand, true)) {
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                } else if (doFluidInteraction(machine, result.getDirection(), player, hand, false)) {
                    level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
            if (blockEntity instanceof MenuProvider menuProvider) {
                NetworkHooks.openScreen((ServerPlayer) player, menuProvider, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level arg, BlockState arg2, BlockEntityType<T> arg3) {
        return (level1, blockPos, blockState, t) -> {
            if (t instanceof AbstractMachineBlockEntity tickable) {
                if (level1.isClientSide()) {
                    tickable.tickClient();
                } else {
                    tickable.tickServer();
                }
            }
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean bl) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof AbstractMachineBlockEntity machine) {
                machine.dropItemContents();
            }
        }

        super.onRemove(state, level, pos, newState, bl);
    }


    private static boolean doFluidInteraction(BlockEntity te, Direction face, Player player, InteractionHand hand, boolean isInserting) {
        ItemStack stack = player.getItemInHand(hand);
        return FluidUtil.getFluidHandler(stack).map(stackHandler -> {
            if (te.getCapability(ForgeCapabilities.FLUID_HANDLER, face).isPresent()) {
                if (stackHandler.getTanks() == 0) return false;
                int capacity = stackHandler.getTankCapacity(0);
                return te.getCapability(ForgeCapabilities.FLUID_HANDLER, face).map(handler -> {
                    PlayerInvWrapper invWrapper = new PlayerInvWrapper(player.getInventory());
                    FluidActionResult result = isInserting ?
                            FluidUtil.tryEmptyContainerAndStow(player.getItemInHand(hand), handler, invWrapper, capacity, player, true) :
                            FluidUtil.tryFillContainerAndStow(player.getItemInHand(hand), handler, invWrapper, capacity, player, true);
                    if (result.isSuccess()) {
                        player.setItemInHand(hand, result.getResult());
                        return true;
                    }
                    return false;
                }).orElse(false);
            }
            return false;
        }).orElse(false);
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
