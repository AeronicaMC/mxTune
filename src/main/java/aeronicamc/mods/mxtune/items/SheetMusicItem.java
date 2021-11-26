package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.util.IMusic;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static aeronicamc.mods.mxtune.util.SheetMusicHelper.*;

public class SheetMusicItem extends Item implements IMusic
{
    public SheetMusicItem(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag)
    {
        if (hasMusicText(pStack))
        {
            pTooltip.clear();
            pTooltip.add(getFormattedMusicTitle(pStack));
        }
        pTooltip.add(getFormattedMusicDuration(pStack));
        pTooltip.add(getFormattedSheetMusicDaysLeft(pStack));
    }

    @Override
    public boolean hasMusicText(ItemStack pStack)
    {
        return SheetMusicHelper.hasMusicText(pStack);
    }
}
