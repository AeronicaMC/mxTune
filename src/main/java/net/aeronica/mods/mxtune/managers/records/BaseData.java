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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.NBTHelper;
import net.minecraft.nbt.CompoundNBT;

import java.io.Serializable;
import java.util.Objects;

public abstract class BaseData implements Serializable, Comparable<GUID>
{
    private static final long serialVersionUID = -76044260522231311L;
    protected GUID guid;

    public BaseData()
    {
        guid = Reference.EMPTY_GUID;
    }

    public void readFromNBT(CompoundNBT compound)
    {
        guid = NBTHelper.getGuidFromCompound(compound);
    }

    public void writeToNBT(CompoundNBT compound)
    {
        NBTHelper.setGuidToCompound(compound, guid);
    }

    public GUID getGUID()
    {
        return guid;
    }

    public String getFileName()
    {
        return guid.toString() + FileHelper.EXTENSION_DAT;
    }

    public abstract <T extends BaseData> T factory();

    @Override
    public int hashCode()
    {
        return this.guid.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseData baseData = (BaseData) o;
        return Objects.equals(guid, baseData.guid);
    }

    @Override
    public int compareTo(GUID o)
    {
        return guid.compareTo(o);
    }
}
