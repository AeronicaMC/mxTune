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

import net.aeronica.mods.mxtune.MXTuneMain;
import net.minecraft.client.audio.Sound;
import net.minecraft.util.ResourceLocation;

public class PCMSound extends Sound
{
    
    /**
     * This will associate a sound with the PCM codec.
     * This is an in memory representation of a sounds.json entry
     * 
     */
    public PCMSound()
    {
        super("pcm-proxy", 1F, 1F, 0, Type.SOUND_EVENT, true);
    }

    public PCMSound(String nameIn, float volumeIn, float pitchIn, int weightIn, Type typeIn, boolean isStreaming)
    {
        super(nameIn, volumeIn, pitchIn, weightIn, typeIn, isStreaming);
    }

    @Override
    public ResourceLocation getSoundAsOggLocation()
    {
        return new ResourceLocation(MXTuneMain.MOD_ID, "music/" + getSoundLocation().getPath() + ".nul");
    }
}
