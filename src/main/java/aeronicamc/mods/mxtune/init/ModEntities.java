package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities
{
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Reference.MOD_ID);
    public static final String MXTUNE_MUSIC_SOURCE = "MXTUNE_MUSIC_SOURCE";

    private static boolean isInitialized;

    public static final RegistryObject<EntityType<MusicSourceEntity>> MUSIC_SOURCE = register("music_source", EntityType.Builder.<MusicSourceEntity>of((type, world) -> new MusicSourceEntity(world), EntityClassification.byName(MXTUNE_MUSIC_SOURCE)).sized(0.0F, 0.0F).setCustomClientFactory((spawnEntity, world) -> new MusicSourceEntity(world)));

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder)
    {
        return ENTITIES.register(name, () -> builder.build(new ResourceLocation(Reference.MOD_ID, name).toString()));
    }

    public static void registerToModEventBus(final IEventBus modEventBus)
    {
        if (isInitialized) {
            throw new IllegalStateException("Already initialized");
        }

        ENTITIES.register(modEventBus);

        isInitialized = true;
    }

    public static void extendEntityClassification()
    {
        EntityClassification.create(MXTUNE_MUSIC_SOURCE, "mxtune_sound_source", -1, true, true, 64);
    }
}
