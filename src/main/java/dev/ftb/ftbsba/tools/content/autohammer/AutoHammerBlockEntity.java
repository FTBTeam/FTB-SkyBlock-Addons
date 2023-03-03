package dev.ftb.ftbsba.tools.content.autohammer;

import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.recipies.ToolsRecipeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class AutoHammerBlockEntity extends BlockEntity {
    private static final int[][] IO_DIRECTIONAL_MATRIX = new int[][] {
            {4, 5}, // 2 north -> input[west] -> output[east]
            {5, 4}, // 3 south -> input[east] -> output[west]
            {3, 2}, // 4 west  -> input[south] -> output[north]
            {2, 3}, // 5 east  -> input[north] -> output[south]
    };

    private final ItemStackHandler inputInventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            AutoHammerProperties props = AutoHammerBlockEntity.this.getProps();

            Block blockOfInput = Block.byItem(stack.getItem());
            boolean correctToolForDrops = props.getHammerItem().get().isCorrectToolForDrops(new ItemStack(props.getHammerItem().get()), blockOfInput.defaultBlockState());

            if (correctToolForDrops) {
                return ToolsRecipeCache.hammerable(blockOfInput.defaultBlockState());
            }

            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            AutoHammerBlockEntity.this.setChanged();
        }
    };

    private final AutoHammerOutputItemHandler outputInventory = new AutoHammerOutputItemHandler(this, 8);

    private final LazyOptional<ItemStackHandler> inputInvLazy = LazyOptional.of(() -> inputInventory);
    private final LazyOptional<AutoHammerOutputItemHandler> outputInvLazy = LazyOptional.of(() -> outputInventory);
    private final Supplier<Item> hammerItem;

    private int progress = 0;
    private int maxProgress = 0;
    private int timeOut = 0;
    private boolean processing = false;
    private ItemStack heldItem = ItemStack.EMPTY;

    public AutoHammerBlockEntity(BlockPos pos, BlockState state, BlockEntityType<?> blockEntityType, Supplier<Item> hammerItem) {
        super(blockEntityType, pos, state);
        this.hammerItem = hammerItem;
    }

    public static <T extends BlockEntity> void ticker(Level level, BlockPos pos, BlockState state, T t) {
        if (level == null || level.isClientSide) {
            return;
        }

        if (!(t instanceof AutoHammerBlockEntity blockEntity)) {
            return;
        }

        if (blockEntity.timeOut > 0) {
            blockEntity.timeOut--;
            return;
        }

        // By default, lets try and insert and export items in and out of the internal buffers
        blockEntity.pushPullInventories();

        ItemStack inputStack = blockEntity.inputInventory.getStackInSlot(0);
        List<ItemStack> hammerDrops = ToolsRecipeCache.getHammerDrops(level, inputStack);

        boolean isActive = state.getValue(AutoHammerBlock.ACTIVE);
        boolean shouldBeActive = blockEntity.inputHasItemsAndOutputIsClear(hammerDrops);
        if (shouldBeActive && !isActive) {
            level.setBlock(pos, state.setValue(AutoHammerBlock.ACTIVE, true), 3);
        } else if(!shouldBeActive && isActive) {
            level.setBlock(pos, state.setValue(AutoHammerBlock.ACTIVE, false), 3);
        }

        if (!blockEntity.processing) {
            if (!inputStack.isEmpty()) {
                // Attempt to insert the items into the output, Time out and stop if any items would be lost
                // If we consumed all items, start processing
                if (hammerDrops.size() > 0 && blockEntity.pushIntoInternalOutputInventory(hammerDrops, true) >= hammerDrops.size()) {
                    blockEntity.heldItem = inputStack.copy();
                    blockEntity.heldItem.setCount(1);

                    blockEntity.inputInventory.extractItem(0, 1, false);
                    blockEntity.processing = true;
                    blockEntity.maxProgress = blockEntity.getProps().getHammerSpeed().get();
                    blockEntity.progress = 0;
                } else {
                    blockEntity.timeOut = blockEntity.getTimeoutDuration(); // Timeout for a while
                }
            } else {
                blockEntity.timeOut = blockEntity.getTimeoutDuration(); // Timeout for a while
            }
        } else {
            if (blockEntity.progress < blockEntity.maxProgress) {
                // if we're running, tick the progress
                blockEntity.progress++;
            } else {
                // We're done, lets try to insert the items into the output
                blockEntity.processing = false;
                blockEntity.progress = 0;
                blockEntity.maxProgress = 0;

                blockEntity.pushIntoInternalOutputInventory(ToolsRecipeCache.getHammerDrops(level, blockEntity.heldItem), false);
                blockEntity.heldItem = ItemStack.EMPTY;
            }
        }
    }

    private void pushPullInventories() {
        // First, try and push items out of the output if any exist
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        IItemHandler external = getExternalInventory(getOutputDirection(facing));
        if (!(external instanceof EmptyHandler)) {
            for (int i = 0; i < outputInventory.getSlots(); i++) {
                ItemStack stackInSlot = outputInventory.extractItem(i, outputInventory.getStackInSlot(i).getCount(), true);
                if (!stackInSlot.isEmpty()) {
                    ItemStack stack = ItemHandlerHelper.insertItem(external, stackInSlot, false);
                    outputInventory.extractItem(i, stackInSlot.getCount() - stack.getCount(), false);
                    break;
                }
            }
        }

        // Now try and insert items into the input inventory
        IItemHandler pullSource = getExternalInventory(getInputDirection(facing));
        for (int i = 0; i < pullSource.getSlots(); i++) {
            ItemStack stack = pullSource.extractItem(i, 64, true);
            if (!stack.isEmpty() && hasItemAndIsHammerable(stack)) {
                ItemStack insertedStack = ItemHandlerHelper.insertItemStacked(inputInventory, stack, false);
                pullSource.extractItem(i, stack.getCount() - insertedStack.getCount(), false);
            }
        }
    }

    /**
     * Attempts to insert the item into the output inventory, and if successful, starts processing
     *
     * @param stack The item to insert
     */
    private boolean hasItemAndIsHammerable(ItemStack stack) {
        BlockState blockState = Block.byItem(stack.getItem()).defaultBlockState();

        boolean toolEffective = blockState.is(BlockTags.MINEABLE_WITH_PICKAXE);
        boolean toolEffective2 = blockState.is(BlockTags.MINEABLE_WITH_SHOVEL);

        boolean isCorrectTool = hammerItem.get().isCorrectToolForDrops(new ItemStack(hammerItem.get()), blockState);

        if (!isCorrectTool || (!toolEffective && !toolEffective2)) {
            return false;
        }

        return !stack.isEmpty() && ToolsRecipeCache.hammerable(stack);
    }

    private int pushIntoInternalOutputInventory(List<ItemStack> items, boolean simulate) {
        int inserted = 0;

        for (ItemStack item : items) {
            ItemStack insert = outputInventory.internalInsert(item.copy(), simulate);
            if (insert.isEmpty()) {
                inserted ++;
            }
        }

        return inserted;
    }

    private IItemHandler getExternalInventory(Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
        if (blockEntity != null) {
            return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).orElse(EmptyHandler.INSTANCE);
        }

        return EmptyHandler.INSTANCE;
    }

    public boolean inputHasItemsAndOutputIsClear(List<ItemStack> hammerDrops) {
        if (inputInventory.getStackInSlot(0).isEmpty()) {
            return false;
        }

        boolean hasSpace = false;
        for (int i = 0; i < outputInventory.getSlots(); i++) {
            ItemStack stackInSlot = outputInventory.getStackInSlot(i);
            if (stackInSlot.isEmpty()) {
                hasSpace = true;
                break;
            }

            for (ItemStack hammerDrop : hammerDrops) {
                if (stackInSlot.getItem().equals(hammerDrop.getItem()) && stackInSlot.getCount() < stackInSlot.getMaxStackSize()) {
                    hasSpace = true;
                    break;
                }
            }
        }

        return hasSpace;
    }


    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");
        processing = tag.getBoolean("Processing");

        inputInvLazy.ifPresent(e -> e.deserializeNBT(tag.getCompound("InputInventory")));
        outputInvLazy.ifPresent(e -> e.deserializeNBT(tag.getCompound("OutputInventory")));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag arg) {
        super.saveAdditional(arg);

        arg.putInt("Progress", progress);
        arg.putInt("MaxProgress", maxProgress);
        arg.putBoolean("Processing", processing);

        inputInvLazy.ifPresent(e -> arg.put("InputInventory", e.serializeNBT()));
        outputInvLazy.ifPresent(e -> arg.put("OutputInventory", e.serializeNBT()));
    }

    public static Direction getInputDirection(Direction facing) {
        return Direction.from3DDataValue(IO_DIRECTIONAL_MATRIX[facing.get3DDataValue() - 2][0]);
    }

    public static Direction getOutputDirection(Direction facing) {
        return Direction.from3DDataValue(IO_DIRECTIONAL_MATRIX[facing.get3DDataValue() - 2][1]);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        Direction dir = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == getInputDirection(dir)) {
                return inputInvLazy.cast();
            } else if (side == getOutputDirection(dir)) {
                return outputInvLazy.cast();
            }
        }

        return super.getCapability(cap, side);
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public int getTimeoutDuration() {
        return 100;
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public AutoHammerProperties getProps() {
        return AutoHammerProperties.IRON;
    }

    public static class Iron extends AutoHammerBlockEntity {
        public Iron(BlockPos pos, BlockState state) {
            super(pos, state, ToolsRegistry.IRON_AUTO_HAMMER_BLOCK_ENTITY.get(), ToolsRegistry.IRON_HAMMER);
        }
    }

    public static class Gold extends AutoHammerBlockEntity {
        public Gold(BlockPos pos, BlockState state) {
            super(pos, state, ToolsRegistry.GOLD_AUTO_HAMMER_BLOCK_ENTITY.get(), ToolsRegistry.GOLD_HAMMER);
        }

        @Override
        public AutoHammerProperties getProps() {
            return AutoHammerProperties.GOLD;
        }
    }

    public static class Diamond extends AutoHammerBlockEntity {
        public Diamond(BlockPos pos, BlockState state) {
            super(pos, state, ToolsRegistry.DIAMOND_AUTO_HAMMER_BLOCK_ENTITY.get(), ToolsRegistry.DIAMOND_HAMMER);
        }

        @Override
        public AutoHammerProperties getProps() {
            return AutoHammerProperties.DIAMOND;
        }
    }

    public static class Netherite extends AutoHammerBlockEntity {
        public Netherite(BlockPos pos, BlockState state) {
            super(pos, state, ToolsRegistry.NETHERITE_AUTO_HAMMER_BLOCK_ENTITY.get(), ToolsRegistry.NETHERITE_HAMMER);
        }

        @Override
        public AutoHammerProperties getProps() {
            return AutoHammerProperties.NETHERITE;
        }
    }
}
