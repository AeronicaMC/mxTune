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

public class MXTunePart
{
    private static final String TAG_INSTRUMENT = "instrument";
    private static final String TAG_PACKED_PATCH = "packedPatch";
    private static final String TAG_SUGGESTED = "suggestedName";
    private static final String TAG_STAFF_PREFIX = "staff";
    private static final String TAG_STAFF_COUNT = "staffCount";

    private String instrument;
    private int packedPatch;
    private String suggestedInstrument;
    private List<MXTuneStaff> staves;

    public MXTunePart()
    {
        this.suggestedInstrument = "";
        this.instrument = "";
        this.staves = new ArrayList<>();
    }

    public MXTunePart(String instrument, String suggestedInstrument, int packedPatch, List<MXTuneStaff> staves)
    {
        this.suggestedInstrument = suggestedInstrument != null ? suggestedInstrument : "";
        this.instrument = instrument != null ? instrument : "";
        this.packedPatch = packedPatch;
        this.staves = staves != null ? staves : new ArrayList<>();
    }

    public MXTunePart(NBTTagCompound compound)
    {
        instrument = compound.getString(TAG_INSTRUMENT);
        suggestedInstrument = compound.getString(TAG_SUGGESTED);
        packedPatch = compound.getInteger(TAG_PACKED_PATCH);
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
        compound.setString(TAG_SUGGESTED, suggestedInstrument);
        compound.setInteger(TAG_STAFF_COUNT, staves.size());
        compound.setInteger(TAG_PACKED_PATCH, packedPatch);

        int i = 0;
        for (MXTuneStaff staff : staves)
        {
            NBTTagCompound compoundStaff = new NBTTagCompound();
            staff.writeToNBT(compoundStaff);

            compound.setTag(TAG_STAFF_PREFIX + i, compoundStaff);
            i++;
        }
    }

    public List<MXTuneStaff> getStaves()
    {
        return staves != null ? staves : Collections.emptyList();
    }

    @SuppressWarnings("unused")
    public void setStaves(List<MXTuneStaff> staves)
    {
        this.staves = staves;
    }

    public String getInstrument()
    {
        return instrument != null ? instrument : "";
    }

    public void setInstrument(String instrument)
    {
        this.instrument = instrument;
    }

    @SuppressWarnings("unused")
    public String getSuggestedInstrument()
    {
        return suggestedInstrument != null ? suggestedInstrument : "";
    }

    @SuppressWarnings("unused")
    public void setSuggestedInstrument(String suggestedInstrument)
    {
        this.suggestedInstrument = suggestedInstrument;
    }

    @SuppressWarnings("unused")
    public int getPackedPatch()
    {
        return packedPatch;
    }

    @SuppressWarnings("unused")
    public void setPackedPatch(int packedPatch)
    {
        this.packedPatch = packedPatch;
    }
}
