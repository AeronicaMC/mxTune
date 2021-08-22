package net.aeronica.mods.mxtune.network;

import net.aeronica.mods.mxtune.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketDispatcher
{
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Reference.MOD_ID, "network");
    private static final String NETWORK_VERSION = new ResourceLocation(Reference.MOD_ID, "1").toString();
    private static int packetId = 0;
    private static SimpleChannel HANDLER  = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
        .clientAcceptedVersions(version -> true)
        .serverAcceptedVersions(version -> true)
        .networkProtocolVersion(() -> NETWORK_VERSION)
        .simpleChannel();

    public static void register()
    {
        // Bidirectional
        registerMessage(SendKeyMessage.class, SendKeyMessage::encode, SendKeyMessage::decode, SendKeyMessage::handle);
        // To Client(s)
        registerMessage(LivingEntityModCapSync.class, LivingEntityModCapSync::encode, LivingEntityModCapSync::decode, LivingEntityModCapSync::handle);
        registerMessage(OpenScreenMessage.class, OpenScreenMessage::encode, OpenScreenMessage::decode, OpenScreenMessage::handle);
    }

    private static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler)
    {
        HANDLER.registerMessage(packetId++, messageType, encoder, decoder, handler);
    }

    private PacketDispatcher() { /* NOP */ }

    /**
     * Send this message to the specified player's client-side counterpart. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <MSG> void sendTo(MSG message, ServerPlayerEntity player)
    {
        HANDLER.send(PacketDistributor.PLAYER.with(()->player), message);
    }

    /**
     * Send this message to everyone. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <MSG> void sendToAll(MSG message)
    {
        HANDLER.send(PacketDistributor.ALL.with(null), message);
    }

    /**
     * Send this message to everyone within a certain range of a point. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <MSG> void sendToAllAround(MSG message, PacketDistributor.TargetPoint point)
    {
        HANDLER.send(PacketDistributor.NEAR.with(()->point), message);
    }

    /**
     * Sends a message to everyone within a certain range of the coordinates in
     * the same dimension. Shortcut to
     * {@link net.aeronica.mods.mxtune.network.PacketDispatcher#sendToAllAround(MSG, PacketDistributor.TargetPoint)}
     */
    public static <MSG> void sendToAllAround(MSG message, RegistryKey<World> dimension, double x, double y, double z, double range)
    {
        sendToAllAround(message, new PacketDistributor.TargetPoint(x, y, z, range, dimension));
    }

    /**
     * Sends a message to everyone within a certain range of the player
     * provided. Shortcut to
     * {@link net.aeronica.mods.mxtune.network.PacketDispatcher#sendToAllAround(MSG, PlayerEntity, double)}
     */
    public static <MSG> void sendToAllAround(MSG message, PlayerEntity player, double range)
    {
        sendToAllAround(message, player.getCommandSenderWorld().dimension(), player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getY(), range);
    }

    /**
     * Send this message to everyone within the supplied dimension. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <MSG> void sendToDimension(MSG message, RegistryKey<World> dimension)
    {
        HANDLER.send(PacketDistributor.DIMENSION.with(()->dimension), message);
    }

    /**
     * Send this message to the server. See
     * {@link SimpleChannel#sendToServer(Object)}
     */
    public static <MSG> void sendToServer(MSG message)
    {
        HANDLER.sendToServer(message);
    }

}
