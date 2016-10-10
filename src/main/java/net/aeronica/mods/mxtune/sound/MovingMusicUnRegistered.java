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

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class MovingMusicUnRegistered extends MovingSound
{

    ResourceLocation resourceLocation = null;
    
    SoundEventAccessor soundEventAccessor = null;
    
    SoundCategory soundCategory = SoundCategory.MASTER;

    boolean canRepeat =  false;

    int repeatDelay = 0;

    float volume = 1;

    float pitch = 1;

    float xPosF = 0;

    float yPosF = 0;

    float zPosF = 0;

    ISound.AttenuationType attenuationType;
    
    EntityPlayer player;

    UnregisteredSound sound;
    public MovingMusicUnRegistered(EntityPlayer player, UnregisteredSound sound)
    {
        super(ModSoundEvents.PCM_PROXY, SoundCategory.getByName("mxtune"));
        this.player = player;
        this.sound = sound;
    }

    @Override
    public ResourceLocation getSoundLocation()
    {
        return this.resourceLocation;
    }

    @Override
    public SoundEventAccessor createAccessor(SoundHandler handler)
    {
        return this.soundEventAccessor;
    }

    @Override
    public Sound getSound()
    {
        return this.sound;
    }

    @Override
    public SoundCategory getCategory()
    {
        return soundCategory;
    }

    @Override
    public boolean canRepeat()
    {
        return this.canRepeat;
    }

    @Override
    public int getRepeatDelay()
    {
        return this.repeatDelay;
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
    public float getXPosF()
    {
        return this.xPosF;
    }

    @Override
    public float getYPosF()
    {
        return this.yPosF;
    }

    @Override
    public float getZPosF()
    {
        return this.zPosF;
    }

    @Override
    public AttenuationType getAttenuationType()
    {
        return this.attenuationType;
    }

    public void setResourceLocation(ResourceLocation resourceLocation)
    {
        this.resourceLocation = resourceLocation;
    }

    public void setSoundEventAccessor(SoundEventAccessor soundEventAccessor)
    {
        this.soundEventAccessor = soundEventAccessor;
    }

    public void setSoundCategory(SoundCategory soundCategory)
    {
        this.soundCategory = soundCategory;
    }

    public void setCanRepeat(boolean canRepeat)
    {
        this.canRepeat = canRepeat;
    }

    public void setRepeatDelay(int repeatDelay)
    {
        this.repeatDelay = repeatDelay;
    }

    public void setVolume(float volume)
    {
        this.volume = volume;
    }

    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }

    public void setxPosF(float xPosF)
    {
        this.xPosF = xPosF;
    }

    public void setyPosF(float yPosF)
    {
        this.yPosF = yPosF;
    }

    public void setzPosF(float zPosF)
    {
        this.zPosF = zPosF;
    }

    public void setAttenuationType(ISound.AttenuationType attenuationType)
    {
        this.attenuationType = attenuationType;
    }

    public void setSound(UnregisteredSound sound)
    {
        this.sound = sound;
    }

    @Override
    public boolean isDonePlaying()
    {
        return this.donePlaying;
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
        this.yPosF = (float)this.player.posY-5;
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
