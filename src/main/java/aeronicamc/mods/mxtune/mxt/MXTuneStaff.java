
package aeronicamc.mods.mxtune.mxt;

import net.minecraft.nbt.CompoundNBT;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nullable;
import java.io.Serializable;

public class MXTuneStaff implements Serializable
{
    private static final long serialVersionUID = -76024260522131311L;
    private static final String TAG_MML = "mml";
    private static final String TAG_META = "meta";

    private final int staff;
    private final String mml;
    private String meta = "";

    public MXTuneStaff(int staff, @Nullable String mml)
    {
        this.staff = staff;
        this.mml = mml != null ? mml : "";
    }

    public MXTuneStaff(int i, CompoundNBT compound)
    {
        staff = i;
        mml = compound.getString(TAG_MML);
        meta = compound.getString(TAG_META);
    }

    public void writeToNBT(CompoundNBT compound)
    {
        compound.putString(TAG_MML, mml);
        compound.putString(TAG_META, meta);
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
