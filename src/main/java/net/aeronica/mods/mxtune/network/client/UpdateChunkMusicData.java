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

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.world.caps.chunk.ModChunkPlaylistHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;

public class UpdateChunkMusicData extends AbstractClientMessage<UpdateChunkMusicData>
{
    private int chunkX;
    private int chunkZ;
    private GUID guid;
    private long ddddSigBits;
    private long ccccSigBits;
    private long bbbbSigBits;
    private long aaaaSigBits;

    @SuppressWarnings("unused")
    public UpdateChunkMusicData() {/* Required by the PacketDispatcher */}

    public UpdateChunkMusicData(int chunkX, int chunkZ, GUID guid)
    {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.guid = guid;
        ddddSigBits = guid.getDdddSignificantBits();
        ccccSigBits = guid.getCcccSignificantBits();
        bbbbSigBits = guid.getBbbbSignificantBits();
        aaaaSigBits = guid.getAaaaSignificantBits();
    }

    @Override
    protected void read(PacketBuffer buffer)
    {
        chunkX = buffer.readInt();
        chunkZ = buffer.readInt();
        ddddSigBits = buffer.readLong();
        ccccSigBits = buffer.readLong();
        bbbbSigBits = buffer.readLong();
        aaaaSigBits = buffer.readLong();
        guid = new GUID(ddddSigBits, ccccSigBits, bbbbSigBits, aaaaSigBits);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        buffer.writeLong(ddddSigBits);
        buffer.writeLong(ccccSigBits);
        buffer.writeLong(bbbbSigBits);
        buffer.writeLong(aaaaSigBits);
    }

    @Override
    public void process(PlayerEntity player, Side side)
    {
        World world = MXTune.proxy.getClientWorld();
        if (world != null && world.isChunkGeneratedAt(chunkX, chunkZ))
        {
            Chunk chunk = world.getChunk(chunkX, chunkZ);
            if (chunk.hasCapability(ModChunkPlaylistHelper.MOD_CHUNK_DATA, null))
                ModChunkPlaylistHelper.setPlaylistGuid(chunk, guid);
        }
    }
}
