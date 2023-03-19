package aeronicamc.mods.mxtune.datagen.loot;

import aeronicamc.mods.mxtune.init.ModBlocks;
import aeronicamc.mods.mxtune.util.RegistryUtil;
import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraftforge.registries.ForgeRegistries;

public class MXTuneBlockLootTables extends BlockLootTables
{
    @Override
    protected void addTables()
    {
        // TODO: populate as needed
        dropSelf(ModBlocks.MUSIC_VENUE_TOOL_BLOCK.get());
        add(ModBlocks.MUSIC_BLOCK.get(), MXTuneBlockLootTables::notDroppingRollsZERO);
    }

    @Override
    protected Iterable<Block> getKnownBlocks()
    {
        return RegistryUtil.getModRegistryEntries(ForgeRegistries.BLOCKS);
    }

    /**
     * Rolls set to zero for no drop! Block must handle drop in onBlockHarvested method
     * @param block target
     * @return lootTable for this target
     */
    protected static LootTable.Builder notDroppingRollsZERO(final Block block)
    {
        return LootTable.lootTable()
                .withPool(applyExplosionCondition(block, LootPool.lootPool()
                                                          .setRolls(ConstantRange.exactly(0))
                                                          .add(ItemLootEntry.lootTableItem(block))
                                                 ));
    }

}
