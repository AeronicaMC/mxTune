package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.caps.stages.ServerStageAreaProvider;
import aeronicamc.mods.mxtune.caps.stages.StageAreaData;
import aeronicamc.mods.mxtune.caps.stages.StageToolHelper;
import aeronicamc.mods.mxtune.caps.stages.StageToolState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public class StageToolItem extends Item
{
    private static final Logger LOGGER = LogManager.getLogger();
    public StageToolItem(Properties properties)
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
        if (!context.getHand().equals(Hand.MAIN_HAND))
            return super.onItemUseFirst(stack, context);
        StageToolState toolState;
        if (context.getPlayer() != null && !context.getLevel().isClientSide())
        {
            if (StageToolHelper.get(context.getPlayer()) == null)
            {
                StageToolHelper.create(context.getPlayer());
                toolState = StageToolHelper.next(context.getPlayer(), StageToolHelper.stageToolState(context.getPlayer()), context.getClickedPos());
                StageToolHelper.set(context.getPlayer(), toolState);
                return ActionResultType.SUCCESS;
            } else
            {
                toolState = StageToolHelper.next(context.getPlayer(), StageToolHelper.stageToolState(context.getPlayer()), context.getClickedPos());
                StageToolHelper.set(context.getPlayer(), toolState);
                StageAreaData stageArea = StageToolHelper.get(context.getPlayer());
                ServerStageAreaProvider.getServerStageAreas(context.getLevel()).ifPresent(areas ->
                                                                                          {
                                                                                              if (stageArea != null)
                                                                                              {
                                                                                                  areas.addArea(stageArea);
                                                                                                  areas.sync();
                                                                                                  StageToolHelper.reset(context.getPlayer());
                                                                                              }
                                                                                          });
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> use(@Nonnull World worldIn, @Nonnull PlayerEntity playerIn, @Nonnull Hand handIn)
    {
        if (!worldIn.isClientSide)
        {
            ServerPlayerEntity serverPlayerEntity = ((ServerPlayerEntity) playerIn);
            if (!playerIn.isShiftKeyDown())
            {
                //ServerStageAreaProvider.getServerStageAreas(worldIn).ifPresent(IServerStageAreas::test);
            }
        }
        return super.use(worldIn, playerIn, handIn);
    }



    @Override
    public ActionResultType useOn(ItemUseContext context)
    {
        return super.useOn(context);
    }
}
