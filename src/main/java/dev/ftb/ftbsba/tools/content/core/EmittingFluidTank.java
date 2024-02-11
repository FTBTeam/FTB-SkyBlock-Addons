package dev.ftb.ftbsba.tools.content.core;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class EmittingFluidTank extends FluidTank {
    private final Consumer<EmittingFluidTank> onChange;

    public EmittingFluidTank(int capacity, Consumer<EmittingFluidTank> onChange) {
        super(capacity);
        this.onChange = onChange;
    }

    public EmittingFluidTank(int capacity, Predicate<FluidStack> validator, Consumer<EmittingFluidTank> onChange) {
        super(capacity, validator);
        this.onChange = onChange;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        onChange.accept(this);
    }
}
