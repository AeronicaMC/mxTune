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

import net.aeronica.mods.mxtune.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder(Reference.MOD_ID)
public class ModSoundEvents
{
    @ObjectHolder("pcm-proxy")
    public static final SoundEvent PCM_PROXY = registerSound("pcm-proxy");
    public static final SoundEvent ENTITY_TINY_TIMPANI_SQUISH = registerSound("entity.tiny.timpani.squish");
    public static final SoundEvent ENTITY_MEDIUM_TIMPANI_SQUISH = registerSound("entity.medium.timpani.squish");
    public static final SoundEvent ENTITY_LARGE_TIMPANI_SQUISH = registerSound("entity.large.timpani.squish");
    public static final SoundEvent ENTITY_TIMPANI_JUMP = registerSound("entity.timpani.jump");
    public static final SoundEvent ENTITY_TINY_TIMPANI_HURT = registerSound("entity.small.timpani.hurt");
    public static final SoundEvent ENTITY_TINY_TIMPANI_DEATH = registerSound("entity.small.timpani.death");
    public static final SoundEvent ENTITY_TIMPANI_HURT = registerSound("entity.timpani.hurt");
    public static final SoundEvent ENTITY_TIMPANI_DEATH = registerSound("entity.timpani.death");

    private ModSoundEvents() { /* NOP */ }

    /**
     * Register a {@link SoundEvent}.
     * 
     * @author Choonster
     * @param soundName The SoundEvent's name without the [MOD_ID] prefix
     * @return The SoundEvent
     */
    private static SoundEvent registerSound(String soundName)
    {
        final ResourceLocation soundID = new ResourceLocation(Reference.MOD_ID, soundName);
        return new SoundEvent(soundID).setRegistryName(soundID);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class RegistrationHandler
    {
        private RegistrationHandler() { /* NOP */ }

        @SubscribeEvent
        public static void registerSoundEvents(final RegistryEvent.Register<SoundEvent> event) {
            event.getRegistry().registerAll(
                    PCM_PROXY,
                    ENTITY_TINY_TIMPANI_SQUISH,
                    ENTITY_MEDIUM_TIMPANI_SQUISH,
                    ENTITY_LARGE_TIMPANI_SQUISH,
                    ENTITY_TIMPANI_JUMP,
                    ENTITY_TINY_TIMPANI_HURT,
                    ENTITY_TINY_TIMPANI_DEATH,
                    ENTITY_TIMPANI_HURT,
                    ENTITY_TIMPANI_DEATH
            );
        }
    }
}
