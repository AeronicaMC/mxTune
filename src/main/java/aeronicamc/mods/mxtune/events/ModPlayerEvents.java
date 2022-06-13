package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.ILockable;
import aeronicamc.mods.mxtune.blocks.IMusicPlayer;
import aeronicamc.mods.mxtune.blocks.LockableHelper;
import aeronicamc.mods.mxtune.caps.player.IPlayerNexus;
import aeronicamc.mods.mxtune.caps.venues.IMusicVenues;
import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import aeronicamc.mods.mxtune.managers.PlayManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
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

    @SubscribeEvent
    public static void event(PlayerContainerEvent.Close event)
    {
        if(!event.getEntityLiving().getCommandSenderWorld().isClientSide())
            LOGGER.debug("{}", event.getContainer());
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
        PlayManager.sendMusicTo((ServerPlayerEntity) event.getPlayer(), event.getPlayer());
        LOGGER.debug("PlayerChangedDimensionEvent: {}", event.getPlayer());
    }

    // Test if a player can break this block with a BE of type ILockable.
    // Only the owner of the block can break these.
    @SubscribeEvent
    public static void onEvent(BlockEvent.BreakEvent event)
    {
        if(event.getWorld().isClientSide()) return;
        if(event.getState().getBlock() instanceof IMusicPlayer)
        {
            TileEntity tileEntity = event.getWorld().getBlockEntity(event.getPos());
            if(tileEntity instanceof ILockable)
            {
                boolean isCreativeMode = event.getPlayer() != null && event.getPlayer().isCreative();
                if (LockableHelper.cannotBreak(event.getPlayer(), (World) event.getWorld(), event.getPos()) && !isCreativeMode)
                    event.setCanceled(true);
            }
        }
    }

//    @SubscribeEvent
    public static void event(PlayerEvent.PlayerLoggedInEvent event)
    {
        /* NOP See ClientEvents event(ClientPlayerNetworkEvent.LoggedInEvent event) */
    }

//    @SubscribeEvent
    public static void event(PlayerEvent.PlayerRespawnEvent event)
    {
        /* NOP See ClientEvents event(ClientPlayerNetworkEvent.RespawnEvent) */
    }

    @SubscribeEvent
    public static void event(LivingDeathEvent event)
    {
        if (event.getEntity().level.isClientSide()) return;
        if (event.getEntityLiving() instanceof ServerPlayerEntity)
        {
            PlayManager.stopPlayingEntity(event.getEntityLiving());
        }
    }

    private static void capabilitySynchronize(LivingEntity livingEntity)
    {
        getMusicVenues(livingEntity.level).ifPresent(IMusicVenues::sync);
        getNexus(livingEntity).ifPresent(IPlayerNexus::sync);
    }
}
