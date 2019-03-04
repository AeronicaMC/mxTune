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

import java.util.UUID;

public class Song
{
    private static final String TAG_TITLE = "title";
    private static final String TAG_MML = "mml";
    private static final String TAG_UUID_MSB = "uuid_msb";
    private static final String TAG_UUID_LSB = "uuid_lsb";
    private static final String NULL_TITLE = "--- null title ---";
    private static final String NULL_MML = "@MML;";

    private final UUID uuid;
    private final String title;
    private final String mml;

    /**
     * NULL song
     */
    public Song()
    {
        title = NULL_TITLE;
        mml = NULL_MML;
        this.uuid = new UUID(0L, 0L);
    }

    public Song(String title, String mml)
    {
        this.title = title != null ? title : NULL_TITLE;
        this.mml = mml != null ? mml : NULL_MML;
        this.uuid = UUIDType5.nameUUIDFromNamespaceAndString(UUIDType5.NAMESPACE_SONG, this.mml);
    }

    public Song(NBTTagCompound compound)
    {
        title = compound.getString(TAG_TITLE);
        mml = compound.getString(TAG_MML);
        long msb = compound.getLong(TAG_UUID_MSB);
        long lsb = compound.getLong(TAG_UUID_LSB);
        uuid = new UUID(msb, lsb);
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString(TAG_TITLE, title);
        compound.setString(TAG_MML, mml);
        compound.setLong(TAG_UUID_MSB, uuid.getMostSignificantBits());
        compound.setLong(TAG_UUID_LSB, uuid.getLeastSignificantBits());
    }

    public String getTitle()
    {
        return title;
    }

    public String getMml()
    {
        return mml;
    }

    public UUID getUUID()
    {
        return uuid;
    }

    public String getFileName()
    {
        return uuid.toString() + ".dat";
    }
}
