package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.caps.player.IPlayerNexus;
import aeronicamc.mods.mxtune.caps.venues.IMusicVenues;
import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import aeronicamc.mods.mxtune.managers.PlayManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static aeronicamc.mods.mxtune.caps.player.PlayerNexusProvider.getNexus;
import static aeronicamc.mods.mxtune.caps.venues.MusicVenueProvider.getMusicVenues;

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

    /**
     * Synchronise a player's playId to watching clients when they change dimensions.
     *
     * @param event The event
     */
    @SubscribeEvent
    public static void event(final PlayerEvent.PlayerChangedDimensionEvent event)
    {
        capabilitySynchronize(event.getEntityLiving());
        LOGGER.debug("PlayerChangedDimensionEvent: {}", event.getPlayer());
    }

    @SubscribeEvent
    public static void event(PlayerEvent.PlayerLoggedInEvent event)
    {
        /* NOP See ClientEvents event(ClientPlayerNetworkEvent.LoggedInEvent event) */
    }

    @SubscribeEvent
    public static void event(PlayerEvent.PlayerRespawnEvent event)
    {
        /* NOP See ClientEvents event(ClientPlayerNetworkEvent.RespawnEvent) */
    }

    @SubscribeEvent
    public static void event(EntityJoinWorldEvent event)
    {
        /* NOP */
    }

    private static void capabilitySynchronize(LivingEntity livingEntity)
    {
        getMusicVenues(livingEntity.level).ifPresent(IMusicVenues::sync);
        getNexus(livingEntity).ifPresent(IPlayerNexus::sync);
    }
}
