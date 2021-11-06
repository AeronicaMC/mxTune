package aeronicamc.mods.mxtune.blocks;

import aeronicamc.mods.mxtune.util.MusicProperties;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public interface IMusicPlayer
{
    default MusicProperties getMusicProperties() { return MusicProperties.INVALID; }

//    default SoundRange getSoundRange() { /* implement in TE */ return SoundRange.NORMAL; }

    @Nullable
    default IItemHandler getInventory() { /* implement in TE */  return null; }
}
