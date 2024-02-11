package dev.ftb.ftbsba.tools.content.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

public abstract class FluidAndEnergyScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private int fluidXOffset;
    private int progressXOffset;
    private ResourceLocation texture;

    public FluidAndEnergyScreen(T arg, Inventory arg2, Component arg3, int fluidXOffset, int progressXOffset, ResourceLocation texture) {
        super(arg, arg2, arg3);
        this.fluidXOffset = fluidXOffset;
        this.progressXOffset = progressXOffset;
        this.texture = texture;
    }

    @Override
    public void render(PoseStack arg, int mouseX, int mouseY, float partialTicks) {
        super.render(arg, mouseX, mouseY, partialTicks);

        if (mouseX > this.leftPos + this.fluidXOffset && mouseX < this.leftPos + this.fluidXOffset + 19 && mouseY > this.topPos + 3 && mouseY < this.topPos + 5 + 65) {
            arg.pushPose();
            arg.translate(mouseX - 5, mouseY, 600);
            arg.scale(0.6F, 0.6F, 0F);
            this.renderTooltip(arg, Component.literal(this.getFluidAmount() + " / " + this.getFluidCapacity() + " mB"), 0, 0);
            arg.popPose();
        }

        if (mouseX > this.leftPos + 166 && mouseX < this.leftPos + 174 && mouseY > this.topPos + 3 && mouseY < this.topPos + 5 + 65) {
            MutableComponent energyText = Component.literal(this.getEnergyAmount() + " / " + this.getEnergyCapacity() + " FE");
            int width = this.font.width(energyText);
            int scaledWidth = (int) (width * 0.6F);
            arg.pushPose();
            arg.translate(mouseX - 10 - scaledWidth, mouseY, 600);
            arg.scale(0.6F, 0.6F, 0F);
            this.renderTooltip(arg, energyText, 0, 0);
            arg.popPose();
        }

        renderTooltip(arg, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack arg, float f, int i, int j) {
        renderBackground(arg);

        arg.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.texture);

        this.blit(arg, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        arg.popPose();

        arg.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.8F);

        // Energy
        float x = (float) this.getEnergyAmount() / this.getEnergyCapacity();
        int energyHeight = (int) (x * 65);
        this.blit(arg, this.leftPos + imageWidth - 9, this.topPos + 4 + 65 - energyHeight, 197, 4 + 65 - energyHeight, 5, energyHeight);
        arg.popPose();

        RenderSystem.disableBlend();

        var fluidStack = this.getFluidStack();
        if (fluidStack == null || fluidStack.getFluid() == null) {
            return;
        }

        var fluid = fluidStack.getFluid();
        var fluidExtensions = IClientFluidTypeExtensions.of(fluid);
        var texture = fluidExtensions.getStillTexture(fluidStack);
        var atlasSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);

        // Fluid amount
        float fluidHeight = (float) this.getFluidAmount() / this.getFluidCapacity() * 65;
        int textureHeight = 16;

        int tilesRequired = (int) Math.ceil(fluidHeight / textureHeight);

        arg.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int[] cols = decomposeColor(fluidExtensions.getTintColor(fluidStack));

        for (int k = 0; k < tilesRequired; k++) {
            int height = 16 - (int) Math.min(textureHeight, fluidHeight - k * textureHeight);
            drawFluidTexture(arg, this.leftPos + (this.fluidXOffset + 1), this.topPos + 6 + 47 - (k * textureHeight), atlasSprite, height, cols);
        }

        RenderSystem.disableBlend();
        arg.popPose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.texture);

        // Fluid gauge
        arg.pushPose();
        arg.translate(0, 0, 101);
        this.blit(arg, this.leftPos + (this.fluidXOffset + 1), this.topPos + 6, 178, 3, 18, 67);
        arg.popPose();

        // Finally, draw the progress bar
        if (this.getProgressRequired() != 0) {
            float computedPercentage = (float) this.getProgress() / this.getProgressRequired() * 24;
            this.blit(arg, this.leftPos + this.progressXOffset, this.topPos + 28, 203, 0, (int) computedPercentage + 1, 16);
        }
    }

    // Thanks Des
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

    // Thanks Des
    public static int[] decomposeColor(int color) {
        int[] res = new int[4];
        res[0] = color >> 24 & 0xff;
        res[1] = color >> 16 & 0xff;
        res[2] = color >> 8  & 0xff;
        res[3] = color       & 0xff;
        return res;
    }

    public abstract int getEnergyAmount();
    public abstract int getEnergyCapacity();

    public abstract int getFluidAmount();
    public abstract int getFluidCapacity();

    @Nullable
    public abstract FluidStack getFluidStack();

    public abstract int getProgress();
    public abstract int getProgressRequired();
}
