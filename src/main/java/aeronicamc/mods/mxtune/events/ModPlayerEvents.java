package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.caps.player.IPlayerNexus;
import aeronicamc.mods.mxtune.caps.stages.IServerStageAreas;
import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import aeronicamc.mods.mxtune.managers.PlayManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static aeronicamc.mods.mxtune.caps.player.PlayerNexusProvider.getNexus;
import static aeronicamc.mods.mxtune.caps.stages.ServerStageAreaProvider.getServerStageAreas;

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
        capabilitySynchronize(event.getEntityLiving());
        LOGGER.debug("PlayerLoggedInEvent: {}", event.getPlayer());
    }

    @SubscribeEvent
    public static void event(PlayerEvent.PlayerRespawnEvent event)
    {
//        LOGGER.debug("PlayerRespawnEvent: {}", event.getPlayer());
    }

    @SubscribeEvent
    public static void event(EntityJoinWorldEvent event)
    {
        // TODO: see if this is typical or related to something I'm doing incorrectly?!
        // LAN play worlds need this so the visiting player can sync our capabilities.
        // There client side network errors that occur in the connecting players logs:
        //     [09:17:09] [Render thread/FATAL] [minecraft/ThreadTaskExecutor]: Error executing task on Client
        //     java.lang.IndexOutOfBoundsException: null
        //	       at io.netty.buffer.EmptyByteBuf.readUnsignedByte(EmptyByteBuf.java:536) ~[netty-all-4.1.25.Final.jar:4.1.25.Final] {}
        //         ...
        //         at cpw.mods.modlauncher.Launcher.main(Launcher.java:66) [modlauncher-8.1.3.jar:?] {}
        //	       at net.minecraftforge.userdev.LaunchTesting.main(LaunchTesting.java:108) [forge-1.16.5-36.2.28_mapped_parchment_2021.10.17-1.16.5.jar:?] {}
        //
        if (event.getEntity() instanceof PlayerEntity)
        {
//            capabilitySynchronize((PlayerEntity) event.getEntity());
//            LOGGER.debug("EntityJoinWorldEvent: {}", event.getEntity());
        }
    }

    private static void capabilitySynchronize(LivingEntity livingEntity)
    {
        getServerStageAreas(livingEntity.level).ifPresent(IServerStageAreas::sync);
        getNexus(livingEntity).ifPresent(IPlayerNexus::sync);
    }

}
