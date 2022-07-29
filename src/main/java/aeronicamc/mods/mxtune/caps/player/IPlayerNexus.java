package aeronicamc.mods.mxtune.caps.player;

import aeronicamc.mods.mxtune.caps.venues.EntityVenueState;
import net.minecraft.nbt.INBT;

import javax.annotation.Nullable;

/**
 * Per PLayer Persistent and Volatile Data used to manage personal settings and in game mechanics.<p></p>
 * e.g. Volatile: Stage Tool use in the world. Create and edit stage areas.<p></p>
 * e.g. Persistent: HUD display options and positions.
 */
public interface IPlayerNexus
{
    void setPlayId(int playId);

    int getPlayId();

    EntityVenueState getEntityVenueState();

    void setEntityVenueState(EntityVenueState evs);

    @Nullable
    INBT serializeNBT();

    void deserializeNBT(INBT nbt);

    void sync();
}
