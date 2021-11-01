package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.entity.SittableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

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
public class ModEntities
{
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Reference.MOD_ID);
    public static final String MXTUNE_SOUND_SOURCE = "MXTUNE_SOUND_SOURCE";

    private static boolean isInitialized;

    public static final RegistryObject<EntityType<SittableEntity>> SITTABLE_ENTITY = registerEntityType("sittable_entity",
        () -> EntityType.Builder.<SittableEntity>of((SittableEntity::new), EntityClassification.MISC)
            .sized(0.0f, 0.0f)
        );

    /**
     * Registers an entity type.
     *
     * @param name    The registry name of the entity type
     * @param factory The factory used to create the entity type builder
     * @return A RegistryObject reference to the entity type
     */
    private static <T extends Entity> RegistryObject<EntityType<T>> registerEntityType(final String name, final Supplier<EntityType.Builder<T>> factory) {
        return ENTITIES.register(name,
            () -> factory.get().build(new ResourceLocation(Reference.MOD_ID, name).toString())
        );
    }

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

        ENTITIES.register(modEventBus);

        isInitialized = true;
    }

    public static void extendEntityClassification()
    {
            EntityClassification.create(MXTUNE_SOUND_SOURCE, "mxtune_sound_source",-1, false, false, 72);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class SpawnHandler {
        @SubscribeEvent(priority = EventPriority.LOW)
        public static void registerEntitySpawns(final BiomeLoadingEvent event) {
            final RegistryKey<Biome> biomeRegistryKey = RegistryKey.create(ForgeRegistries.Keys.BIOMES, event.getName());

            if (BiomeDictionary.hasType(biomeRegistryKey, BiomeDictionary.Type.OCEAN)) {
//                addSpawn(event, EntityType.GUARDIAN, 100, 5, 20, EntityClassification.WATER_CREATURE);
            }

//            copySpawns(event, PLAYER_AVOIDING_CREEPER.get(), EntityClassification.MONSTER, EntityType.CREEPER, EntityClassification.MONSTER);
        }

        /**
         * Add a spawn entry for the supplied entity to the biome being loaded in {@link BiomeLoadingEvent}.
         * <p>
         * Adapted from Forge's {@code EntityRegistry.addSpawn} method in 1.12.2.
         *
         * @param event          The event
         * @param entityType     The entity type
         * @param itemWeight     The weight of the spawn list entry (higher weights have a higher chance to be chosen)
         * @param minGroupCount  Min spawn count
         * @param maxGroupCount  Max spawn count
         * @param classification The entity classification
         */
        private static void addSpawn(final BiomeLoadingEvent event, final EntityType<? extends MobEntity> entityType, final int itemWeight, final int minGroupCount, final int maxGroupCount, final EntityClassification classification) {
            final List<MobSpawnInfo.Spawners> spawnersList = event.getSpawns()
                    .getSpawner(classification);

            // Try to find an existing entry for the entity type
            spawnersList.stream()
                    .filter(spawners -> spawners.type == entityType)
                    .findFirst()
                    .ifPresent(spawnersList::remove); // If there is one, remove it

            // Add a new one
            spawnersList.add(new MobSpawnInfo.Spawners(entityType, itemWeight, minGroupCount, maxGroupCount));
        }

        /**
         * Add a spawn list entry for {@code entityTypeToAdd} to the biome being loaded in {@link BiomeLoadingEvent} with an entry for {@code entityTypeToCopy} using the same weight and group count.
         *
         * @param event                The event
         * @param entityTypeToAdd      The entity type to add spawn entries for
         * @param classificationToAdd  The entity classification to add spawn entries for
         * @param entityTypeToCopy     The entity type to copy spawn entries from
         * @param classificationToCopy The entity classification to copy spawn entries from
         */
        private static void copySpawns(final BiomeLoadingEvent event, final EntityType<? extends MobEntity> entityTypeToAdd, final EntityClassification classificationToAdd, final EntityType<? extends MobEntity> entityTypeToCopy, final EntityClassification classificationToCopy) {
            event.getSpawns()
                    .getSpawner(classificationToCopy)
                    .stream()
                    .filter(spawners -> spawners.type == entityTypeToCopy)
                    .findFirst()
                    .ifPresent(spawners ->
                                       event.getSpawns().getSpawner(classificationToAdd)
                                               .add(new MobSpawnInfo.Spawners(entityTypeToAdd, spawners.weight, spawners.minCount, spawners.maxCount))
                              );
        }
    }
}
