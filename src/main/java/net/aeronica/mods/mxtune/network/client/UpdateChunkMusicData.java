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
package net.aeronica.mods.mxtune.network.client;

import net.aeronica.mods.mxtune.caps.chunk.ModChunkPlaylistHelper;
import net.aeronica.mods.mxtune.util.GUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateChunkMusicData
{
    private final int chunkX;
    private final int chunkZ;
    private final GUID guid;
    private final long ddddSigBits;
    private final long ccccSigBits;
    private final long bbbbSigBits;
    private final long aaaaSigBits;

    public UpdateChunkMusicData(final int chunkX, final int chunkZ, final GUID guid)
    {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.guid = guid;
        ddddSigBits = guid.getDdddSignificantBits();
        ccccSigBits = guid.getCcccSignificantBits();
        bbbbSigBits = guid.getBbbbSignificantBits();
        aaaaSigBits = guid.getAaaaSignificantBits();
    }

    public static UpdateChunkMusicData decode(final PacketBuffer buffer)
    {
        int chunkX = buffer.readInt();
        int chunkZ = buffer.readInt();
        long ddddSigBits = buffer.readLong();
        long ccccSigBits = buffer.readLong();
        long bbbbSigBits = buffer.readLong();
        long aaaaSigBits = buffer.readLong();
        GUID guid = new GUID(ddddSigBits, ccccSigBits, bbbbSigBits, aaaaSigBits);
        return new UpdateChunkMusicData(chunkX, chunkZ, guid);
    }

    public static void encode(final UpdateChunkMusicData message, final PacketBuffer buffer)
    {
        buffer.writeInt(message.chunkX);
        buffer.writeInt(message.chunkZ);
        buffer.writeLong(message.ddddSigBits);
        buffer.writeLong(message.ccccSigBits);
        buffer.writeLong(message.bbbbSigBits);
        buffer.writeLong(message.aaaaSigBits);
    }

    public static void handle(final UpdateChunkMusicData message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(() ->
                {
                    ServerPlayerEntity player = ctx.get().getSender();
                    World world = player != null ? player.getEntityWorld() : null;
                    if (world != null && world.chunkExists(message.chunkX, message.chunkZ))
                    {
                        Chunk chunk = world.getChunk(message.chunkX, message.chunkZ);
                        ModChunkPlaylistHelper.setPlaylistGuid(chunk, message.guid);
                    }
                });
        ctx.get().setPacketHandled(true);
    }
}
