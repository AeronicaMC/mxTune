package aeronicamc.mods.mxtune.inventory;

import aeronicamc.mods.mxtune.util.IChangedCallBack;
import aeronicamc.mods.mxtune.util.IMusic;
import aeronicamc.mods.mxtune.util.MusicType;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotInstrument extends Slot
{
    protected final IChangedCallBack callback;

    public SlotInstrument(IInventory pContainer, int pIndex, int pX, int pY, IChangedCallBack callback)
    {
        super(pContainer, pIndex, pX, pY);
        this.callback = callback;
    }

    public SlotInstrument(IInventory pContainer, int pIndex, int pX, int pY)
    {
        super(pContainer, pIndex, pX, pY);
        this.callback = null;
    }

    @Override
    public boolean mayPlace(ItemStack pStack)
    {
        return (pStack.getItem() instanceof IMusic) && ((IMusic) pStack.getItem()).getMusicType(pStack) == MusicType.PART && SheetMusicHelper.hasMusicText(pStack);
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }

    /**
     * Called when the stack in a Slot changes
     */
    @Override
    public void setChanged()
    {
        super.setChanged();
        if (callback != null)
            callback.onChangedCallback();
    }
}
