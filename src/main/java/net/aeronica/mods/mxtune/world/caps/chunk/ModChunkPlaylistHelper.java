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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.UpdateChunkMusicData;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;

public class ModChunkPlaylistHelper
{
    @CapabilityInject(IModChunkPlaylist.class)
    public static final Capability<IModChunkPlaylist> MOD_CHUNK_DATA = Miscellus.nonNullInjected();

    private ModChunkPlaylistHelper() { /* NOP */ }

    public static void setPlaylistGuid(final Chunk chunk, final GUID guid)
    {
        getImpl(chunk).ifPresent(IModChunkPlaylist -> {
            IModChunkPlaylist.setPlaylistGuid(guid);
            chunk.markDirty();
        });
    }

    public static GUID getPlaylistGuid(final IChunk chunk)
    {
        return getImpl((Chunk) chunk)
            .map(IModChunkPlaylist::getPlaylistGuid
                ).orElseGet(()-> Reference.EMPTY_GUID);
    }

    public static LazyOptional<IModChunkPlaylist> getImpl(final Chunk chunk)
    {
        return chunk.getCapability(MOD_CHUNK_DATA, null);
    }

    public static void sync(PlayerEntity entityPlayer, Chunk chunk)
    {
        if (!chunk.getWorld().isRemote)
        {
            PacketDispatcher.sendToDimension(new UpdateChunkMusicData(chunk.getPos().x, chunk.getPos().z, getPlaylistGuid(chunk)), entityPlayer.getEntityWorld().getDimension());
        }
    }
}
