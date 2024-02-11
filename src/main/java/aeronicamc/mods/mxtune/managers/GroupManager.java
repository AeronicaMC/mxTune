package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.RandomUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class GroupManager
{
    private static final Map<Integer, Group> groups = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> memberState = new ConcurrentHashMap<>();
    private static final Map<Integer, String> memberMusic = new ConcurrentHashMap<>();
    private static final BlockingDeque<Integer> lastPins = new LinkedBlockingDeque<>(100);

    public static final int REST = 0;
    public static final int QUEUED = 1;
    public static final int PLAYING = 2;

    //Pin Management
    private static final Map<Integer, Integer> requesterGroupId = new HashMap<>();

    private GroupManager() { /* NOOP */ }

    /**
     * Generate a 4 digit leading zero pin string in the range of 0001 to 9999 that is not already in use.
     * @return a unique 4 digit pin.
     */
    public static String generatePin()
    {
        Integer[] pin = new Integer[1];
        Set<Integer> inUsePins = new HashSet<>(16);
        groups.forEach((key, value) -> {
            pin[0] = !value.getPin().isEmpty() ? Integer.parseInt(value.getPin()) : 0;
            inUsePins.add(pin[0]);
        });
        do {
            pin[0] = RandomUtils.nextInt(1, 10000);
        } while (lastPins.contains(pin[0]) || inUsePins.contains(pin[0]));
        if (!lastPins.offerFirst(pin[0])) {
            lastPins.removeLast();
            lastPins.addFirst(pin[0]);
        }
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
        synchronized (groups)
        {
            boolean[] result = { false };
            groups.values().stream().filter(group -> group.isMember(memberId)).forEach(group -> {
                if (tryRemoveMember(group, memberId) || tryRemoveLeaderPromoteNext(group, memberId) || tryRemoveGroupIfLast(group))
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

    private static boolean tryRemoveGroupIfLast(Group group)
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

    public static Group getGroupByPlayId(int playId)
    {
        return groups.values().stream().filter(group -> group.getPlayId() == playId).findFirst().orElse(Group.EMPTY);
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

    // Click on leader Methods
    public static void addMemberOnRightClick(int groupId, PlayerEntity initiator)
    {
        Group group = groups.getOrDefault(groupId, Group.EMPTY);
        if (!group.isEmpty())
        {
            switch (group.getMode())
            {
                case PIN:
                    requesterGroupId.put(initiator.getId(), groupId);
                    PacketDispatcher.sendTo(new OpenPinEntryMessage(groupId), (ServerPlayerEntity) initiator);
                    break;
                case OPEN:
                    addMember(groupId, initiator);
                    requesterGroupId.remove(initiator.getId());
                    break;
                default:
            }
        }
    }

    private static void removeGroup(Group group)
    {
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

    public static void handleGroupOpen(ServerPlayerEntity serverPlayer)
    {
        PacketDispatcher.sendTo(new OpenScreenMessage(OpenScreenMessage.SM.GROUP_OPEN), serverPlayer);
    }

    public static void handleGroupCmd(ServerPlayerEntity serverPlayer, GroupCmdMessage.Cmd cmd, int taggedMemberId)
    {
        int sourceMemberId = serverPlayer.getId();
        Group group = getGroup(sourceMemberId);
        if (!group.isEmpty())
            switch (cmd)
            {
                case DISBAND:
                    if (isLeader(sourceMemberId))
                        removeGroup(group);
                    break;
                case MODE_PIN:
                    if (isLeader(sourceMemberId)) {
                        group.setMode(Group.Mode.PIN);
                        sync();
                    }
                    break;
                case MODE_OPEN:
                    if (isLeader(sourceMemberId)) {
                        group.setMode(Group.Mode.OPEN);
                        sync();
                    }
                    break;
                case NEW_PIN:
                    if (isLeader(sourceMemberId))
                    {
                        group.setPin(generatePin());
                        PacketDispatcher.sendTo(new GroupCmdMessage(group.getPin(), GroupCmdMessage.Cmd.PIN, taggedMemberId), serverPlayer);
                    }
                    break;
                case PIN:
                    if (group.isMember(sourceMemberId) || group.isMember(taggedMemberId))
                        PacketDispatcher.sendTo(new GroupCmdMessage(GroupManager.getGroup(serverPlayer.getId()).getPin(), GroupCmdMessage.Cmd.PIN, taggedMemberId), serverPlayer);
                    break;
                case PROMOTE:
                    if (isLeader(sourceMemberId)) {
                        group.setLeader(taggedMemberId);
                        sync();
                    }
                    break;
                case REMOVE:
                    if (isLeader(sourceMemberId) && group.isMember(taggedMemberId) || sourceMemberId == taggedMemberId) {
                        LivingEntity taggedEntity = (serverPlayer.level.getEntity(taggedMemberId) != null) ? (LivingEntity) serverPlayer.level.getEntity(taggedMemberId) : null;
                        if (taggedEntity instanceof ServerPlayerEntity)
                            PacketDispatcher.sendTo(new GroupCmdMessage(null, GroupCmdMessage.Cmd.CLOSE_GUI, taggedMemberId), (ServerPlayerEntity) taggedEntity);
                        removeMember(serverPlayer, taggedMemberId);
                    }
                    break;
                case NIL:
                default:
            } else
        {
            if (cmd == GroupCmdMessage.Cmd.CREATE_GROUP && !isGrouped(sourceMemberId))
                addGroup(serverPlayer);
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

    static void sync()
    {
        PacketDispatcher.sendToAll(new SyncGroupsMessage(groups));
    }

    public static void syncTo(@Nullable ServerPlayerEntity listeningPlayer)
    {
        if (listeningPlayer != null)
        {
            synchronized (groups)
            {
                PacketDispatcher.sendTo(new SyncGroupsMessage(groups), listeningPlayer);
            }
            synchronized (memberState)
            {
                cleanStatus();
                PacketDispatcher.sendTo(new SyncGroupMemberState(memberState), listeningPlayer);
            }
        }
    }

    static void syncStatus()
    {
        cleanStatus();
        PacketDispatcher.sendToAll(new SyncGroupMemberState(memberState));
    }

    static void cleanStatus()
    {
        Set<Integer> remove = new HashSet<>();
        memberState.keySet().stream().filter(GroupManager::isNotGrouped).forEach(remove::add);
        remove.forEach(memberState::remove);
    }

    static void queuePart(int memberId, String musicText)
    {
       memberMusic.put(memberId, musicText);
       setState(memberId, QUEUED);
       syncStatus();
    }

    public static boolean isActiveOrQueuedPlayId(int playId)
    {
        Group group = getGroupByPlayId(playId);
        return PlayManager.isActivePlayId(playId) || isQueued(group);
    }

    static boolean isQueued(Group group)
    {
        for(Integer member : group.getMembers()) {
            if (memberState.containsKey(member) && memberState.get(member) == QUEUED)
                return true;
        }
        return false;
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
        private EventHandler() { /* NOOP */ }

        @SubscribeEvent
        public static void event(PlayerSleepInBedEvent event)
        {
            if (!event.getPlayer().level.isClientSide() && !isNotGrouped(event.getPlayer().getId())) {
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
            if (!event.getEntity().level.isClientSide() && event.getEntity().isAlive() && event.getAmount() > 1.5F)
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
