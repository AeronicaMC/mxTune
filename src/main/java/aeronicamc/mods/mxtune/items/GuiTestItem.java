package aeronicamc.mods.mxtune.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class GuiTestItem extends Item
{
    private static final Logger LOGGER = LogManager.getLogger();
    public GuiTestItem(Properties properties)
    {
        super(properties);
    }

    /**
     * This is called when the item is used, before the block is activated.
     *
     * @param stack
     * @param context
     * @return Return PASS to allow vanilla handling, any other to skip normal code.
     */
    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context)
    {
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> use(@Nonnull World worldIn, @Nonnull PlayerEntity playerIn, @Nonnull Hand handIn)
    {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
//        if (!worldIn.isClientSide)
//        {
//            if (!playerIn.isShiftKeyDown())
//            {
//                //PacketDispatcher.sendTo(new OpenScreenMessage(OpenScreenMessage.SM.TEST_ONE), (ServerPlayerEntity) playerIn);
//                PlayerNexusProvider.getNexus(playerIn).ifPresent(p->{
//                        p.setPlayId(worldIn.getRandom().nextInt(10) + 5);
//                });
//                MusicVenueProvider.getMusicVenues(playerIn.level).ifPresent(s->{
//                    s.setInt((worldIn.getRandom().nextInt(50)) + 50);
//                });
//            }
//            else
//            {
//                //PacketDispatcher.sendTo(new OpenScreenMessage(OpenScreenMessage.SM.TEST_TWO), (ServerPlayerEntity) playerIn);
//            }
//
//        } else if (!playerIn.isShiftKeyDown())
//        {
//            // nop
//        } else
//        {
//            PlayerNexusProvider.getNexus(playerIn).ifPresent(p->{
//                LOGGER.debug("PlayerNexus: playId: {}", p.getPlayId());
//            });
//            MusicVenueProvider.getMusicVenues(playerIn.level).ifPresent(s->{
//                LOGGER.debug("MusicVenus: someInt: {}, Venues {}", s.getInt(), s.getMusicVenues().size());
//            });
//        }
        return ActionResult.pass(itemstack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag)
    {
        pTooltip.add(new StringTextComponent("Does anyone read these? Should mxTune include a wrench?").withStyle(TextFormatting.GOLD));
        pTooltip.add(new StringTextComponent("Music Block: Right-Click rotates front to the clicked face. SHIFT-Right-Click pick up into inventory").withStyle(TextFormatting.GOLD));
        pTooltip.add(new StringTextComponent("is ItemTag forge:tools/wrench").withStyle(TextFormatting.DARK_GREEN));
        pTooltip.add(new StringTextComponent("Ref: Other popular mods with wrenches RF... Ther...").withStyle(TextFormatting.DARK_GREEN));
    }

    @Override
    public boolean is(ITag<Item> p_206844_1_)
    {
        return super.is(p_206844_1_);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context)
    {
        return ActionResultType.PASS;
    }

    // For a wrench where we want to use SHIFT-Right-Click to pick up a block this needs to return true.
    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player)
    {
        return true;
    }
}
