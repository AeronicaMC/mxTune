package net.aeronica.mods.mxtune.inventory;

import net.aeronica.mods.mxtune.items.ItemMusicPaper;
import net.aeronica.mods.mxtune.items.ItemSheetMusic;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotInstrument extends Slot
{
    public SlotInstrument(IInventory inventory, int slotIndex, int xPos, int yPos)
    {
        super(inventory, slotIndex, xPos, yPos);
    }

    /** Check if the stack is a Music Item. */
    @Override
    public boolean isItemValid(ItemStack stack)
    {
        
        return !stack.isEmpty() && ((stack.getItem() instanceof ItemSheetMusic) || (stack.getItem() instanceof ItemMusicPaper))
                && stack.hasTagCompound() && stack.getTagCompound().hasKey("SheetMusic") ? true : false;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }
}
