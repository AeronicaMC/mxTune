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

import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.NBTHelper;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayList extends BaseData
{
    private static final String TAG_NAME = "name";
    private static final String TAG_SONG_PREFIX = "song";
    private static final String TAG_SONG_COUNT = "song_count";

    private String name;
    private List<GUID> songGUIDs;

    public PlayList()
    {
        super();
        this.name = "";
        this.songGUIDs = new ArrayList<>();
    }

    public PlayList(String name, List<GUID> songGUIDs)
    {
        this.name = name != null ? name : "";
        this.songGUIDs = songGUIDs != null ? songGUIDs : new ArrayList<>();
        guid = GUID.fromString(this.name);
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

        songGUIDs = new ArrayList<>();
        for(int i = 0; i < songCount; i++)
        {
            NBTTagCompound compoundSong = compound.getCompoundTag(TAG_SONG_PREFIX + i);
            GUID guid = NBTHelper.getGuidFromCompound(compoundSong);
            songGUIDs.add(guid);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString(TAG_NAME, name);
        compound.setInteger(TAG_SONG_COUNT, songGUIDs.size());

        int i = 0;
        for (GUID guid : songGUIDs)
        {
            if (guid != null)
            {
                NBTTagCompound compoundSong = new NBTTagCompound();
                NBTHelper.setGuidToCompound(compoundSong, guid);
                compound.setTag(TAG_SONG_PREFIX + i, compoundSong);
                i++;
            }
        }
    }

    public List<GUID> getSongGUIDs()
    {
        return songGUIDs != null ? songGUIDs : Collections.emptyList();
    }

    @SuppressWarnings("unused")
    public void setSongGUIDs(List<GUID> songGUIDs)
    {
        this.songGUIDs = songGUIDs;
    }

    public String getName()
    {
        return name != null ? name : "";
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseData> T factory()
    {
        return (T) new PlayList();
    }

    @Override
    public boolean equals(Object o)
    {
        return super.equals(o);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public int compareTo(GUID o)
    {
        return super.compareTo(o);
    }
}
