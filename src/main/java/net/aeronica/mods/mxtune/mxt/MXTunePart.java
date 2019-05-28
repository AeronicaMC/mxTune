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

package net.aeronica.mods.mxtune.mxt;

import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MXTunePart
{
    private static final String TAG_INSTRUMENT = "instrument";
    private static final String TAG_PACKED_PATCH = "packedPatch";
    private static final String TAG_META = "meta";
    private static final String TAG_STAFF_PREFIX = "staff";
    private static final String TAG_STAFF_COUNT = "staffCount";
    private static final String TAG_TRANSPOSE = "transpose";

    private String instrumentName;
    private int packedPatch;
    private String meta;
    private int transpose;
    private List<MXTuneStaff> staves;

    public MXTunePart()
    {
        meta = "";
        instrumentName = "";
        staves = new ArrayList<>();
        transpose = 0;
    }

    public MXTunePart(String instrumentName, String meta, int packedPatch, List<MXTuneStaff> staves)
    {
        this.meta = meta != null ? meta : "";
        this.instrumentName = instrumentName != null ? instrumentName : "";
        this.packedPatch = packedPatch;
        this.staves = staves != null ? staves : new ArrayList<>();
        transpose = 0;
    }

    public MXTunePart(NBTTagCompound compound)
    {
        instrumentName = compound.getString(TAG_INSTRUMENT);
        meta = compound.getString(TAG_META);
        packedPatch = compound.getInteger(TAG_PACKED_PATCH);
        transpose = compound.getInteger(TAG_TRANSPOSE);
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
        compound.setString(TAG_INSTRUMENT, instrumentName);
        compound.setString(TAG_META, meta);
        compound.setInteger(TAG_PACKED_PATCH, packedPatch);
        compound.setInteger(TAG_TRANSPOSE, transpose);
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

    public List<MXTuneStaff> getStaves()
    {
        return staves != null ? staves : Collections.emptyList();
    }

    @SuppressWarnings("unused")
    public void setStaves(List<MXTuneStaff> staves)
    {
        this.staves = staves;
    }

    public String getInstrumentName()
    {
        return instrumentName != null ? instrumentName : "";
    }

    @SuppressWarnings("unused")
    public void setInstrumentName(String instrumentName)
    {
        this.instrumentName = instrumentName;
    }

    @SuppressWarnings("unused")
    public String getMeta()
    {
        return meta != null ? meta : "";
    }

    @SuppressWarnings("unused")
    public void setMeta(String meta)
    {
        this.meta = meta;
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

    @SuppressWarnings("unused")
    public int getTranspose()
    {
        return transpose;
    }

    @SuppressWarnings("unused")
    public void setTranspose(int transpose)
    {
        this.transpose = transpose;
    }
}
