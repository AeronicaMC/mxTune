/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.network;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.network.bidirectional.ClientStateDataMessage;
import net.aeronica.mods.mxtune.network.bidirectional.GetBaseDataListsMessage;
import net.aeronica.mods.mxtune.network.bidirectional.GetServerDataMessage;
import net.aeronica.mods.mxtune.network.client.*;
import net.aeronica.mods.mxtune.network.server.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.Dimension;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketDispatcher
{
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Reference.MOD_ID, "network");
    private static final String NETWORK_VERSION = new ResourceLocation(Reference.MOD_ID, "1").toString();
    private static SimpleChannel modChannel;
    private static int packetId = 1;

    public static SimpleChannel getNetworkChannel()
    {
        final SimpleChannel channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
                .clientAcceptedVersions(version -> true)
                .serverAcceptedVersions(version -> true)
                .networkProtocolVersion(() -> NETWORK_VERSION)
                .simpleChannel();

        registerMessages(channel);
        modChannel = channel;

        return channel;
    }

    private static void registerMessages(SimpleChannel channel)
    {
        /*
         * Send to Server
         */
        channel.messageBuilder(BandAmpMessage.class, packetId++)
                .decoder(BandAmpMessage::decode)
                .encoder(BandAmpMessage::encode)
                .consumer(BandAmpMessage::handle)
                .add();

        channel.messageBuilder(ChunkToolMessage.class, packetId++)
                .decoder(ChunkToolMessage::decode)
                .encoder(ChunkToolMessage::encode)
                .consumer(ChunkToolMessage::handle)
                .add();

        channel.messageBuilder(PlayerSelectedPlayListMessage.class, packetId++)
                .decoder(PlayerSelectedPlayListMessage::decode)
                .encoder(PlayerSelectedPlayListMessage::encode)
                .consumer(PlayerSelectedPlayListMessage::handle)
                .add();

        channel.messageBuilder(ByteArrayPartMessage.class, packetId++)
                .decoder(ByteArrayPartMessage::decode)
                .encoder(ByteArrayPartMessage::encode)
                .consumer(ByteArrayPartMessage::handle)
                .add();

        channel.messageBuilder(HudOptionsMessage.class, packetId++)
                .decoder(HudOptionsMessage::decode)
                .encoder(HudOptionsMessage::encode)
                .consumer(HudOptionsMessage::handle)
                .add();

        channel.messageBuilder(ManageGroupMessage.class, packetId++)
                .decoder(ManageGroupMessage::decode)
                .encoder(ManageGroupMessage::encode)
                .consumer(ManageGroupMessage::handle)
                .add();

        channel.messageBuilder(MusicOptionsMessage.class, packetId++)
                .decoder(MusicOptionsMessage::decode)
                .encoder(MusicOptionsMessage::encode)
                .consumer(MusicOptionsMessage::handle)
                .add();

        channel.messageBuilder(MusicTextMessage.class, packetId++)
                .decoder(MusicTextMessage::decode)
                .encoder(MusicTextMessage::encode)
                .consumer(MusicTextMessage::handle)
                .add();

        channel.messageBuilder(SetServerSerializedDataMessage.class, packetId++)
                .decoder(SetServerSerializedDataMessage::decode)
                .encoder(SetServerSerializedDataMessage::encode)
                .consumer(SetServerSerializedDataMessage::handle)
                .add();

        /*
         * Send to Client(s)
         */
        channel.messageBuilder(ResetClientPlayEngine.class, packetId++)
                .decoder(ResetClientPlayEngine::decode)
                .encoder(ResetClientPlayEngine::encode)
                .consumer(ResetClientPlayEngine::handle)
                .add();

        channel.messageBuilder(StopPlayIDMessage.class, packetId++)
                .decoder(StopPlayIDMessage::decode)
                .encoder(StopPlayIDMessage::encode)
                .consumer(StopPlayIDMessage::handle)
                .add();

        channel.messageBuilder(PlayJamMessage.class, packetId++)
                .decoder(PlayJamMessage::decode)
                .encoder(PlayJamMessage::encode)
                .consumer(PlayJamMessage::handle)
                .add();

        channel.messageBuilder(SyncStatusMessage.class, packetId++)
                .decoder(SyncStatusMessage::decode)
                .encoder(SyncStatusMessage::encode)
                .consumer(SyncStatusMessage::handle)
                .add();

        channel.messageBuilder(PlaySoloMessage.class, packetId++)
                .decoder(PlaySoloMessage::decode)
                .encoder(PlaySoloMessage::encode)
                .consumer(PlaySoloMessage::handle)
                .add();

        channel.messageBuilder(PlayBlockMusicMessage.class, packetId++)
                .decoder(PlayBlockMusicMessage::decode)
                .encoder(PlayBlockMusicMessage::encode)
                .consumer(PlayBlockMusicMessage::handle)
                .add();

        channel.messageBuilder(UpdateChunkMusicData.class, packetId++)
                .decoder(UpdateChunkMusicData::decode)
                .encoder(UpdateChunkMusicData::encode)
                .consumer(UpdateChunkMusicData::handle)
                .add();

        channel.messageBuilder(UpdateWorldMusicData.class, packetId++)
                .decoder(UpdateWorldMusicData::decode)
                .encoder(UpdateWorldMusicData::encode)
                .consumer(UpdateWorldMusicData::handle)
                .add();
        /*
         * Bi-Directional
         */
        channel.messageBuilder(GetServerDataMessage.class, packetId++)
                .decoder(GetServerDataMessage::decode)
                .encoder(GetServerDataMessage::encode)
                .consumer(GetServerDataMessage::handle)
                .add();

        channel.messageBuilder(ClientStateDataMessage.class, packetId++)
                .decoder(ClientStateDataMessage::decode)
                .encoder(ClientStateDataMessage::encode)
                .consumer(ClientStateDataMessage::handle)
                .add();

        channel.messageBuilder(GetBaseDataListsMessage.class, packetId++)
                .decoder(GetBaseDataListsMessage::decode)
                .encoder(GetBaseDataListsMessage::encode)
                .consumer(GetBaseDataListsMessage::handle)
                .add();

        channel.messageBuilder(SendResultMessage.class, packetId++)
                .decoder(SendResultMessage::decode)
                .encoder(SendResultMessage::encode)
                .consumer(SendResultMessage::handle)
                .add();

        channel.messageBuilder(SendCSDChatMessage.class, packetId++)
                .decoder(SendCSDChatMessage::decode)
                .encoder(SendCSDChatMessage::encode)
                .consumer(SendCSDChatMessage::handle)
                .add();

        channel.messageBuilder(AudiblePingPlayerMessage.class, packetId++)
                .decoder(AudiblePingPlayerMessage::decode)
                .encoder(AudiblePingPlayerMessage::encode)
                .consumer(AudiblePingPlayerMessage::handle)
                .add();

        channel.messageBuilder(JoinGroupMessage.class, packetId++)
                .decoder(JoinGroupMessage::decode)
                .encoder(JoinGroupMessage::encode)
                .consumer(JoinGroupMessage::handle)
                .add();

        channel.messageBuilder(SyncGroupMessage.class, packetId++)
                .decoder(SyncGroupMessage::decode)
                .encoder(SyncGroupMessage::encode)
                .consumer(SyncGroupMessage::handle)
                .add();
    }

    private PacketDispatcher() { /* NOP */ }

    // ========================================================//
    // The following methods are the 'wrapper' methods; again,
    // this just makes sending a message slightly more compact
    // and is purely a matter of stylistic preference
    // ========================================================//

    /**
     * Send this message to the specified player's client-side counterpart. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static void sendTo(IMessage message, ServerPlayerEntity player)
    {
        PacketDispatcher.modChannel.send(PacketDistributor.PLAYER.with(()->player), message);
    }

    /**
     * Send this message to everyone. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static void sendToAll(IMessage message)
    {
        PacketDispatcher.modChannel.send(PacketDistributor.ALL.with(null), message);
    }

    /**
     * Send this message to everyone within a certain range of a point. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    private static void sendToAllAround(IMessage message, PacketDistributor.TargetPoint point)
    {
        PacketDispatcher.modChannel.send(PacketDistributor.NEAR.with(()->point), message);
    }

    /**
     * Sends a message to everyone within a certain range of the coordinates in
     * the same dimension. Shortcut to
     * {@link PacketDispatcher#sendToAllAround(IMessage, PacketDistributor.TargetPoint)}
     */
    public static void sendToAllAround(IMessage message, Dimension dimension, double x, double y, double z, double range)
    {
        PacketDispatcher.sendToAllAround(message, new PacketDistributor.TargetPoint(x, y, z, range, dimension.getType()));
    }

    /**
     * Sends a message to everyone within a certain range of the player
     * provided. Shortcut to
     * {@link PacketDispatcher#sendToAllAround(IMessage, Dimension, double, double, double, double)}
     */
    public static void sendToAllAround(IMessage message, PlayerEntity player, double range)
    {
        PacketDispatcher.sendToAllAround(message, player.getEntityWorld().getDimension(), player.posX, player.posY, player.posZ, range);
    }

    /**
     * Send this message to everyone within the supplied dimension. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static void sendToDimension(IMessage message, Dimension dimension)
    {
        PacketDispatcher.modChannel.send(PacketDistributor.DIMENSION.with(dimension::getType), message);
    }

    /**
     * Send this message to the server. See
     * {@link SimpleChannel#sendToServer(Object)}
     */
    public static void sendToServer(IMessage message)
    {
        PacketDispatcher.modChannel.sendToServer(message);
    }
}
