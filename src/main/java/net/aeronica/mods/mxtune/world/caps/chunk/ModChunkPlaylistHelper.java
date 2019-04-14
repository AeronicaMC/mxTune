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

package net.aeronica.mods.mxtune.world.caps.chunk;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.UpdateChunkMusicData;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.MXTuneException;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.relauncher.Side;

public class ModChunkPlaylistHelper
{
    @CapabilityInject(IModChunkPlaylist.class)
    public static final Capability<IModChunkPlaylist> MOD_CHUNK_DATA = Miscellus.nonNullInjected();

    private ModChunkPlaylistHelper() { /* NOP */ }

    public static void setPlaylistGuid(Chunk chunk, GUID guid)
    {
        try
        {
            getImpl(chunk).setPlaylistGuid(guid);
        }
        catch (MXTuneException e)
        {
            ModLogger.error(e);
        }
        chunk.markDirty();
    }

    public static GUID getPlaylistGuid(Chunk chunk)
    {
        try
        {
            return getImpl(chunk).getPlaylistGuid();
        }
        catch (MXTuneException e)
        {
            ModLogger.error(e);
        }
        return Reference.EMPTY_GUID;
    }

    private static IModChunkPlaylist getImpl(Chunk chunk) throws MXTuneException
    {
        IModChunkPlaylist chunkData;
        if (chunk.hasCapability(MOD_CHUNK_DATA, null))
            chunkData =  chunk.getCapability(MOD_CHUNK_DATA, null);
        else
            throw new MXTuneException("IModChunkData capability is null");
        return chunkData;
    }

    public static void sync(EntityPlayer entityPlayer, Chunk chunk)
    {
        if (MXTune.proxy.getEffectiveSide() == Side.SERVER)
        {
            PacketDispatcher.sendTo(new UpdateChunkMusicData(chunk.x, chunk.z, getPlaylistGuid(chunk)), (EntityPlayerMP) entityPlayer);
        }
    }
}
