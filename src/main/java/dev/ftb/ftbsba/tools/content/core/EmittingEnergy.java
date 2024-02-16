package dev.ftb.ftbsba.tools.content.core;

import net.minecraft.util.Mth;
import net.minecraftforge.energy.EnergyStorage;

import java.util.function.Consumer;

public class EmittingEnergy extends EnergyStorage {
    private final Consumer<EmittingEnergy> onChange;

    public EmittingEnergy(int capacity, Consumer<EmittingEnergy> onChange) {
        super(capacity);
        this.onChange = onChange;
    }

    public EmittingEnergy(int capacity, int maxTransfer, Consumer<EmittingEnergy> onChange) {
        super(capacity, maxTransfer);
        this.onChange = onChange;
    }

    public EmittingEnergy(int capacity, int maxReceive, int maxExtract, Consumer<EmittingEnergy> onChange) {
        super(capacity, maxReceive, maxExtract);
        this.onChange = onChange;
    }

    public EmittingEnergy(int capacity, int maxReceive, int maxExtract, int energy, Consumer<EmittingEnergy> onChange) {
        super(capacity, maxReceive, maxExtract, energy);
        this.onChange = onChange;
    }

    /**
     * Only use when you need to override the energy value and bypass the maxReceive or maxExtract
     * @param energy the energy value to override
     */
    public void overrideEnergy(int energy) {
        this.energy = Mth.clamp(energy, 0, capacity);
        this.onChange.accept(this);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        var result = super.receiveEnergy(maxReceive, simulate);
        if (!simulate) {
            this.onChange.accept(this);
        }

        return result;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        var result = super.extractEnergy(maxExtract, simulate);
        if (!simulate) {
            this.onChange.accept(this);
        }

        return result;
    }
}
