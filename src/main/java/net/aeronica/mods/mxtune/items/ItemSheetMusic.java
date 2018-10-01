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
package net.aeronica.mods.mxtune.items;

import net.aeronica.mods.mxtune.inventory.IMusic;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSheetMusic extends Item implements IMusic
{

    public ItemSheetMusic()
    {
        this.setMaxStackSize(1);
    }

    @Override
    public boolean hasMML(ItemStack itemStackIn)
    {
        return true;
    }

    @Override
    public void addInformation(ItemStack stackIn, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        if (stackIn.hasTagCompound())
        {
            NBTTagCompound contents = stackIn.getTagCompound();
            if (contents != null && contents.hasKey("MusicBook"))
            {
                NBTTagCompound mml = contents.getCompoundTag("MusicBook");
                if (mml.getString("MML").contains("MML@"))
                    tooltip.add(TextFormatting.GREEN + mml.getString("MML").substring(0, mml.getString("MML").length() > 25 ? 25 : mml.getString("MML").length()));
                else
                    tooltip.add(TextFormatting.RED + I18n.format("item.mxtune:sheet_music.help")); 
            }
        }
    }
}
