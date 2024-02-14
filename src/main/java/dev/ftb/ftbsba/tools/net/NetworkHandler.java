package dev.ftb.ftbsba.tools.net;

import dev.ftb.ftbsba.FTBSBA;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "2";
    public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(FTBSBA.MOD_ID, "main_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();
    private static int det = 0;

    private static int nextId() {
        return det++;
    }

    public static void init() {
        register(FluidTankSync.class,
                FluidTankSync::toBytes, FluidTankSync::new, FluidTankSync::handle,
                NetworkDirection.PLAY_TO_CLIENT);
    }

    private static <MSG> void register(Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer, NetworkDirection direction) {
        NETWORK.registerMessage(nextId(), messageType, encoder, decoder, messageConsumer, Optional.ofNullable(direction));
    }
}
