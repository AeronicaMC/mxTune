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

package net.aeronica.mods.mxtune.util;

import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class NBTHelper
{
    private static final String TAG_UUID_MSB = "uuid_msb";
    private static final String TAG_UUID_LSB = "uuid_lsb";

    private NBTHelper() { /* NOP */ }


    public static UUID getUuidFromCompound(NBTTagCompound compound)
    {
        long msb = compound.getLong(TAG_UUID_MSB);
        long lsb = compound.getLong(TAG_UUID_LSB);
        return new UUID(msb, lsb);
    }

    public static void setUuidToCompound(NBTTagCompound compound, UUID uuid)
    {
        compound.setLong(TAG_UUID_MSB, uuid.getMostSignificantBits());
        compound.setLong(TAG_UUID_LSB, uuid.getLeastSignificantBits());
    }

    public static UUID getUuidFromTag(NBTTagCompound compound, String tagKey)
    {
        NBTTagCompound compoundTag = compound.getCompoundTag(tagKey);
        return getUuidFromCompound(compoundTag);
    }

    public static void setUuidToTag(UUID uuid, NBTTagCompound compound, String tagKey)
    {
        NBTTagCompound tagCompound = new NBTTagCompound();
        setUuidToCompound(tagCompound, uuid);
        compound.setTag(tagKey, tagCompound);
    }
}
