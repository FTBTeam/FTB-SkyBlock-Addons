package dev.ftb.ftbsba.tools;

import dev.ftb.ftbsba.tools.content.fusion.FusingMachineScreen;
import dev.ftb.ftbsba.tools.content.supercooler.SuperCoolerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

public class ToolsClient {
    public static void init() {
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.IRON_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.GOLD_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.DIAMOND_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.NETHERITE_AUTO_HAMMER.get(), RenderType.cutout());

        MenuScreens.register(ToolsRegistry.FUSING_MACHINE_CONTAINER.get(), FusingMachineScreen::new);
        MenuScreens.register(ToolsRegistry.SUPER_COOLER_CONTAINER.get(), SuperCoolerScreen::new);
    }
}
