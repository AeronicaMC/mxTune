package net.aeronica.mods.mxtune.sound;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.SyncPlayStatusMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

public class PlayStatusImpl implements IPlayStatus{

	private boolean playing = false;

	public PlayStatusImpl() {}
	
	@Override
	public String toString() {return new String("Playing: " + this.playing);}

    @Override
    public void setPlaying(EntityPlayer playerIn, boolean playing)
    {
        this.playing = playing;
        if (playerIn != null && MXTuneMain.proxy.getEffectiveSide().equals(Side.SERVER))
        {
            PacketDispatcher.sendToAll(new SyncPlayStatusMessage(playerIn, this.playing));
        }
    }

    @Override
    public boolean isPlaying() {return this.playing;}
	
}
