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
import java.util.List;

public class Area extends BaseData
{
    private static final  String TAG_NAME = "name";
    private static final String TAG_PLAY_LIST_DAY = "play_list_day";
    private static final String TAG_PLAY_LIST_NIGHT = "play_list_night";
    private static final String TAG_SONG_PREFIX = "song";
    private static final String TAG_SONG_COUNT = "song_count";

    private String name;
    private List<SongProxy> playListDay;
    private List<SongProxy> playListNight;

    public Area()
    {
        this.name = "";
        playListDay = new ArrayList<>();
        playListNight = new ArrayList<>();
        uuid = UUIDType5.nameUUIDFromNamespaceAndString(UUIDType5.NAMESPACE_AREA, this.name);
    }

    public Area(String name)
    {
        this.name = name;
        playListDay = new ArrayList<>();
        playListNight = new ArrayList<>();
        uuid = UUIDType5.nameUUIDFromNamespaceAndString(UUIDType5.NAMESPACE_AREA, this.name);
    }

    public Area(String name, List<SongProxy> playListDay, List<SongProxy> playListNight)
    {
        this.name = name;
        this.playListDay = new ArrayList<>();
        this.playListDay.addAll(playListDay);
        this.playListNight = new ArrayList<>();
        this.playListNight.addAll(playListNight);
        uuid = UUIDType5.nameUUIDFromNamespaceAndString(UUIDType5.NAMESPACE_AREA, this.name);
    }

    public static Area build(NBTTagCompound compound)
    {
        Area area = new Area();
        area.readFromNBT(compound);
        return area;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        name = compound.getString(TAG_NAME);

        NBTTagCompound compoundPlayListDay = compound.getCompoundTag(TAG_PLAY_LIST_DAY);
        int songCount = compoundPlayListDay.getInteger(TAG_SONG_COUNT);

        playListDay = new ArrayList<>();
        for(int i = 0; i < songCount; i++)
        {
            NBTTagCompound compoundSong = compoundPlayListDay.getCompoundTag(TAG_SONG_PREFIX + i);
            SongProxy songProxy = new SongProxy(compoundSong);
            playListDay.add(songProxy);
        }

        NBTTagCompound compoundPlayListNight = compound.getCompoundTag(TAG_PLAY_LIST_NIGHT);
        songCount = compoundPlayListNight.getInteger(TAG_SONG_COUNT);

        playListNight = new ArrayList<>();
        for(int i = 0; i < songCount; i++)
        {
            NBTTagCompound compoundSong = compoundPlayListNight.getCompoundTag(TAG_SONG_PREFIX + i);
            SongProxy songProxy = new SongProxy(compoundSong);
            playListNight.add(songProxy);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString(TAG_NAME, name);

        NBTTagCompound compoundPlayListDay = new NBTTagCompound();
        compoundPlayListDay.setInteger(TAG_SONG_COUNT, playListDay.size());
        int i = 0;
        for (SongProxy songProxy : playListDay)
        {
            NBTTagCompound compoundSong = new NBTTagCompound();
            songProxy.writeToNBT(compoundSong);
            compoundPlayListDay.setTag(TAG_SONG_PREFIX + i, compoundSong);
            i++;
        }
        compound.setTag(TAG_PLAY_LIST_DAY, compoundPlayListDay);

        NBTTagCompound compoundPlayListNight = new NBTTagCompound();
        compoundPlayListNight.setInteger(TAG_SONG_COUNT, playListNight.size());
        i = 0;
        for (SongProxy songProxy : playListNight)
        {
            NBTTagCompound compoundSong = new NBTTagCompound();
            songProxy.writeToNBT(compoundSong);
            compoundPlayListNight.setTag(TAG_SONG_PREFIX + i, compoundSong);
            i++;
        }
        compound.setTag(TAG_PLAY_LIST_NIGHT, compoundPlayListNight);
    }

    public String getName()
    {
        return name;
    }

    public List<SongProxy> getPlayListDay()
    {
        return playListDay;
    }

    public void setPlayListDay(List<SongProxy> playListDay)
    {
        this.playListDay = playListDay;
    }

    public List<SongProxy> getPlayListNight()
    {
        return playListNight;
    }

    public void setPlayListNight(List<SongProxy> playListNight)
    {
        this.playListNight = playListNight;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseData> T factory()
    {
        return (T) new Area();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Area && this.uuid.equals(((Area) obj).getUUID());
    }
}
