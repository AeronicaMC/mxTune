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

package net.aeronica.mods.mxtune.managers.records;

import net.aeronica.mods.mxtune.caches.UUIDType5;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayList extends BaseData
{
    private static final String TAG_NAME = "name";
    private static final String TAG_SONG_PREFIX = "song";
    private static final String TAG_SONG_COUNT = "song_count";

    private String name;
    private List<UUID> songUUIDs;

    public PlayList()
    {
        super();
        this.name = "";
        this.songUUIDs = new ArrayList<>();
    }

    public PlayList(String name, List<UUID> songUUIDs)
    {
        this.name = name != null ? name : "";
        this.songUUIDs = songUUIDs != null ? songUUIDs : new ArrayList<>();
        uuid = UUIDType5.nameUUIDFromNamespaceAndString(UUIDType5.NAMESPACE_LIST, this.name);
    }

    public PlayList(NBTTagCompound compound)
    {
        this.readFromNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        name = compound.getString(TAG_NAME);
        int songCount = compound.getInteger(TAG_SONG_COUNT);


        songUUIDs = new ArrayList<>();
        for(int i = 0; i < songCount; i++)
        {
            NBTTagCompound compoundSong = compound.getCompoundTag(TAG_SONG_PREFIX + i);
            long msb = compoundSong.getLong(TAG_UUID_MSB);
            long lsb = compoundSong.getLong(TAG_UUID_LSB);
            UUID uuid = new UUID(msb, lsb);
            songUUIDs.add(uuid);
        }
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString(TAG_NAME, name);
        compound.setInteger(TAG_SONG_COUNT, songUUIDs.size());

        int i = 0;
        for (UUID uuid : songUUIDs)
        {
            NBTTagCompound compoundSong = new NBTTagCompound();
            compoundSong.setLong(TAG_UUID_MSB, uuid.getMostSignificantBits());
            compoundSong.setLong(TAG_UUID_LSB, uuid.getLeastSignificantBits());
            compound.setTag(TAG_SONG_PREFIX + i, compoundSong);
            i++;
        }
    }

    public List<UUID> getSongUUIDs()
    {
        return songUUIDs != null ? songUUIDs : Collections.emptyList();
    }

    @SuppressWarnings("unused")
    public void setSongUUIDs(List<UUID> songUUIDs)
    {
        this.songUUIDs = songUUIDs;
    }

    public String getName()
    {
        return name != null ? name : "";
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
