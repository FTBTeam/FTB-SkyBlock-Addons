package dev.ftb.ftbsba.tools.content.fusion;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.content.core.FluidAndEnergyScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class FusingMachineScreen extends FluidAndEnergyScreen<FusingMachineContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(FTBSBA.MOD_ID, "textures/gui/fusing_machine_background.png");

    public FusingMachineScreen(FusingMachineContainer arg, Inventory arg2, Component arg3) {
        super(arg, arg2, arg3, 140, 90, TEXTURE);
//        this.titleLabelX = 96;
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
        return this.menu.blockEntity.getProgress();
    }

    @Override
    public int getProgressRequired() {
        return menu.blockEntity.getMaxProgress();
    }
}
