package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.inventory.InstrumentContainer;
import aeronicamc.mods.mxtune.util.IInstrument;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMultiInst extends Item implements IInstrument, INamedContainerProvider
{
    public ItemMultiInst(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand)
    {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!pLevel.isClientSide)
        {
            if (pPlayer.isCrouching() && pHand.equals(Hand.MAIN_HAND))
            {
                NetworkHooks.openGui((ServerPlayerEntity) pPlayer, this, pPlayer.blockPosition());
            }
        }
        return ActionResult.pass(pPlayer.getItemInHand(pHand));
    }

    @Override
    public void fillItemCategory(ItemGroup pGroup, NonNullList<ItemStack> pItems) // getSubItems
    {
        super.fillItemCategory(pGroup, pItems);
    }

    @Override
    public String getDescriptionId(ItemStack pStack) // getTranslationKey
    {
        return super.getDescriptionId(pStack); // "." + SoundFontProxyManager.getName(pStack.getItemDamage());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag)
    {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }

    @Override
    public int getUseDuration(ItemStack pStack) // getMaxItemUseDuration
    {
        return 72000;
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack pStack, PlayerEntity pPlayer, LivingEntity pTarget, Hand pHand)
    {
        return ActionResultType.SUCCESS;
    }

    @Override
    public int getPatch(ItemStack itemStack)
    {
        return itemStack.getDamageValue();
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new StringTextComponent("What's dis?");
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        if (playerEntity.level == null) return null;
        return new InstrumentContainer(i, playerEntity.level, playerEntity.blockPosition(), playerInventory, playerEntity);
    }
}
