package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.gui.Handler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class MusicPaperItem extends Item
{
    public MusicPaperItem(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand)
    {
        if (pLevel.isClientSide)
        {
            Handler.openSheetMusicScreen();
        }
        return ActionResult.pass(pPlayer.getItemInHand(pHand));
    }

    @Override
    public int getUseDuration(ItemStack pStack) // getMaxItemUseDuration
    {
        return 72000;
    }

    // Don't open gui when clicking on LivingEntity
    @Override
    public ActionResultType interactLivingEntity(ItemStack pStack, PlayerEntity pPlayer, LivingEntity pTarget, Hand pHand)
    {
        return ActionResultType.SUCCESS;
    }
}
