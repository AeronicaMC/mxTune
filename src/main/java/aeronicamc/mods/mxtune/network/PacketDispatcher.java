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
    private static final String PROTOCOL_VERSION = "2.0.5";
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
        registerMessage(OpenScreenMessage.class, new OpenScreenMessage());
        registerMessage(GroupCmdMessage.class, new GroupCmdMessage());
        // To Client(s)
        registerMessage(PlayerNexusSync.class, new PlayerNexusSync());
        registerMessage(PlayMusicMessage.class, new PlayMusicMessage());
        registerMessage(StopPlayMessage.class, new StopPlayMessage());
        registerMessage(MusicVenueSyncMessage.class, new MusicVenueSyncMessage());
        registerMessage(SyncGroupsMessage.class, new SyncGroupsMessage());
        registerMessage(SyncGroupMemberState.class, new SyncGroupMemberState());
        registerMessage(OpenPinEntryMessage.class, new OpenPinEntryMessage());
        // To Server
        registerMessage(CreateIMusicMessage.class, new CreateIMusicMessage());
        registerMessage(ChooseInstrumentMessage.class, new ChooseInstrumentMessage());
        registerMessage(AudiblePingPlayerMessage.class, new AudiblePingPlayerMessage());
        registerMessage(StringPartMessage.class, new StringPartMessage());
        registerMessage(ToolManagerSyncMessage.class, new ToolManagerSyncMessage());
        registerMessage(SyncRequestMessage.class, new SyncRequestMessage());
        registerMessage(MusicBlockMessage.class, new MusicBlockMessage());
        registerMessage(SendPinEntryMessage.class, new SendPinEntryMessage());
    }

    private static <M extends AbstractMessage<M>> void registerMessage(Class<M> messageType, AbstractMessage<M> message)
    {
        channel.registerMessage(packetId++, messageType, message::encode, message::decode, message::handle);
    }

    /**
     * Send this message to the specified player's client-side counterpart. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <M extends AbstractMessage<M>> void sendTo(M message, ServerPlayerEntity player)
    {
        channel.send(PacketDistributor.PLAYER.with(()->player), message);
    }

    /**
     * Send this message to everyone. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <M extends AbstractMessage<M>> void sendToAll(M message)
    {
        channel.send(PacketDistributor.ALL.noArg(), message);
    }

    /**
     * Send this message to everyone within a certain range of a point. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <M extends AbstractMessage<M>> void sendToAllAround(M message, PacketDistributor.TargetPoint point)
    {
        channel.send(PacketDistributor.NEAR.with(()->point), message);
    }

    /**
     * Sends a message to everyone within a certain range of the coordinates in
     * the same dimension. Shortcut to
     * {@link PacketDispatcher#sendToAllAround(M, PacketDistributor.TargetPoint)}
     */
    public static <M extends AbstractMessage<M>> void sendToAllAround(M message, RegistryKey<World> dimension, double x, double y, double z, double range)
    {
        sendToAllAround(message, new PacketDistributor.TargetPoint(x, y, z, range, dimension));
    }

    /**
     * Sends a message to everyone within a certain range of the player
     */
    public static <M extends AbstractMessage<M>> void sendToAllAround(M message, PlayerEntity player, double range)
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
    public static <M extends AbstractMessage<M>> void sendToAllAround(M message, RegistryKey<World> dimension, BlockPos blockPos, double range)
    {
        sendToAllAround(message, dimension, blockPos.getX(), blockPos.getY(), blockPos.getY(), range);
    }

    /**
     * Send this message to everyone within the supplied dimension. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <M extends AbstractMessage<M>> void sendToDimension(M message, RegistryKey<World> dimension)
    {
        channel.send(PacketDistributor.DIMENSION.with(()->dimension), message);
    }

    /**
     * Send this message to the tracking entity. see
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     * @param message custom message
     * @param entity the tracking entity
     */
    public static <M extends AbstractMessage<M>> void sendToTrackingEntity(M message, Entity entity)
    {
        channel.send(PacketDistributor.TRACKING_ENTITY.with(()->entity), message);
    }

    /**
     * Send this message to the tracking entity and self. see
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     * @param message custom message
     * @param entity the tracking entity
     * <a href="https://forums.minecraftforge.net/topic/103538-solved116-how-to-sync-other-players-capability-and-update-it-in-own-client/?tab=comments#comment-464260">...</a>
     */
    public static <M extends AbstractMessage<M>> void sendToTrackingEntityAndSelf(M message, Entity entity)
    {
        channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->entity), message);
    }

    /**
     * Send this message to the server. See
     * {@link SimpleChannel#sendToServer(Object)}
     */
    public static <M extends AbstractMessage<M>> void sendToServer(M message)
    {
        channel.sendToServer(message);
    }

}
