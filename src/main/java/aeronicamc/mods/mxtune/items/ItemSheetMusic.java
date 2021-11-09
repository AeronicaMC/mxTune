package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.util.IMusic;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static aeronicamc.mods.mxtune.util.SheetMusicHelper.getFormattedMusicDuration;

public class ItemSheetMusic extends Item implements IMusic
{
    public ItemSheetMusic(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag)
    {
        if (hasMusicText(pStack))
        {
            String itemName = pTooltip.get(0).getString();
            pTooltip.clear();
            pTooltip.add(new StringTextComponent(itemName).withStyle(TextFormatting.GOLD));
        }
        pTooltip.add(getFormattedMusicDuration(pStack));
    }

    @Override
    public boolean hasMusicText(ItemStack pStack)
    {
        return SheetMusicHelper.hasMusicText(pStack);
    }
}
