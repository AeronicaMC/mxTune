package aeronicamc.mods.mxtune.items;

import aeronicamc.libs.mml.util.TestData;
import aeronicamc.mods.mxtune.caps.ILivingEntityModCap;
import aeronicamc.mods.mxtune.caps.LivingEntityModCapProvider;
import aeronicamc.mods.mxtune.entity.SittableEntity;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Random;

public class MusicItem extends Item
{
    private static final Random rand = new Random();
    private static final Logger LOGGER = LogManager.getLogger();
    private static int lastPlayID;
    public MusicItem(Properties properties)
    {
        super(properties);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> use(@Nonnull World worldIn, @Nonnull PlayerEntity playerIn, @Nonnull Hand handIn)
    {
        if (!worldIn.isClientSide())
        {
            if (!playerIn.isShiftKeyDown())
            {
                LivingEntityModCapProvider.getLivingEntityModCap(playerIn).ifPresent(livingCap ->
                     {
                         livingCap.setPlayId(rand.nextInt());
                     });
                if (!playerIn.isPassenger())
                {
                    SittableEntity.standOnBlock(worldIn, playerIn.blockPosition(), playerIn, 0D);
                }
            }
            else
            {
                LivingEntityModCapProvider.getLivingEntityModCap(playerIn).ifPresent(ILivingEntityModCap::synchronize);
            }

        } else if (!playerIn.isShiftKeyDown())
        {
            int newPlayId = PlayIdSupplier.PlayType.BACKGROUND.getAsInt();
            lastPlayID = newPlayId;
            ClientAudio.playLocal(newPlayId, getRandomMML(), null);
        } else
        {
            ClientAudio.stop(lastPlayID);
        }
        return ActionResult.pass(playerIn.getItemInHand(handIn));
    }

    private String getRandomMML()
    {
        int index = rand.nextInt(TestData.values().length);
        LOGGER.debug("MusicItem: song: {}", TestData.getMML(index).getTitle());
        return TestData.getMML(index).getMML();
    }
}
