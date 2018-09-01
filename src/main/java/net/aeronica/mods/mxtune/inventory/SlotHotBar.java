package net.aeronica.mods.mxtune.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotHotBar extends Slot
{
    public SlotHotBar(IInventory inventory, int slotIndex, int xPos, int yPos)
    {
        super(inventory, slotIndex, xPos, yPos);
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
