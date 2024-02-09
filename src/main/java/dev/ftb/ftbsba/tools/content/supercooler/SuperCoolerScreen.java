package dev.ftb.ftbsba.tools.content.supercooler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.ftbsba.FTBSBA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.IFluidTank;

public class SuperCoolerScreen extends AbstractContainerScreen<SuperCoolerContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(FTBSBA.MOD_ID, "textures/gui/super_cooler_background.png");

    public SuperCoolerScreen(SuperCoolerContainer arg, Inventory arg2, Component arg3) {
        super(arg, arg2, arg3);
        this.titleLabelX = 96;

    }

    @Override
    public void render(PoseStack arg, int mouseX, int mouseY, float partialTicks) {
        super.render(arg, mouseX, mouseY, partialTicks);

        var data = this.menu.entity.containerData;
        if (mouseX > this.leftPos + 3 && mouseX < this.leftPos + 21 && mouseY > this.topPos + 3 && mouseY < this.topPos + 5 + 65) {
            arg.pushPose();
            arg.translate(mouseX - 5, mouseY, 0);
            arg.scale(0.6F, 0.6F, 0F);
            this.renderTooltip(arg, Component.literal(data.get(2) + " / " + this.menu.entity.tank.map(IFluidTank::getCapacity).orElse(0) + " mB"), 0, 0);
            arg.popPose();
        }

        if (mouseX > this.leftPos + 166 && mouseX < this.leftPos + 174 && mouseY > this.topPos + 3 && mouseY < this.topPos + 5 + 65) {
            var uncompressedEnergy = (data.get(0) & 0xFFFF) | (data.get(1) << 16);

//            System.out.println(data.get(0) + " " + data.get(1) + " " + uncompressedEnergy);

            MutableComponent energyText = Component.literal(uncompressedEnergy + " / " + this.menu.entity.energy.map(IEnergyStorage::getMaxEnergyStored).orElse(0) + " FE");
            int width = this.font.width(energyText);
            int scaledWidth = (int) (width * 0.6F);
            arg.pushPose();
            arg.translate(mouseX - 10 - scaledWidth, mouseY, 0);
            arg.scale(0.6F, 0.6F, 0F);
            this.renderTooltip(arg, energyText, 0, 0);
            arg.popPose();
        }
    }

    @Override
    protected void renderBg(PoseStack arg, float f, int i, int j) {
        renderBackground(arg);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        this.blit(arg, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.blit(arg, this.leftPos + 4, this.topPos + 6, 178, 3, 18, 67);

        var data = this.menu.entity.containerData;

        // Energy
        int energyHeight = (int) (data.get(0) / (float) data.get(1) * 65);
        this.blit(arg, this.leftPos + imageWidth - 9, this.topPos + 4, 197, 4, 5, energyHeight);

        // Fluid amount
        int fluidHeight = (int) (data.get(2) / (float) data.get(3) * 65);
        this.blit(arg, this.leftPos + 4, this.topPos + 6 + 65, 178, 71, 18, -fluidHeight);
    }

    @Override
    protected void renderLabels(PoseStack arg, int i, int j) {
        arg.pushPose();
        arg.scale(0.75F, 0.75F, 0.75F);
        this.font.draw(arg, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        arg.popPose();
    }
}
