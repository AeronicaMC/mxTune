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

import net.aeronica.mods.mxtune.groups.GROUPS;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class MusicMoving extends MovingSound
{

    Integer playID;
    SoundEventAccessor soundEventAccessor;

    /** This is used to fake out the vanilla SoundHandler. From this point we will force the system to use the sound codec of our choice */
    public MusicMoving(Integer playID)
    {
        super(ModSoundEvents.PCM_PROXY, SoundCategory.getByName("mxtune"));
        this.playID = playID;
        this.sound = new PCMSound();
        this.volume = 1F;
        this.pitch = 1F;
        this.repeat = false;
        this.repeatDelay = 0;
        this.donePlaying = false;
        Vec3d pos = GROUPS.getMedianPos(playID);
        this.xPosF = (float) pos.xCoord;
        this.yPosF = (float) pos.yCoord;
        this.zPosF = (float) pos.zCoord;
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
        if (this.playID != null && ClientAudio.isPlaying(playID))
        {
            Vec3d pos = GROUPS.getMedianPos(playID);
            this.xPosF = (float) pos.xCoord;
            this.yPosF = (float) pos.yCoord;
            this.zPosF = (float) pos.zCoord; 
        }
        else
        {
            this.setDonePlaying();
        }

    }
    
    public void setDonePlaying()
    {
        this.repeat = false;
        this.donePlaying = true;
        this.repeatDelay = 0;
    }

}

