package dev.ftb.ftbsba.tools.content.supercooler;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.content.core.FluidAndEnergyScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
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
        return menu.blockEntity.getEnergy();
    }

    @Override
    public int getEnergyCapacity() {
        return this.menu.blockEntity.getMaxEnergy();
    }

    @Override
    public int getFluidCapacity() {
        return this.menu.blockEntity.getMaxFluid();
    }

    @Override
    public @Nullable FluidStack getFluidStack() {
        return this.menu.blockEntity.getFluid();
    }

    @Override
    public int getProgress() {
        return menu.blockEntity.getProgress();
    }

    @Override
    public int getProgressRequired() {
        return menu.blockEntity.getMaxProgress();
    }
}
