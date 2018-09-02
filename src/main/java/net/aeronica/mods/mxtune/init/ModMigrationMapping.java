/**
 * Copyright {2016} Paul Boese aka Aeronica
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.init;

import net.aeronica.mods.mxtune.MXTuneMain;
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
        @SubscribeEvent
        public static void missingBlockMapping(RegistryEvent.MissingMappings<Block> event)
        {
            for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getMappings()) {
                if(mapping.key.equals(new ResourceLocation(MXTuneMain.prependModID("block_piano")))) {
                    mapping.remap(ModBlocks.SPINET_PIANO);
                }
            }
        }
        
        @SubscribeEvent
        public static void missingItemMapping(RegistryEvent.MissingMappings<Item> event)
        {
            for (RegistryEvent.MissingMappings.Mapping<Item> missing : event.getMappings()) {
                if (missing.key.getNamespace().equals(MXTuneMain.MODID) && missing.key.getPath().equals("block_piano")) {
                    missing.remap(ModItems.ITEM_SPINET_PIANO);
                } else if(missing.key.getNamespace().equals(MXTuneMain.MODID) && missing.key.getPath().equals("item_inst")){
                    missing.remap(ModItems.ITEM_INSTRUMENT);
                } else if(missing.key.getNamespace().equals(MXTuneMain.MODID) && missing.key.getPath().equals("item_musicpaper")){
                    missing.remap(ModItems.ITEM_MUSIC_PAPER);
                } else if(missing.key.getNamespace().equals(MXTuneMain.MODID) && missing.key.getPath().equals("item_sheetmusic")){
                    missing.remap(ModItems.ITEM_SHEET_MUSIC);
                } else if(missing.key.getNamespace().equals(MXTuneMain.MODID) && missing.key.getPath().equals("item_converter")){
                    missing.ignore();
                }
            }            
        }
    }
}
