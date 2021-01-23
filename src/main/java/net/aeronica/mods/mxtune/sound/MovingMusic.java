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

import net.aeronica.mods.mxtune.managers.GroupHelper;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class MovingMusic extends MxSound
{
    /**
     * Implements ISound<br></br>
     * For musical machines carried or used in the world
     * @param audioData
     */
    MovingMusic(AudioData audioData)
    {
        super(audioData, SoundCategory.PLAYERS);
        Vec3d pos = GroupHelper.getMedianPos(playID);
        this.xPosF = (float) pos.x;
        this.yPosF = (float) pos.y;
        this.zPosF = (float) pos.z;
    }

    /** This is used as the key for our PlaySoundEvent handler */
    MovingMusic()
    {
        super();
    }

    @Override
    public void onUpdate()
    {
        Vec3d pos = GroupHelper.getMedianPos(this.playID);
        this.xPosF = (float) pos.x;
        this.yPosF = (float) pos.y;
        this.zPosF = (float) pos.z;
    }
}


