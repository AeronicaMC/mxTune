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
import net.aeronica.mods.mxtune.network.client.ResetClientPlayEngine;
import net.aeronica.mods.mxtune.network.server.BandAmpMessage;
import net.aeronica.mods.mxtune.network.server.ChunkToolMessage;
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
        // To Server
        channel.messageBuilder(BandAmpMessage.class, packetId++)
                .decoder(BandAmpMessage::decode)
                .encoder(BandAmpMessage::encode)
                .consumer(BandAmpMessage::handle)
                .add();

        // To Server
        channel.messageBuilder(ChunkToolMessage.class, packetId++)
                .decoder(ChunkToolMessage::decode)
                .encoder(ChunkToolMessage::encode)
                .consumer(ChunkToolMessage::handle)
                .add();

        // To All Clients
        channel.messageBuilder(ResetClientPlayEngine.class, packetId++)
                .decoder(ResetClientPlayEngine::decode)
                .encoder(ResetClientPlayEngine::encode)
                .consumer(ResetClientPlayEngine::handle)
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
    public static void sendToAllAround(IMessage message, PacketDistributor.TargetPoint point)
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
