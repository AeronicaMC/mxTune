package aeronicamc.mods.mxtune.inventory;

import aeronicamc.mods.mxtune.blocks.MusicBlockTile;
import aeronicamc.mods.mxtune.util.IInstrument;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotMusicBlock extends SlotItemHandler
{
    private MusicBlockTile musicBlockTile;

    public SlotMusicBlock(IItemHandler itemHandler, MusicBlockTile musicBlockTile, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
        this.musicBlockTile = musicBlockTile;
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return super.mayPlace(stack) && (stack.getItem() instanceof IInstrument);
    }

    @Override
    public void setChanged() {
        musicBlockTile.setChanged();
    }
}
