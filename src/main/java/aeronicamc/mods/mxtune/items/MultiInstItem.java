package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.MXTune;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.inventory.InstrumentContainer;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.managers.PlayManager;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class MultiInstItem extends Item implements IInstrument, INamedContainerProvider
{
    private final static String KEY_PLAY_ID = "MXTunePlayId";
    private final static ITextComponent SHIFT_HELP_01 = new TranslationTextComponent("tooltip.mxtune.instrument_item.shift_help_01");
    private final static ITextComponent SHIFT_HELP_02 = new TranslationTextComponent("tooltip.mxtune.instrument_item.shift_help_02");
    private final static ITextComponent SHIFT_HELP_03 = new TranslationTextComponent("tooltip.mxtune.instrument_item.shift_help_03");

    public MultiInstItem(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand)
    {
        if (!pLevel.isClientSide())
        {
            ItemStack itemStackIn = pPlayer.getItemInHand(pHand);
            int playId = getPlayId(itemStackIn);
            if (pPlayer.isCrouching() && pHand.equals(Hand.MAIN_HAND))
            {
                NetworkHooks.openGui((ServerPlayerEntity) pPlayer, this, pPlayer.blockPosition());
            }
            else if (!pPlayer.isCrouching() && pHand.equals(Hand.MAIN_HAND))
            {
                if ((playId <= 0) || !PlayManager.isActivePlayId(playId))
                {
                    setPlayId(itemStackIn, PlayManager.playMusic(pPlayer));
                }
            }
        }
        return ActionResult.pass(pPlayer.getItemInHand(pHand));
    }

    /**
     * Get this stack's playId, or INVALID (-1) if no playId is defined.
     */
    private int getPlayId(ItemStack pStack) {
        return pStack.hasTag() && pStack.getTag() != null && pStack.getTag().contains(KEY_PLAY_ID, Constants.NBT.TAG_INT) ? pStack.getTag().getInt(KEY_PLAY_ID) : PlayIdSupplier.PlayType.INVALID.getAsInt();
    }

    /**
     * Set this stack's playId.
     */
    private void setPlayId(ItemStack pStack, int pCost) {
        pStack.getOrCreateTag().putInt(KEY_PLAY_ID, pCost);
    }

    /**
     * Get this stack's patch, or 0 if no patch is defined.
     */
    @Override
    public int getPatch(ItemStack pStack) {
        return pStack.getDamageValue();
    }

    // Stop playing if active and the item is no longer selected.
    @Override
    public void inventoryTick(ItemStack pStack, World pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected)
    {
        if (!pLevel.isClientSide())
        {
            int playId = getPlayId(pStack);
            if (!pIsSelected && PlayManager.isActivePlayId(playId))
            {
                PlayManager.stopPlayId(playId);
                setPlayId(pStack, PlayIdSupplier.PlayType.INVALID.getAsInt());
            }
            // Check for expired sheet music. Deposit scrap in the players inventory if space permits or in the world.
            SheetMusicHelper.scrapSheetMusicInInstrumentIfExpired(null, pStack, pLevel, pEntity, null);
        }
        super.inventoryTick(pStack, pLevel, pEntity, pItemSlot, pIsSelected);
    }

    // Stop playing if dropped
    @Override
    public boolean onDroppedByPlayer(ItemStack pStack, PlayerEntity pPlayer)
    {
        if (!pPlayer.level.isClientSide())
        {
            int playId = getPlayId(pStack);
            if (PlayManager.isActivePlayId(playId))
            {
                PlayManager.stopPlayId(playId);
                setPlayId(pStack, PlayIdSupplier.PlayType.INVALID.getAsInt());
            }
        }
        return true;
    }

    // Stop playing when moved from inventory into the world
    @Override
    public int getEntityLifespan(ItemStack pStack, World pLevel)
    {
        if (!pLevel.isClientSide())
        {
            int playId = getPlayId(pStack);
            if (PlayManager.isActivePlayId(playId))
            {
                PlayManager.stopPlayId(playId);
                setPlayId(pStack, PlayIdSupplier.PlayType.INVALID.getAsInt());
            }
        }
        return super.getEntityLifespan(pStack, pLevel);
    }

    @Override
    public void onCraftedBy(ItemStack pStack, World pLevel, PlayerEntity pPlayer)
    {
        setPlayId(pStack, PlayIdSupplier.PlayType.INVALID.getAsInt());
    }

    @Override
    public void fillItemCategory(ItemGroup pGroup, NonNullList<ItemStack> pItems) // getSubItems
    {
        if (pGroup.equals(MXTune.ITEM_GROUP))
            SoundFontProxyManager.soundFontProxyMapByIndex.forEach( (index, proxy)-> {
                ItemStack itemStack = new ItemStack(ModItems.MULTI_INST.get());
                itemStack.setDamageValue(index);
                pItems.add(itemStack);
            });
    }

    @Override
    public String getDescriptionId(ItemStack pStack)
    {
        return SoundFontProxyManager.getLangKeyName(pStack.getDamageValue());
    }

    @Override
    public int getUseDuration(ItemStack pStack)
    {
        return 72000;
    }

    public UseAction getUseAnimation(ItemStack pStack) {
        return UseAction.NONE;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity)
    {
        return true;
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack pStack, PlayerEntity pPlayer, LivingEntity pTarget, Hand pHand)
    {
        return ActionResultType.PASS;
    }

    // Prevent the item from activating [this.use(...)] when clicking a block with a TileEntity
    @Override
    public ActionResultType useOn(ItemUseContext pContext)
    {
        TileEntity tileEntity = pContext.getLevel().getBlockEntity(pContext.getClickedPos());

        if (tileEntity != null && tileEntity.getBlockState().hasTileEntity())
            return ActionResultType.SUCCESS;

        return super.useOn(pContext);
    }

    @Override
    public ITextComponent getName(ItemStack pStack)
    {
        return new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(pStack.getDamageValue()));
    }

    /**
     * Allow the item one last chance to modify its name used for the tool highlight
     * useful for adding something extra that can't be removed by a user in the
     * displayed name, such as a mode of operation.
     *
     * @param item        the ItemStack for the item.
     * @param displayName the name that will be displayed unless it is changed in
     */
    @Override
    public ITextComponent getHighlightTip(ItemStack item, ITextComponent displayName)
    {
        return new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(item.getDamageValue()));
    }

    /**
     * Container Title
     * @return the translated container title.
     */
    @SuppressWarnings("deprecation")
    @Override
    public ITextComponent getDisplayName()
    {
        return new StringTextComponent("TODO: Replace from ItemStack damage value name lookup");
    }


    @Override
    public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag)
    {
        ItemStack iMusic = SheetMusicHelper.getIMusicFromIInstrument(pStack);
        pTooltip.add(SheetMusicHelper.getFormattedMusicTitle(iMusic));
        if (SheetMusicHelper.hasMusicText(SheetMusicHelper.getIMusicFromIInstrument(pStack)))
        {
            pTooltip.add(SheetMusicHelper.getFormattedMusicDuration(iMusic));
            pTooltip.add(SheetMusicHelper.getFormattedSheetMusicDaysLeft(iMusic));
        }
        if (Screen.hasShiftDown())
        {
            pTooltip.add(SHIFT_HELP_02);
            pTooltip.add(SHIFT_HELP_03);
        }
        else
        {
            pTooltip.add(SHIFT_HELP_01);
        }
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        if (playerEntity.level == null) return null;
        return new InstrumentContainer(i, playerEntity.level, playerEntity.blockPosition(), playerInventory, playerEntity);
    }
}
