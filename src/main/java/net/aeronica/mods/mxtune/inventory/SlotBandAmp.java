package net.aeronica.mods.mxtune.inventory;

import net.aeronica.mods.mxtune.blocks.TileBandAmp;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotBandAmp extends SlotItemHandler
{
    private TileBandAmp tileBandAmp;

    public SlotBandAmp(IItemHandler itemHandler, TileBandAmp tileBandAmp, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
        this.tileBandAmp = tileBandAmp;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack)
    {
        return super.isItemValid(stack) && stack.getItem() instanceof ItemInstrument;
    }

    @Override
    public void onSlotChanged() {
        tileBandAmp.markDirty();
    }
}
