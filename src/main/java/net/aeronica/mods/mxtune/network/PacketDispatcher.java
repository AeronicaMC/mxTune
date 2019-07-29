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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.Dimension;
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
    private static SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
            .clientAcceptedVersions(version -> true)
            .serverAcceptedVersions(version -> true)
            .networkProtocolVersion(() -> NETWORK_VERSION)
            .simpleChannel();;

    public static void register()
    {
        /*
         * Send to Server
         */
        registerMessage(BandAmpMessage.class, BandAmpMessage::encode, BandAmpMessage::decode, BandAmpMessage::handle);
        registerMessage(ChunkToolMessage.class, ChunkToolMessage::encode, ChunkToolMessage::decode, ChunkToolMessage::handle);
        registerMessage(PlayerSelectedPlayListMessage.class, PlayerSelectedPlayListMessage::encode, PlayerSelectedPlayListMessage::decode, PlayerSelectedPlayListMessage::handle);
        registerMessage(ByteArrayPartMessage.class, ByteArrayPartMessage::encode, ByteArrayPartMessage::decode, ByteArrayPartMessage::handle);
        registerMessage(HudOptionsMessage.class, HudOptionsMessage::encode, HudOptionsMessage::decode, HudOptionsMessage::handle);
        registerMessage(ManageGroupMessage.class, ManageGroupMessage::encode, ManageGroupMessage::decode, ManageGroupMessage::handle);
        registerMessage(MusicOptionsMessage.class, MusicOptionsMessage::encode, MusicOptionsMessage::decode, MusicOptionsMessage::handle);
        registerMessage(MusicTextMessage.class, MusicTextMessage::encode, MusicTextMessage::decode, MusicTextMessage::handle);
        registerMessage(SetServerSerializedDataMessage.class, SetServerSerializedDataMessage::encode, SetServerSerializedDataMessage::decode, SetServerSerializedDataMessage::handle);

        /*
         * Send to Client(s)
         */
        registerMessage(ResetClientPlayEngine.class, ResetClientPlayEngine::encode, ResetClientPlayEngine::decode, ResetClientPlayEngine::handle);
        registerMessage(StopPlayIDMessage.class, StopPlayIDMessage::encode, StopPlayIDMessage::decode, StopPlayIDMessage::handle);
        registerMessage(PlayJamMessage.class, PlayJamMessage::encode, PlayJamMessage::decode, PlayJamMessage::handle);
        registerMessage(SyncStatusMessage.class, SyncStatusMessage::encode, SyncStatusMessage::decode, SyncStatusMessage::handle);
        registerMessage(PlaySoloMessage.class, PlaySoloMessage::encode, PlaySoloMessage::decode, PlaySoloMessage::handle);
        registerMessage(PlayBlockMusicMessage.class, PlayBlockMusicMessage::encode, PlayBlockMusicMessage::decode, PlayBlockMusicMessage::handle);
        registerMessage(UpdateChunkMusicData.class, UpdateChunkMusicData::encode, UpdateChunkMusicData::decode, UpdateChunkMusicData::handle);
        registerMessage(UpdateWorldMusicData.class, UpdateWorldMusicData::encode, UpdateWorldMusicData::decode, UpdateWorldMusicData::handle);

        /*
         * Bi-Directional
         */
        registerMessage(GetServerDataMessage.class, GetServerDataMessage::encode, GetServerDataMessage::decode, GetServerDataMessage::handle);
        registerMessage(ClientStateDataMessage.class, ClientStateDataMessage::encode, ClientStateDataMessage::decode, ClientStateDataMessage::handle);
        registerMessage(GetBaseDataListsMessage.class, GetBaseDataListsMessage::encode, GetBaseDataListsMessage::decode, GetBaseDataListsMessage::handle);
        registerMessage(SendResultMessage.class, SendResultMessage::encode, SendResultMessage::decode, SendResultMessage::handle);
        registerMessage(SendCSDChatMessage.class, SendCSDChatMessage::encode, SendCSDChatMessage::decode, SendCSDChatMessage::handle);
        registerMessage(AudiblePingPlayerMessage.class, AudiblePingPlayerMessage::encode, AudiblePingPlayerMessage::decode, AudiblePingPlayerMessage::handle);
        registerMessage(JoinGroupMessage.class, JoinGroupMessage::encode, JoinGroupMessage::decode, JoinGroupMessage::handle);
        registerMessage(SyncGroupMessage.class, SyncGroupMessage::encode, SyncGroupMessage::decode, SyncGroupMessage::handle);
    }

    private static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler)
    {
        HANDLER.registerMessage(packetId++, messageType, encoder, decoder, handler);
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
    public static <MSG> void sendTo(MSG message, ServerPlayerEntity player)
    {
        PacketDispatcher.HANDLER.send(PacketDistributor.PLAYER.with(()->player), message);
    }

    /**
     * Send this message to everyone. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <MSG> void sendToAll(MSG message)
    {
        PacketDispatcher.HANDLER.send(PacketDistributor.ALL.with(null), message);
    }

    /**
     * Send this message to everyone within a certain range of a point. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    private static <MSG> void sendToAllAround(MSG message, PacketDistributor.TargetPoint point)
    {
        PacketDispatcher.HANDLER.send(PacketDistributor.NEAR.with(()->point), message);
    }

    /**
     * Sends a message to everyone within a certain range of the coordinates in
     * the same dimension. Shortcut to
     * {@link PacketDispatcher#sendToAllAround(MSG, PacketDistributor.TargetPoint)}
     */
    public static <MSG> void sendToAllAround(MSG message, Dimension dimension, double x, double y, double z, double range)
    {
        PacketDispatcher.sendToAllAround(message, new PacketDistributor.TargetPoint(x, y, z, range, dimension.getType()));
    }

    /**
     * Sends a message to everyone within a certain range of the player
     * provided. Shortcut to
     * {@link PacketDispatcher#sendToAllAround(MSG, Dimension, double, double, double, double)}
     */
    public static <MSG> void sendToAllAround(MSG message, PlayerEntity player, double range)
    {
        PacketDispatcher.sendToAllAround(message, player.getEntityWorld().getDimension(), player.posX, player.posY, player.posZ, range);
    }

    /**
     * Send this message to everyone within the supplied dimension. See
     * {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)}
     */
    public static <MSG> void sendToDimension(MSG message, Dimension dimension)
    {
        PacketDispatcher.HANDLER.send(PacketDistributor.DIMENSION.with(dimension::getType), message);
    }

    /**
     * Send this message to the server. See
     * {@link SimpleChannel#sendToServer(Object)}
     */
    public static <MSG> void sendToServer(MSG message)
    {
        PacketDispatcher.HANDLER.sendToServer(message);
    }
}
