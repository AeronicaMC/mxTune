package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.ILockable;
import aeronicamc.mods.mxtune.blocks.IMusicPlayer;
import aeronicamc.mods.mxtune.blocks.LockableHelper;
import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import aeronicamc.mods.mxtune.entity.MusicVenueInfoEntity;
import aeronicamc.mods.mxtune.init.ModTags;
import aeronicamc.mods.mxtune.managers.Group;
import aeronicamc.mods.mxtune.managers.GroupManager;
import aeronicamc.mods.mxtune.managers.PlayManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
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
        if(!event.getWorld().isClientSide() && initiatorPredicate(event) && targetPredicate(event) && hasInstrument(event.getItemStack()))
        {
            PlayerEntity initiator = (PlayerEntity) event.getEntityLiving();
            PlayerEntity target = (PlayerEntity) event.getTarget();
            Group iniGroup = GroupManager.getGroup(initiator.getId());
            Group tarGroup = GroupManager.getGroup(target.getId());
            LOGGER.debug("Initiator: {} right-clicked Target: {} with a {}. Is it an Instrument? {}",
                         initiator.getDisplayName().getString(),
                         target.getDisplayName().getString(),
                         hooverName(event.getItemStack()), hasInstrument(event.getItemStack()) ? "Yes" : "No");
            if (tarGroup.isValid() && tarGroup.getLeader() == target.getId() && iniGroup.isEmpty())
            {
                initiator.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.requests_to_join", target.getName()), target.getUUID());
                LOGGER.debug("{} Requests to join group", initiator.getDisplayName().getString());
                GroupManager.addMember(tarGroup.getGroupId(), initiator); // TODO this is a test only. Adding self to targets group is a no-no :P
            }
            else if (tarGroup.isValid())
            {
                if (tarGroup.getLeader() != target.getId() && iniGroup.isEmpty())
                    target.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.player_not_leader", target.getName()), target.getUUID());
                else
                    target.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.cannot_join_if_group_member", target.getName()), target.getUUID());
            }
        }
    }

    private static boolean initiatorPredicate(PlayerInteractEvent.EntityInteractSpecific event)
    {
        return event.getEntityLiving() instanceof PlayerEntity && event.getHand().equals(Hand.MAIN_HAND);
    }

    private static boolean targetPredicate(PlayerInteractEvent.EntityInteractSpecific event)
    {
        return event.getTarget() instanceof PlayerEntity;
    }

    private static boolean hasInstrument(ItemStack itemStack)
    {
        return itemStack.getItem().is(ModTags.Items.INSTRUMENTS) || itemStack.getItem().is(ModTags.Items.MUSIC_MACHINES);
    }

    private static String hooverName(ItemStack itemStack)
    {
        return itemStack.getHoverName().getString();
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