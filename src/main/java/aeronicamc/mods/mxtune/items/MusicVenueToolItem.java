package aeronicamc.mods.mxtune.items;

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
import java.util.Optional;

public class MusicVenueToolItem extends Item
{
    private static final Logger LOGGER = LogManager.getLogger();
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
                LOGGER.debug("onItemUseFirst: poke poke poke...");
        });
//        if (context.getPlayer() != null && !context.getLevel().isClientSide())
//        {
//            // TODO: Music venue construction...
//        }

        return ActionResultType.SUCCESS;
    }

    // player optional wrapper so we can use the syntactic sugar
    private Optional<PlayerEntity> getPlayer(ItemUseContext context)
    {
        return Optional.ofNullable(context.getPlayer());
    }

    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block.
     */
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
