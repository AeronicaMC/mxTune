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

import net.aeronica.libs.mml.parser.MMLAllowedChars;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class MXTuneStaff implements Serializable
{
    private static final long serialVersionUID = -76024260522131311L;
    private static final String TAG_MML = "mml";
    private static final String TAG_META = "meta";

    private final int staff;
    private final String mml;
    private String meta = "";

    public MXTuneStaff(int staff, String mml)
    {
        this.staff = staff;
        this.mml = MMLAllowedChars.filter(mml != null ? mml : "", false);
    }

    public MXTuneStaff(int i, NBTTagCompound compound)
    {
        staff = i;
        mml = MMLAllowedChars.filter(compound.getString(TAG_MML), false);
        meta = compound.getString(TAG_META);
    }

    public void writeToNBT(NBTTagCompound compound)
    {
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

    public String getMeta()
    {
        return meta;
    }

    public void setMeta(String meta)
    {
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MXTuneStaff mxTuneStaff = (MXTuneStaff) o;
        return new EqualsBuilder()
                .append(staff, mxTuneStaff.getStaff())
                .append(mml, mxTuneStaff.getMml())
                .append(meta, mxTuneStaff.getMeta())
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(staff)
                .append(mml)
                .append(meta)
                .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("staff", staff)
                .append("mml", mml)
                .append("meta", meta)
                .toString();
    }
}
