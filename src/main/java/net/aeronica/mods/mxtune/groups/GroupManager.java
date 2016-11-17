/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.groups;

import java.util.HashSet;
import java.util.Iterator;

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.JoinGroupMessage;
import net.aeronica.mods.mxtune.network.client.SyncGroupMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.Side;

// Notes: For saving to disk use UUIDs. For client-server communication use getEntityID. Done.
// UUID does not work on the client.
public class GroupManager
{
    
    private GroupManager() {}
    private static class GroupManagerHolder {public static final GroupManager INSTANCE = new GroupManager();}
    public static GroupManager getInstance() {return GroupManagerHolder.INSTANCE;}

    /*
     * The guts of the GroupManager - After looking over this weeks later I can
     * see I made some bad decisions, but I'll live with it for now. It's called
     * fail fast! Learn from your mistake and move on.
     * 
     * 2016-Oct-21 Converted the whole shibang from String Player Names to Integer
     * Entity IDs. That simplified usage a bit. 
     * 
     * Sync'd to the Client side using the GROUPS.class and
     * the associated networking classes.
     */
    private static class Member
    {
        public Integer memberEntityID;
    }

    private static class Group
    {
        public Integer groupID;
        public Integer leaderEntityID;
        public HashSet<Member> members;
    }

    private static HashSet<Group> groups = null;

    /**
     * Any player can be a leader or in a group. A player who makes a group is
     * the leader of the group. A player in a group can't join another group.
     * If the leader leaves a group another member will be promoted to group
     * leader automatically.
     * 
     * @param creatorID
     * @return true is successful
     */
    public static boolean addGroup(Integer creatorID)
    {
        log("addGroup " + creatorID);
        if (groups == null)
        {
            groups = new HashSet<Group>(1, 0.3f);
        }

        if (getGroup(creatorID) == null && (groupsHaveMember(creatorID) == null))
        {

            Group theGroup = new Group();

            theGroup.groupID = PlayManager.getNextPlayID();
            
            theGroup.leaderEntityID = creatorID;

            Member theMember = new Member();
            theMember.memberEntityID = creatorID;

            theGroup.members = new HashSet<Member>(GROUPS.MAX_MEMBERS);
            theGroup.members.add(theMember);

            groups.add(theGroup);
            sync();
            return true;
        }
        log("----- Can't create a group if you are a member of a group.");
        return false;
    }

    /**
     * addMember TODO: setup language file keys
     * 
     * @param groupID
     * @param memberID
     * @return
     */
    public static boolean addMember(Integer groupID, Integer memberID)
    {
        if (groups != null && !groups.isEmpty())
        {
            Group g = getGroup(groupID);

            /** Grab instances of the leader and other player */
            EntityPlayer playerTarget = getEntityPlayer(g.leaderEntityID);
            EntityPlayer playerInitiator = getEntityPlayer(memberID);

            Member n = groupsHaveMember(memberID);
            log("addMember " + groupID + " : " + memberID);
            if ((g != null) && (n == null))
            {
                if (g.members.size() < GROUPS.MAX_MEMBERS)
                {
                    Member m = new Member();
                    m.memberEntityID = memberID;
                    g.members.add(m);
                    sync();

                    playerInitiator.addChatMessage(new TextComponentString("You Joined " + playerTarget.getDisplayName().getFormattedText() + "'s group"));
                    playerTarget.addChatMessage(new TextComponentString(playerInitiator.getDisplayName().getFormattedText() + " joined the group"));
                    return true;
                }

                log("----- Can't join. Too many members.");
                playerInitiator.addChatMessage(new TextComponentString("You can't join " + playerTarget.getDisplayName().getFormattedText() + "'s group. Too many members."));
                playerTarget.addChatMessage(new TextComponentString(playerInitiator.getDisplayName().getFormattedText() + " can't join group. Too many members."));
                return false;
            }

            log("----- Can't join a group if you are a member of a group.");
            playerInitiator.addChatMessage(new TextComponentString("You can't join a group if you are a member of a group."));
            return false;
        }
        log("----- No group exists!");
        return false;
    }

