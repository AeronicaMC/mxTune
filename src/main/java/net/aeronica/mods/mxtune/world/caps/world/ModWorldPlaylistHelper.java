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

package net.aeronica.mods.mxtune.world.caps.world;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.UpdateWorldMusicData;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.MXTuneException;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class ModWorldPlaylistHelper
{
    @CapabilityInject(IModWorldPlaylist.class)
    public static final Capability<IModWorldPlaylist> MOD_WORLD_DATA = Miscellus.nonNullInjected();

    private ModWorldPlaylistHelper() { /* NOP */ }

    public static void setPlaylistGuid(World world, GUID guid)
    {
        try
        {
            getImpl(world).setPlaylistGuid(guid);
        }
        catch (MXTuneException e)
        {
            ModLogger.error(e);
        }
        // mark dirty?
    }

    public static GUID getPlaylistGuid(World world)
    {
        try
        {
            return getImpl(world).getPlaylistGuid();
        }
        catch (MXTuneException e)
        {
            ModLogger.error(e);
        }
        return Reference.EMPTY_GUID;
    }

    private static IModWorldPlaylist getImpl(World world) throws MXTuneException
    {
        IModWorldPlaylist worldData;
        if (world.hasCapability(MOD_WORLD_DATA, null))
            worldData =  world.getCapability(MOD_WORLD_DATA, null);
        else
            throw new MXTuneException("IModWorldData capability is null");
        return worldData;
    }

    public static void sync(EntityPlayer entityPlayer, World world)
    {
        PacketDispatcher.sendToAllAround(new UpdateWorldMusicData(getPlaylistGuid(world)), entityPlayer, 80);
        PacketDispatcher.sendTo(new UpdateWorldMusicData(getPlaylistGuid(world)), (EntityPlayerMP) entityPlayer);
    }
}
