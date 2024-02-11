package dev.ftb.ftbsba.tools.content.supercooler;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.content.core.FluidAndEnergyScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.Nullable;

public class SuperCoolerScreen extends FluidAndEnergyScreen<SuperCoolerContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(FTBSBA.MOD_ID, "textures/gui/super_cooler_background.png");

    public SuperCoolerScreen(SuperCoolerContainer arg, Inventory arg2, Component arg3) {
        super(arg, arg2, arg3, 3, 79, TEXTURE);
        this.titleLabelX = 96;
    }

    @Override
    protected void renderLabels(PoseStack arg, int i, int j) {
        arg.pushPose();
        arg.scale(0.75F, 0.75F, 0.75F);
        this.font.draw(arg, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        arg.popPose();
    }

    @Override
    public int getEnergyAmount() {
        var data = this.menu.containerData;
        return (data.get(0) & 0xFFFF) | (data.get(1) << 16);
    }

    @Override
    public int getEnergyCapacity() {
        return this.menu.entity.energy.map(IEnergyStorage::getMaxEnergyStored).orElse(0);
    }

    @Override
    public int getFluidAmount() {
        return this.menu.containerData.get(2);
    }

    @Override
    public int getFluidCapacity() {
        return this.menu.entity.tank.map(IFluidTank::getCapacity).orElse(0);
    }

    @Override
    public @Nullable FluidStack getFluidStack() {
        return this.menu.entity.tank.map(IFluidTank::getFluid).orElse(null);
    }

    @Override
    public int getProgress() {
        return this.menu.containerData.get(3);
    }

    @Override
    public int getProgressRequired() {
        return this.menu.containerData.get(4);
    }
}
