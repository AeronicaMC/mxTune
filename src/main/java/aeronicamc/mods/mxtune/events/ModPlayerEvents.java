package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.managers.PlayManager;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModPlayerEvents
{
    @SubscribeEvent
    public static void event(PlayerEvent.StartTracking event)
    {
        //LOGGER.debug("{} Start Tracking {}", event.getPlayer(), event.getTarget());
        //LOGGER.debug("Listeners {}", event.getListenerList());
    }

    @SubscribeEvent
    public static void event(PlayerEvent.StopTracking event)
    {
        //LOGGER.debug("{} Stop Tracking {}", event.getPlayer(), event.getTarget());
        //LOGGER.debug("Listeners {}", event.getListenerList());
    }

    @SubscribeEvent
    public static void event(PlayerContainerEvent.Open event)
    {
        if(!event.getEntityLiving().getCommandSenderWorld().isClientSide())
            PlayManager.stopPlayingLivingEntity(event.getEntityLiving());
    }
}
