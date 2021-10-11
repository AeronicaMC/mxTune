package aeronicamc.mods.mxtune.items;

import aeronicamc.libs.mml.util.TestData;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.CreateSheetMusicMessage;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ItemMusicPaper extends Item
{
    private static final Random rand = new Random();

    public ItemMusicPaper(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand)
    {
        if (pLevel.isClientSide)
        {
            //Handler.openTestScreen();
            writeRandomSheetMusic();
        }
        return ActionResult.pass(pPlayer.getItemInHand(pHand));
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

    private void writeRandomSheetMusic()
    {
        int index = rand.nextInt(TestData.values().length);
        PacketDispatcher.sendToServer(
                new CreateSheetMusicMessage(TestData.getMML(index).getTitle(),
                                            TestData.getMML(index).getMML()));
    }
}
