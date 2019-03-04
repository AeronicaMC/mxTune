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

import java.util.UUID;

public abstract class BaseData
{
    static final String TAG_UUID_MSB = "uuid_msb";
    static final String TAG_UUID_LSB = "uuid_lsb";
    protected UUID uuid;

    BaseData()
    {
        uuid = new UUID(0L, 0L);
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        long msb = compound.getLong(TAG_UUID_MSB);
        long lsb = compound.getLong(TAG_UUID_LSB);
        uuid = new UUID(msb, lsb);
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setLong(TAG_UUID_MSB, uuid.getMostSignificantBits());
        compound.setLong(TAG_UUID_LSB, uuid.getLeastSignificantBits());
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
