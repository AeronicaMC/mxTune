package net.aeronica.mods.mxtune.init;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.items.ItemMusicPaper;
import net.aeronica.mods.mxtune.items.ItemPiano;
import net.aeronica.mods.mxtune.items.ItemSheetMusic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@SuppressWarnings("unused")
public class ModItems
{
    public static final ItemInstrument ITEM_INSTRUMENT = registerItem(new ItemInstrument(), "instrument");
    public static final ItemMusicPaper ITEM_MUSIC_PAPER = registerItem(new ItemMusicPaper(), "music_paper");
    public static final ItemSheetMusic ITEM_SHEET_MUSIC = registerItem(new ItemSheetMusic(), "sheet_music");
    public static final ItemPiano ITEM_SPINET_PIANO = registerItem(new ItemPiano(), "spinet_piano");
    public static final ItemBlock ITEM_BAND_AMP = (ItemBlock) registerItem((new ItemBlock(ModBlocks.BAND_AMP).setCreativeTab(MXTune.TAB_MUSIC)), "band_amp");
   
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
                    ITEM_SPINET_PIANO,
                    ITEM_BAND_AMP,
            };

            final IForgeRegistry<Item> registry = event.getRegistry();
            for (final Item item : items) {
                registry.register(item);
                ITEMS.add(item);
            }
        }
    }
        
    private static <T extends Item> T registerItem(T item, String name) {
        item.setRegistryName(name.toLowerCase(Locale.US));
        item.setTranslationKey(item.getRegistryName().toString());
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
