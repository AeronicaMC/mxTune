package aeronicamc.mods.mxtune.init;


import aeronicamc.mods.mxtune.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


/**
 *
 Licenses for assets can be found in ASSETS_LICENSE.txt. All other files are licensed under the terms below:

 The MIT License (MIT)

 Test Mod 3 - Copyright (c) 2015-2021 Choonster

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 *
 * @author Choonster
 */
public class ModSoundEvents
{
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Reference.MOD_ID);
    private static boolean isInitialized;

    public static final RegistryObject<SoundEvent> PCM_PROXY = registerSoundEvent("pcm-proxy");
    public static final RegistryObject<SoundEvent> FAILURE = registerSoundEvent("failure");
    public static final RegistryObject<SoundEvent> CRUMPLE_PAPER = registerSoundEvent("crumple-paper");
    public static final RegistryObject<SoundEvent> ROTATE_BLOCK = registerSoundEvent("rotate_block");
    public static final RegistryObject<SoundEvent> ROTATE_BLOCK_FAILED = registerSoundEvent("rotate_block_failed");

    private ModSoundEvents() { /* NOP */ }

    public static void registerToModEventBus(final IEventBus modEventBus)
    {
        if (isInitialized)
        {
            throw new IllegalStateException("Already initialized");
        }
        SOUND_EVENTS.register(modEventBus);
        isInitialized = true;
    }

    /**
     * Registers a sound event.
     *
     * @param soundName The sound event's name, without the mxtune prefix
     * @return A RegistryObject reference to the SoundEvent
     */
    private static RegistryObject<SoundEvent> registerSoundEvent(final String soundName) {
        return SOUND_EVENTS.register(soundName, () -> new SoundEvent(new ResourceLocation(Reference.MOD_ID, soundName)));
    }
}
