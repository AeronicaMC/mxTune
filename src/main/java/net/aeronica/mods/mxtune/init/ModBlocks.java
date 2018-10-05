package net.aeronica.mods.mxtune.init;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.blocks.BlockBandAmp;
import net.aeronica.mods.mxtune.blocks.BlockPiano;
import net.aeronica.mods.mxtune.blocks.TileBandAmp;
import net.aeronica.mods.mxtune.blocks.TilePiano;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class ModBlocks
{
    public static final BlockPiano SPINET_PIANO = registerBlock(new BlockPiano(), "spinet_piano");
    public static final BlockBandAmp BAND_AMP = registerBlock(new BlockBandAmp(), "band_amp");

    private ModBlocks() {}
    
    @Mod.EventBusSubscriber
    public static class RegistrationHandler {
        protected static final Set<Item> ITEM_BLOCKS = new HashSet<>();
        private RegistrationHandler() {}
        
        /**
         * Register this mod's {@link Block}s.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            final IForgeRegistry<Block> registry = event.getRegistry();

            final Block[] blocks = {
                    SPINET_PIANO,
                    BAND_AMP,
            };

            registry.registerAll(blocks);
        }

        /**
         * Register this mod's {@link ItemBlock}s.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
            final ItemBlock[] items = {
            };

            final IForgeRegistry<Item> registry = event.getRegistry();

            for (final ItemBlock item : items) {
                registry.register(item.setRegistryName(item.getBlock().getRegistryName()));
                ITEM_BLOCKS.add(item);
            }
            
            registerTileEntities();
        }
    }
    
    private static void registerTileEntities() {
        registerTileEntity(TilePiano.class, "tile_piano");
        registerTileEntity(TileBandAmp.class, "tile_band_amp");
    }

    private static <T extends Block> T registerBlock(T block, String name) {
        block.setRegistryName(name.toLowerCase());
        block.setTranslationKey(block.getRegistryName().toString());
        return block;
    }

    private static <T extends Block> T registerBlock(T block) {
        return registerBlock(block, block.getClass().getSimpleName());
    }

    private static void registerTileEntity(Class<? extends TileEntity> tileEntityClass, String name) {
        GameRegistry.registerTileEntity(tileEntityClass, new ResourceLocation(MXTuneMain.MOD_ID, name));
    }
}
