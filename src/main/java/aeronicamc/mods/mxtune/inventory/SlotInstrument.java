package aeronicamc.mods.mxtune.inventory;

import aeronicamc.mods.mxtune.util.IMusic;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
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
        return (pStack.getItem() instanceof IMusic) && SheetMusicHelper.hasMusicText(pStack);
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }
}
