package aeronicamc.mods.mxtune.datagen.loot;

import aeronicamc.mods.mxtune.util.RegistryUtil;
import net.minecraft.data.loot.EntityLootTables;
import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Generates this mod's entity loot tables.
 *
 * @author Choonster
 */
public class MXTuneEntityLootTables extends EntityLootTables
{
    @Override
    protected void addTables()
    {
        // populate as needed
    }

    @Override
    protected Iterable<EntityType<?>> getKnownEntities()
    {
        return RegistryUtil.getModRegistryEntries(ForgeRegistries.ENTITIES);
    }
}
