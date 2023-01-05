package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.util.IMusic;
import aeronicamc.mods.mxtune.util.MusicType;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;

import static aeronicamc.mods.mxtune.util.SheetMusicHelper.*;

public class MusicScoreItem extends Item implements IMusic
{
    private static final Logger LOGGER = LogManager.getLogger(MusicScoreItem.class);
    private final static ITextComponent SHIFT_PARTS_01 = new TranslationTextComponent("tooltip.mxtune.music_score.shift_parts_01");

    public MusicScoreItem(Properties pProperties)
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
        if (Screen.hasShiftDown())
        {
            pTooltip.addAll(SheetMusicHelper.getFormattedMusicScoreParts(pStack));
        }
        else
        {
            pTooltip.add(SHIFT_PARTS_01);
        }
    }

    @Override
    public void inventoryTick(ItemStack pStack, World pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected)
    {
        if (!pLevel.isClientSide())
        {
            SheetMusicHelper.scrapSheetMusicIfExpired(pStack, pLevel, pEntity, pItemSlot, pIsSelected);
        }
    }

    @Override
    public boolean hasMusicText(ItemStack pStack)
    {
        return SheetMusicHelper.hasMusicText(pStack);
    }

    @Override
    public MusicType getMusicType(ItemStack itemStackIn)
    {
        return MusicType.SCORE;
    }
}