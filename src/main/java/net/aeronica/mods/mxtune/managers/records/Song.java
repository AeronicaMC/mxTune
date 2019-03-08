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
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.aeronica.mods.mxtune.util.ValidDuration;
import net.minecraft.nbt.NBTTagCompound;

public class Song extends BaseData
{
    private static final String TAG_TITLE = "title";
    private static final String TAG_MML = "mml";
    private static final String TAG_DURATION = "duration";
    private static final String NULL_TITLE = "--- null title ---";
    private static final String NULL_MML = "@MML;";

    private String title;
    private String mml;
    private int duration;

    public Song()
    {
        super();
        title = NULL_TITLE;
        mml = NULL_MML;
        duration = 0;
    }

    public Song(String title, String mml)
    {
        this.title = title != null ? title : NULL_TITLE;
        this.mml = mml != null ? mml : NULL_MML;
        ValidDuration validDuration = SheetMusicUtil.validateMML(this. mml);
        this.duration = validDuration.getDuration();
        uuid = UUIDType5.nameUUIDFromNamespaceAndString(UUIDType5.NAMESPACE_SONG, applyServerID(this.title));
    }

    public Song(NBTTagCompound compound)
    {
        this.readFromNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        title = compound.getString(TAG_TITLE);
        mml = compound.getString(TAG_MML);
        duration = compound.getInteger(TAG_DURATION);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString(TAG_TITLE, title);
        compound.setString(TAG_MML, mml);
        compound.setInteger(TAG_DURATION, duration);
    }

    public String getTitle()
    {
        return title;
    }

    public String getMml()
    {
        return mml;
    }

    public int getDuration()
    {
        return duration;
    }

    @Override
    public <T extends BaseData> T factory()
    {
        return (T) new Song();
    }
}
