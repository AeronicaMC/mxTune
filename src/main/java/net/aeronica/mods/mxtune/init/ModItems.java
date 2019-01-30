/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

//The MIT License (MIT)
//
//        Test Mod 3 - Copyright (c) 2015-2017 Choonster
//
//        Permission is hereby granted, free of charge, to any person obtaining a copy
//        of this software and associated documentation files (the "Software"), to deal
//        in the Software without restriction, including without limitation the rights
//        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//        copies of the Software, and to permit persons to whom the Software is
//        furnished to do so, subject to the following conditions:
//
//        The above copyright notice and this permission notice shall be included in all
//        copies or substantial portions of the Software.
//
//        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//        SOFTWARE.
package net.aeronica.mods.mxtune.init;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.items.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class ModItems
{
    public static final ItemInstrument ITEM_INSTRUMENT = registerItem(new ItemInstrument(), "instrument");
    public static final ItemMusicPaper ITEM_MUSIC_PAPER = registerItem(new ItemMusicPaper(), "music_paper");
    public static final ItemSheetMusic ITEM_SHEET_MUSIC = registerItem(new ItemSheetMusic(), "sheet_music");
    public static final ItemPiano ITEM_SPINET_PIANO = registerItem(new ItemPiano(), "spinet_piano");
    public static final ItemBlock ITEM_BAND_AMP = (ItemBlock) registerItem((new ItemBlock(ModBlocks.BAND_AMP).setCreativeTab(MXTune.TAB_MUSIC)), "band_amp");
    public static final Item ITEM_PLACE_HOLDER = registerItem(new Item(), "place_holder");
    public static final ItemStaffOfMusic ITEM_STAFF_OF_MUSIC = registerItem(new ItemStaffOfMusic(), "staff_of_music");

    private ModItems() { /* NOP */ }

    @Mod.EventBusSubscriber
    public static class RegistrationHandler
    {
        static final Set<Item> ITEMS = new HashSet<>();

        private RegistrationHandler() { /* NOP */ }

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
                    ITEM_PLACE_HOLDER,
                    ITEM_STAFF_OF_MUSIC
            };

            final IForgeRegistry<Item> registry = event.getRegistry();
            for (final Item item : items) {
                registry.register(item);
                ITEMS.add(item);
            }
        }
    }
        
    private static <T extends Item> T registerItem(T item, String name)
    {
        item.setRegistryName(name.toLowerCase(Locale.US));
        item.setTranslationKey(Objects.requireNonNull(item.getRegistryName()).toString());
        return item;
    }

    @SuppressWarnings("unused")
    private static <T extends Item> T registerItem(T item)
    {
        String simpleName = item.getClass().getSimpleName();
        if (item instanceof ItemBlock) {
            simpleName = ((ItemBlock) item).getBlock().getClass().getSimpleName();
        }
        return registerItem(item, simpleName);
    }
}
