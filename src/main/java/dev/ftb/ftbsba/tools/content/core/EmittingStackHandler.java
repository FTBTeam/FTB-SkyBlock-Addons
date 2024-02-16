package dev.ftb.ftbsba.tools.content.core;

import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Consumer;

public class EmittingStackHandler extends ItemStackHandler {
    private final Consumer<EmittingStackHandler> onChange;

    public EmittingStackHandler(int size, Consumer<EmittingStackHandler> onChange) {
        super(size);
        this.onChange = onChange;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        onChange.accept(this);
    }
}
