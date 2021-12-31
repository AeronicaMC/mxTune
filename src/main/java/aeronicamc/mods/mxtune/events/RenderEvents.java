package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModBlocks;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class RenderEvents
{
    @SubscribeEvent
    public static void event(ItemTooltipEvent event)
    {
        if (event.getItemStack().getItem().equals(ModBlocks.MUSIC_BLOCK.get().asItem()))
        {
            event.getToolTip().add(new TranslationTextComponent("tooltip.mxtune.block_music.help").withStyle(TextFormatting.YELLOW));
        }
    }
}
