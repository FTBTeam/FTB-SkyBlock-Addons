package dev.ftb.ftbsba.tools.content.supercooler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import dev.ftb.ftbsba.FTBSBA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.IFluidTank;
import org.lwjgl.opengl.GL11;

public class SuperCoolerScreen extends AbstractContainerScreen<SuperCoolerContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(FTBSBA.MOD_ID, "textures/gui/super_cooler_background.png");

    public SuperCoolerScreen(SuperCoolerContainer arg, Inventory arg2, Component arg3) {
        super(arg, arg2, arg3);
        this.titleLabelX = 96;

    }

    @Override
    public void render(PoseStack arg, int mouseX, int mouseY, float partialTicks) {
        super.render(arg, mouseX, mouseY, partialTicks);

        var data = this.menu.containerData;
        if (mouseX > this.leftPos + 3 && mouseX < this.leftPos + 21 && mouseY > this.topPos + 3 && mouseY < this.topPos + 5 + 65) {
            arg.pushPose();
            arg.translate(mouseX - 5, mouseY, 600);
            arg.scale(0.6F, 0.6F, 0F);
            this.renderTooltip(arg, Component.literal(data.get(2) + " / " + this.menu.entity.tank.map(IFluidTank::getCapacity).orElse(0) + " mB"), 0, 0);
            arg.popPose();
        }

        if (mouseX > this.leftPos + 166 && mouseX < this.leftPos + 174 && mouseY > this.topPos + 3 && mouseY < this.topPos + 5 + 65) {
            var uncompressedEnergy = (data.get(0) & 0xFFFF) | (data.get(1) << 16);

            MutableComponent energyText = Component.literal(uncompressedEnergy + " / " + this.menu.entity.energyLazy.map(IEnergyStorage::getMaxEnergyStored).orElse(0) + " FE");
            int width = this.font.width(energyText);
            int scaledWidth = (int) (width * 0.6F);
            arg.pushPose();
            arg.translate(mouseX - 10 - scaledWidth, mouseY, 600);
            arg.scale(0.6F, 0.6F, 0F);
            this.renderTooltip(arg, energyText, 0, 0);
            arg.popPose();
        }
    }

    @Override
    protected void renderBg(PoseStack arg, float f, int i, int j) {
        renderBackground(arg);

        arg.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        this.blit(arg, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        arg.popPose();

        var data = this.menu.entity.containerData;

        int combinedInt = data.get(0) & 0xFFFF | (data.get(1) & 0xFFFF) << 16;
        int energyMax = this.menu.entity.energyLazy.map(IEnergyStorage::getMaxEnergyStored).orElse(0);
        int fluidMax = this.menu.entity.tank.map(IFluidTank::getCapacity).orElse(0);

        arg.pushPose();
        // Energy
        float x = (float) combinedInt / energyMax;
        int energyHeight = (int) (x * 65);
        this.blit(arg, this.leftPos + imageWidth - 9, this.topPos + 4 + 65 - energyHeight, 197, 4 + 65 - energyHeight, 5, energyHeight);
        arg.popPose();

        var fluid = this.menu.entity.tank.map(IFluidTank::getFluid).orElse(null);
        if (fluid == null) {
            return;
        }

        var fluidExtensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        var texture = fluidExtensions.getStillTexture(fluid);
        var atlasSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);

        // Fluid amount
        int fluidHeight = (data.get(2) / fluidMax) * 65;
        int textureHeight = 16;

        int tilesRequired = (int) Math.ceil((float) fluidHeight / textureHeight);

        arg.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int[] cols = decomposeColor(fluidExtensions.getTintColor(fluid));

        for (int k = 0; k < tilesRequired; k++) {
            int height = Math.min(textureHeight, fluidHeight - k * textureHeight);
            drawFluidTexture(arg, this.leftPos + 4, this.topPos + 6 + 48 - fluidHeight + k * textureHeight, atlasSprite, height, cols);
        }

        RenderSystem.disableBlend();
        arg.popPose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);

        // Fluid gauge
        arg.pushPose();
        arg.translate(0, 0, 101);
        this.blit(arg, this.leftPos + 4, this.topPos + 6, 178, 3, 18, 67);
        arg.popPose();
    }

    private static void drawFluidTexture(PoseStack stack, float xCoord, float yCoord, TextureAtlasSprite textureSprite, int maskTop, int[] cols) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - 0 / 16.0f * (uMax - uMin);
        vMax = vMax - maskTop / 16.0f * (vMax - vMin);

        Matrix4f posMat = stack.last().pose();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuilder();
        worldrenderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        worldrenderer.vertex(posMat, xCoord, yCoord + 16, 100).color(cols[1], cols[2], cols[3], cols[0]).uv(uMin, vMax).endVertex();
        worldrenderer.vertex(posMat,xCoord + 16 - 0, yCoord + 16, 100).color(cols[1], cols[2], cols[3], cols[0]).uv(uMax, vMax).endVertex();
        worldrenderer.vertex(posMat, xCoord + 16 - 0, yCoord + maskTop, 100).color(cols[1], cols[2], cols[3], cols[0]).uv(uMax, vMin).endVertex();
        worldrenderer.vertex(posMat, xCoord, yCoord + maskTop, 100).color(cols[1], cols[2], cols[3], cols[0]).uv(uMin, vMin).endVertex();
        tessellator.end();
    }

    public static int[] decomposeColor(int color) {
        int[] res = new int[4];
        res[0] = color >> 24 & 0xff;
        res[1] = color >> 16 & 0xff;
        res[2] = color >> 8  & 0xff;
        res[3] = color       & 0xff;
        return res;
    }

    @Override
    protected void renderLabels(PoseStack arg, int i, int j) {
        arg.pushPose();
        arg.scale(0.75F, 0.75F, 0.75F);
        this.font.draw(arg, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        arg.popPose();
    }
}
