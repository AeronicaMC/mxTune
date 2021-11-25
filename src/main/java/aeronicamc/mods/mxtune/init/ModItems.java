package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.MXTune;
import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.items.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

import static aeronicamc.mods.mxtune.util.SoundFontProxyManager.soundFontProxyMapByIndex;

/**
 *
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
public class ModItems
{
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);
    private static boolean isInitialized;

    public static final RegistryObject<MusicItem> MUSIC_ITEM = ITEMS.register("music_item",
        () -> new MusicItem(defaultItemProperties().stacksTo(1)));

    public static final RegistryObject<GuiTestItem> GUI_TEST_ITEM = ITEMS.register("gui_test_item",
        () -> new GuiTestItem(defaultItemProperties().stacksTo(1)));

    public static final RegistryObject<ItemMusicPaper> MUSIC_PAPER = ITEMS.register("music_paper",
        () -> new ItemMusicPaper(defaultItemProperties().stacksTo(16)));

    public static final RegistryObject<ItemSheetMusic> SHEET_MUSIC = ITEMS.register("sheet_music",
        () -> new ItemSheetMusic(defaultItemProperties().stacksTo(1)));

    public static final Map<Integer, RegistryObject<InstrumentItem>> INSTRUMENT_ITEMS = new HashMap<>();

    static { registerMultiInst(); }

    private static void registerMultiInst()
    {
        soundFontProxyMapByIndex.forEach(
            (key, value) -> INSTRUMENT_ITEMS.put(value.index, ITEMS.register(
                value.id, () -> new InstrumentItem(defaultItemProperties()
                    .stacksTo(1).setNoRepair().defaultDurability(value.index))
                                                                            )));
    }

    /**
     * Gets an {@link Item.Properties} instance with the {@link ItemGroup} set to {@link MXTune#ITEM_GROUP}.
     *
     * @return The item properties
     */
    private static Item.Properties defaultItemProperties() {
        return new Item.Properties().tab(MXTune.ITEM_GROUP);
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

        ITEMS.register(modEventBus);

        isInitialized = true;
    }
}
