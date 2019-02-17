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

package net.aeronica.mods.mxtune.caches;

import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MXTunePart implements Comparable<MXTunePart>
{
    private static final String TAG_INSTRUMENT = "instrument";
    private static final String TAG_META = "meta";
    private static final String TAG_STAFF_PREFIX = "staff";
    private static final String TAG_STAFF_COUNT = "staffCount";

    private String instrument;
    private String meta = "";
    private List<MXTuneStaff> staves;

    public MXTunePart(String instrument, List<MXTuneStaff> staves)
    {
        this.instrument = instrument != null ? instrument : "";
        this.staves = staves != null ? staves : Collections.emptyList();
    }

    public MXTunePart(NBTTagCompound compound)
    {
        instrument = compound.getString(TAG_INSTRUMENT);
        meta = compound.getString(TAG_META);
        int staffCount = compound.getInteger(TAG_STAFF_COUNT);

        staves = new ArrayList<>();
        for(int i = 0; i < staffCount; i++)
        {
            NBTTagCompound compoundStaff = compound.getCompoundTag(TAG_STAFF_PREFIX + i);
            staves.add(new MXTuneStaff(i, compoundStaff));
        }
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString(TAG_INSTRUMENT, instrument);
        compound.setString(TAG_META, meta);
        compound.setInteger(TAG_STAFF_COUNT, staves.size());

        int i = 0;
        for (MXTuneStaff staff : staves)
        {
            NBTTagCompound compoundStaff = new NBTTagCompound();
            staff.writeToNBT(compoundStaff);

            compound.setTag(TAG_STAFF_PREFIX + i, compoundStaff);
            i++;
        }
    }

    public String getInstrument()
    {
        return instrument != null ? instrument : "";
    }

    public void setInstrument(String instrument)
    {
        this.instrument = instrument;
    }

    public List<MXTuneStaff> getStaves()
    {
        return staves != null ? staves : Collections.emptyList();
    }

    public void setStaves(List<MXTuneStaff> staves)
    {
        this.staves = staves;
    }

    public String getMeta()
    {
        return meta != null ? meta : "";
    }

    public void setMeta(String meta)
    {
        this.meta = meta;
    }

    private String getSortingKey()
    {
        return instrument.trim() + meta.trim();
    }

    @Override
    public int compareTo(MXTunePart o)
    {
        return getSortingKey().compareTo(o.getSortingKey());
    }
}
