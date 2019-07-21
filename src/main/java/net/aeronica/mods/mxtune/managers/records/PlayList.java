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
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.List;

public class PlayList extends BaseData
{
    private static final String TAG_NAME = "name";
    private static final String TAG_TITLE = "title";
    private static final String TAG_PLAY_LIST_DAY = "play_list_day";
    private static final String TAG_PLAY_LIST_NIGHT = "play_list_night";
    private static final String TAG_SONG_PREFIX = "song";
    private static final String TAG_SONG_COUNT = "song_count";

    private String name;
    private String title;
    private List<SongProxy> playListDay;
    private List<SongProxy> playListNight;

    public PlayList()
    {
        super();
        this.name = "";
        this.title = "";
        playListDay = new ArrayList<>();
        playListNight = new ArrayList<>();
    }

    public PlayList(String name)
    {
        this.name = name.trim();
        this.title = "";
        playListDay = new ArrayList<>();
        playListNight = new ArrayList<>();
        guid = GUID.stringToSHA2Hash(this.name);
    }

    public PlayList(String name, List<SongProxy> playListDay, List<SongProxy> playListNight)
    {
        this.name = name.trim();
        this.title = "";
        this.playListDay = new ArrayList<>();
        this.playListDay.addAll(playListDay);
        this.playListNight = new ArrayList<>();
        this.playListNight.addAll(playListNight);
        guid = GUID.stringToSHA2Hash(this.name);
    }

    public static PlayList build(CompoundNBT compound)
    {
        PlayList playList = new PlayList();
        playList.readFromNBT(compound);
        return playList;
    }

    public static PlayList emptyPlaylist()
    {
        PlayList playList = new PlayList();
        playList.name = "[ empty playlist ]";
        playList.guid = new GUID(0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff);
        return playList;
    }

    public static PlayList undefinedPlaylist()
    {
        PlayList playList = new PlayList();
        playList.name = " [ undefined playlist ] ";
        playList.guid = new GUID(0L, 0L, 0L, 0L);
        return playList;
    }

    @Override
    public void readFromNBT(CompoundNBT compound)
    {
        super.readFromNBT(compound);
        name = compound.getString(TAG_NAME);
        title = compound.getString(TAG_TITLE);

        CompoundNBT compoundPlayListDay = compound.getCompound(TAG_PLAY_LIST_DAY);
        int songCount = compoundPlayListDay.getInt(TAG_SONG_COUNT);

        playListDay = new ArrayList<>();
        for(int i = 0; i < songCount; i++)
        {
            CompoundNBT compoundSong = compoundPlayListDay.getCompound(TAG_SONG_PREFIX + i);
            SongProxy songProxy = new SongProxy(compoundSong);
            playListDay.add(songProxy);
        }

        CompoundNBT compoundPlayListNight = compound.getCompound(TAG_PLAY_LIST_NIGHT);
        songCount = compoundPlayListNight.getInt(TAG_SONG_COUNT);

        playListNight = new ArrayList<>();
        for(int i = 0; i < songCount; i++)
        {
            CompoundNBT compoundSong = compoundPlayListNight.getCompound(TAG_SONG_PREFIX + i);
            SongProxy songProxy = new SongProxy(compoundSong);
            playListNight.add(songProxy);
        }
    }

    @Override
    public void writeToNBT(CompoundNBT compound)
    {
        super.writeToNBT(compound);
        compound.putString(TAG_NAME, name);
        compound.putString(TAG_TITLE, title);

        CompoundNBT compoundPlayListDay = new CompoundNBT();
        int i = 0;
        for (SongProxy songProxy : playListDay)
        {
            if (songProxy != null)
            {
                CompoundNBT compoundSong = new CompoundNBT();
                songProxy.writeToNBT(compoundSong);
                compoundPlayListDay.put(TAG_SONG_PREFIX + i, compoundSong);
                i++;
            }
        }
        compound.put(TAG_PLAY_LIST_DAY, compoundPlayListDay);
        compoundPlayListDay.putInt(TAG_SONG_COUNT, i);

        CompoundNBT compoundPlayListNight = new CompoundNBT();
        i = 0;
        for (SongProxy songProxy : playListNight)
        {
            if (songProxy != null)
            {
                CompoundNBT compoundSong = new CompoundNBT();
                songProxy.writeToNBT(compoundSong);
                compoundPlayListNight.put(TAG_SONG_PREFIX + i, compoundSong);
                i++;
            }
        }
        compound.put(TAG_PLAY_LIST_NIGHT, compoundPlayListNight);
        compoundPlayListNight.putInt(TAG_SONG_COUNT, i);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name.trim();
        this.guid = GUID.stringToSHA2Hash(this.name);
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title.trim();
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
}
