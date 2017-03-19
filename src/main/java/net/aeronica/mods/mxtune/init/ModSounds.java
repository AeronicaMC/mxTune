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
package net.aeronica.mods.mxtune.init;

import net.aeronica.mods.mxtune.sound.MODSoundCategory;
import net.aeronica.mods.mxtune.sound.ModSoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.FMLLog;

public class ModSounds
{
    
    public static final SoundCategory SC_MXTUNE = MODSoundCategory.add("MXTUNE");
    public static final SoundEvent PCM_PROXY = ModSoundEvents.PCM_PROXY;
    private static boolean initialized = false;
    private ModSounds() {}
    
    public static void init()
    {
        if (!initialized)
        {
            FMLLog.info("[mxTune] ModSounds Intialized");
            initialized = true;
        }
    }
    
}
