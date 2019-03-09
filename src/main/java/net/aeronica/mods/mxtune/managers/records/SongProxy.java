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

import net.minecraft.nbt.NBTTagCompound;

/**
 * A no MML version of the Song data class for use on the client to restrict memory usage.
 * MML must be loaded from disk cache on the client.
 */
public class SongProxy extends BaseData
{
    private static final String TAG_TITLE = "title";
    private static final String TAG_DURATION = "duration";
    private static final String NULL_TITLE = "--- null title ---";

    private String title;
    private int duration;

    public SongProxy()
    {
        super();
        title = NULL_TITLE;
        duration = 0;
    }

    public SongProxy(NBTTagCompound compound)
    {
        this.readFromNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        title = compound.getString(TAG_TITLE);
        duration = compound.getInteger(TAG_DURATION);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString(TAG_TITLE, title);
        compound.setInteger(TAG_DURATION, duration);
    }

    public String getTitle()
    {
        return title;
    }

    public int getDuration()
    {
        return duration;
    }

    @Override
    public <T extends BaseData> T factory()
    {
        return (T) new SongProxy();
    }
}
