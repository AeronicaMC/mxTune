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
package net.aeronica.mods.mxtune.network.server;

import net.aeronica.mods.mxtune.caps.chunk.ModChunkPlaylistHelper;
import net.aeronica.mods.mxtune.caps.player.MusicOptionsUtil;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.ResetClientPlayEngine;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ChunkToolMessage
{
    private final Operation op;

    public ChunkToolMessage(Operation op)
    {
        this.op = op;
    }

    public static ChunkToolMessage decode(final PacketBuffer buffer)
    {
        final Operation op = buffer.readEnumValue(Operation.class);
        return new ChunkToolMessage(op);
    }

    public static void encode(ChunkToolMessage message, PacketBuffer buffer)
    {
        buffer.writeEnumValue(message.op);
    }

    public static void handle(final ChunkToolMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->
                {
                    if (player != null && MusicOptionsUtil.isMxTuneServerUpdateAllowed(player))
                    {
                        World world = player.world;
                        Chunk chunk = (Chunk) world.getChunk(player.getPosition());
                        if (!chunk.isEmpty())
                        {
                            switch (message.op)
                            {
                                case START:
                                    MusicOptionsUtil.setChunkStart(player, chunk);
                                    MusicOptionsUtil.setChunkToolOperation(player, Operation.END);
                                    break;
                                case END:
                                    MusicOptionsUtil.setChunkEnd(player, chunk);
                                    MusicOptionsUtil.setChunkToolOperation(player, Operation.APPLY);
                                    break;
                                case APPLY:
                                    apply(player);
                                    MusicOptionsUtil.setChunkToolOperation(player, Operation.START);
                                    break;
                                case RESET:
                                    MusicOptionsUtil.setChunkToolOperation(player, Operation.START);
                                    MusicOptionsUtil.setChunkStart(player, null);
                                    MusicOptionsUtil.setChunkEnd(player, null);
                                default:
                            }
                        }
                    }
                });
        ctx.get().setPacketHandled(true);
    }

    private static void apply(PlayerEntity player)
    {
        int errorCount = 0;

        World world = player.world;
        Chunk chunkStart = MusicOptionsUtil.getChunkStart(player);
        Chunk chunkEnd = MusicOptionsUtil.getChunkEnd(player);
        if (chunkStart != null && chunkEnd != null && world != null && world.equals(chunkStart.getWorld()) && world.equals(chunkEnd.getWorld()))
        {
            GUID guidPlaylist = MusicOptionsUtil.getSelectedPlayListGuid(player);
            int totalChunks = (Math.abs(chunkStart.getPos().x - chunkEnd.getPos().x) + 1) * (Math.abs(chunkStart.getPos().z - chunkEnd.getPos().z) + 1);
            ModLogger.debug("ChunkToolMessage: Total Chunks: %d", totalChunks);
            int minX = Math.min(chunkStart.getPos().x, chunkEnd.getPos().x);
            int maxX = Math.max(chunkStart.getPos().x, chunkEnd.getPos().x);
            int minZ = Math.min(chunkStart.getPos().z, chunkEnd.getPos().z);
            int maxZ = Math.max(chunkStart.getPos().z, chunkEnd.getPos().z);
            ModLogger.debug("ChunkToolMessage: x: %d to %d", minX, maxX);
            ModLogger.debug("ChunkToolMessage: z: %d to %d", minZ, maxZ);
            for(int x = minX; x <= maxX; x++)
            {
                for(int z = minZ; z <= maxZ; z++)
                {
                    if (world.chunkExists(x, z))
                    {
                        Chunk chunk = world.getChunk(x, z);
                        ModChunkPlaylistHelper.setPlaylistGuid(chunk, guidPlaylist);
                        ModChunkPlaylistHelper.sync(player, chunk);
                    } else
                    {
                        errorCount++;
                        ModLogger.debug("  ChunkToolMessage: Not Loaded @ x: %+03d, z: %+03d", x, z);
                    }
                }
            }
            ModLogger.debug("  ChunkToolMessage: Error count: %d", errorCount);
            PacketDispatcher.sendToAll(new ResetClientPlayEngine());
        }
    }

    public enum Operation
    {
        START, END, RESET, APPLY
    }
}
