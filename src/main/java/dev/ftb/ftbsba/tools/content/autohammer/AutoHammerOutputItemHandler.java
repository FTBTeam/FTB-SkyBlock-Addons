package dev.ftb.ftbsba.tools.content.autohammer;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class AutoHammerOutputItemHandler extends ItemStackHandler {
    private final AutoHammerBlockEntity autoHammerBlockEntity;

    public AutoHammerOutputItemHandler(AutoHammerBlockEntity autoHammerBlockEntity, int size) {
        super(size);
        this.autoHammerBlockEntity = autoHammerBlockEntity;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @NotNull
    public ItemStack internalInsert(@NotNull ItemStack stack, boolean simulate) {
        var availableSlot = getAvailableSlot(stack);
        if (availableSlot == -1) {
            return stack;
        }

        ItemStack itemStack = super.insertItem(availableSlot, stack, simulate);
        if (!simulate) {
            autoHammerBlockEntity.setChanged();
        }
        return itemStack;
    }

    private int getAvailableSlot(ItemStack tryStack) {
        for (int i = 0; i < this.getSlots(); i++) {
            ItemStack stackInSlot = this.getStackInSlot(i);
            if (stackInSlot.isEmpty() || (stackInSlot.getCount() < stackInSlot.getMaxStackSize() && stackInSlot.getItem().equals(tryStack.getItem()))) {
                return i;
            }
        }

        return -1;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack itemStack = super.extractItem(slot, amount, simulate);
        if (!simulate) {
            autoHammerBlockEntity.setChanged();
        }
        return itemStack;
    }
}
