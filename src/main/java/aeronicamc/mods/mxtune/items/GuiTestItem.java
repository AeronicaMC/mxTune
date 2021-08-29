package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.network.OpenScreenMessage;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
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

public class GuiTestItem extends Item
{
    private static final Logger LOGGER = LogManager.getLogger();
    public GuiTestItem(Properties properties)
    {
        super(properties);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> use(@Nonnull World worldIn, @Nonnull PlayerEntity playerIn, @Nonnull Hand handIn)
    {
        if (!worldIn.isClientSide)
        {
            if (!playerIn.isShiftKeyDown())
            {
                PacketDispatcher.sendTo(new OpenScreenMessage(OpenScreenMessage.SM.TEST_ONE), (ServerPlayerEntity) playerIn);
            }
            else
            {
                // nop
            }

        } else if (!playerIn.isShiftKeyDown())
        {
            // nop
        } else
        {
            // nop
        }
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context)
    {
        return super.useOn(context);
    }
}