    /**
     * Removes a member from all groups potentially changing the leader of a
     * group or removing the group entirely.
     * 
     * @param memberID
     * @return the group of the member or null.
     */
    public static Group removeMember(Integer memberID)
    {
        log("removeMember " + memberID);
        PlayManager.dequeueMember(memberID);
        if (groups != null && !groups.isEmpty())
        {
            Group theGroup;
            Member theMember;
            for (Iterator<Group> ig = groups.iterator(); ig.hasNext();)
            {
                theGroup = ig.next();
                for (Iterator<Member> im = theGroup.members.iterator(); im.hasNext();)
                {
                    theMember = (Member) im.next();
                    if (theMember.memberEntityID.equals(memberID))
                    {

                        if (!theGroup.leaderEntityID.equals(memberID))
                        {
                            /** This is not the leader so simply remove the member. */
                            im.remove();
                            log("----- removed " + memberID);
                            sync();
                            return theGroup;
                        } else
                        {
                            /** This is the leader of the group and if we are the last or only member then we will remove the group. */
                            if (theGroup.members.size() == 1)
                            {
                                log("----- " + theMember.memberEntityID + " is the last member so remove the group");
                                theGroup.members.clear();
                                theGroup.members = null;
                                ig.remove();
                                sync();
                                return null;
                            }
                            /** Remove the leader */
                            im.remove();
                            sync();
                            /** Promote the next member of the group to leader. */
                            Iterator<Member> ix = theGroup.members.iterator();
                            if (ix.hasNext())
                            {
                                theMember = (Member) ix.next();
                                theGroup.leaderEntityID = theMember.memberEntityID;
                                log("----- " + theMember.memberEntityID + " is promoted to the group leader");
                                sync();
                                return theGroup;
                            }
                        }
                    }
                }
            }
        }
        log("----- " + memberID + " is not a member of a group.");
        return null;
    }

    public static boolean isLeader(Integer entityID)
    {
        Group g = getMembersGroup(entityID);
        return (g != null) ? g.leaderEntityID == entityID : false;
    }
    
    /**
     * setLeader A rather unsafe way to change the leader of the group, but this
     * will do for now
     * 
     * @param memberID
     * @return success or failure.
     */
    public static boolean setLeader(Integer memberID)
    {
        boolean result = false;
        Group g = getMembersGroup(memberID);
        if (g != null)
        {
            g.leaderEntityID = memberID;
            sync();
            result = true;
        }
        return result;
    }

    public static Integer getMembersGroupID(Integer memberID)
    {
        Group group = getMembersGroup(memberID);
        return group == null ? null : group.groupID;
    }

    /**
     * Searches all groups and returns the group or null.
     * 
     * @param creatorID
     * @return the group or null.
     */
    protected static Group getGroup(Integer creatorID)
    {
        if (groups != null && !groups.isEmpty())
        {
            for (Iterator<Group> it = groups.iterator(); it.hasNext();)
            {
                Group theGroup = it.next();
                if (theGroup.groupID.equals(creatorID)) return theGroup;
            }
        }
        return null;
    }

    /**
     * Search all groups for the named member.
     * 
     * @param memberID
     * @return the Group if found or null.
     */
    public static Group getMembersGroup(Integer memberID)
    {
        if (groups != null && !groups.isEmpty())
        {
            for (Iterator<Group> it = groups.iterator(); it.hasNext();)
            {
                Group theGroup = it.next();
                for (Iterator<Member> im = theGroup.members.iterator(); im.hasNext();)
                {
                    Member theMember = (Member) im.next();
                    if (theMember.memberEntityID.equals(memberID)) return theGroup;
                }
            }
        }
        return null;
    }

    /**
     * Search all groups for the named member.
     * 
     * @param groups
     * @param creatorID
     * @return the members if found or null.
     */
    protected static Member groupsHaveMember(Integer creatorID)
    {
        if (groups != null && !groups.isEmpty())
        {
            for (Iterator<Group> it = groups.iterator(); it.hasNext();)
            {
                Group theGroup = it.next();
                for (Iterator<Member> im = theGroup.members.iterator(); im.hasNext();)
                {
                    Member theMember = (Member) im.next();
                    if (theMember.memberEntityID.equals(creatorID)) return theMember;
                }
            }
        }
        return null;
    }

    public static void dump()
    {
        if (groups != null && !groups.isEmpty())
        {
            for (Iterator<Group> it = groups.iterator(); it.hasNext();)
            {
                Group theGroup = it.next();
                debug("Group: " + theGroup.groupID);
                debug("  Leader: " + theGroup.leaderEntityID);
                for (Iterator<Member> im = theGroup.members.iterator(); im.hasNext();)
                {
                    Member theMember = (Member) im.next();
                    debug("    member: " + theMember.memberEntityID);
                }
            }
        }
    }

