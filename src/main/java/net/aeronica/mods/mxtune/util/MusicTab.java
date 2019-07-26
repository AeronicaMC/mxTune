/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
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

package net.aeronica.mods.mxtune.util;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.init.ModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MusicTab extends ItemGroup
{
    public MusicTab(String name) {super(name);}

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getTranslationKey()
    {
        return Reference.MOD_NAME;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack getIcon() {return new ItemStack(ModItems.ITEM_SHEET_MUSIC);}

    @Override
    public ItemStack createIcon() {return ItemStack.EMPTY;}
}
