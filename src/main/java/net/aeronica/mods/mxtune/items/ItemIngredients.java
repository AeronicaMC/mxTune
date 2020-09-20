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

package net.aeronica.mods.mxtune.items;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.util.IVariant;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemIngredients extends Item
{
    public ItemIngredients()
    {
        setHasSubtypes(true);
        setMaxStackSize(64);
        setCreativeTab(MXTune.TAB_MUSIC);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + "." + EnumType.byMetadata(stack.getMetadata()).getName();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        if (isInCreativeTab(tab)) {
            final List<ItemStack> items = Stream.of(EnumType.values())
                    .map(enumType -> new ItemStack(this, 1, enumType.getMeta()))
                    .collect(Collectors.toList());

            subItems.addAll(items);
        }
    }

    public enum EnumType implements IVariant
    {
        GOLD_BONE(0, "gold_bone"),
        TIMPANIC_MEMBRANE(1, "timpanic_membrane"),
        ;

        private final int meta;
        private final String name;
        private static final EnumType[] META_LOOKUP = new EnumType[values().length];

        EnumType(int metaIn, String nameIn)
        {
            this.meta = metaIn;
            this.name = nameIn;
        }

        static
        {
            for (EnumType value : values())
            {
                META_LOOKUP[value.getMeta()] = value;
            }
        }

        public static EnumType byMetadata(int metaIn)
        {
            int metaLocal = metaIn;
            if (metaLocal < 0 || metaLocal >= META_LOOKUP.length) {metaLocal = 0;}
            return META_LOOKUP[metaLocal];
        }

        @Override
        public int getMeta()
        {
            return meta;
        }

        @Override
        public String getName()
        {
            return name;
        }
    }
}
