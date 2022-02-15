package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.caps.player.PlayerNexusProvider;
import aeronicamc.mods.mxtune.caps.stages.ServerStageAreaProvider;
import net.minecraft.entity.player.PlayerEntity;
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
                //PacketDispatcher.sendTo(new OpenScreenMessage(OpenScreenMessage.SM.TEST_ONE), (ServerPlayerEntity) playerIn);
                PlayerNexusProvider.getNexus(playerIn).ifPresent(p->{
                        p.setPlayId(worldIn.getRandom().nextInt(10) + 5);
                });
                ServerStageAreaProvider.getServerStageAreas(playerIn.level).ifPresent(s->{
                    s.setInt((worldIn.getRandom().nextInt(50)) + 50);
                });
            }
            else
            {
                //PacketDispatcher.sendTo(new OpenScreenMessage(OpenScreenMessage.SM.TEST_TWO), (ServerPlayerEntity) playerIn);
            }

        } else if (!playerIn.isShiftKeyDown())
        {
            // nop
        } else
        {
            PlayerNexusProvider.getNexus(playerIn).ifPresent(p->{
                LOGGER.debug("playId: {}", p.getPlayId());
            });
            ServerStageAreaProvider.getServerStageAreas(playerIn.level).ifPresent(s->{
                LOGGER.debug("someInt: {}, Stages {}", s.getInt(), s.getStageAreas().size());
            });
        }
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context)
    {
        return super.useOn(context);
    }
}
