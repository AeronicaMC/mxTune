package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GroupManager
{
    private static final Logger LOGGER = LogManager.getLogger(GroupManager.class);
    private static final Map<Integer, Group> groups = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> memberState = new ConcurrentHashMap<>();
    private static final Map<Integer, String> memberMusic = new ConcurrentHashMap<>();

    public static final int REST = 0;
    public static final int QUEUED = 1;
    public static final int PLAYING = 2;

    //Pin Management
    private static final Map<Integer, Integer> requesterGroupId = new HashMap<>();

    /**
     * Generate a 4 digit leading zero pin string in the range of 0001 to 9999 that is not already in use.
     * @return a unique 4 digit pin.
     */
    public static String generatePin()
    {
        Integer[] pin = new Integer[1];
        Set<Integer> pins = new HashSet<>(16);
        groups.forEach((key, value) -> {
            pin[0] = !value.getPin().isEmpty() ? Integer.parseInt(value.getPin()) : 0;
            pins.add(pin[0]);
        });
        do {
            pin[0] = RandomUtils.nextInt(1, 10000);
        } while (pins.contains(pin[0]));
        return String.format("%04d", pin[0]);
    }

    public static void clear()
    {
        synchronized (groups)
        {
            groups.clear();
            memberState.clear();
            memberMusic.clear();
            requesterGroupId.clear();
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
            if (isNotGrouped(leader.getId()))
            {
                Group group = new Group(leader.getId());
                group.setPin(generatePin());
                groups.put(group.getGroupId(), group);
                LOGGER.debug("groupId: {}, Pin: {}", group.getGroupId(), group.getPin());
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
                if (isNotGrouped(member.getId()))
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

    public static void removeMember(@Nullable LivingEntity livingEntity, int memberId)
    {
        Group group = getGroup(livingEntity != null ? livingEntity.getId() : 0);
        if (removeMember(memberId))
        {
            if (livingEntity != null && group.isValid())
            {
                LivingEntity memberEntity = (LivingEntity) livingEntity.level.getEntity(memberId);
                LivingEntity leader = (LivingEntity) livingEntity.level.getEntity(group.getLeader());
                String member = memberEntity != null ? memberEntity.getDisplayName().getString() : Integer.toString(memberId);
                if (leader != null)
                    leader.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.member_left_group", member), leader.getUUID());
                else
                    livingEntity.sendMessage(new TranslationTextComponent("chat.mxtune.groupManager.member_left_group", member), livingEntity.getUUID());
            }
            memberState.remove(memberId);
            memberMusic.remove(memberId);
            syncStatus();
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
     * @param memberId search all groups for thia member.
     * @return the Group or the Group.EMPTY.
     */
    public static Group getGroup(int memberId)
    {
        return groups.values().stream().filter(group -> group.isMember(memberId)).findFirst().orElse(Group.EMPTY);
    }

    /**
     * Search all groups for the memberID.
     *
     * @param memberId member to search for
     * @return true if the memberID is found.
     */
    static boolean isGrouped(int memberId)
    {
        return groups.values().stream().anyMatch(group -> group.isMember(memberId));
    }

    /**
     * Search all groups for the memberID.
     *
     * @param memberId member to search for
     * @return true if the memberID is not found.
     */
    private static boolean isNotGrouped(int memberId)
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
        Group group = getGroup(memberId);
        if (group.isValid())
        {
            group.setLeader(memberId);
            sync();
        }
    }

    // Click on leader Methods
    public static void addMemberOnRightClick(int groupId, PlayerEntity initiator)
    {
        Group group = groups.getOrDefault(groupId, Group.EMPTY);
        if (!group.isEmpty())
        {
            switch (group.getMode())
            {
                case Pin:
                    requesterGroupId.put(initiator.getId(), groupId);
                    PacketDispatcher.sendTo(new OpenPinEntryMessage(groupId), (ServerPlayerEntity) initiator);
                    break;
                case Open:
                    addMember(groupId, initiator);
                    requesterGroupId.remove(initiator.getId());
                    break;
                default:
            }
        }
    }

    private static void removeGroup(Group group)
    {
        PlayManager.stopGroupMusic(group.getLeader());
        removeGroupsMusicText(group.getLeader());
        removeGroupsPlayState(group.getLeader());
        groups.remove(group.getGroupId());
        sync();
        syncStatus();
    }

    public static void handlePin(ServerPlayerEntity serverPlayer, String pin)
    {
        if (requesterGroupId.containsKey(serverPlayer.getId()))
        {
            Group group = groups.getOrDefault(requesterGroupId.get(serverPlayer.getId()), Group.EMPTY);
            if (!group.isEmpty() && group.getPin().equals(pin))
            {
                addMember(group.getGroupId(), serverPlayer);
                requesterGroupId.remove(serverPlayer.getId());
            }
        }
    }

    public static void handleGroupCheck(ServerPlayerEntity serverPlayer, OpenScreenMessage.SM sm)
    {
        int playerId = serverPlayer.getId();
        if (!isGrouped(playerId))
            addGroup(serverPlayer);
        PacketDispatcher.sendTo(new OpenScreenMessage(OpenScreenMessage.SM.GROUP_OPEN), serverPlayer);
    }

    public static void handleGroupCmd(ServerPlayerEntity serverPlayer, GroupCmdMessage.Cmd cmd, int taggedMemberId)
    {
        int sourceMemberId = serverPlayer.getId();
        Group group = getGroup(sourceMemberId);
        if (!group.isEmpty())
            switch (cmd)
            {
                case Disband:
                    if (isLeader(sourceMemberId))
                        removeGroup(group);
                    break;
                case ModePin:
                    if (isLeader(sourceMemberId)) {
                        group.setMode(Group.Mode.Pin);
                        sync();
                    }
                    break;
                case ModeOpen:
                    if (isLeader(sourceMemberId)) {
                        group.setMode(Group.Mode.Open);
                        sync();
                    }
                    break;
                case NewPin:
                    if (isLeader(sourceMemberId))
                    {
                        group.setPin(generatePin());
                        PacketDispatcher.sendTo(new GroupCmdMessage(group.getPin(), GroupCmdMessage.Cmd.Pin, taggedMemberId), serverPlayer);
                    }
                    break;
                case Pin:
                    if (group.isMember(sourceMemberId) || group.isMember(taggedMemberId))
                        PacketDispatcher.sendTo(new GroupCmdMessage(GroupManager.getGroup(serverPlayer.getId()).getPin(), GroupCmdMessage.Cmd.Pin, taggedMemberId), serverPlayer);
                    break;
                case Promote:
                    if (isLeader(sourceMemberId)) {
                        group.setLeader(taggedMemberId);
                        sync();
                    }
                    break;
                case Remove:
                    LOGGER.info("{}, {}", serverPlayer.getId() ,taggedMemberId);
                    if (isLeader(sourceMemberId) && group.isMember(taggedMemberId) || sourceMemberId == taggedMemberId) {
                        ServerPlayerEntity taggedEntity = (serverPlayer.level.getEntity(taggedMemberId) != null) ? (ServerPlayerEntity) serverPlayer.level.getEntity(taggedMemberId) : null;
                        if (taggedEntity != null)
                            PacketDispatcher.sendTo(new GroupCmdMessage(null, GroupCmdMessage.Cmd.CloseGui, taggedMemberId), taggedEntity);
                        removeMember(serverPlayer, taggedMemberId);
                    }
                    break;
                case Nil:
                default:
            }
    }

    // Member Music Methods
    /**
     *
     * @param membersId to search for
     * @param duration to apply
     */
    static void setMemberPartDuration(Integer membersId, int duration)
    {
        Group group = getGroup(membersId);
        if (group.isValid())
            group.setPartDuration(duration);
    }

    /**
     * Retrieve a group's duration for a given member
     * @param memberId of a group
     * @return a {@link Tuple<Integer,Boolean>} where if {@link Tuple<Integer,Boolean>#getB()} is true then {@link Tuple<Integer,Boolean>#getA()} contains a valid Integer
     */
    static int getGroupDuration(int memberId)
    {
        Group group = getGroup(memberId);
        if (group.isEmpty())
            return 0;
        else
            return group.getMaxDuration();
    }

    static void sync()
    {
        LOGGER.debug("sync groups total {}", groups.size());
        PacketDispatcher.sendToAll(new SyncGroupsMessage(groups));
    }

    public static void syncTo(@Nullable ServerPlayerEntity listeningPlayer)
    {
        if (listeningPlayer != null)
        {
            synchronized (groups)
            {
                PacketDispatcher.sendTo(new SyncGroupsMessage(groups), listeningPlayer);
                LOGGER.debug("sync all to {}: group count {}", listeningPlayer.getDisplayName().getString(), groups.size());
            }
            synchronized (memberState)
            {
                cleanStatus();
                PacketDispatcher.sendTo(new SyncGroupMemberState(memberState), listeningPlayer);
                LOGGER.debug("sync memberStates count {}", memberState.size());
            }
        }
    }

    static void syncStatus()
    {
        cleanStatus();
        PacketDispatcher.sendToAll(new SyncGroupMemberState(memberState));
        LOGGER.debug("sync memberStates count {}", memberState.size());
    }

    static void cleanStatus()
    {
        Set<Integer> remove = new HashSet<>();
        memberState.keySet().stream().filter(GroupManager::isNotGrouped).forEach(remove::add);
        remove.forEach(memberState::remove);
    }

    static void queuePart(int playID, int memberId, String musicText)
    {
       memberMusic.put(memberId, musicText);
       setState(memberId, QUEUED);
       syncStatus();
    }

    static void setState(int memberId, int state)
    {
        synchronized (memberState)
        {
            memberState.put(memberId, state);
        }
    }

    static void setGroupPlaying(int memberId)
    {
        synchronized (memberState)
        {
            getGroup(memberId).getMembers().forEach(member -> {
                if (memberState.getOrDefault(member, REST).equals(QUEUED))
                    setState(member, PLAYING);
            });
        }
        syncStatus();
    }

    static void setGroupRest(int memberId)
    {
        synchronized (memberState)
        {
            getGroup(memberId).getMembers().forEach(memberState::remove);
        }
        syncStatus();
    }

    static String getGroupsMusicText(int memberId)
    {
        StringBuilder musicText = new StringBuilder();
        synchronized (memberMusic) { getGroup(memberId).getMembers().forEach(member -> musicText.append(memberMusic.getOrDefault(member, ""))); }
        return musicText.toString();
    }

    static void removeGroupsMusicText(int memberId)
    {
        synchronized (memberMusic) { getGroup(memberId).getMembers().forEach(memberMusic::remove); }
    }

    static void removeGroupsPlayState(int memberId)
    {
        synchronized (memberMusic) { getGroup(memberId).getMembers().forEach(memberState::remove); }
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class EventHandler
    {
        @SubscribeEvent
        public static void event(PlayerSleepInBedEvent event)
        {
            if (!event.getPlayer().level.isClientSide() && !isNotGrouped(event.getPlayer().getId()))
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