    public static void sync()
    {
        StringBuilder buildgroups = new StringBuilder("|");
        StringBuilder buildmembers = new StringBuilder("|");

        if (groups != null && !groups.isEmpty())
        {
            for (Iterator<Group> it = groups.iterator(); it.hasNext();)
            {
                Group theGroup = it.next();
                debug("Group: " + theGroup.groupID);
                debug("  Leader: " + theGroup.leaderEntityID);
                buildgroups.append(theGroup.groupID).append("=").append(theGroup.leaderEntityID).append("|");
                for (Iterator<Member> im = theGroup.members.iterator(); im.hasNext();)
                {
                    Member theMember = (Member) im.next();
                    debug("    member: " + theMember.memberEntityID);
                    buildmembers.append(theMember.memberEntityID).append("=").append(theGroup.groupID).append("|");
                }
            }
        }
        /** sync server */
        GROUPS.setClientGroups(buildgroups.toString());
        GROUPS.setClientMembers(buildmembers.toString());
        /** sync to clients */
        PacketDispatcher.sendToAll(new SyncGroupMessage(buildgroups.toString(), buildmembers.toString()));
    }

    private static int interactFlag = 0;
    /** Forge and FML Event Handling */
    /**
     * TODO: Add a yes/no gui to ask user if that want to join. Indicate if a
     * party is full, or if it requires a password.
     * @param event
     */
    @SubscribeEvent
    public void onEntityInteractEvent(EntityInteract event)
    {
        if (event.getTarget() != null && event.getTarget() instanceof EntityPlayer && event.getEntityLiving() instanceof EntityPlayer)
        {
            EntityPlayer playerInitiator = (EntityPlayer) event.getEntityPlayer();
            EntityPlayer playerTarget = (EntityPlayer) event.getTarget();

            ModLogger.debug(playerInitiator.getDisplayName().getUnformattedText() + " pokes " + playerTarget.getDisplayName().getUnformattedText());

            if ((event.getSide() == Side.SERVER) && (interactFlag++ % 2) == 0)
            {
                
                Group targetGroup = getMembersGroup(playerTarget.getEntityId());
                if (targetGroup != null && targetGroup.leaderEntityID.equals(playerTarget.getEntityId()) /* && initatorGroup == null */)
                {
                    if (MusicOptionsUtil.isMuteAll(playerInitiator) == false)
                    {
                        if (MusicOptionsUtil.getMuteResult(playerInitiator, playerTarget) == false)
                        {
                            MusicOptionsUtil.setSParams(playerInitiator, targetGroup.groupID.toString(), "", "");
                            PacketDispatcher.sendTo(new JoinGroupMessage(targetGroup.groupID), (EntityPlayerMP) playerInitiator);
                        } else
                        {
                            /** target fails the mute options check */
                            playerInitiator.addChatComponentMessage(new TextComponentTranslation("mxtune.chat.gm.noJoinGroupWhenPlayerIsMuted", new Object[] {playerTarget.getDisplayName().getUnformattedText()}));
                        }
                    } else
                    {
                        /** MuteALL is true so playerInitator can't join */
                        playerInitiator.addChatComponentMessage(new TextComponentTranslation("mxtune.chat.gm.noJoinGroupWhenMuteAll"));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerSleepInBedEvent(PlayerSleepInBedEvent event)
    {
        Group group = getMembersGroup(event.getEntityPlayer().getEntityId());
        if (group != null)
        {
            event.setResult(SleepResult.NOT_POSSIBLE_NOW);
            event.getEntityPlayer().addChatComponentMessage(new TextComponentTranslation("mxtune.chat.gm.noSleepInJam"));
        }
    }

    @SubscribeEvent
    public void onLivingDeathEvent(LivingDeathEvent event)
    {
        if (event.getEntityLiving() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            removeMember(player.getEntityId());
        }
    }

    // @SubscribeEvent
    public void onLivingAttackEvent(LivingAttackEvent event) {}

    /** FML Gaming Events */
    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityPlayerMP)
        {
            GroupManager.sync();
        }
    }
    
    @SubscribeEvent
    public void onPlayerLoggedOutEvent(PlayerLoggedOutEvent event)
    {
        removeMember(event.player.getEntityId());
    }

    // @SubscribeEvent
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {}

    @SubscribeEvent
    public void onPlayerChangedDimensionEvent(PlayerChangedDimensionEvent event)
    {
        removeMember(event.player.getEntityId());
    }

    private static void debug(String strMessage) {/*ModLogger.debug(strMessage);*/}

    private static void log(String strMessage) {/*ModLogger.logInfo(strMessage);*/}

    private static EntityPlayer getEntityPlayer(Integer leaderEntityID)
    {
        MinecraftServer world = FMLCommonHandler.instance().getMinecraftServerInstance();
        return (EntityPlayer) world.getServer().getEntityWorld().getEntityByID(leaderEntityID);
    }
    
}
