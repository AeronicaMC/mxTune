package aeronicamc.mods.mxtune.caps.venues;

import net.minecraft.util.Tuple;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class EntityVenueState extends Tuple<Boolean, MusicVenue>
{
    public static final EntityVenueState INVALID = new EntityVenueState(false, MusicVenue.EMPTY);
    public EntityVenueState(Boolean inVenue, MusicVenue musicVenue)
    {
        super(inVenue, musicVenue);
    }

    public boolean inVenue()
    {
       return super.getA();
    }

    public MusicVenue getVenue()
    {
        return super.getB();
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
        EntityVenueState state = (EntityVenueState) o;
        return new EqualsBuilder()
                .append(super.getB().getId(), state.getB().getId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(super.getA())
                .append(super.getB())
                .toHashCode();
    }
}
