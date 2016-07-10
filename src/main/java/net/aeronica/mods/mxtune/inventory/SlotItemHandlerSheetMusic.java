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
package net.aeronica.mods.mxtune.inventory;

import net.aeronica.mods.mxtune.items.ItemMusicPaper;
import net.aeronica.mods.mxtune.items.ItemSheetMusic;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotItemHandlerSheetMusic extends SlotItemHandler
{


    public SlotItemHandlerSheetMusic(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
    }

    /** Check if the stack is a Music Item. */
    @Override
    public boolean isItemValid(ItemStack stack)
    {
        
        return stack != null && ((stack.getItem() instanceof ItemSheetMusic) | (stack.getItem() instanceof ItemMusicPaper))
                && stack.hasTagCompound() && stack.getTagCompound().hasKey("MusicBook") ? true : false;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }
}
