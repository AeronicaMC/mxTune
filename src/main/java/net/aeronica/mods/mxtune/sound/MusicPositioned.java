/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
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

import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

/**
 * Implements ISound<br></br>
 * For musical machines placed in the world
 */
public class MusicPositioned extends MxSound
{
    public MusicPositioned(Integer playID, BlockPos pos, SoundRange soundRange)
    {
        super(playID,SoundCategory.RECORDS);
        this.xPosF = (float)pos.getX()+0.5F;
        this.yPosF = (float)pos.getY()+0.5F;
        this.zPosF = (float)pos.getZ()+0.5F;
        this.volume = soundRange.getRange();
        this.attenuationType = soundRange.getAttenuationType();
    }

    @Override
    public void update()
    {
        if (this.playID != null && ClientAudio.hasPlayID(playID))
        {
            // Override ModSoundVolume updates
        }
        else
        {
            this.setDonePlaying();
        }
    }
}
