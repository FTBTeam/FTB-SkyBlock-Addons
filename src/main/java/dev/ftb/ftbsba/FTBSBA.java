package dev.ftb.ftbsba;

import dev.ftb.ftbsba.config.FTBSBConfig;
import dev.ftb.ftbsba.tools.ToolsClient;
import dev.ftb.ftbsba.tools.ToolsMain;
import dev.ftb.ftbsba.tools.ToolsRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(FTBSBA.MOD_ID)
public class FTBSBA {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "ftbsba";

    public FTBSBA() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FTBSBConfig.COMMON_CONFIG);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register everything that's part of the tools system
        ToolsRegistry.REGISTERS.forEach(e -> e.register(modBus));

        modBus.addListener(this::clientSetup);
        modBus.addListener(this::postSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ToolsClient.init();
    }

    public void postSetup(FMLLoadCompleteEvent event) {
        ToolsMain.setup();
    }
}
