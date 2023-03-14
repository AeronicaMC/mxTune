package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.SyncGroupsMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupManager
{
    private static final Logger LOGGER = LogManager.getLogger(GroupManager.class);
    private static final Map<Integer, Group> groups = new ConcurrentHashMap<>();

    public static void clear()
    {
        synchronized (groups)
        {
            groups.clear();
        }
    }

    /**
     * Any player can be a leader or in a group. A player who makes a group is
     * the leader of the group. A player in a group can't join another group.
     * If the leader leaves a group another member will be promoted to group
     * leader automatically.
     *
     * @param leader of a group
     */
    public static void addGroup(PlayerEntity leader)
    {
        synchronized (groups)
        {
            if (isNotMemberOfAnyGroup(leader.getId()))
            {
                Group group = new Group(leader.getId());
                groups.put(group.getGroupId(), group);
                sync();
                leader.displayClientMessage(new TranslationTextComponent("message.mxtune.groupManager.created_group"), true);
            }
            else
                leader.displayClientMessage(new TranslationTextComponent("message.mxtune.groupManager.cannot_create_group"), true);
        }
    }

    public static void addMember(int groupId, @Nullable Entity member)
    {
        synchronized (groups)
        {
            Group group = groups.getOrDefault(groupId, Group.EMPTY);
            if (groups.isEmpty() || group.isEmpty() || member == null) return;

            PlayerEntity leader = (PlayerEntity) member.level.getEntity(group.getLeader());

            if (leader != null)
            {
                if (isNotMemberOfAnyGroup(member.getId()))
                {
                    if (group.notFull())
                    {
                        group.addMember(member.getId());
                        sync();
                        member.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.you_joined_players_group", leader.getDisplayName()), member.getUUID());
                        leader.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.player_joined_the_group", member.getDisplayName()), leader.getUUID());
                    }
                    else
                    {
                        member.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.cannot_join_too_many", leader.getDisplayName()), member.getUUID());
                        leader.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.player_cannot_join_too_many", member.getDisplayName()), leader.getUUID());
                    }
                }
                else
                {
                    member.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.cannot_join_if_group_member"), member.getUUID());
                }
            }
            else
            {
                member.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.invalid_leader"), member.getUUID());
            }
        }
    }

    /**
     * Removes a member from all groups potentially changing the leader of a
     * group or removing the group entirely.
     *
     * @param memberId to be removed
     */
    public static boolean removeMember(int memberId)
    {
        //PlayManager.purgeMember(memberID);
        synchronized (groups)
        {
            boolean[] result = new boolean[1];
            groups.values().stream().filter(group -> group.isMember(memberId)).forEach(group -> {
                   if (tryRemoveMember(group, memberId))
                       result[0] = true;
                   else if (tryRemoveLeaderPromoteNext(group, memberId))
                       result[0] = true;
                   else if (tryRemoveGroupIfLast(group, memberId))
                       result[0] = true;
               });
            if (result[0])
            {
                sync();
                return true;
            }
            return false;
        }
    }

    // TODO: Setup localization for the remove member chat messages
    public static void removeMember(@Nullable LivingEntity livingEntity, int memberId)
    {
        Group group = getMembersGroup(livingEntity != null ? livingEntity.getId() : 0);
        if (removeMember(memberId))
        {
            if (livingEntity != null && !group.isEmpty())
            {
                LivingEntity memberEntity = (LivingEntity) livingEntity.level.getEntity(memberId);
                LivingEntity leader = (LivingEntity) livingEntity.level.getEntity(group.getLeader());
                String member = memberEntity != null ? memberEntity.getDisplayName().getString() : Integer.toString(memberId);
                if (leader != null)
                    leader.sendMessage(new StringTextComponent(String.format("%s left the group", member)), leader.getUUID());
                else
                    livingEntity.sendMessage(new StringTextComponent(String.format("%s left the group", member)), livingEntity.getUUID());
            }
        }
    }

    private static boolean tryRemoveMember(Group group, int memberId)
    {
        boolean result = false;
        /* This is not the leader so simply remove the member. */
        if (group.getLeader() != memberId)
        {
            /* This is not the leader so simply remove the member. */
            group.removeMember(memberId);
            result = true;
        }
        return result;
    }

    private static boolean tryRemoveGroupIfLast(Group group, int memberID)
    {
        boolean result = false;
        /* This is the leader of the group and if we are the last or only member then we will remove the group. */
        if (group.getMembers().size() <= 1)
        {
            group.getMembers().clear();
            groups.remove(group.getGroupId());
            result = true;
        }
        return result;
    }

    private static boolean tryRemoveLeaderPromoteNext(Group group, int memberId)
    {
        boolean result = false;
        // Remove the leader
        if (group.getLeader() == memberId)
        {
            group.removeMember(memberId);

            /* Promote the next member of the group to leader. */
            Iterator<Integer> remainingMembers = group.getMembers().iterator();
            if (remainingMembers.hasNext())
            {
                int member = remainingMembers.next();
                group.setLeader(member);
                result = true;
            }
        }
        return result;
    }

    /**
     * Searches all groups and returns the group or null.
     *
     * @param groupId member in question
     * @return the Group or the Group.EMPTY.
     */
    private static Group getGroup(int groupId)
    {
        return groups.getOrDefault(groupId, Group.EMPTY);
    }

    /**
     * @param memberId search all groups for thia member.
     * @return the Group or the Group.EMPTY.
     */
    public static Group getMembersGroup(Integer memberId)
    {
        for (Group group : groups.values())
            if (group.getMembers().contains(memberId)) return group;

        return Group.EMPTY;
    }

    /**
     * Search all groups for the memberID.
     *
     * @param memberId member to search for
     * @return true if the memberID is not found.
     */
    private static boolean isNotMemberOfAnyGroup(int memberId)
    {
        return groups.values().stream().noneMatch(group -> group.isMember(memberId));
    }

    static boolean isLeader(int entityID)
    {
        return groups.values().stream().anyMatch(group -> group.getLeader() == entityID);
    }

    /**
     * Set a new Leader
     * @param memberId the new leader
     */
    public static void setLeader(int memberId)
    {
        Group group = getMembersGroup(memberId);
        if (!group.isEmpty())
        {
            group.setLeader(memberId);
            sync();
        }
    }

    /**
     *
     * @param membersId to search for
     * @param duration to apply
     */
    static void setMemberPartDuration(Integer membersId, int duration)
    {
        Group group = getMembersGroup(membersId);
        if (!group.isEmpty())
            group.setPartDuration(duration);
    }

    /**
     * Retrieve a group's duration for a given member
     * @param memberId of a group
     * @return a {@link Tuple<Integer,Boolean>} where if {@link Tuple<Integer,Boolean>#getB()} is true then {@link Tuple<Integer,Boolean>#getA()} contains a valid Integer
     */
    static Tuple<Integer, Boolean> getGroupDuration(Integer memberId)
    {
        Group group = getMembersGroup(memberId);
        if (!group.isEmpty())
            return new Tuple<>(group.getMaxDuration(), true);
        else
            return new Tuple<>(0, false);
    }

    private static void sync()
    {
        LOGGER.debug("sync groups total {}", groups.size());
        PacketDispatcher.sendToAll(new SyncGroupsMessage(groups));
    }

    public static void syncTo(@Nullable ServerPlayerEntity listeningPlayer)
    {
        synchronized (groups)
        {
            LOGGER.debug("sync all to {}: group count {}", listeningPlayer != null ? listeningPlayer.getDisplayName().getString() : "-dead beef-", groups.size());
            if (listeningPlayer != null)
                PacketDispatcher.sendTo(new SyncGroupsMessage(groups), listeningPlayer);
        }
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class EventHandler
    {
        @SubscribeEvent
        public static void event(PlayerSleepInBedEvent event)
        {
            if (!event.getPlayer().level.isClientSide() && !isNotMemberOfAnyGroup(event.getPlayer().getId()))
            {
                event.getPlayer().displayClientMessage(new TranslationTextComponent("message.mxtune.groupManager.cannot_sleep_when_grouped"), true);
                event.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
            }
        }

        @SubscribeEvent
        public static void event(LivingDeathEvent event)
        {
            if (!event.getEntity().level.isClientSide())
                removeMember(event.getEntity().getId());
        }

        @SubscribeEvent
        public static void event(LivingDamageEvent event)
        {
            if (!event.getEntity().level.isClientSide())
                if (event.getEntity().isAlive() && event.getAmount() > 1.5F)
                    removeMember(event.getEntityLiving(), event.getEntity().getId());
        }

        @SubscribeEvent
        public static void event(PlayerEvent.PlayerLoggedOutEvent event)
        {
            if (!event.getPlayer().level.isClientSide())
                removeMember(event.getPlayer(), event.getPlayer().getId());
        }

        @SubscribeEvent
        public static void event(PlayerEvent.PlayerChangedDimensionEvent event)
        {
            if (!event.getPlayer().level.isClientSide())
                removeMember(event.getPlayer(), event.getPlayer().getId());
        }
    }
}
