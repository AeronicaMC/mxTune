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

public class MXTuneStaff implements Comparable<MXTuneStaff>
{
    private static final String TAG_WRAP_OCTAVE = "wrapOctave";
    private static final String TAG_TRANSPOSE = "transpose";
    private static final String TAG_MML = "mml";
    private static final String TAG_META = "meta";

    private final int staff;
    private boolean wrapOctave;
    private int transpose;
    private final String mml;
    private String meta = "";

    public MXTuneStaff(int staff, String mml)
    {
        this.staff = staff;
        this.mml = mml != null ? mml : "";
    }

    public MXTuneStaff(int i, NBTTagCompound compound)
    {
        staff = i;
        wrapOctave = compound.getBoolean(TAG_WRAP_OCTAVE);
        transpose = compound.getInteger(TAG_TRANSPOSE);
        mml = compound.getString(TAG_MML);
        meta = compound.getString(TAG_META);
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setBoolean(TAG_WRAP_OCTAVE, wrapOctave);
        compound.setInteger(TAG_TRANSPOSE, transpose);
        compound.setString(TAG_MML, mml);
        compound.setString(TAG_META, meta);
    }

    public int getStaff()
    {
        return staff;
    }

    public String getMml()
    {
        return mml;
    }

    public boolean isWrapOctave()
    {
        return wrapOctave;
    }

    public void setWrapOctave(boolean wrapOctave)
    {
        this.wrapOctave = wrapOctave;
    }

    public int getTranspose()
    {
        return transpose;
    }

    public void setTranspose(int transpose)
    {
        this.transpose = transpose;
    }

    public String getMeta()
    {
        return meta;
    }

    public void setMeta(String meta)
    {
        this.meta = meta;
    }

    @Override
    public int compareTo(MXTuneStaff o)
    {
        return o.getStaff() - getStaff();
    }
}
