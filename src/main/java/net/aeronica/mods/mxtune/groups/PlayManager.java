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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.ibm.icu.impl.Assert;

import net.aeronica.mods.mxtune.blocks.BlockPiano;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.PlayJamMessage;
import net.aeronica.mods.mxtune.network.client.PlaySoloMessage;
import net.aeronica.mods.mxtune.network.client.SyncStatusMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * The SERVER side class for managing playing
 * @author Paul Boese aka Aeronica
 *
 */
public class PlayManager
{
    private PlayManager() {}
    private static class PlayManagerHolder {public static final PlayManager INSTANCE = new PlayManager();}
    public static PlayManager getInstance() {return PlayManagerHolder.INSTANCE;}

    private static Map<Integer, String> membersMML;
    private static HashMap<Integer, String> membersQueuedStatus;
    private static HashMap<Integer, Integer> membersPlayID;
    private static Set<Integer> activePlayIDs;
    private static Integer playID;

    static {
        membersMML = new HashMap<Integer, String>();
        membersPlayID = new HashMap<Integer, Integer>();
        membersQueuedStatus = new HashMap<Integer, String>();
        activePlayIDs = Sets.newHashSet();
        playID = 1;
    }
    
    private static Map<Integer, String> getMapMembersMML() {return membersMML;}
    private static HashMap<Integer, Integer> getHashMapMembersPlayID() {return membersPlayID;}
    private static Set<Integer> getSetActivePlayIDs() {return activePlayIDs;}
    private static HashMap<Integer, String> getHashMapMembersQueuedStatus() {return membersQueuedStatus;}

    private static Integer getNextPlayID() {return (playID == Integer.MAX_VALUE) ? playID=1 : playID++;}
    private static void setPlaying(Integer playerID) {getHashMapMembersQueuedStatus().put(playerID, GROUPS.PLAYING.name());}
    private static void setQueued(Integer playerID) {getHashMapMembersQueuedStatus().put(playerID, GROUPS.QUEUED.name());}

    /**
     * 
     * @param playerIn
     * @param pos       position of block instrument
     * @param isPlaced  true is this is a block instrument
     */
    public static Integer playMusic(EntityPlayer playerIn, BlockPos pos, boolean isPlaced)
    {
        if (MusicOptionsUtil.isMuteAll(playerIn)) return null;
        ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(pos, playerIn, isPlaced);
        if (sheetMusic != null)
        {
            NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("MusicBook");
            if (contents != null)
            {
                Integer playerID = playerIn.getEntityId();
                String title = sheetMusic.getDisplayName();
                String mml = contents.getString("MML");

                mml = mml.replace("MML@", "MML@I" + getPatch(pos, playerIn, isPlaced));
                ModLogger.debug("MML Title: " + title);
                ModLogger.debug("MML Sub25: " + mml.substring(0, (mml.length() >= 25 ? 25 : mml.length())));

                if (GroupManager.getMembersGroupID(playerID) == null)
                {
                    /** Solo Play */
                    ModLogger.debug("playMusic playSolo");
                    return playSolo(playerIn, mml, playerID);
                } else
                {
                    /** Jam Play */
                    ModLogger.debug("playMusic queueJam");
                    return queueJam(playerIn, mml, playerID);
                }                
            }
        }
        return null;
    }

