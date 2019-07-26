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

package net.aeronica.mods.mxtune.caps.world;

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.UpdateWorldMusicData;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import static net.aeronica.mods.mxtune.caps.world.ModWorldPlaylistCap.getWorldCap;

public class ModWorldPlaylistHelper
{
    @CapabilityInject(IModWorldPlaylist.class)
    public static final Capability<IModWorldPlaylist> MOD_WORLD_DATA = Miscellus.nonNullInjected();

    private ModWorldPlaylistHelper() { /* NOP */ }

    public static void setPlaylistGuid(World world, GUID guid)
    {
        getWorldCap(world).ifPresent(playlist->playlist.setPlaylistGuid(guid));
    }

    public static GUID getPlaylistGuid(World world)
    {
        return getWorldCap(world).orElse(null).getPlaylistGuid();
    }

    public static void sync(PlayerEntity entityPlayer, World world)
    {
        PacketDispatcher.sendToDimension(new UpdateWorldMusicData(getPlaylistGuid(world)), entityPlayer.getEntityWorld().getDimension());
    }
}
