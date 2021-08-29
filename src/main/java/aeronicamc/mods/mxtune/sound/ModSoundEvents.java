package aeronicamc.mods.mxtune.sound;


import aeronicamc.mods.mxtune.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;


@ObjectHolder(Reference.MOD_ID)
public class ModSoundEvents
{
    @ObjectHolder("pcm-proxy")
    public static final SoundEvent PCM_PROXY = registerSound("pcm-proxy");

    private ModSoundEvents() { /* NOP */ }

    /**
     * Register a {@link SoundEvent}.
     *
     * @param soundName The SoundEvent's name without the [MODID] prefix
     * @return The SoundEvent
     * @author Choonster
     */
    private static SoundEvent registerSound(String soundName)
    {
        final ResourceLocation soundID = new ResourceLocation(Reference.MOD_ID, soundName);
        return new SoundEvent(soundID).setRegistryName(soundID);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler
    {
        private RegistrationHandler() { /* NOP */ }

        @SubscribeEvent
        public static void registerSoundEvents(final RegistryEvent.Register<SoundEvent> event)
        {
            event.getRegistry().registerAll(PCM_PROXY);
        }
    }
}
