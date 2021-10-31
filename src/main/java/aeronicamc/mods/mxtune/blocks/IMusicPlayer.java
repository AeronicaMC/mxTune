package aeronicamc.mods.mxtune.blocks;

import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public interface IMusicPlayer
{
    default String getMML() { /* implement in TE */  return ""; }

    default int getDuration() { /* implement in TE */ return 0; }

//    default SoundRange getSoundRange() { /* implement in TE */ return SoundRange.NORMAL; }

    @Nullable
    default IItemHandler getInventory() { /* implement in TE */  return null; }
}
