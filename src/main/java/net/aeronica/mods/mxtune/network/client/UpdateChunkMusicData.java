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
import net.aeronica.mods.mxtune.world.chunk.ModChunkDataHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class UpdateChunkMusicData extends AbstractClientMessage<UpdateChunkMusicData>
{
    private int chunkX;
    private int chunkZ;
    private UUID uuid;
    private long uuidMSB;
    private long uuidLSB;

    @SuppressWarnings("unused")
    public UpdateChunkMusicData() {/* Required by the PacketDispatcher */}

    public UpdateChunkMusicData(int chunkX, int chunkZ, UUID uuid)
    {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.uuid = uuid;
        uuidMSB = uuid.getMostSignificantBits();
        uuidLSB = uuid.getLeastSignificantBits();
    }

    @Override
    protected void read(PacketBuffer buffer)
    {
        chunkX = buffer.readInt();
        chunkZ = buffer.readInt();
        uuidMSB = buffer.readLong();
        uuidLSB = buffer.readLong();
        uuid = new UUID(uuidMSB, uuidLSB);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        buffer.writeLong(uuidMSB);
        buffer.writeLong(uuidLSB);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        World world = MXTune.proxy.getClientWorld();
        if (world != null && world.isChunkGeneratedAt(chunkX, chunkZ))
        {
            Chunk chunk = world.getChunk(chunkX, chunkZ);
            if (chunk.hasCapability(ModChunkDataHelper.MOD_CHUNK_DATA, null))
                ModChunkDataHelper.setAreaUuid(chunk, uuid);
        }
    }
}
