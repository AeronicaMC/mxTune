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

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.ResetClientPlayEngine;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.world.caps.chunk.ModChunkPlaylistHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;

public class ChunkToolMessage extends AbstractServerMessage<ChunkToolMessage>
{
    private Operation op;

    @SuppressWarnings("unused")
    public ChunkToolMessage() {/* Required by the PacketDispatcher */}

    public ChunkToolMessage(Operation op)
    {
        this.op = op;
    }
    
    @Override
    protected void read(PacketBuffer buffer)
    {
        op = buffer.readEnumValue(Operation.class);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeEnumValue(op);
    }

    @Override
    public void process(PlayerEntity player, Side side)
    {
        if (MusicOptionsUtil.isMxTuneServerUpdateAllowed(player))
        {
            World world = player.world;
            Chunk chunk = world.getChunk(player.getPosition());
            if (chunk.isLoaded())
            {
                switch (op)
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
    }

    private void apply(PlayerEntity player)
    {
        int errorCount = 0;

        World world = player.world;
        Chunk chunkStart = MusicOptionsUtil.getChunkStart(player);
        Chunk chunkEnd = MusicOptionsUtil.getChunkEnd(player);
        if (chunkStart != null && chunkEnd != null && world != null && world.equals(chunkStart.getWorld()) && world.equals(chunkEnd.getWorld()))
        {
            GUID guidPlaylist = MusicOptionsUtil.getSelectedPlayListGuid(player);
            int totalChunks = (Math.abs(chunkStart.x - chunkEnd.x) + 1) * (Math.abs(chunkStart.z - chunkEnd.z) + 1);
            ModLogger.debug("ChunkToolMessage: Total Chunks: %d", totalChunks);
            int minX = Math.min(chunkStart.x, chunkEnd.x);
            int maxX = Math.max(chunkStart.x, chunkEnd.x);
            int minZ = Math.min(chunkStart.z, chunkEnd.z);
            int maxZ = Math.max(chunkStart.z, chunkEnd.z);
            ModLogger.debug("ChunkToolMessage: x: %d to %d", minX, maxX);
            ModLogger.debug("ChunkToolMessage: z: %d to %d", minZ, maxZ);
            for(int x = minX; x <= maxX; x++)
            {
                for(int z = minZ; z <= maxZ; z++)
                {
                    if (world.isChunkGeneratedAt(x, z))
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
