package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.render.particles.SpeakerParticle;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid= Reference.MOD_ID, value= Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModBusClientRegistryEventHandler
{
    private ModBusClientRegistryEventHandler() { /* NOOP */}

    @SubscribeEvent
    public static void event(ParticleFactoryRegisterEvent event)
    {
        Minecraft.getInstance().particleEngine.register(ModParticles.SPEAKER_TYPE.get(), new SpeakerParticle.Factory());
    }
}
