package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.ILockable;
import aeronicamc.mods.mxtune.blocks.IMusicPlayer;
import aeronicamc.mods.mxtune.blocks.LockableHelper;
import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import aeronicamc.mods.mxtune.entity.MusicVenueInfoEntity;
import aeronicamc.mods.mxtune.init.ModTags;
import aeronicamc.mods.mxtune.managers.PlayManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModCommonEvents
{
    private static final Logger LOGGER = LogManager.getLogger(ModCommonEvents.class);
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
    public static void event(PlayerInteractEvent.EntityInteractSpecific event)
    {
        if(!event.getEntityLiving().getCommandSenderWorld().isClientSide())
        {
            LOGGER.debug("Source: {}, Target: {}", event.getPlayer().getDisplayName().getString(), event.getTarget().getDisplayName().getString());
            LOGGER.debug("isInstrument? {}, Part: {}", hasInstrument(event.getItemStack()), event.getLocalPos());
        }
    }

    private static boolean hasInstrument(ItemStack itemStack)
    {
        return itemStack.getItem().is(ModTags.Items.INSTRUMENTS);
    }

    // Test if a player can break this block with a BE of type ILockable.
    // Only the owner of the block can break these.
    @SubscribeEvent
    public static void event(BlockEvent.BreakEvent event)
    {
        if(event.getWorld().isClientSide()) return;
        if(event.getState().hasTileEntity() && event.getWorld().getBlockEntity(event.getPos()) instanceof IMusicPlayer)
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

    @SubscribeEvent
    public static void event(LivingDeathEvent event)
    {
        if (event.getEntity().level.isClientSide()) return;
        if (event.getEntityLiving() instanceof ServerPlayerEntity)
        {
            PlayManager.stopPlayingEntity(event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public static void event(EntityEvent.Size event)
    {
        Entity entity = event.getEntity();
        if ((entity instanceof MusicVenueInfoEntity) && !entity.isAddedToWorld())
            event.setNewEyeHeight(0.25F);
    }
}
