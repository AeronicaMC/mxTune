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

import net.aeronica.mods.mxtune.managers.PlayIdSupplier;
import net.minecraft.util.SoundCategory;

/**
 * MusicClient ISound
 *
 * @author Paul Boese a.k.a. Aeronica
 */
public class MusicClient extends MxSound
{
    public MusicClient(Integer playID)
    {
        super(playID, SoundCategory.MUSIC);
        category = getSoundCategory(playID);
        attenuationType = AttenuationType.NONE;
    }

    private SoundCategory getSoundCategory(int playID)
    {
        PlayIdSupplier.PlayType playType = PlayIdSupplier.getTypeForPlayId(playID);

        switch (playType)
        {
            case BACKGROUND:
            case EVENT:
                return SoundCategory.MUSIC;
            case PERSONAL:
            case PLAYERS:
                return SoundCategory.PLAYERS;
            default:
        }
        return SoundCategory.MUSIC;
    }
}
