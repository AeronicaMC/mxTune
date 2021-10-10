package aeronicamc.mods.mxtune.inventory;

import aeronicamc.mods.mxtune.util.IMusic;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotInstrument extends Slot
{
    public SlotInstrument(IInventory pContainer, int pIndex, int pX, int pY)
    {
        super(pContainer, pIndex, pX, pY);
    }

    @Override
    public boolean mayPlace(ItemStack pStack)
    {
        assert pStack.getTag() != null;
        return !pStack.isEmpty() && ((pStack.getItem() instanceof IMusic))
                /* && pStack.hasTag() && pStack.getTag().contains(KEY_SHEET_MUSIC)*/;
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }
}
