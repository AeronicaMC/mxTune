package aeronicamc.mods.mxtune.blocks;

import aeronicamc.mods.mxtune.util.MusicProperties;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public interface IMusicPlayer
{
    MusicProperties getMusicProperties();

    int getMusicSourceEntityId();

    void setMusicSourceEntityId(int entityId);

//    default SoundRange getSoundRange() { /* implement in TE */ return SoundRange.NORMAL; }

    LazyOptional<IItemHandler> getItemHandler();
}
