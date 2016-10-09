/**
 * Copyright {2016} Paul Boese aka Aeronica
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.sound;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;

public class MovingMusicRegistered extends MovingSound
{
    private final EntityPlayer player;
    protected boolean repeat = false;
    protected int repeatDelay = 0;
    protected float pitch;
    
    public MovingMusicRegistered()
    {
        super(ModSoundEvents.PCM_PROXY, SoundCategory.getByName("mxtune"));
        this.player = null;
        this.volume = 1.0F;
        this.pitch = 1.0F;
    }
    
    public MovingMusicRegistered(EntityPlayer player)
    {
        super(ModSoundEvents.PCM_PROXY, SoundCategory.getByName("mxtune"));
        this.player = player;
        this.volume = 1.0f;
        this.pitch = 1.0F;
    }

    public void setDonePlaying()
    {
        boolean hasSimple = player.hasCapability(PlayStatusUtil.PLAY_STATUS, null); 
        if (hasSimple) player.getCapability(PlayStatusUtil.PLAY_STATUS, null).setPlaying(this.player, false);
        
        this.repeat = false;
        this.donePlaying = true;
        this.repeatDelay = 0;
    }
    
    @Override
    public boolean isDonePlaying()
    {
        return this.donePlaying;
    }

    @Override
    public void update()
    {
        boolean hasSimple = player.hasCapability(PlayStatusUtil.PLAY_STATUS, null); 
        IPlayStatus simple = player.getCapability(PlayStatusUtil.PLAY_STATUS, null);
        if (this.player != null && !this.player.isDead && hasSimple && simple.isPlaying())
        {
            // stuff like velocity and direction detection and Doppler effects. 
        }
        else
        {
            this.setDonePlaying();
        }
        this.xPosF = (float)this.player.posX;
        this.yPosF = (float)this.player.posY;
        this.zPosF = (float)this.player.posZ;

    }
    @Override
    public boolean canRepeat()
    {
        return this.repeat;
    }

    @Override
    public float getVolume()
    {
        return this.volume;
    }

    @Override
    public float getPitch()
    {
        return this.pitch;
    }

    @Override
    public int getRepeatDelay(){ return this.repeatDelay; }

    @Override
    public AttenuationType getAttenuationType()
    {
        return AttenuationType.LINEAR;
    }
}
