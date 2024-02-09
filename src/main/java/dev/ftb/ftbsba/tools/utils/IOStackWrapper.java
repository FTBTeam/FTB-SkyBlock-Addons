package dev.ftb.ftbsba.tools.utils;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class IOStackWrapper implements IItemHandler {
    ItemStackHandler input;
    ItemStackHandler output;

    public IOStackWrapper(ItemStackHandler input, ItemStackHandler output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public int getSlots() {
        return input.getSlots() + output.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int i) {
        return i < input.getSlots() ? input.getStackInSlot(i) : output.getStackInSlot(i - input.getSlots());
    }

    @Override
    public @NotNull ItemStack insertItem(int i, @NotNull ItemStack arg, boolean bl) {
        return i < input.getSlots() ? input.insertItem(i, arg, bl) : arg;
    }

    @Override
    public @NotNull ItemStack extractItem(int i, int j, boolean bl) {
        return i < input.getSlots() ? input.extractItem(i, j, bl) : output.extractItem(i - input.getSlots(), j, bl);
    }

    @Override
    public int getSlotLimit(int i) {
        return i < input.getSlots() ? input.getSlotLimit(i) : output.getSlotLimit(i - input.getSlots());
    }

    @Override
    public boolean isItemValid(int i, @NotNull ItemStack arg) {
        return i < input.getSlots() && input.isItemValid(i, arg);
    }

    public ItemStackHandler getInput() {
        return input;
    }

    public ItemStackHandler getOutput() {
        return output;
    }
}
