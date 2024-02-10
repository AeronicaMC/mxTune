package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.MXTune;
import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.items.*;
import aeronicamc.mods.mxtune.util.IInstrument;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

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
    private ModItems() { /* NOOP */ }

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);
    private static boolean isInitialized;

    public static final RegistryObject<Item> SCRAP_ITEM = ITEMS.register("scrap_item",
        () -> new Item(ingredientItemProperties().stacksTo(64)));

    public static final RegistryObject<Item> PLACARD_ITEM = ITEMS.register("placard_item",
        () -> new Item(ingredientItemProperties().stacksTo(1)));

    public static final RegistryObject<MusicVenueToolItem> MUSIC_VENUE_TOOL = ITEMS.register("music_venue_tool",
        () -> new MusicVenueToolItem(defaultItemProperties().stacksTo(1)));

    public static final RegistryObject<WrenchItem> WRENCH = ITEMS.register("wrench",
        () -> new WrenchItem(defaultItemProperties().stacksTo(1)));

    public static final RegistryObject<MusicPaperItem> MUSIC_PAPER = ITEMS.register("music_paper",
        () -> new MusicPaperItem(defaultItemProperties().stacksTo(16)));

    public static final RegistryObject<SheetMusicItem> SHEET_MUSIC = ITEMS.register("sheet_music",
        () -> new SheetMusicItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<MusicScoreItem> MUSIC_SCORE = ITEMS.register("music_score",
        () -> new MusicScoreItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<MusicVenueInfoItem> MUSIC_VENUE_INFO = ITEMS.register("music_venue_info",
        () -> new MusicVenueInfoItem(defaultItemProperties().stacksTo(4)));

    public static final RegistryObject<MultiInstItem> MULTI_INST = ITEMS.register("multi_inst",
        () -> new MultiInstItem(defaultItemProperties().stacksTo(1).setNoRepair()));

    /**
     * Gets an {@link Item.Properties} instance with the {@link ItemGroup} set to {@link MXTune#ITEM_GROUP}.
     *
     * @return The item properties
     */
    private static Item.Properties defaultItemProperties() {
        return new Item.Properties().tab(MXTune.ITEM_GROUP);
    }

    private static Item.Properties ingredientItemProperties()
    {
        return new Item.Properties();
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

    public static ItemStack getMultiInst(int index)
    {
        ItemStack stack = new ItemStack(MULTI_INST.get());
        ((IInstrument)stack.getItem()).setPatch(stack, index);
        return stack;
    }
}
