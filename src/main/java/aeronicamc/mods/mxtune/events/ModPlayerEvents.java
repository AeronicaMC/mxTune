package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import aeronicamc.mods.mxtune.items.ItemSheetMusic;
import aeronicamc.mods.mxtune.managers.PlayManager;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModPlayerEvents
{
    private static final Logger LOGGER = LogManager.getLogger(ModPlayerEvents.class);
    @SubscribeEvent
    public static void event(PlayerEvent.StartTracking event)
    {
        if (!event.getPlayer().level.isClientSide() && ((event.getTarget() instanceof ServerPlayerEntity) || event.getTarget() instanceof MusicSourceEntity))
        {
            PlayManager.sendMusicTo((ServerPlayerEntity) event.getPlayer(), event.getTarget());
            LOGGER.debug("{} Start Tracking {}", event.getPlayer(), event.getTarget());
        }
    }

    @SubscribeEvent
    public static void event(PlayerEvent.StopTracking event)
    {
        if (!event.getPlayer().level.isClientSide() && ((event.getTarget() instanceof ServerPlayerEntity) || event.getTarget() instanceof MusicSourceEntity))
        {
            LOGGER.debug("{} Stop Tracking {}", event.getPlayer(), event.getTarget());
        }
    }

    @SubscribeEvent
    public static void event(PlayerContainerEvent.Open event)
    {
        if(!event.getEntityLiving().getCommandSenderWorld().isClientSide())
            PlayManager.stopPlayingEntity(event.getEntityLiving());
    }

    @SubscribeEvent
    public static void event(PlayerDestroyItemEvent event)
    {
        if (!event.getEntityLiving().level.isClientSide())
        {
            ItemStack itemStack = event.getOriginal();
            Item item = !itemStack.isEmpty() ? itemStack.getItem() : null;
            if (item == null) return;
            if (item instanceof ItemSheetMusic && SheetMusicHelper.hasMML(itemStack))
            {
                //ModDataStore.removeMusic(itemStack);
            }
            else if (item instanceof IInstrument)
            {

            }
        }
    }
}
