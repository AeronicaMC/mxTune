package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.caps.venues.MusicVenueProvider;
import aeronicamc.mods.mxtune.caps.venues.MusicVenueTool;
import aeronicamc.mods.mxtune.caps.venues.ToolManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class MusicVenueToolItem extends Item
{
    private final static ITextComponent SHIFT_HELP = new TranslationTextComponent("tooltip.mxtune.instrument_item.shift_help_01");
    private static final ITextComponent HELP_01 = new TranslationTextComponent("tooltip.mxtune.music_venue_tool_block.help_01").withStyle(TextFormatting.YELLOW);
    private final static ITextComponent SHIFT_HELP_02 = new TranslationTextComponent("tooltip.mxtune.music_venue_tool_item.shift_help_02").withStyle(TextFormatting.AQUA);
    private final static ITextComponent SHIFT_HELP_03 = new TranslationTextComponent("tooltip.mxtune.music_venue_tool_item.shift_help_03").withStyle(TextFormatting.YELLOW);
    private final static ITextComponent SHIFT_HELP_04 = new TranslationTextComponent("tooltip.mxtune.music_venue_tool_item.shift_help_04").withStyle(TextFormatting.GREEN);
    private final static ITextComponent SHIFT_HELP_05 = new TranslationTextComponent("tooltip.mxtune.music_venue_tool_item.shift_help_05").withStyle(TextFormatting.YELLOW);
    private final static ITextComponent SHIFT_HELP_06 = new TranslationTextComponent("tooltip.mxtune.music_venue_tool_item.shift_help_06").withStyle(TextFormatting.GREEN);

    public MusicVenueToolItem(Properties properties)
    {
        super(properties);
    }

    /**
     * This is called when the item is used, before the block is activated.
     * @return Return PASS to allow vanilla handling, any other to skip normal code.
     */
    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context)
    {
        if (!context.getHand().equals(Hand.MAIN_HAND))
            return super.onItemUseFirst(stack, context);

        getPlayer(context).filter(p -> !p.level.isClientSide()).ifPresent(player -> {
            MusicVenueProvider.getMusicVenues(context.getLevel()).ifPresent(mvp -> {
            if (!player.isShiftKeyDown())
                ToolManager.apply(context);
            else
                ToolManager.reset(player);
            });
        });

        return ActionResultType.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack pStack, World pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected)
    {
        MusicVenueTool tool = ToolManager.getTool((PlayerEntity) pEntity);
        if (tool != null && tool.getSlot() == pItemSlot)
        {
            tool.setSlot(pItemSlot);
            ToolManager.sync((PlayerEntity) pEntity);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag)
    {
        if (Screen.hasShiftDown())
        {
            pTooltip.add(SHIFT_HELP_02);
            pTooltip.add(SHIFT_HELP_03);
            pTooltip.add(SHIFT_HELP_04);
            pTooltip.add(SHIFT_HELP_05);
            pTooltip.add(SHIFT_HELP_06);
        }
        else
        {
            pTooltip.add(SHIFT_HELP);
            pTooltip.add(HELP_01);
        }
    }

    private Optional<PlayerEntity> getPlayer(ItemUseContext context)
    {
        return Optional.ofNullable(context.getPlayer());
    }

    @Override
    public ActionResultType useOn(ItemUseContext context)
    {
        return super.useOn(context);
    }
}
