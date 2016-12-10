/**
 * Aeronica's mxTune MOD
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

import net.aeronica.mods.mxtune.init.StartupItems;
import net.aeronica.mods.mxtune.items.ItemMusicPaper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerInstrument extends Container
{
    /** The Item Inventory for this Container */
    public final InventoryInstrument invInst;

    public ContainerInstrument(EntityPlayer player, InventoryPlayer inventoryIn, InventoryInstrument inventoryInst)
    {
        invInst = inventoryInst;

        /** ItemInventory */
        addSlotToContainer(new SlotInstrument(invInst, 0, 12, 8 + 2 * 18));

        int slotIndex = -1;
        //Player Hotbar
        for (int i = 0; i < 9; i++)
        {
            this.addSlotToContainer(new SlotHotBar(inventoryIn, ++slotIndex, 18 * i + 12, 142));
        }
        //Player Inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new Slot(inventoryIn, ++slotIndex, 18 * j + 12, 18 * i + 84));
            }
        }
        StartupItems.item_converter.convertIInventory(invInst, player.getHeldItemMainhand());
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        Slot slot = this.getSlot(slotIndex);
        
        if (slot == null || !slot.getHasStack())
        {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack newStack = stack.copy();

        if (!(stack.getItem() instanceof IMusic) && !((stack.getItem() instanceof ItemMusicPaper) && stack.hasDisplayName())) return ItemStack.EMPTY;

        if (slotIndex == 0)
        {
            if (!this.mergeItemStack(stack, 1, this.inventorySlots.size(), false))
                return ItemStack.EMPTY;
            slot.onSlotChanged();
        }
        else if (!this.mergeItemStack(stack, 0, 1, false))
        {
            return ItemStack.EMPTY;
        }
        if (stack.getCount() == 0)
        {
            slot.putStack(ItemStack.EMPTY);
        }
        else
        {
            slot.onSlotChanged();
        }

        slot.onSlotChanged();
        return newStack;
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer player) {return true;} 
    
    @Override
    public void onContainerClosed(EntityPlayer player) {super.onContainerClosed(player);}
}
