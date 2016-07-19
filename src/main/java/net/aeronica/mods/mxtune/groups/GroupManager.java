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

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.capabilities.IJamPlayer;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.JoinGroupMessage;
import net.aeronica.mods.mxtune.network.client.SyncGroupMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;


public class GroupManager
{
    private GroupManager() {}
    private static class GroupManagerHolder {public static final GroupManager INSTANCE = new GroupManager();}
    public static GroupManager getInstance() {return GroupManagerHolder.INSTANCE;}

    /**
     * The guts of the GroupManager - After looking over this weeks later I can
     * see I made some bad decisions, but I'll live with it for now. It's called
     * fail fast! Learn from your mistake and move on.
     */
    private static class Member
    {
        public String memberName;
    }

    private static class Group
    {
        public String groupID;
        public String leaderName;
        public HashSet<Member> members;
    }

    private static HashSet<Group> groups = null;
    private static Long groupID = 0L;

    /**
     * Any player can be a leader or in a group. A player who makes a group is
     * the leader of the group. A player in a group can't join another group.
     * Each groups gets a unique ID. If the leader leaves a group another member
     * will be promoted to group leader automatically.
     * 
     * @param creatorName
     * @return true is successful
     */
    public static boolean addGroup(String creatorName)
    {
        log("addGroup " + creatorName);
        if (groups == null)
        {
            groups = new HashSet<Group>(1, 0.3f);
        }

        if (getGroup(creatorName) == null && (groupsHaveMember(creatorName) == null))
        {

            Group theGroup = new Group();

            theGroup.groupID = groupID.toString();
            groupID++;
            theGroup.leaderName = creatorName;

            Member theMember = new Member();
            theMember.memberName = creatorName;

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
     * addMember
     * 
     * @param groupID
     * @param memberName
     * @return
     */
    public static boolean addMember(String groupID, String memberName)
    {
        if (groups != null && !groups.isEmpty())
        {
            Group g = getGroup(groupID);

            /** Grab instances of the leader and other player */
            EntityPlayer playerTarget = getEntityPlayer(g.leaderName);
            EntityPlayer playerInitiator = getEntityPlayer(memberName);

            Member n = groupsHaveMember(memberName);
            log("addMember " + groupID + " : " + memberName);
            if ((g != null) && (n == null))
            {
                if (g.members.size() < GROUPS.MAX_MEMBERS)
                {
                    Member m = new Member();
                    m.memberName = memberName;
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
     * @param name
     * @return the group of the member or null.
     */
    public static Group removeMember(String name)
    {
        log("removeMember " + name);
        PlayManager.getInstance().dequeueMember(name);
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
                    if (theMember.memberName.equalsIgnoreCase(name))
                    {

                        if (!theGroup.leaderName.equalsIgnoreCase(name))
                        {
                            /** This is not the leader so simply remove the member. */
                            im.remove();
                            log("----- removed " + name);
                            sync();
                            return theGroup;
                        } else
                        {
                            /** This is the leader of the group and if we are the last or only member then we will remove the group. */
                            if (theGroup.members.size() == 1)
                            {
                                log("----- " + theMember.memberName + " is the last member so remove the group");
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
                                theGroup.leaderName = theMember.memberName;
                                log("----- " + theMember.memberName + " is promoted to the group leader");
                                sync();
                                return theGroup;
                            }
                        }
                    }
                }
            }
        }
        log("----- " + name + " is not a member of a group.");
        return null;
    }

    /**
     * setLeader A rather unsafe way to change the leader of the group, but this
     * will do for now
     * 
     * @param memberName
     * @return success or failure.
     */
    public static boolean setLeader(String memberName)
    {
        boolean result = false;
        Group g = getMembersGroup(memberName);
        if (g != null)
        {
            g.leaderName = memberName;
            sync();
            result = true;
        }
        return result;
    }

    public boolean isLeader(String name)
    {
        return getLeadersGroup(name) != null ? true : false;
    }

    public String getMembersGroupID(String memberName)
    {
        Group group = getMembersGroup(memberName);
        return group == null ? null : group.groupID;
    }

    /**
     * Searches all groups for this leader and returns the group or null.
     * 
     * @param leaderName
     * @return the group or null.
     */
    protected static Group getLeadersGroup(String leaderName)
    {
        if (groups != null && !groups.isEmpty())
        {
            for (Iterator<Group> it = groups.iterator(); it.hasNext();)
            {
                Group theGroup = it.next();
                if (theGroup.leaderName.equalsIgnoreCase(leaderName)) return theGroup;
            }
        }
        return null;
    }

    /**
     * Searches all groups and returns the group or null.
     * 
     * @param groupID
     * @return the group or null.
     */
    protected static Group getGroup(String groupID)
    {
        if (groups != null && !groups.isEmpty())
        {
            for (Iterator<Group> it = groups.iterator(); it.hasNext();)
            {
                Group theGroup = it.next();
                if (theGroup.groupID.equalsIgnoreCase(groupID)) return theGroup;
            }
        }
        return null;
    }

    /**
     * Search all groups for the named member.
     * 
     * @param memberName
     * @return the Group if found or null.
     */
    public static Group getMembersGroup(String memberName)
    {
        if (groups != null && !groups.isEmpty())
        {
            for (Iterator<Group> it = groups.iterator(); it.hasNext();)
            {
                Group theGroup = it.next();
                for (Iterator<Member> im = theGroup.members.iterator(); im.hasNext();)
                {
                    Member theMember = (Member) im.next();
                    if (theMember.memberName.equalsIgnoreCase(memberName)) return theGroup;
                }
            }
        }
        return null;
    }

    /**
     * Search all groups for the named member.
     * 
     * @param groups
     * @param name
     * @return the members if found or null.
     */
    protected static Member groupsHaveMember(String name)
    {
        if (groups != null && !groups.isEmpty())
        {
            for (Iterator<Group> it = groups.iterator(); it.hasNext();)
            {
                Group theGroup = it.next();
                for (Iterator<Member> im = theGroup.members.iterator(); im.hasNext();)
                {
                    Member theMember = (Member) im.next();
                    if (theMember.memberName.equalsIgnoreCase(name)) return theMember;
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
                debug("  Leader: " + theGroup.leaderName);
                for (Iterator<Member> im = theGroup.members.iterator(); im.hasNext();)
                {
                    Member theMember = (Member) im.next();
                    debug("    member: " + theMember.memberName);
                }
            }
        }
    }

    public static void sync()
    {
        String buildgroups = " ";
        String buildmembers = " ";

        if (groups != null && !groups.isEmpty())
        {
            for (Iterator<Group> it = groups.iterator(); it.hasNext();)
            {
                Group theGroup = it.next();
                debug("Group: " + theGroup.groupID);
                debug("  Leader: " + theGroup.leaderName);
                buildgroups = buildgroups + theGroup.groupID + "=" + theGroup.leaderName + " ";
                for (Iterator<Member> im = theGroup.members.iterator(); im.hasNext();)
                {
                    Member theMember = (Member) im.next();
                    debug("    member: " + theMember.memberName);
                    buildmembers = buildmembers + theMember.memberName + "=" + theGroup.groupID + " ";
                }
            }
        }
        /** sync server */
        GROUPS.setClientGroups(buildgroups.trim());
        GROUPS.setClientMembers(buildmembers.trim());
        /** sync to clients */
        PacketDispatcher.sendToAll(new SyncGroupMessage(buildgroups.trim(), buildmembers.trim()));
    }

    /** Forge and FML Event Handling */
    /**
     * TODO: Add a yes/no gui to ask user if that want to join. Indicate if a
     * party is full, or if it requires a password.
     * @param event
     */
    @SubscribeEvent
    public void onEntityInteractEvent(EntityInteractSpecific event)
    {
        if (event.getTarget() != null && event.getTarget() instanceof EntityPlayer && event.getEntityLiving() instanceof EntityPlayer)
        {

            EntityPlayer playerInitiator = (EntityPlayer) event.getEntityPlayer();
            EntityPlayer playerTarget = (EntityPlayer) event.getTarget();

            ModLogger.logInfo(playerInitiator.getDisplayName().getUnformattedText() + " pokes " + playerTarget.getDisplayName().getUnformattedText());

            if (!event.getEntityPlayer().worldObj.isRemote)
            {
                /** Server side */
                Group targetGroup = getMembersGroup(playerTarget.getDisplayName().getUnformattedText());
                if (targetGroup != null && targetGroup.leaderName.equalsIgnoreCase(playerTarget.getDisplayName()
                        .getUnformattedText()) /* && initatorGroup == null */)
                {

                    IJamPlayer props = playerInitiator.getCapability(MXTuneMain.JAM_PLAYER, null);
                    props.setSParams(targetGroup.groupID, "", "");

                    PacketDispatcher.sendTo(new JoinGroupMessage(targetGroup.groupID), (EntityPlayerMP) playerInitiator);

                    /*
                     * if (addMember(group.groupID,
                     * playerInitiator.getDisplayName())) {
                     * playerInitiator.addChatMessage(new ChatComponentText(
                     * "You Joined " + playerTarget.getDisplayName() +
                     * "'s group")); playerTarget.addChatMessage(new
                     * ChatComponentText( playerInitiator.getDisplayName() +
                     * " joined the group")); }
                     */
                 }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerSleepInBedEvent(PlayerSleepInBedEvent event)
    {
        Group group = getMembersGroup(event.getEntityPlayer().getDisplayName().getUnformattedText());
        if (group != null)
        {
            event.setResult(SleepResult.NOT_POSSIBLE_NOW);
            event.getEntityPlayer().addChatMessage(new TextComponentString("You can't sleep while in a JAM!"));
        }
    }

    @SubscribeEvent
    public void onLivingDeathEvent(LivingDeathEvent event)
    {
        if (event.getEntityLiving() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            removeMember(player.getDisplayName().getUnformattedText());
        }
    }

    // @SubscribeEvent
    public void onLivingAttackEvent(LivingAttackEvent event) {}

    /** FML Gaming Events */
    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerLoggedInEvent event)
    {
        event.player.addChatMessage(new TextComponentString("[" + MXTuneMain.MODNAME + "] Welcome!"));
    }

    @SubscribeEvent
    public void onPlayerLoggedOutEvent(PlayerLoggedOutEvent event)
    {
        removeMember(event.player.getDisplayName().getUnformattedText());
        ModLogger.logInfo("Disconnected " + event.player.getDisplayNameString());
    }

    // @SubscribeEvent
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {}

    @SubscribeEvent
    public void onPlayerChangedDimensionEvent(PlayerChangedDimensionEvent event)
    {
        removeMember(event.player.getDisplayName().getUnformattedText());
    }

    private static void debug(String strMessage) {ModLogger.debug(strMessage);}

    private static void log(String strMessage) {ModLogger.logInfo(strMessage);}

    private static EntityPlayer getEntityPlayer(String name)
    {
        MinecraftServer world = FMLCommonHandler.instance().getMinecraftServerInstance();
        return world.getServer().getEntityWorld().getPlayerEntityByName(name);
    }
}
