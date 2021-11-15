package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.config.MXTuneConfig;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static aeronicamc.mods.mxtune.util.SheetMusicHelper.getFormattedMusicDuration;
import static aeronicamc.mods.mxtune.util.SheetMusicHelper.getMusicTextKey;

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
            // TODO: Need a SheetMusicHelper method to getDaysLeft( itemStack )
            // FIXME: The days left calculation is incorrect.
            String keyDateTimeString = getMusicTextKey(pStack);
            assert keyDateTimeString != null;
            LocalDateTime keyDateTime = LocalDateTime.parse(keyDateTimeString);
            LocalDateTime keyPlusDaysLeft = keyDateTime.plusDays(MXTuneConfig.getSheetMusicLifeInDays());
            LocalDateTime now = LocalDateTime.now(ZoneId.of("GMT0"));
            long days = Duration.between(now, keyPlusDaysLeft).getSeconds() / 86400;
            pTooltip.clear();
            pTooltip.add(new StringTextComponent(itemName).withStyle(TextFormatting.GOLD));
            pTooltip.add(new StringTextComponent(String.format("Days left: %d", Math.max(days, 0))));
        }
        pTooltip.add(getFormattedMusicDuration(pStack));
    }

    @Override
    public boolean hasMusicText(ItemStack pStack)
    {
        return SheetMusicHelper.hasMusicText(pStack);
    }
}
