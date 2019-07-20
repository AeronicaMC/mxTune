package net.aeronica.mods.mxtune.inventory;

import net.aeronica.mods.mxtune.items.ItemMusicPaper;
import net.aeronica.mods.mxtune.items.ItemSheetMusic;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.Objects;

import static net.aeronica.mods.mxtune.util.SheetMusicUtil.KEY_SHEET_MUSIC;

public class SlotInstrument extends net.minecraft.inventory.container.Slot
{
    SlotInstrument(IInventory inventory, int slotIndex, int xPos, int yPos)
    {
        super(inventory, slotIndex, xPos, yPos);
    }

    /** Check if the stack is a Music Item. */
    @Override
    public boolean isItemValid(ItemStack stack)
    {
        
        return !stack.isEmpty() && ((stack.getItem() instanceof ItemSheetMusic) || (stack.getItem() instanceof ItemMusicPaper))
                && stack.hasTagCompound() && Objects.requireNonNull(stack.getTagCompound()).hasKey(KEY_SHEET_MUSIC);
    }

    @Override
    public int getSlotStackLimit() { return 1; }
}
