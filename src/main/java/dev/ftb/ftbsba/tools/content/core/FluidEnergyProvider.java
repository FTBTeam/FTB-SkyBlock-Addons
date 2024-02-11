package dev.ftb.ftbsba.tools.content.core;

public interface FluidEnergyProvider {
    int getEnergy();
    int getMaxEnergy();

    int getFluid();
    int getMaxFluid();

    void setFluid(int fluid);

    void setEnergy(int energy);

    void setProgress(int progress);

    void setMaxProgress(int maxProgress);
}
