package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.util.IMusic;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static aeronicamc.mods.mxtune.Reference.*;
import static aeronicamc.mods.mxtune.util.SheetMusicHelper.formatDuration;

public class ItemSheetMusic extends Item implements IMusic
{
    public ItemSheetMusic(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag)
    {
        CompoundNBT contents = pStack.getTag();
        if (contents != null && contents.contains(KEY_SHEET_MUSIC))
        {
            CompoundNBT sm = contents.getCompound(KEY_SHEET_MUSIC);
            if (sm.getString(KEY_MML).contains("MML@") && sm.getInt(KEY_DURATION) > 0)
            {
                pTooltip.add(new TranslationTextComponent("item.mxtune.sheet_music.duration", formatDuration(sm.getInt(KEY_DURATION))));
            }
        }
    }

    @Override
    public boolean hasMML(ItemStack pStack)
    {
        CompoundNBT contents = pStack.getTag();
        if (contents != null && contents.contains(KEY_SHEET_MUSIC))
        {
            CompoundNBT sm = contents.getCompound(KEY_SHEET_MUSIC);
            return sm.getString(KEY_MML).contains("MML@") && sm.getInt(KEY_DURATION) > 0;
        }
        return false;
    }
}
