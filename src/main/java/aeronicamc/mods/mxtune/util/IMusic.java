package aeronicamc.mods.mxtune.util;

import net.minecraft.item.ItemStack;

/**
 * Classes that contain MML sources should implement this interface
 * 
 * @author Aeronica
 * 
 */
public interface IMusic
{
    boolean hasMusicText(ItemStack itemStackIn);
    MusicType getMusicType(ItemStack itemStackIn);
}
