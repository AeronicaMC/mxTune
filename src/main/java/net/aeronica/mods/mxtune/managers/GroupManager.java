/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.managers;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.JoinGroupMessage;
import net.aeronica.mods.mxtune.network.client.SyncGroupMessage;
import net.aeronica.mods.mxtune.status.ServerCSDManager;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.world.caps.world.ModWorldPlaylistHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity.SleepResult;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static net.aeronica.mods.mxtune.config.ModConfig.isJAMPartyRightClickDisabled;
import static net.aeronica.mods.mxtune.options.MusicOptionsUtil.*;

public class GroupManager
{
    public static final GroupManager INSTANCE = new GroupManager();
    private static Integer groupID = 0;

    private GroupManager() {}

    private static final Set<Group> groups = new HashSet<>(1, 0.3f);
    
    private static Integer getNextGroupID()
    {
        groupID++;
        if (groupID == Integer.MAX_VALUE)
            groupID = 0;

        return groupID;
    }

    static boolean hasActiveGroups() { return !groups.isEmpty(); }

    /**
     * Any player can be a leader or in a group. A player who makes a group is
     * the leader of the group. A player in a group can't join another group.
     * If the leader leaves a group another member will be promoted to group
     * leader automatically.
     * 
     * @param creatorID the leader/creator of a group
     */
    public static void addGroup(int creatorID)
    {
        debug("Add Group -----");

        if (isNotGroupMember(creatorID))
        {
            Group theGroup = new Group(getNextGroupID(), creatorID);
            Member theMember = new Member(creatorID);
            theGroup.addMember(theMember);
            groups.add(theGroup);
            debug("  addGroup groupID: " + theGroup.getGroupID()+ ", [" + theMember.getMemberID() + "]");
            sync();
        } else
            debug("Can't create a group if you are a member of a group.");
    }

    /**
     * @param groupID target group
     * @param memberID member to add
     */
    public static void addMember(int groupID, int memberID)
    {
        if (groups.isEmpty())
        {
            debug("No group exists!");
            return;
        }
        Group group = getGroup(groupID);

        /* Grab instance of the other player */
        PlayerEntity playerInitiator = getEntityPlayer(memberID);
        if (!group.isEmpty() && isNotGroupMember(memberID))
        {
            /* Grab instance of the leader */
            PlayerEntity playerTarget = getEntityPlayer(group.getLeaderEntityID());
            if (group.getMembers().size() < GroupHelper.MAX_MEMBERS)
            {
                Member member =new Member(memberID);
                group.addMember(member);
                debug("addMember: groupID: " + groupID + ", memberID " + member.getMemberID());
                sync();
                playerInitiator.sendMessage(new TranslationTextComponent("mxtune.chat.groupManager.you_joined_players_group", playerTarget.getDisplayName()));
                playerTarget.sendMessage(new TranslationTextComponent("mxtune.chat.groupManager.player_joined_the_group", playerInitiator.getDisplayName()));
            }
            else
            {
                debug("Can't join. Too many members.");
                playerInitiator.sendMessage(new TranslationTextComponent("mxtune.chat.groupManager.cannot_join_too_many", playerTarget.getDisplayName()));
                playerTarget.sendMessage(new TranslationTextComponent("mxtune.chat.groupManager.player_cannot_join_too_many", playerInitiator.getDisplayName()));
            }
        }
        else
        {
            debug("Can't join a group if you are a member of a group.");
            playerInitiator.sendMessage(new TranslationTextComponent("mxtune.chat.groupManager.cannot_join_if_group_member"));
        }
    }

    /**
     * Removes a member from all groups potentially changing the leader of a
     * group or removing the group entirely.
     * 
     * @param memberID to be removed
     */
    public static void removeMember(int memberID)
    {
        debug("removeMember " + memberID);
        PlayManager.purgeMember(memberID);
        if (isNotGroupMember(memberID))
        {
            debug(memberID + " is not a member of a group.");
            return;
        }

        for (Group theGroup : groups)
            for (Member theMember : theGroup.getMembers())
            {
                if (
                        tryRemoveMember(theGroup, theMember, memberID) ||
                        tryRemoveGroupIfLast(theGroup, theMember, memberID) ||
                        tryRemoveLeaderPromoteNext(theGroup, theMember, memberID))
                    return;
            }
    }

    private static boolean tryRemoveMember(Group theGroup, Member theMember, Integer memberID)
    {
        boolean result = false;
        /* This is not the leader so simply remove the member. */
        if (theMember.getMemberID().equals(memberID) && !theGroup.getLeaderEntityID().equals(memberID))
        {
            /* This is not the leader so simply remove the member. */
            theGroup.getMembers().remove(theMember);
            debug( "  memberID: " + theMember.getMemberID() + " from groupID: " + theGroup.getGroupID());
            sync();
            result = true;
        }
        return result;
    }

