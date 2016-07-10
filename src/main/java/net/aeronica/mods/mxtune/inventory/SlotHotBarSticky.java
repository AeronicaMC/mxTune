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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotHotBarSticky extends Slot
{    
    public SlotHotBarSticky(IInventory inventoryIn, int index, int xPosition, int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    /**
     * Prevent the held item from being taken or moved
     * Useful to prevent and item from being placed into
     * it's own inventory and crashing the game
     */
    @Override
    public boolean canTakeStack(EntityPlayer playerIn)
    {
        ItemStack is = playerIn.getHeldItemMainhand();
        if (is != null)
        {
            if ((this.slotNumber - 1) == playerIn.inventory.currentItem) return false;
        }
        return true;
    }
}
