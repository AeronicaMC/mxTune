/**
 * Aeronica's mxTune MOD
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
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;

public class MusicMoving extends MovingSound
{

    EntityPlayer player;
    SoundEventAccessor soundEventAccessor;

    /** This is used to fake out the vanilla SoundHandler. From this point we will force the system to use the sound codec of our choice */
    public MusicMoving(EntityPlayer player)
    {
        super(ModSoundEvents.PCM_PROXY, SoundCategory.getByName("mxtune"));
        this.player = player;
        this.sound = new PCMSound();
        this.volume = 1F;
        this.pitch = 1F;
        this.repeat = false;
        this.repeatDelay = 0;
        this.xPosF = (float)this.player.posX;
        this.yPosF = (float)this.player.posY;
        this.zPosF = (float)this.player.posZ;
        this.attenuationType = AttenuationType.LINEAR;
        this.soundEventAccessor = new SoundEventAccessor(this.sound.getSoundLocation(), "mxtune.subtitle.pcm-proxy");
    }
    /** This is used as the key for our PlaySoundEvent handler */
    public MusicMoving()
    {
        super(ModSoundEvents.PCM_PROXY, SoundCategory.getByName("mxtune"));
    }
    

    @Override
    public SoundEventAccessor createAccessor(SoundHandler handler)
    {
        return this.soundEventAccessor;
    }


    @Override
    public void update()
    {
        if (this.player != null && !this.player.isDead && (player.hasCapability(PlayStatusUtil.PLAY_STATUS, null)) && (player.getCapability(PlayStatusUtil.PLAY_STATUS, null).isPlaying()))
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
    
    public void setDonePlaying()
    {
        boolean hasSimple = player.hasCapability(PlayStatusUtil.PLAY_STATUS, null); 
        if (hasSimple) player.getCapability(PlayStatusUtil.PLAY_STATUS, null).setPlaying(this.player, false);

        this.repeat = false;
        this.donePlaying = true;
        this.repeatDelay = 0;
    }

}

