
package aeronicamc.mods.mxtune.mxt;

import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MXTunePart implements Serializable
{
    private static final long serialVersionUID = -76034260522031311L;
    private static final String TAG_INSTRUMENT = "instrument";
    private static final String TAG_PACKED_PATCH = "packedPatch";
    private static final String TAG_META = "meta";
    private static final String TAG_STAFF_PREFIX = "staff";
    private static final String TAG_STAFF_COUNT = "staffCount";
    private static final String TAG_TRANSPOSE = "transpose";

    private String instrumentId;
    private int packedPatch;
    private String meta;
    private int transpose;
    private List<MXTuneStaff> staves;

    public MXTunePart()
    {
        meta = "";
        instrumentId = "";
        staves = new ArrayList<>();
        transpose = 0;
    }

    public MXTunePart(@Nullable String instrumentId, @Nullable String meta, int packedPatch, @Nullable List<MXTuneStaff> staves)
    {
        this.meta = meta != null ? meta : "";
        this.instrumentId = instrumentId != null ? instrumentId : "";
        this.packedPatch = packedPatch;
        this.staves = staves != null ? staves : new ArrayList<>();
        transpose = 0;
    }

    public MXTunePart(CompoundNBT compound)
    {
        instrumentId = compound.contains(TAG_INSTRUMENT, Constants.NBT.TAG_STRING) ? compound.getString(TAG_INSTRUMENT) : SoundFontProxyManager.INSTRUMENT_DEFAULT_ID;
        meta = compound.getString(TAG_META);
        packedPatch = compound.getInt(TAG_PACKED_PATCH);
        transpose = compound.getInt(TAG_TRANSPOSE);
        int staffCount = compound.getInt(TAG_STAFF_COUNT);

        staves = new ArrayList<>();
        for (int i = 0; i < staffCount; i++)
        {
            CompoundNBT compoundStaff = (CompoundNBT) compound.get(TAG_STAFF_PREFIX + i);
            staves.add(new MXTuneStaff(i, compoundStaff));
        }
    }

    public void writeToNBT(CompoundNBT compound)
    {
        compound.putString(TAG_INSTRUMENT, instrumentId);
        compound.putString(TAG_META, meta);
        compound.putInt(TAG_PACKED_PATCH, packedPatch);
        compound.putInt(TAG_TRANSPOSE, transpose);
        compound.putInt(TAG_STAFF_COUNT, staves.size());

        int i = 0;
        for (MXTuneStaff staff : staves)
        {
            CompoundNBT compoundStaff = new CompoundNBT();
            staff.writeToNBT(compoundStaff);

            compound.put(TAG_STAFF_PREFIX + i, compoundStaff);
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

    public String getInstrumentId()
    {
        return instrumentId != null ? instrumentId : "";
    }

    @SuppressWarnings("unused")
    public void setInstrumentId(String instrumentId)
    {
        this.instrumentId = instrumentId;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MXTunePart mxTunePart = (MXTunePart) o;
        return new EqualsBuilder()
                .append(instrumentId, mxTunePart.getInstrumentId())
                .append(packedPatch, mxTunePart.getPackedPatch())
                .append(meta, mxTunePart.getMeta())
                .append(transpose, mxTunePart.getMeta())
                .append(staves, mxTunePart.getStaves())
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(instrumentId)
                .append(packedPatch)
                .append(meta)
                .append(transpose)
                .append(staves)
                .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("instrumentName", instrumentId)
                .append("packedPatch", packedPatch)
                .append("meta", meta)
                .append("transpose", transpose)
                .append("staves", staves)
                .toString();
    }
}
