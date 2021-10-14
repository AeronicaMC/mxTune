package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public final class ServerPlayManager
{
    private static final Logger LOGGER = LogManager.getLogger(ServerPlayManager.class.getSimpleName());

    private ServerPlayManager()
    {
        /* NOP */
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = {Dist.DEDICATED_SERVER})
    private static class EventHandler
    {
        @SubscribeEvent
        public static void event(PlayerEvent.StartTracking event)
        {
            LOGGER.debug("Tracking {}", event.getTarget());
            LOGGER.debug("Listeners {}", event.getListenerList());
        }
    }
}
