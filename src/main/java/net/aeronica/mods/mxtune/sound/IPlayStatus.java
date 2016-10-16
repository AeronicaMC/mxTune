package net.aeronica.mods.mxtune.sound;

import net.minecraft.entity.player.EntityPlayer;

public interface IPlayStatus {

	/** Playing is NOT saved in NBT and is false by default */
	public void setPlaying(EntityPlayer playerIn, boolean playing);
	
	public boolean isPlaying();

}