    private static Integer playSolo(EntityPlayer playerIn, String mml, Integer playerID)
    {
        Integer playID = getNextPlayID();
        queue(playID, playerID, mml);
        String musicText = getMML(playID);
        getSetActivePlayIDs().add(playID);
        syncStatus();
        PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playID, musicText);
        PacketDispatcher.sendToAllAround(packetPlaySolo, playerIn.dimension, playerIn.posX, playerIn.posY, playerIn.posZ, ModConfig.getListenerRange());
        return playID;
    }
    
    private static Integer queueJam(EntityPlayer playerIn, String mml, Integer playerID)
    {
        Integer groupsPlayID = getGroupsPlayID(playerID);
        /** Queue members parts */
        queue(groupsPlayID, playerID, mml);
        syncStatus();
        /** Only send the groups MML when the leader starts the JAM */
        if (GroupManager.isLeader(playerID))
        {
            String musicText = getMML(groupsPlayID);
            Vec3d pos = getMedianPos(groupsPlayID);
            getSetActivePlayIDs().add(groupsPlayID);
            syncStatus();
            resetGroupsPlayID(playerID);
            PlayJamMessage playJamMessage = new PlayJamMessage(playerID, groupsPlayID, musicText);
            PacketDispatcher.sendToAllAround(playJamMessage, playerIn.dimension, pos.xCoord, pos.yCoord, pos.zCoord, ModConfig.getListenerRange());
        }
        return groupsPlayID;
    }

    /**
     * Reset the groups PlayID to null.
     * It's only needed for queuing the MML parts and should be used when the leader kicks off the session.
     * 
     * @param membersID
     */
    private static void resetGroupsPlayID(Integer membersID)
    {
        GroupManager.Group g = GroupManager.getMembersGroup(membersID);
        Assert.assrt("*** PlayManager#resetGroupsPlayID(Integer memberID): " + membersID + " is not a group member ***", g!=null);
        g.playID = null;
    }
    
    /**
     * Generate a new PlayID if this is the first member to queue, or return the existing one.
     * This assumes the member is already been validated as a member of the group
     * 
     * @param membersID
     * @return
     */
    private static Integer getGroupsPlayID(Integer membersID)
    {
        GroupManager.Group g = GroupManager.getMembersGroup(membersID);
        Assert.assrt("*** PlayManager#getGroupsPlayID(Integer memberID): " + membersID + " is not a group member ***", g!=null);
        if (g.playID == null)
        {
            return g.playID = getNextPlayID();
        } else
        {
            return g.playID;
        }
    }
    
    public static boolean isPlayerPlaying(EntityPlayer playerIn)
    {
        Integer entityID = playerIn.getEntityId();
        return (getHashMapMembersPlayID() != null && !getHashMapMembersPlayID().isEmpty()) ? getHashMapMembersPlayID().containsKey(entityID) : false;
    }
    
    public static boolean isActivePlayID(Integer playID) { return getSetActivePlayIDs() != null ? getSetActivePlayIDs().contains(playID) : false; }
    
    public static boolean hasPlayID(Integer playID)
    {
        return (getHashMapMembersPlayID() != null && !getHashMapMembersPlayID().isEmpty()) ? getHashMapMembersPlayID().containsValue(playID) : false;
    }
    
    public static Set<Integer> getMembersByPlayID(Integer playID) 
    {
        Set<Integer> members = Sets.newHashSet();
        if (getHashMapMembersPlayID() != null)
        {
            for(Integer someMember: getHashMapMembersPlayID().keySet())
            {
                if(getHashMapMembersPlayID().get(someMember).equals(playID))
                {
                    members.add(someMember);
                }
            }
        }
        return members;
    }
        
    public static void stopPlayID(Integer playID)
    {
        Set<Integer> memberSet = getMembersByPlayID(playID);
        for(Integer member: memberSet)
        {
            if (getHashMapMembersPlayID() != null && getHashMapMembersPlayID().containsKey(member))
            {
                getHashMapMembersPlayID().remove(member, playID);
            }
            if (getHashMapMembersQueuedStatus() != null && getHashMapMembersQueuedStatus().containsKey(member))
            {
                getHashMapMembersQueuedStatus().remove(member);
            }
        }
        dequeuePlayID(playID);
        syncStatus();        
    }
    
    private static void dequeuePlayID(Integer playID) {if (getSetActivePlayIDs() != null) getSetActivePlayIDs().remove(playID);}
    
    private static int getPatch(BlockPos pos, EntityPlayer playerIn, boolean isPlaced)
    {
        if (isPlaced)
        {
            if (playerIn.worldObj.getBlockState(pos).getBlock() instanceof BlockPiano)
            {
                BlockPiano piano = (BlockPiano) playerIn.worldObj.getBlockState(pos).getBlock();
                return piano.getPatch();
            }
        } else
        {
            ItemInstrument.EnumInstruments enumInst = ItemInstrument.EnumInstruments.byMetadata(playerIn.getHeldItemMainhand().getMetadata());
            return enumInst.getPatch();
        }
        return 0;
    }
    
    private static void syncStatus()
    {
        /** server side */
        GROUPS.setClientPlayStatuses(GROUPS.serializeIntStrMap(getHashMapMembersQueuedStatus()));
        GROUPS.setPlayIDMembers(GROUPS.serializeIntIntMap(getHashMapMembersPlayID()));
        GROUPS.setActivePlayIDs(GROUPS.serializeIntegerSet(getSetActivePlayIDs()));
        /** client side */
        PacketDispatcher.sendToAll(new SyncStatusMessage(GROUPS.serializeIntStrMap(getHashMapMembersQueuedStatus()), GROUPS.serializeIntIntMap(getHashMapMembersPlayID()), GROUPS.serializeIntegerSet(getSetActivePlayIDs())));
    }

    private static void queue(Integer playID, Integer memberID, String mml)
    {
        try
        {
            getMapMembersMML().put(memberID, mml);
            getHashMapMembersPlayID().put(memberID, playID);
            setQueued(memberID);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Returns a string in Map ready format. e.g.
     * "playerId1=MML@...;|playerId2=MML@...|playerId3=MML@..."
     * 
     * @param playID
     * @return string in Map ready format.
     */
    private static String getMML(Integer playID)
    {
        StringBuilder buildMML = new StringBuilder("");
        try
        {
            Set<Integer> keys = getHashMapMembersPlayID().keySet();
            Iterator<Integer> it = keys.iterator();
            while (it.hasNext())
            {
                Integer member = (Integer) it.next();
                Integer memberPlayID = getHashMapMembersPlayID().get(member);
                if (memberPlayID.equals(playID))
                {
                    buildMML.append(member).append("=").append(getMapMembersMML().get(member)).append("|");
                    getMapMembersMML().remove(member);
                    setPlaying(member);
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return buildMML.toString();
    }

    /**
     * 
     * @param playID
     * @return
     */
    public static Vec3d getMedianPos(Integer playID)
    {
        double x, y, z, count; x = y = z = count = 0;
        Vec3d pos;
        for(Integer member: getMembersByPlayID(playID))
        {   
            EntityPlayer player = (EntityPlayer) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getEntityByID(member);
            if(player == null) continue;
            x = x + player.getPositionVector().xCoord;
            y = y + player.getPositionVector().yCoord;
            z = z + player.getPositionVector().zCoord;
            count++;
        }            

        if (count == 0) return new Vec3d(0,0,0);
        x/=count;
        y/=count;
        z/=count;
        pos = new Vec3d(x,y,z);
        return pos;
    }

    /**
     * Called by server tick once every two seconds to calculate the distance between
     * group members and to stop the playID if the distance exceeds stopDistane.
     * 
     * @param stopDistance
     */
    public static void testStopDistance(double stopDistance)
    {
        if (getSetActivePlayIDs() != null && !getSetActivePlayIDs().isEmpty())
        {
            double distance = 0;
            for(Integer playID: getSetActivePlayIDs())
            {
                if (getMembersByPlayID(playID) != null && !getMembersByPlayID(playID).isEmpty())
                {
                    for (Integer memberA: getMembersByPlayID(playID))
                    {
                        for (Integer memberB:  getMembersByPlayID(playID) )
                        {
                            if (memberA != memberB)
                            {
                               distance = getMemberVector(memberA).distanceTo(getMemberVector(memberB));
                               if (distance > stopDistance) PlayManager.stopPlayID(playID);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static Vec3d getMemberVector(Integer entityID)
    {
        Vec3d v3d;
        EntityPlayer player = (EntityPlayer) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getEntityByID(entityID);
        if (player != null)
            v3d = new Vec3d(player.posX, player.prevPosY, player.posZ);
        else
            v3d = new Vec3d(0,0,0);
        return v3d;
    }
    
    /**
     * Used by the GroupMananger to purge unused/aborted Jam data
     * 
     * @param memberID
     */
    public static void dequeueMember(Integer memberID)
    {
        if (getSetActivePlayIDs() != null && (getHashMapMembersPlayID() != null && !getHashMapMembersPlayID().isEmpty() && getHashMapMembersPlayID().containsKey(memberID)))
        {
            getSetActivePlayIDs().remove(getHashMapMembersPlayID().get(memberID));
        }
        if (getMapMembersMML() != null && !getMapMembersMML().isEmpty() && getMapMembersMML().containsKey(memberID))
        {
            getMapMembersMML().remove(memberID);
        }
        if (getHashMapMembersPlayID() != null && !getHashMapMembersPlayID().isEmpty() && getHashMapMembersPlayID().containsKey(memberID))
        {
            getHashMapMembersPlayID().remove(memberID);
        }
        if (getHashMapMembersQueuedStatus() != null && !getHashMapMembersQueuedStatus().isEmpty() && getHashMapMembersQueuedStatus().containsKey(memberID))
        {
            getHashMapMembersQueuedStatus().remove(memberID);
            syncStatus();
        }
    }
    
}
