package aeronicamc.mods.mxtune.mxt.base;

import net.minecraft.nbt.CompoundNBT;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public abstract class BaseData implements Serializable
{
    public static final long serialVersionUID = -76044260522231311L;

    public BaseData()
    {
        /* NOP */
    }

    public abstract void readFromNBT(CompoundNBT compound);

    public abstract void writeToNBT(CompoundNBT compound);

    public abstract <T extends BaseData> T factory();


    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseData baseData = (BaseData) o;
        return new EqualsBuilder()
                .append(this, baseData)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(serialVersionUID)
                .toHashCode();
    }
}
