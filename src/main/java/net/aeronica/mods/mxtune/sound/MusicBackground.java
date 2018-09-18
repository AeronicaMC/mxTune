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
import net.minecraft.util.SoundCategory;

import net.minecraft.client.audio.ISound.AttenuationType;

/**
 * Using MovingSound just to make this ITickableSound based. This keeps the sound from timing out after 20 ticks.
 * @author Paul Boese a.k.a. Aeronica
 *
 */
public class MusicBackground extends MovingSound
{

    Integer playID;
    SoundEventAccessor soundEventAccessor;
    
    public MusicBackground(Integer playID)
    {
        super(ModSoundEvents.PCM_PROXY, SoundCategory.PLAYERS);
        this.playID = playID;
        this.sound = new PCMSound();
        this.volume = 0.70F;
        this.pitch = 1F;
        this.xPosF = 0;
        this.yPosF = 0;
        this.zPosF = 0;
        this.repeat = false;
        this.repeatDelay = 0;
        this.donePlaying = false;
        this.attenuationType = AttenuationType.NONE;
        this.soundEventAccessor = new SoundEventAccessor(this.sound.getSoundLocation(), "mxtune.subtitle.pcm-proxy");
    }

    @Override
    public SoundEventAccessor createAccessor(SoundHandler handler)
    {
        return this.soundEventAccessor;
    }

    @Override
    public void update()
    {
        if (this.playID != null && ClientAudio.hasPlayID(playID))
        {
            /* update nothing - just hold the stream open until done */
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
