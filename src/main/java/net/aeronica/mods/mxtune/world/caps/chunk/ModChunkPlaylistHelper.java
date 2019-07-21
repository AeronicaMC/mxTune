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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import java.util.function.Consumer;

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
        Consumer<IModChunkPlaylist> chunkData = null;
        chunk.getCapability(MOD_CHUNK_DATA)
                .ifPresent(chunkData.accept(MOD_CHUNK_DATA.getDefaultInstance());
//        if (chunk.getCapability(MOD_CHUNK_DATA).isPresent())
//            chunk.getCapability(MOD_CHUNK_DATA).;
//        else
//            throw new MXTuneException("IModChunkData capability is null");
        return chunkData;
    }

    public static void sync(PlayerEntity entityPlayer, Chunk chunk)
    {
        if (MXTune.proxy.getEffectiveSide() == Dist.DEDICATED_SERVER)
        {
            PacketDispatcher.sendToDimension(new UpdateChunkMusicData(chunk.getPos().x, chunk.getPos().z, getPlaylistGuid(chunk)), entityPlayer.getEntityWorld().provider.getDimension());
        }
    }
}
