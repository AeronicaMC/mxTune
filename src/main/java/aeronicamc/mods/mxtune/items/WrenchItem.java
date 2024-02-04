package aeronicamc.mods.mxtune.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class WrenchItem extends Item
{
    private final static ITextComponent SHIFT_HELP_01 = new TranslationTextComponent("tooltip.mxtune.wrench_item.shift_help_01").withStyle(TextFormatting.YELLOW);
    public WrenchItem(Properties properties)
    {
        super(properties);
    }
    private final static ITextComponent SHIFT_HELP_02 = new TranslationTextComponent("tooltip.mxtune.wrench_item.shift_help_02").withStyle(TextFormatting.GREEN);
    private final static ITextComponent SHIFT_HELP_03 = new TranslationTextComponent("tooltip.mxtune.wrench_item.shift_help_03").withStyle(TextFormatting.AQUA);

    @Nonnull
    @Override
    public ActionResult<ItemStack> use(@Nonnull World worldIn, @Nonnull PlayerEntity playerIn, @Nonnull Hand handIn)
    {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        return ActionResult.pass(itemstack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag)
    {
        pTooltip.add(SHIFT_HELP_01);
        pTooltip.add(SHIFT_HELP_02);
        pTooltip.add(SHIFT_HELP_03);
    }

    @Override
    public boolean is(ITag<Item> itemITag)
    {
        return super.is(itemITag);
    }

    // For a wrench where we want to use SHIFT-Right-Click to pick up a block this needs to return true.
    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player)
    {
        return true;
    }
}
