package dev.ftb.ftbsba;

import dev.ftb.ftbsba.config.FTBSAConfig;
import dev.ftb.ftbsba.tools.ToolsClient;
import dev.ftb.ftbsba.tools.ToolsMain;
import dev.ftb.ftbsba.tools.ToolsRegistry;
import dev.ftb.ftbsba.tools.content.core.RecipeCaches;
import dev.ftb.ftbsba.tools.net.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
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
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FTBSAConfig.COMMON_CONFIG);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register everything that's part of the tools system
        ToolsRegistry.REGISTERS.forEach(e -> e.register(modBus));

        modBus.addListener(this::clientSetup);
        modBus.addListener(this::postSetup);

        forgeBus.addListener(this::addReloadListeners);

        NetworkHandler.init();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ToolsClient.init();
    }

    public void postSetup(FMLLoadCompleteEvent event) {
        ToolsMain.setup();
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RecipeCaches.ReloadListener());
    }
}
