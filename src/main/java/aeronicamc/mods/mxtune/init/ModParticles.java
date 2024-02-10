package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModParticles
{
    private ModParticles() { /* NOOP */ }

    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Reference.MOD_ID);
    private static boolean isInitialized;

    static final RegistryObject<ParticleType<BasicParticleType>> SPEAKER_TYPE = PARTICLES.register("speaker", () -> new BasicParticleType(true));

    /**
     * Registers the {@link DeferredRegister} instance with the mod event bus.
     * <p>
     * This should be called during mod construction.
     *
     * @param modEventBus The mod event bus
     */
    public static void registerToModEventBus(final IEventBus modEventBus)
    {
        if (isInitialized) {
            throw new IllegalStateException("Already initialized");
        }

        PARTICLES.register(modEventBus);

        isInitialized = true;
    }
}