    private static boolean tryRemoveGroupIfLast(Group theGroup, Member theMember, Integer memberID)
    {
        boolean result = false;
        /* This is the leader of the group and if we are the last or only member then we will remove the group. */
        if (theMember.getMemberID().equals(memberID) && (theGroup.getMembers().size() == 1))
        {
            debug( "  memberIDe: " + theMember.getMemberID() + " is the last member so remove groupID: " + theGroup.getGroupID());
            theGroup.getMembers().clear();
            groups.remove(theGroup);
            sync();
            result = true;
        }
        return result;
    }

    private static boolean tryRemoveLeaderPromoteNext(Group theGroup, Member theMember, Integer memberID)
    {
        boolean result = false;
        // Remove the leader
        if (theMember.getMemberID().equals(memberID) && theGroup.getLeaderEntityID().equals(memberID))
        {
            theGroup.getMembers().remove(theMember);

            /* Promote the next member of the group to leader. */
            Iterator<Member> remainingMembers = theGroup.getMembers().iterator();
            if (remainingMembers.hasNext())
            {
                theMember = remainingMembers.next();
                theGroup.setLeaderEntityID(theMember.getMemberID());
                debug(theMember.getMemberID() + " is promoted to leader of groupID: " + theGroup.getGroupID());
                sync();
                result = true;
            }
        }
        return result;
    }

    static boolean isLeader(Integer entityID)
    {
        Group group = getMembersGroup(entityID);
        return !group.isEmpty() && group.getLeaderEntityID().equals(entityID);
    }
    
    /**
     * setLeader
     * @param memberID the new leader
     */
    public static void setLeader(Integer memberID)
    {
        debug("setLeader: " + memberID);
        Group group = getMembersGroup(memberID);
        if (!group.isEmpty())
        {
            group.setLeaderEntityID(memberID);
            debug("  GroupID: " + group.getGroupID() + ", leaderName: " + getEntityPlayer(memberID).getName());
            sync();
        }
    }

    /**
     * Searches all groups and returns the group or null.
     *
     * @param groupID member in question
     * @return the group or null.
     */
    private static Group getGroup(Integer groupID)
    {
        for (Group theGroup : groups)
        {
            if (theGroup.getGroupID().equals(groupID)) return theGroup;
        }
        return Group.EMPTY;
    }

    /**
     * @param memberID search all groups for thia member.
     * @return the Group if found or null.
     */
    static Group getMembersGroup(Integer memberID)
    {
        for (Group theGroup : groups)
            for (Member theMember : theGroup.getMembers())
                if (theMember.getMemberID().equals(memberID)) return theGroup;

        return Group.EMPTY;
    }

    /**
     * Search all groups for the memberID.
     * 
     * @param memberID member to search for
     * @return true if the memberID is not found.
     */
    private static boolean isNotGroupMember(Integer memberID)
    {
        boolean notMember = false;
        for (Group theGroup : groups)
            for (Member theMember : theGroup.getMembers())
                if (theMember.getMemberID().equals(memberID)) notMember = true;

        return !notMember;
    }

    static void setMembersPartDuration(Integer membersID, int duration)
    {
        Group group = getMembersGroup(membersID);
        if (!group.isEmpty())
            group.setPartDuration(duration);
    }

    static int getGroupDuration(Integer membersID)
    {
        Group group = getMembersGroup(membersID);
        if (!group.isEmpty())
            return group.getMaxDuration();
        else
        {
            ModLogger.error("%s %s", GroupManager.class.getSimpleName(),
                            "#getGroupDuration was passed an invalid memberID which resulted in a null groupID which caused this method to return a duration value of ZERO.");
            return 0;
        }
    }

    @SuppressWarnings("unused")
    public static void dump()
    {
        for (Group theGroup : groups)
        {
            debug("Group: " + theGroup.getGroupID());
            debug("  Leader: " + theGroup.getLeaderEntityID());
            for (Member theMember : theGroup.getMembers())
            {
                debug("    member: " + theMember.getMemberID());
            }
        }
    }

    public static void sync()
    {
        StringBuilder buildGroups = new StringBuilder("|");
        StringBuilder buildMembers = new StringBuilder("|");

        debug("Sync to client -----");
        for (Group theGroup : groups)
        {

            debug("Group: " + theGroup.getGroupID());
            debug("  Leader: " + theGroup.getLeaderEntityID());
            buildGroups.append(theGroup.getGroupID()).append("=").append(theGroup.getLeaderEntityID()).append("|");
            for (Member theMember : theGroup.getMembers())
            {
                debug("    member: " + theMember.getMemberID());
                buildMembers.append(theMember.getMemberID()).append("=").append(theGroup.getGroupID()).append("|");
            }
        }
        /* sync server */
        GroupHelper.setClientGroups(buildGroups.toString());
        GroupHelper.setClientMembers(buildMembers.toString());
        /* sync to clients */
        PacketDispatcher.sendToAll(new SyncGroupMessage(buildGroups.toString(), buildMembers.toString()));
    }


