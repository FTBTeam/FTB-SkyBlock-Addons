package dev.ftb.ftbsba.tools.content.fusion;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.ftbsba.FTBSBA;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FusingMachineScreen extends AbstractContainerScreen<FusingMachineContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(FTBSBA.MOD_ID, "textures/gui/fusing_machine_background.png");

    public FusingMachineScreen(FusingMachineContainer arg, Inventory arg2, Component arg3) {
        super(arg, arg2, arg3);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(PoseStack arg, int m, int n, float g) {
        super.render(arg, m, n, g);
    }

    @Override
    protected void renderBg(PoseStack arg, float f, int i, int j) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        this.blit(arg, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
