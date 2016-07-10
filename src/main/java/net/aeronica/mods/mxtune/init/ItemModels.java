/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
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

import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public class ItemModels
{

    private static final int DEFAULT_ITEM_SUBTYPE = 0;

    public static void register()
    {
        simpleItemModelRegister(StartupItems.item_basic);
        simpleItemModelRegister(StartupItems.item_musicpaper);
        simpleItemModelRegister(StartupItems.item_sheetmusic);
        simpleItemModelRegister(StartupItems.item_converter);
        varientItemRegister();
    }

    private static void simpleItemModelRegister(Item item)
    {
        ModelLoader.setCustomModelResourceLocation(item, DEFAULT_ITEM_SUBTYPE, new ModelResourceLocation(new ResourceLocation(item.getRegistryName().toString()), "inventory"));
    }

    private static void varientItemRegister()
    {
        for (ItemInstrument.EnumInstruments inst : ItemInstrument.EnumInstruments.values())
        {
            ModelLoader.setCustomModelResourceLocation(StartupItems.item_instrument, inst.getMetadata(),
                    new ModelResourceLocation(new ResourceLocation(StartupItems.item_instrument.getRegistryName().toString() + "_" + inst.getName()), "inventory"));
        }
    }
}
