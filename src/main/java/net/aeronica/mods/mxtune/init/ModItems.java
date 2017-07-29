package net.aeronica.mods.mxtune.init;

import java.util.HashSet;
import java.util.Set;

import net.aeronica.mods.mxtune.blocks.ItemPiano;
import net.aeronica.mods.mxtune.items.ItemConverter;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.items.ItemMusicPaper;
import net.aeronica.mods.mxtune.items.ItemSheetMusic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@SuppressWarnings("unused")
public class ModItems
{
    
    public static final ItemInstrument ITEM_INSTRUMENT = registerItem(new ItemInstrument(), "item_inst");
    public static final ItemMusicPaper ITEM_MUSIC_PAPER = registerItem(new ItemMusicPaper(), "item_musicpaper");
    public static final ItemSheetMusic ITEM_SHEET_MUSIC = registerItem(new ItemSheetMusic(), "item_sheetmusic");
    public static final ItemConverter ITEM_CONVERTER = registerItem(new ItemConverter(), "item_converter");
    public static final ItemPiano ITEM_PIANO = registerItem(new ItemPiano(), "block_piano");
   
    @Mod.EventBusSubscriber
    public static class RegistrationHandler {
        protected static final Set<Item> ITEMS = new HashSet<>();

        /**
         * Register this mod's {@link Item}s.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            final Item[] items = {
                    ITEM_INSTRUMENT,
                    ITEM_MUSIC_PAPER,
                    ITEM_SHEET_MUSIC,
                    ITEM_CONVERTER,
                    ITEM_PIANO,
            };

            final IForgeRegistry<Item> registry = event.getRegistry();
            for (final Item item : items) {
                registry.register(item);
                ITEMS.add(item);
            }
        }
    }
        
    private static <T extends Item> T registerItem(T item, String name) {
        item.setRegistryName(name.toLowerCase());
        item.setUnlocalizedName(item.getRegistryName().toString());
        return item;
    }

    private static <T extends Item> T registerItem(T item) {
        String simpleName = item.getClass().getSimpleName();
        if (item instanceof ItemBlock) {
            simpleName = ((ItemBlock) item).getBlock().getClass().getSimpleName();
        }
        return registerItem(item, simpleName);
    }

}
