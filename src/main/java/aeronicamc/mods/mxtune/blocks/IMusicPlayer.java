package aeronicamc.mods.mxtune.blocks;

import aeronicamc.mods.mxtune.util.MusicProperties;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public interface IMusicPlayer
{
    default MusicProperties getMusicProperties() { return MusicProperties.INVALID; }

//    default SoundRange getSoundRange() { /* implement in TE */ return SoundRange.NORMAL; }

    default LazyOptional<IItemHandler> getItemHandler() { /* implement in TE */  return LazyOptional.empty(); }
}
