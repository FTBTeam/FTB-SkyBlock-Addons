package dev.ftb.ftbsba.tools;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

public class ToolsClient {
    public static void init() {
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.IRON_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.GOLD_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.DIAMOND_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.NETHERITE_AUTO_HAMMER.get(), RenderType.cutout());
    }
}
