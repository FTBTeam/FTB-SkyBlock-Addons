package dev.ftb.ftbsba.tools.content.core;

import net.minecraft.world.inventory.ContainerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Mostly borrowed from https://github.com/desht/ModularRouters/blob/MC1.20.1-master/src/main/java/me/desht/modularrouters/block/tile/ModularRouterBlockEntity.java#L1103-L1129 with permission.
 * Thanks Desht!
 */
public class FluidEnergyProcessorContainerData implements ContainerData {
    private final FluidEnergyProvider fluidEnergyProvider;
    private final ProgressProvider progressProvider;

    public FluidEnergyProcessorContainerData(FluidEnergyProvider fluidEnergyProvider, ProgressProvider progressProvider) {
        this.fluidEnergyProvider = fluidEnergyProvider;
        this.progressProvider = progressProvider;
    }

    @Override
    public int get(int index) {
        int result = 0;

        if (index == 0) {
            result = this.fluidEnergyProvider.getEnergy() & 0x0000FFFF;
        } else if (index == 1) {
            result = (this.fluidEnergyProvider.getEnergy() & 0xFFFF0000) >> 16;
        } else if (index == 2) {
            return this.fluidEnergyProvider.getFluid();
        } else if (index == 3) {
            return this.progressProvider.getProgress();
        } else if (index == 4) {
            return this.progressProvider.getMaxProgress();
        }

        return result;
    }

    @Override
    public void set(int index, int value) {
        if (value < 0) value += 65536;
        final int finalValue = value;

        if (index == 0) {
            this.fluidEnergyProvider.setEnergy(this.fluidEnergyProvider.getEnergy() & 0xFFFF0000 | finalValue);
        } else if (index == 1) {
            this.fluidEnergyProvider.setEnergy(this.fluidEnergyProvider.getEnergy() & 0x0000FFFF | (finalValue << 16));
        } else if (index == 2) {
            this.fluidEnergyProvider.setFluid(finalValue);
        } else if (index == 3) {
            this.progressProvider.setProgress(finalValue);
        } else if (index == 4) {
            this.progressProvider.setMaxProgress(finalValue);
        }
    }

    @Override
    public int getCount() {
        return 5;
    }
}
