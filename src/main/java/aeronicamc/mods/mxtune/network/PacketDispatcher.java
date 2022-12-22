package aeronicamc.mods.mxtune.network;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.network.messages.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@SuppressWarnings("unused")
public class PacketDispatcher
{
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Reference.MOD_ID, "network");
    private static final String PROTOCOL_VERSION = "2.0.1";
    private static int packetId = 0;
    private static SimpleChannel channel;

    private PacketDispatcher() { /* NOP */ }

    public static void register()
    {
        channel = NetworkRegistry.ChannelBuilder
                .named(CHANNEL_NAME)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();

        // Bidirectional
        registerMessage(SendKeyMessage.class, new SendKeyMessage());
        // To Client(s)
        registerMessage(PlayerNexusSync.class, new PlayerNexusSync());
        registerMessage(OpenScreenMessage.class, new OpenScreenMessage());
        registerMessage(PlayMusicMessage.class, new PlayMusicMessage());
        registerMessage(StopPlayMessage.class, new StopPlayMessage());
        registerMessage(MusicVenueSyncMessage.class, new MusicVenueSyncMessage());
        // To Server
        registerMessage(CreateMusicScoreMessage.class, new CreateMusicScoreMessage());
        registerMessage(CreateSheetMusicMessage.class, new CreateSheetMusicMessage());
        registerMessage(ChooseInstrumentMessage.class, new ChooseInstrumentMessage());
        registerMessage(AudiblePingPlayerMessage.class, new AudiblePingPlayerMessage());
        registerMessage(ByteArrayPartMessage.class, new ByteArrayPartMessage());
        registerMessage(ToolManagerSyncMessage.class, new ToolManagerSyncMessage());
        registerMessage(SyncRequestMessage.class, new SyncRequestMessage());
        registerMessage(MusicBlockMessage.class, new MusicBlockMessage());
    }

    private static <MSG extends AbstractMessage<MSG>> void registerMessage(Class<MSG> messageType, AbstractMessage<MSG> message)
    {
        channel.registerMessage(packetId++, messageType, message::encode, message::decode, message::handle);
    }

    /**
     * Send this message to the specified player's client-side counterpart. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <MSG extends AbstractMessage<MSG>> void sendTo(MSG message, ServerPlayerEntity player)
    {
        channel.send(PacketDistributor.PLAYER.with(()->player), message);
    }

    /**
     * Send this message to everyone. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <MSG extends AbstractMessage<MSG>> void sendToAll(MSG message)
    {
        channel.send(PacketDistributor.ALL.noArg(), message);
    }

    /**
     * Send this message to everyone within a certain range of a point. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <MSG extends AbstractMessage<MSG>> void sendToAllAround(MSG message, PacketDistributor.TargetPoint point)
    {
        channel.send(PacketDistributor.NEAR.with(()->point), message);
    }

    /**
     * Sends a message to everyone within a certain range of the coordinates in
     * the same dimension. Shortcut to
     * {@link PacketDispatcher#sendToAllAround(MSG, PacketDistributor.TargetPoint)}
     */
    public static <MSG extends AbstractMessage<MSG>> void sendToAllAround(MSG message, RegistryKey<World> dimension, double x, double y, double z, double range)
    {
        sendToAllAround(message, new PacketDistributor.TargetPoint(x, y, z, range, dimension));
    }

    /**
     * Sends a message to everyone within a certain range of the player
     * provided. Shortcut to
     * {@link PacketDispatcher#sendToAllAround(MSG, PlayerEntity, double)}
     */
    public static <MSG extends AbstractMessage<MSG>> void sendToAllAround(MSG message, PlayerEntity player, double range)
    {
        sendToAllAround(message, player.getCommandSenderWorld().dimension(), player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getY(), range);
    }

    /**
     * Sends a message to everyone within a certain range of the block
     * @param message a custom message
     * @param dimension<World> the world to send it too
     * @param blockPos the position in the world to
     * @param range around the position
     */
    public static <MSG extends AbstractMessage<MSG>> void sendToAllAround(MSG message, RegistryKey<World> dimension, BlockPos blockPos, double range)
    {
        sendToAllAround(message, dimension, blockPos.getX(), blockPos.getY(), blockPos.getY(), range);
    }

    /**
     * Send this message to everyone within the supplied dimension. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <MSG extends AbstractMessage<MSG>> void sendToDimension(MSG message, RegistryKey<World> dimension)
    {
        channel.send(PacketDistributor.DIMENSION.with(()->dimension), message);
    }

    /**
     * Send this message to the tracking entity. see
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     * @param message custom message
     * @param entity the tracking entity
     */
    public static <MSG extends AbstractMessage<MSG>> void sendToTrackingEntity(MSG message, Entity entity)
    {
        channel.send(PacketDistributor.TRACKING_ENTITY.with(()->entity), message);
    }

    /**
     * Send this message to the tracking entity and self. see
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     * @param message custom message
     * @param entity the tracking entity
     * https://forums.minecraftforge.net/topic/103538-solved116-how-to-sync-other-players-capability-and-update-it-in-own-client/?tab=comments#comment-464260
     */
    public static <MSG extends AbstractMessage<MSG>> void sendToTrackingEntityAndSelf(MSG message, Entity entity)
    {
        channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->entity), message);
    }

    /**
     * Send this message to the server. See
     * {@link SimpleChannel#sendToServer(Object)}
     */
    public static <MSG extends AbstractMessage<MSG>> void sendToServer(MSG message)
    {
        channel.sendToServer(message);
    }

}
