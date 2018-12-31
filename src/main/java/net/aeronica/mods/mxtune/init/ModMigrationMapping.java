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

import net.aeronica.mods.mxtune.Reference;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModMigrationMapping
{
    private ModMigrationMapping() { /* NOP */ }

    @Mod.EventBusSubscriber
    public static class ReMap
    {
        private ReMap() { /* NOP */ }

        @SubscribeEvent
        public static void missingBlockMapping(RegistryEvent.MissingMappings<Block> event)
        {
            for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getMappings()) {
                if(mapping.key.equals(new ResourceLocation(Reference.MOD_ID, "block_piano"))) {
                    mapping.remap(ModBlocks.SPINET_PIANO);
                }
            }
        }
        
        @SubscribeEvent
        public static void missingItemMapping(RegistryEvent.MissingMappings<Item> event)
        {
            for (RegistryEvent.MissingMappings.Mapping<Item> missing : event.getMappings()) {
                if (missing.key.getNamespace().equals(Reference.MOD_ID) && missing.key.getPath().equals("block_piano")) {
                    missing.remap(ModItems.ITEM_SPINET_PIANO);
                } else if(missing.key.getNamespace().equals(Reference.MOD_ID) && missing.key.getPath().equals("item_inst")){
                    missing.remap(ModItems.ITEM_INSTRUMENT);
                } else if(missing.key.getNamespace().equals(Reference.MOD_ID) && missing.key.getPath().equals("item_musicpaper")){
                    missing.remap(ModItems.ITEM_MUSIC_PAPER);
                } else if(missing.key.getNamespace().equals(Reference.MOD_ID) && missing.key.getPath().equals("item_sheetmusic")){
                    missing.remap(ModItems.ITEM_SHEET_MUSIC);
                } else if(missing.key.getNamespace().equals(Reference.MOD_ID) && missing.key.getPath().equals("item_converter")){
                    missing.ignore();
                }
            }            
        }
    }
}
