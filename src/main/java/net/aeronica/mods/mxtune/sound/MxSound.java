/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.sound;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.SoundCategory;

public abstract class MxSound extends MovingSound
{
    protected int playID;
    private SoundEventAccessor soundEventAccessor;

    MxSound(int playID, SoundCategory soundCategory)
    {
        super(ModSoundEvents.PCM_PROXY, soundCategory);
        this.playID = playID;
        this.sound = new PCMSound();
        this.volume = 1F;
        this.pitch = 1F;
        this.repeat = false;
        this.repeatDelay = 0;
        this.donePlaying = false;
        this.xPosF = 0F;
        this.yPosF = 0F;
        this.zPosF = 0F;
        this.attenuationType = AttenuationType.LINEAR;
        this.soundEventAccessor = new SoundEventAccessor(this.sound.getSoundLocation(), "mxtune.subtitle.pcm-proxy");
    }

    /** This is used as the key for our PlaySoundEvent handler */
    MxSound()
    {
        super(ModSoundEvents.PCM_PROXY, SoundCategory.MASTER);
    }
    
    @Override
    public SoundEventAccessor createAccessor(SoundHandler handler)
    {
        return this.soundEventAccessor;
    }

    @Override
    public void update()
    {
        if (ClientAudio.hasPlayID(playID))
        {
            onUpdate();
        }
        else
        {
            this.setDonePlaying();
        }
    }

    protected void onUpdate() { /* NOP */ }

    protected void setDonePlaying()
    {
        this.repeat = false;
        this.donePlaying = true;
        this.repeatDelay = 0;
    }
}

