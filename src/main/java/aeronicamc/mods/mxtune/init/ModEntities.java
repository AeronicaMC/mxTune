package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.entity.SittableEntity;
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
    public static final String MXTUNE_SOUND_SOURCE = "MXTUNE_SOUND_SOURCE";

    private static boolean isInitialized;

    public static final RegistryObject<EntityType<SittableEntity>> SITTABLE_ENTITY = register("sittable_entity", EntityType.Builder.<SittableEntity>of((type, world) -> new SittableEntity(world), EntityClassification.byName(MXTUNE_SOUND_SOURCE)).sized(0.0F, 0.0F).setCustomClientFactory((spawnEntity, world) -> new SittableEntity(world)));

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
            EntityClassification.create(MXTUNE_SOUND_SOURCE, "mxtune_sound_source",-1, true, true, 64);
    }
}
