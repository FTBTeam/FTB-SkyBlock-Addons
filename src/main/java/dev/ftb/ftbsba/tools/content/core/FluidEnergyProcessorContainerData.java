package dev.ftb.ftbsba.tools.content.core;

import net.minecraft.world.inventory.ContainerData;

/**
 * Mostly borrowed from https://github.com/desht/ModularRouters/blob/MC1.20.1-master/src/main/java/me/desht/modularrouters/block/tile/ModularRouterBlockEntity.java#L1103-L1129 with permission.
 * Thanks Desht!
 */
public class FluidEnergyProcessorContainerData implements ContainerData {
    public static final int ENERGY_LO = 0;
    public static final int ENERGY_HI = 1;
    public static final int PROGRESS = 2;
    public static final int MAX_PROGRESS = 3;
    private final FluidEnergyProvider fluidEnergyProvider;
    private final ProgressProvider progressProvider;

    public FluidEnergyProcessorContainerData(FluidEnergyProvider fluidEnergyProvider, ProgressProvider progressProvider) {
        this.fluidEnergyProvider = fluidEnergyProvider;
        this.progressProvider = progressProvider;
    }

    @Override
    public int get(int index) {
        return switch (index) {
            case ENERGY_LO -> fluidEnergyProvider.getEnergy() & 0x0000FFFF;
            case ENERGY_HI -> (fluidEnergyProvider.getEnergy() & 0xFFFF0000) >> 16;
            case PROGRESS -> progressProvider.getProgress();
            case MAX_PROGRESS -> progressProvider.getMaxProgress();
            default -> 0;
        };
    }

    @Override
    public void set(int index, int value) {
        final int finalValue = value < ENERGY_LO ? value + 65536 : value;

        switch (index) {
            case ENERGY_LO -> fluidEnergyProvider.setEnergy(fluidEnergyProvider.getEnergy() & 0xFFFF0000 | finalValue);
            case ENERGY_HI -> fluidEnergyProvider.setEnergy(fluidEnergyProvider.getEnergy() & 0x0000FFFF | (finalValue << 16));
            case PROGRESS -> progressProvider.setProgress(finalValue);
            case MAX_PROGRESS -> progressProvider.setMaxProgress(finalValue);
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
