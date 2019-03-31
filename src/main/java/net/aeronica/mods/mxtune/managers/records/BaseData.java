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

import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.util.NBTHelper;
import net.minecraft.nbt.NBTTagCompound;

import java.io.Serializable;
import java.util.UUID;

public abstract class BaseData implements Serializable, Comparable<UUID>
{
    private static final long serialVersionUID = -76044260522231311L;
    protected UUID uuid;

    BaseData()
    {
        uuid = new UUID(0L, 0L);
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        uuid = NBTHelper.getUuidFromCompound(compound);
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        NBTHelper.setUuidToCompound(compound, uuid);
    }

    public UUID getUUID()
    {
        return uuid;
    }

    public String getFileName()
    {
        return uuid.toString() + FileHelper.EXTENSION_DAT;
    }

    public abstract <T extends BaseData> T factory();

    @Override
    public int hashCode()
    {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof BaseData && super.equals(obj);
    }

    @Override
    public int compareTo(UUID uuid)
    {
        return this.uuid.compareTo(uuid);
    }
}
