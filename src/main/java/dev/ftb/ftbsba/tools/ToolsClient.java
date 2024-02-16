package dev.ftb.ftbsba.tools;

import dev.ftb.ftbsba.tools.content.fusion.FusingMachineScreen;
import dev.ftb.ftbsba.tools.content.supercooler.SuperCoolerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class ToolsClient {
    public static void init() {
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.IRON_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.GOLD_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.DIAMOND_AUTO_HAMMER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ToolsRegistry.NETHERITE_AUTO_HAMMER.get(), RenderType.cutout());

        MenuScreens.register(ToolsRegistry.FUSING_MACHINE_CONTAINER.get(), FusingMachineScreen::new);
        MenuScreens.register(ToolsRegistry.SUPER_COOLER_CONTAINER.get(), SuperCoolerScreen::new);
    }

    public static <T> Optional<T> getBlockEntityAt(BlockPos pos, Class<T> cls) {
        Level level = Minecraft.getInstance().level;
        if (level != null && pos != null) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te != null && cls.isAssignableFrom(te.getClass())) {
                //noinspection unchecked
                return Optional.of((T) te);
            }
        }
        return Optional.empty();
    }
}
