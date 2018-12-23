/*
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

import net.aeronica.mods.mxtune.Reference;
import net.minecraft.client.audio.Sound;
import net.minecraft.util.ResourceLocation;

/**
 * <p>This class associates a fake sound file with the PCM codec and is an in-memory representation of a sounds.json entry.
 * In this case there is one file in the path:</p>
 * <code>
 * &nbsp;&nbsp;&nbsp;&nbsp;assets/mxtune/music/pcm-proxy.nul
 * </code>
 * <p></p>
 * <p>The Forge SoundSetupEvent is when the association is made:</p>
 * <code>
 * &nbsp;&nbsp;&nbsp;&nbsp;SoundSystemConfig.setCodec("nul", CodecPCM.class);
 * </code>
 * <p></p>
 * <p>Using this technique we can specify the sound codec of our choice and fake out the vanilla SoundHandler.</p>
 */
public class PCMSound extends Sound
{
    public PCMSound()
    {
        super(Reference.MOD_ID + ":pcm-proxy", 1F, 1F, 0, Type.SOUND_EVENT, true);
    }

    public PCMSound(String nameIn, float volumeIn, float pitchIn, int weightIn, Type typeIn, boolean isStreaming)
    {
        super(nameIn, volumeIn, pitchIn, weightIn, typeIn, isStreaming);
    }

    @Override
    public ResourceLocation getSoundAsOggLocation()
    {
        return new ResourceLocation(Reference.MOD_ID, "music/" + getSoundLocation().getPath() + ".nul");
    }
}
