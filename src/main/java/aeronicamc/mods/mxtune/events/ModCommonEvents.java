package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.ILockable;
import aeronicamc.mods.mxtune.blocks.IMusicPlayer;
import aeronicamc.mods.mxtune.blocks.LockableHelper;
import aeronicamc.mods.mxtune.entity.MusicSourceEntity;
import aeronicamc.mods.mxtune.entity.MusicVenueInfoEntity;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.init.ModTags;
import aeronicamc.mods.mxtune.managers.Group;
import aeronicamc.mods.mxtune.managers.GroupManager;
import aeronicamc.mods.mxtune.managers.PlayManager;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.OpenPinEntryMessage;
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

import java.util.Objects;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModCommonEvents
{
    private static final Logger LOGGER = LogManager.getLogger(ModCommonEvents.class);

    private ModCommonEvents() { /* NOOP */ }

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
            if (tarGroup.isValid() && tarGroup.getLeader() == target.getId() && iniGroup.isEmpty())
            {
                target.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.requests_to_join", initiator.getName()), target.getUUID());
                GroupManager.addMemberOnRightClick(tarGroup.getGroupId(), initiator);
            }
            else if (tarGroup.isValid())
            {
                if (tarGroup.getLeader() != target.getId() && iniGroup.isEmpty())
                    initiator.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.player_not_leader", target.getName()), initiator.getUUID());
                else if (tarGroup.getGroupId() == iniGroup.getGroupId())
                    initiator.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.already_group_member", target.getName()), initiator.getUUID());
                else
                    initiator.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.xxx-xxx_group_member", target.getName()), initiator.getUUID());
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

    @SubscribeEvent
    public static void event(PlayerInteractEvent.RightClickItem event)
    {
        if (!event.getEntity().level.isClientSide() && event.getEntity() instanceof PlayerEntity)
        {
            PlayerEntity player = event.getPlayer();
            // testing PIN Gui
            if (Objects.equals(player.getMainHandItem().getItem().getRegistryName(), ModItems.PLACARD_ITEM.getId()))
                PacketDispatcher.sendTo(new OpenPinEntryMessage(0), (ServerPlayerEntity) player);
        }
    }
}
