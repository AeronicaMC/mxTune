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
package net.aeronica.mods.mxtune.items;

import net.minecraft.item.Item;

/**
 * A base class for this mod's items.
 *
 * @author Choonster
 */
public class ItemBase extends Item
{
    public ItemBase(String itemName) {setItemName(this, itemName);}

    /**
     * Set the registry name of {@code item} to {@code itemName} and the
     * unlocalised name to the full registry name.<br>
     * <br>
     * The items .lang file identifier will be in the format of:<br>
     * item.modid:mod_item_name.name=Localized Name
     *
     * @param item
     *            The item
     * @param itemName
     *            The item's name
     */
    public static void setItemName(Item item, String itemName)
    {
        item.setRegistryName(itemName);
        item.setUnlocalizedName(item.getRegistryName().toString());
    }

}
