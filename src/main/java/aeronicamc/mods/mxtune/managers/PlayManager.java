package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public final class PlayManager
{
    private static final Logger LOGGER = LogManager.getLogger(PlayManager.class.getSimpleName());

    private PlayManager()
    {
        /* NOP */
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = {Dist.DEDICATED_SERVER, Dist.CLIENT})
    private static class EventHandler
    {
        @SubscribeEvent
        public static void event(PlayerEvent.StartTracking event)
        {
            LOGGER.debug("{} Start Tracking {}", event.getPlayer(), event.getTarget());
            LOGGER.debug("Listeners {}", event.getListenerList());
        }

        @SubscribeEvent
        public static void event(PlayerEvent.StopTracking event)
        {
            LOGGER.debug("{} Stop Tracking {}", event.getPlayer(), event.getTarget());
            LOGGER.debug("Listeners {}", event.getListenerList());
        }
    }
}
