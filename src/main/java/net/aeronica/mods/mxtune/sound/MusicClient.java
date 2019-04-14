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

import net.aeronica.mods.mxtune.config.ModConfig;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.SoundCategory;

/**
 * Using MovingSound just to make this ITickableSound based. This keeps the sound from timing out after 20 ticks.
 *
 * @author Paul Boese a.k.a. Aeronica
 */
public class MusicClient extends MovingSound
{
    private Integer playID;
    private SoundEventAccessor soundEventAccessor;

    public MusicClient(Integer playID)
    {
        super(ModSoundEvents.PCM_PROXY, SoundCategory.MASTER);
        this.playID = playID;
        this.sound = new PCMSound();
        this.volume = getModVolume();
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
    public SoundEventAccessor createAccessor(SoundHandler handler) { return this.soundEventAccessor; }

    @Override
    public void update()
    {
        // update nothing - just hold the stream open until done
        if ((this.playID == null) || !ClientAudio.hasPlayID(playID))
            this.setDonePlaying();
        this.volume = getModVolume();
    }

    private void setDonePlaying() { this.donePlaying = true; }

    private float getModVolume() { return ModConfig.getClientPlayerVolume(); }

}
