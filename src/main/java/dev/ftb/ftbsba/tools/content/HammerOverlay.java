package dev.ftb.ftbsba.tools.content;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.ftbsba.FTBSBA;
import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.recipies.HammerRecipe;
import dev.ftb.ftbsba.tools.recipies.NoInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HammerOverlay {
    private static final ResourceLocation TEXTURE = new ResourceLocation(FTBSBA.MOD_ID, "textures/hammer_convert.png");

    private static float tick = 0;
    private static int index = 0;

    public static Cache<Block, List<ItemStack>> recipeCacheResult = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(1))
            .build();

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof HammerItem)) {
            return;
        }

        HitResult pick = player.pick(ForgeMod.REACH_DISTANCE.get().getDefaultValue(), event.getPartialTicks(), false);
        if (!(pick instanceof BlockHitResult blockHit)) {
            return;
        }

        BlockState state = player.level.getBlockState(blockHit.getBlockPos());
        List<ItemStack> recipeResults = new ArrayList<>();
        try {
            recipeResults = recipeCacheResult.get(state.getBlock(), () -> {
                Set<Item> drops = new HashSet<>();
                for (HammerRecipe recipe : player.level.getRecipeManager().getRecipesFor(ToolsRegistry.HAMMER_RECIPE_TYPE.get(), NoInventory.INSTANCE, player.level)) {
                    if (recipe.ingredient.test(new ItemStack(state.getBlock()))) {
                        recipe.results.forEach(e -> drops.add(e.getItem()));
                    }
                }

                return drops.stream().map(ItemStack::new).toList();
            });
        } catch (ExecutionException ignored) {}

        if (recipeResults.isEmpty()) {
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        int x = (window.getGuiScaledWidth() / 2) - (58 / 2);
        int y = window.getGuiScaledHeight() - 80;

        var pose = event.getMatrixStack();
        pose.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        Screen.blit(pose, x, y, 0, 0, 58, 16, 58, 32);

        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode != null) {
            // 17 (start) -> 40 (end)
            Screen.blit(pose, x, y, (float) 0, 16F, 17 + (int) (gameMode.destroyProgress * 23f), 16, 58, 32);
        }

        pose.popPose();

        tick += Minecraft.getInstance().getDeltaFrameTime();
        if (tick > 60) {
            tick = 0;
            index ++;
        }

        if (index >= recipeResults.size()) {
            index = 0;
        }

        renderItem(new ItemStack(player.level.getBlockState(blockHit.getBlockPos()).getBlock()), x + 3, y + 3);
        renderItem(recipeResults.get(index), x + (58 - 16) + 3, y + 3);
    }

    public static void renderItem(ItemStack itemStack, int x, int y) {
        var model = Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, 0);

        Minecraft.getInstance().textureManager.getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.translate(x, y, 100.0f + Minecraft.getInstance().getItemRenderer().blitOffset);
        modelViewStack.translate(5F, 5F, 0.0);
        modelViewStack.scale(1.0f, -1.0f, 1.0F);
        modelViewStack.scale(10.0f, 10.0f, 10.0f);

        PoseStack posestack1 = new PoseStack();
        RenderSystem.applyModelViewMatrix();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        if (!model.usesBlockLight()) {
            Lighting.setupForFlatItems();
        }

        Minecraft.getInstance().getItemRenderer().render(itemStack, ItemTransforms.TransformType.GUI, false, posestack1, bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, model);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if (!model.usesBlockLight()) {
            Lighting.setupFor3DItems();
        }
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
