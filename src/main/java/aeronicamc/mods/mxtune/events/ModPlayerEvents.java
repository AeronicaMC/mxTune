package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.caps.stages.IServerStageAreas;
import aeronicamc.mods.mxtune.caps.stages.ServerStageAreaProvider;
import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import aeronicamc.mods.mxtune.managers.PlayManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
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
            LOGGER.debug("{} Start Tracking {}", event.getPlayer(), event.getTarget());
            PlayManager.sendMusicTo((ServerPlayerEntity) event.getPlayer(), event.getTarget());
        }
    }

    @SubscribeEvent
    public static void event(PlayerEvent.StopTracking event)
    {
        if (!event.getPlayer().level.isClientSide() && ((event.getTarget() instanceof ServerPlayerEntity) || event.getTarget() instanceof MusicSourceEntity))
        {
            LOGGER.debug("{} Stop Tracking {}", event.getPlayer(), event.getTarget());
            PlayManager.stopListeningTo((ServerPlayerEntity) event.getPlayer(), event.getTarget());
        }
    }

    @SubscribeEvent
    public static void event(PlayerContainerEvent.Open event)
    {
        if(!event.getEntityLiving().getCommandSenderWorld().isClientSide())
            PlayManager.stopPlayingEntity(event.getEntityLiving());
    }

    @SubscribeEvent
    public static void event(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(!event.getEntityLiving().getCommandSenderWorld().isClientSide())
            ServerStageAreaProvider.getServerStageAreas(event.getEntity().level).ifPresent(
                    IServerStageAreas::sync);
    }

}
