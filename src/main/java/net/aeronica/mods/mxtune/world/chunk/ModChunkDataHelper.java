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

package net.aeronica.mods.mxtune.world.chunk;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.UpdateChunkMusicData;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.MXTuneException;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class ModChunkDataHelper
{
    @CapabilityInject(IModChunkData.class)
    public static final Capability<IModChunkData> MOD_CHUNK_DATA = Util.nonNullInjected();

    private ModChunkDataHelper() { /* NOP */ }

    public static void setAreaGuid(Chunk chunk, GUID guid)
    {
        try
        {
            getImpl(chunk).setAreaGuid(guid);
        }
        catch (MXTuneException e)
        {
            ModLogger.error(e);
        }
        chunk.markDirty();
    }

    public static GUID getAreaGuid(Chunk chunk)
    {
        try
        {
            return getImpl(chunk).getAreaGuid();
        }
        catch (MXTuneException e)
        {
            ModLogger.error(e);
        }
        return Reference.EMPTY_GUID;
    }

    private static IModChunkData getImpl(Chunk chunk) throws MXTuneException
    {
        IModChunkData chunkData;
        if (chunk.hasCapability(MOD_CHUNK_DATA, null))
            chunkData =  chunk.getCapability(MOD_CHUNK_DATA, null);
        else
            throw new MXTuneException("IModChunkData capability is null");
        return chunkData;
    }

    public static void sync(EntityPlayer entityPlayer, Chunk chunk)
    {
        PacketDispatcher.sendToAllAround(new UpdateChunkMusicData(chunk.x, chunk.z, getAreaGuid(chunk)), entityPlayer, 80);
        PacketDispatcher.sendTo(new UpdateChunkMusicData(chunk.x, chunk.z, getAreaGuid(chunk)), (EntityPlayerMP) entityPlayer);
    }
}
