package aeronicamc.mods.mxtune.datagen.loot;

import aeronicamc.mods.mxtune.Reference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.loot.*;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Generates this mod's loot tables.
 *
 * @author Choonster
 */
public class MXTuneLootTableProvider extends LootTableProvider
{
    // This list is mod specific version based the vanilla parent
    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> lootTableGenerators = ImmutableList.of(
            Pair.of(MXTuneBlockLootTables::new, LootParameterSets.BLOCK),
            Pair.of(MXTuneEntityLootTables::new, LootParameterSets.ENTITY));

    public MXTuneLootTableProvider(DataGenerator pGenerator)
    {
        super(pGenerator);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables()
    {
        return lootTableGenerators;
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker)
    {
        final Set<ResourceLocation> modLootTableIds = LootTables
                .all()
                .stream()
                .filter(lootTable -> lootTable.getNamespace().equals(Reference.MOD_ID))
                .collect(Collectors.toSet());

        for (final ResourceLocation id : Sets.difference(modLootTableIds, map.keySet())) {
            validationtracker.reportProblem("Missing mod loot table: " + id);
        }

        map.forEach((id, lootTable) -> {
            LootTableManager.validate(validationtracker, id, lootTable);
        });
    }

    /**
     * Gets a name for this provider, to use in logging.
     */
    @Override
    public String getName()
    {
        return "mxTuneLootTables";
    }
}
