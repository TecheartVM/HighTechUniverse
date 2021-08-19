package techeart.htu.utils.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import techeart.htu.MainClass;
import techeart.htu.utils.network.packets.PacketSensorTemperatureConfig;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class HTUPacketHandler
{
    private static final String PROTOCOL_VERSION = "0.1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MainClass.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public void init()
    {
        registerMessageToServer(PacketSensorTemperatureConfig.class, PacketSensorTemperatureConfig::write, PacketSensorTemperatureConfig::read, PacketSensorTemperatureConfig::handle);
    }

    private static int lastMessageId = 0;
    protected <MSG> void registerMessageToServer(Class<MSG> messageClass, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler)
    {
        CHANNEL.registerMessage(lastMessageId++, messageClass, encoder, decoder, handler, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    protected <MSG> void registerMessageToClient(Class<MSG> messageClass, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler)
    {
        CHANNEL.registerMessage(lastMessageId++, messageClass, encoder, decoder, handler, Optional.of(NetworkDirection.LOGIN_TO_CLIENT));
    }

    public <MSG> void sendTo(MSG message, ServerPlayerEntity player)
    {
        CHANNEL.sendTo(message, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public <MSG> void sendToAll(MSG message) { CHANNEL.send(PacketDistributor.ALL.noArg(), message); }

    public <MSG> void sendToAllIfServerLoaded(MSG message) { if(ServerLifecycleHooks.getCurrentServer() != null) sendToAll(message); }

    public <MSG> void sendToServer(MSG message) { CHANNEL.sendToServer(message); }
}
