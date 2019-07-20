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
package net.aeronica.mods.mxtune.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotHotBar extends net.minecraft.inventory.container.Slot
{
    SlotHotBar(IInventory inventory, int slotIndex, int xPos, int yPos)
    {
        super(inventory, slotIndex, xPos, yPos);
    }

    /**
     * Prevent the held item from being taken or moved
     * Useful to prevent and item from being placed into
     * it's own inventory and crashing the game
     */
    @Override
    public boolean canTakeStack(PlayerEntity playerIn)
    {
        ItemStack is = playerIn.getHeldItemMainhand();
        if (!is.isEmpty())
        {
            return (this.slotNumber - 1) != playerIn.inventory.currentItem;
        }
        return true;
    }
}