    /* Forge and FML Event Handling */

    // TODO: Add a yes/no gui to ask target user for approval -OR- Tell initiator party is full, or to provide a password.
    @SubscribeEvent
    public void onEntityInteractEvent(EntityInteract event)
    {
        if (isJAMPartyRightClickDisabled()) return;

        if ((event.getTarget() instanceof PlayerEntity) && (event.getEntityLiving() instanceof PlayerEntity) && (event.getHand() == Hand.MAIN_HAND) && !event.getWorld().isRemote)
        {
            PlayerEntity playerInitiator = event.getEntityPlayer();
            PlayerEntity playerTarget = (PlayerEntity) event.getTarget();

            debug(playerInitiator.getName() + " pokes " + playerTarget.getName());
            Group targetGroup = getMembersGroup(playerTarget.getEntityId());
            Group initiatorGroup = getMembersGroup(playerInitiator.getEntityId());
            if ((!targetGroup.isEmpty()) && targetGroup.getLeaderEntityID().equals(playerTarget.getEntityId())
                    && initiatorGroup.isEmpty())
            {
                if (!notMuted(playerInitiator, playerTarget)) return;
                setSParams(playerInitiator, targetGroup.getGroupID().toString(), "", "");
                PacketDispatcher.sendTo(new JoinGroupMessage(targetGroup.getGroupID()), (ServerPlayerEntity) playerInitiator);
            }
            else if (!targetGroup.isEmpty())
            {
                if (!isLeader(playerTarget.getEntityId()) && initiatorGroup.isEmpty())
                    playerInitiator.sendMessage(new TranslationTextComponent("mxtune.chat.groupManager.player_not_leader", playerTarget.getName()));
                else
                    playerInitiator.sendMessage(new TranslationTextComponent("mxtune.chat.groupManager.cannot_join_if_group_member"));
            }
        }
    }

    private boolean notMuted(PlayerEntity playerInitiator, PlayerEntity playerTarget)
    {
        boolean noMute = true;
        if (isMuteAll(playerInitiator))
        {
            // MuteALL is true so playerInitiator can't join
            playerInitiator.sendMessage(new TranslationTextComponent("mxtune.chat.gm.noJoinGroupWhenMuteAll"));
            noMute = false;
        } else
        {
            if (!playerNotMuted(playerInitiator, playerTarget))
            {
                // target fails the mute options check
                playerInitiator.sendMessage(new TranslationTextComponent("mxtune.chat.gm.noJoinGroupWhenPlayerIsMuted", playerTarget.getName()));
                noMute = false;
            }
        }
        return noMute;
    }

    @SubscribeEvent
    public void onPlayerSleepInBedEvent(PlayerSleepInBedEvent event)
    {
        Group group = getMembersGroup(event.getEntityPlayer().getEntityId());
        if (!group.isEmpty())
        {
            event.setResult(SleepResult.NOT_POSSIBLE_NOW);
            event.getEntityPlayer().sendMessage(new TranslationTextComponent("mxtune.chat.gm.noSleepInJam"));
        }
    }

    @SubscribeEvent
    public void onLivingDeathEvent(LivingDeathEvent event)
    {
        if (event.getEntityLiving() instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            removeMember(player.getEntityId());
        }
    }

    /* FML Gaming Events */
    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerLoggedInEvent event)
    {
        if(MXTune.proxy.getEffectiveSide() == Side.SERVER)
        {
            GroupManager.sync();
            ServerCSDManager.queryClient(event.player);
            ModWorldPlaylistHelper.sync(event.player, event.player.world);
        }
    } 
    
    @SubscribeEvent
    public void onPlayerLoggedOutEvent(PlayerLoggedOutEvent event)
    {
        if(MXTune.proxy.getEffectiveSide() == Side.SERVER)
        {
            removeMember(event.player.getEntityId());
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimensionEvent(PlayerChangedDimensionEvent event)
    {
        if(MXTune.proxy.getEffectiveSide() == Side.SERVER)
        {
            removeMember(event.player.getEntityId());
            ModWorldPlaylistHelper.sync(event.player, event.player.world);
        }
    }

    @SubscribeEvent
    public static void onEvent(PlayerEvent.PlayerRespawnEvent event)
    {
        if(MXTune.proxy.getEffectiveSide() == Side.SERVER)
        {
            GroupManager.sync();
            ServerCSDManager.queryClient(event.player);
            ModWorldPlaylistHelper.sync(event.player, event.player.world);
        }
    }

    private static PlayerEntity getEntityPlayer(Integer entityID)
    {
        return MXTune.proxy.getPlayerByEntityID(entityID);
    }

    // for debugging
    @SuppressWarnings("unused")
    private static void debug(String message) { ModLogger.debug("----- " + message); }
}
