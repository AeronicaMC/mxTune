/*
 * Aeronica's mxTune MOD
 * Copyright 2020, Paul Boese a.k.a. Aeronica
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

package net.aeronica.mods.mxtune.init;

import net.aeronica.mods.mxtune.Reference;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModMigrationMapping
{
    @Mod.EventBusSubscriber
    public static class ReMap
    {

        @SubscribeEvent
        public static void missingItemMapping(RegistryEvent.MissingMappings<Item> event)
        {
            for (RegistryEvent.MissingMappings.Mapping<Item> missing : event.getMappings()) {
                if (missing.key.getNamespace().equals(Reference.MOD_ID) && missing.key.getPath().equals("instrument")) {
                    missing.remap(ModItems.ITEM_MULTI_INST);
                }
            }
        }
    }
}
