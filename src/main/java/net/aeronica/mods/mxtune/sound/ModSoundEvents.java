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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder(MXTuneMain.MODID)
public class ModSoundEvents {
    
    @ObjectHolder("pcm-proxy")
    public static final SoundEvent PCM_PROXY = registerSound("pcm-proxy");

    /**
     * Register a {@link SoundEvent}.
     * 
     * @author Choonster
     * @param soundName The SoundEvent's name without the [MODID] prefix
     * @return The SoundEvent
     */
    private static SoundEvent registerSound(String soundName) {
        final ResourceLocation soundID = new ResourceLocation(MXTuneMain.MODID, soundName);
        return new SoundEvent(soundID).setRegistryName(soundID);
    }

    @Mod.EventBusSubscriber(modid = MXTuneMain.MODID)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerSoundEvents(final RegistryEvent.Register<SoundEvent> event) {
            event.getRegistry().registerAll(
                    PCM_PROXY
            );
        }
    }
    
}
