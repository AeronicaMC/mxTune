/*
 * This network code is from coolAlias's github repository
 * https://github.com/coolAlias/Tutorial-Demo
 */
package net.aeronica.mods.mxtune.network;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.network.bidirectional.*;
import net.aeronica.mods.mxtune.network.client.*;
import net.aeronica.mods.mxtune.network.server.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;


/*
 *
 * This class will house the SimpleNetworkWrapper instance, which I will name
 * 'channel', as well as give us a logical place from which to register our
 * packets. These two things could be done anywhere, however, even in your Main
 * class, but I will be adding other functionality (see below) that gives this
 * class a bit more utility.
 *
 * While unnecessary, I'm going to turn this class into a 'wrapper' for
 * SimpleNetworkWrapper so that instead of writing
 * "PacketDispatcher.channel.{method}" I can simply write
 * "PacketDispatcher.{method}" All this does is make it quicker to type and
 * slightly shorter; if you do not care about that, then make the 'channel'
 * field public instead of private, or, if you do not want to add a new class
 * just for one field and one static method that you could put anywhere, feel
 * free to put them wherever.
 *
 * For further convenience, I have also added two extra sendToAllAround methods:
 * one which takes an EntityPlayer and one which takes coordinates.
 *
 */
public class PacketDispatcher
{
    public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Reference.MOD_ID, "network");

    public static final String NETWORK_VERSION = new ResourceLocation(Reference.MOD_ID, "1").toString();
    /*
     * a simple counter will allow us to get rid of 'magic' numbers used during
     * packet registration
     */
    private static byte packetId = 0;

    /*
     * The SimpleNetworkWrapper instance is used both to register and send
     * packets. Since I will be adding wrapper methods, this field is private,
     * but you should make it public if you plan on using it directly.
     */
    private static final SimpleChannel channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
            .clientAcceptedVersions(version -> true)
            .serverAcceptedVersions(version -> true)
            .networkProtocolVersion(() -> NETWORK_VERSION)
            .simpleChannel();

    private PacketDispatcher() { /* NOP */ }

    /**
     * Call this during pre-init or loading and register all of your packets
     * (messages) here
     */
    public static final void registerPackets()
    {
        // Packets handled on CLIENT
        registerMessage(JoinGroupMessage.class);
        registerMessage(PlayJamMessage.class);
        registerMessage(PlaySoloMessage.class);
        registerMessage(SyncGroupMessage.class);
        registerMessage(SyncPlayerMusicOptionsMessage.class);
        registerMessage(SyncStatusMessage.class);
        registerMessage(PlaySoloMessage.class);
        registerMessage(SendCSDChatMessage.class);
        registerMessage(PlayBlockMusicMessage.class);
        registerMessage(AudiblePingPlayerMessage.class);
        registerMessage(UpdateChunkMusicData.class);
        registerMessage(UpdateWorldMusicData.class);
        registerMessage(StopPlayIDMessage.class);
        registerMessage(ResetClientPlayEngine.class);

        // Packets handled on SERVER
        registerMessage(ManageGroupMessage.class);
        registerMessage(MusicOptionsMessage.class);
        registerMessage(MusicTextMessage.class);
        registerMessage(HudOptionsMessage.class);
        registerMessage(BandAmpMessage.class);
        registerMessage(ByteArrayPartMessage.class);
        registerMessage(PlayerSelectedPlayListMessage.class);
        registerMessage(ChunkToolMessage.class);

        /*
         * If you don't want to make a 'registerMessage' method, you can do it
         * directly:
         */
        // PacketDispatcher.channel.registerMessage(SyncPlayerPropsMessage.class,
        // SyncPlayerPropsMessage.class, packetId++, Side.CLIENT);
        // PacketDispatcher.channel.registerMessage(OpenGuiMessage.class,
        // OpenGuiMessage.class, packetId++, Side.SERVER);

        // Bidirectional packets:
        registerMessage(SendKeyMessage.class);
        registerMessage(ClientStateDataMessage.class);
        registerMessage(GetServerDataMessage.class);
        registerMessage(SendResultMessage.class);
        registerMessage(GetBaseDataListsMessage.class);
        registerMessage(SetServerSerializedDataMessage.class);
    }

    /**
     * Registers an {@link AbstractMessage} to the appropriate side(s)
     */
    private static final <T extends AbstractMessage<T> & IMessageHandler<T, PacketBuffer>> void registerMessage(Class<T> clazz)
    {
        /*
         * We can tell by the message class which side to register it on by
         * using #isAssignableFrom (google it)
         */

        /*
         * Also, one can see the convenience of using a static counter
         * 'packetId' to keep track of the current index, rather than
         * hard-coding them all, plus it's one less parameter to pass.
         */
        if (AbstractMessage.AbstractClientMessage.class.isAssignableFrom(clazz))
        {
            PacketDispatcher.channel.m(clazz, clazz, packetId++, Dist.CLIENT);
        } else if (AbstractMessage.AbstractServerMessage.class.isAssignableFrom(clazz))
        {
//            public <MSG> IndexedMessageCodec.MessageHandler<MSG> registerMessage(int index, Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
//            return this.indexedCodec.addCodecIndex(index, messageType, encoder, decoder, messageConsumer);
        }
        try
        {
            PacketDispatcher.channel.messageBuilder(clazz, 1)
                    .decoder(clazz.getMethod("decode", new Class<?>[]{PacketBuffer.class})::decode)
                    .encoder(clazz::encode)
                    .consumer(clazz::handle)
                    .add();
        } catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        } catch (SecurityException e)
        {
            e.printStackTrace();
        }
    } else
        {
            /*
             * hopefully you didn't forget to extend the right class, or you
             * will get registered on both sides
             */
            PacketDispatcher.channel.registerMessage(clazz, clazz, packetId, Dist.CLIENT);
            PacketDispatcher.channel.registerMessage(clazz, clazz, packetId++, Dist.DEDICATED_SERVER);
        }
    }

    // ========================================================//
    // The following methods are the 'wrapper' methods; again,
    // this just makes sending a message slightly more compact
    // and is purely a matter of stylistic preference
    // ========================================================//

    /**
     * Send this message to the specified player's client-side counterpart. See
     * {@link SimpleNetworkWrapper#sendTo(IMessage, ServerPlayerEntity)}
     */
    public static final void sendTo(IMessage message, ServerPlayerEntity player)
    {
        PacketDispatcher.channel.sendTo(message, player);
    }

    /**
     * Send this message to everyone. See
     * {@link SimpleNetworkWrapper#sendToAll(IMessage)}
     */
    public static void sendToAll(IMessage message)
    {
        PacketDispatcher.channel.sendToAll(message);
    }

    /**
     * Send this message to everyone within a certain range of a point. See
     * {@link SimpleNetworkWrapper#sendToAllAround(IMessage, NetworkRegistry.TargetPoint)}
     */
    public static final void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point)
    {
        PacketDispatcher.channel.sendToAllAround(message, point);
    }

    /**
     * Sends a message to everyone within a certain range of the coordinates in
     * the same dimension. Shortcut to
     * {@link SimpleNetworkWrapper#sendToAllAround(IMessage, NetworkRegistry.TargetPoint)}
     */
    public static final void sendToAllAround(IMessage message, int dimension, double x, double y, double z, double range)
    {
        PacketDispatcher.sendToAllAround(message, new NetworkRegistry.TargetPoint(dimension, x, y, z, range));
    }

    /**
     * Sends a message to everyone within a certain range of the player
     * provided. Shortcut to
     * {@link SimpleNetworkWrapper#sendToAllAround(IMessage, NetworkRegistry.TargetPoint)}
     */
    public static final void sendToAllAround(IMessage message, PlayerEntity player, double range)
    {
        PacketDispatcher.sendToAllAround(message, player.getEntityWorld().provider.getDimension(), player.posX, player.posY, player.posZ, range);
    }

    /**
     * Send this message to everyone within the supplied dimension. See
     * {@link SimpleNetworkWrapper#sendToDimension(IMessage, int)}
     */
    public static final void sendToDimension(IMessage message, int dimensionId)
    {
        PacketDispatcher.channel.sendToDimension(message, dimensionId);
    }

    /**
     * Send this message to the server. See
     * {@link SimpleNetworkWrapper#sendToServer(IMessage)}
     */
    public static final void sendToServer(IMessage message)
    {
        PacketDispatcher.channel.sendToServer(message);
    }
}
