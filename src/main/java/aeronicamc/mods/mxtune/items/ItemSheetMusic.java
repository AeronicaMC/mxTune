package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.util.IMusic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemSheetMusic extends Item implements IMusic
{
    public ItemSheetMusic(Properties pProperties)
    {
        super(pProperties);
    }



    @Override
    public boolean hasMML(ItemStack itemStackIn)
    {
        return false;
    }
}
