package aeronicamc.mods.mxtune.caps.venues;

import net.minecraft.nbt.INBT;

import javax.annotation.Nullable;
import java.util.List;

public interface IMusicVenues
{
    List<MusicVenue> getMusicVenues();

    void addMusicVenue(MusicVenue musicVenue);

    boolean removeMusicVenue(MusicVenue musicVenue);

    int getInt();

    void setInt(Integer someInt);

    @Nullable
    INBT serializeNBT();

    void deserializeNBT(INBT nbt);

    void sync();
}
