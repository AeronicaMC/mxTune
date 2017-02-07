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

import net.aeronica.mods.mxtune.blocks.BlockPiano;
import net.aeronica.mods.mxtune.blocks.IPlacedInstrument;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.PlayJamMessage;
import net.aeronica.mods.mxtune.network.client.PlaySoloMessage;
import net.aeronica.mods.mxtune.network.client.SyncStatusMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * The SERVER side class for managing playing
 * FIXME: Refactor this class along with GROUPS and GroupManager and the associated packet messages
 * @author Paul Boese aka Aeronica
 *
 */
public enum PlayManager
{
    
    INSTANCE;

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
    
    /**
     * Play ID's 1 to Integer.MAX, -1 for invalid, 0 for initialization only, null if not set.
     * @return a unique play id
     */
    private static Integer getNextPlayID() {return (playID == Integer.MAX_VALUE) ? playID=1 : playID++;}

    private static void setPlaying(Integer playerID) {membersQueuedStatus.put(playerID, GROUPS.PLAYING.name());}

    private static void setQueued(Integer playerID) {membersQueuedStatus.put(playerID, GROUPS.QUEUED.name());}

    /**
     * For playing music from an Item
     * @param playerIn
     * @return a unique play id
     */
    public static Integer playMusic(EntityPlayer playerIn)
    {
        return playMusic(playerIn, null, false);
    }
 
    /**
     * For playing music from a block/placed instrument 
     * @param playerIn
     * @param pos position of block instrument
     * @return a unique play id
     */
    public static Integer playMusic(EntityPlayer playerIn, BlockPos pos)
    {
        return playMusic(playerIn, pos, true);
    }
    
    /**
     * For playing music
     * @param playerIn
     * @param pos position of block instrument
     * @param isPlaced true is this is a block instrument
     * @return a unique play id
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
        activePlayIDs.add(playID);
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
            activePlayIDs.add(groupsPlayID);
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
        if (g!=null) g.playID = null;
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
        return (g!=null) ? ((g.playID == null) ? g.playID = getNextPlayID() : g.playID) : null;
    }
    
    public static Integer getPlayersPlayID(Integer entityID)
    {
        playID = (membersPlayID != null && !membersPlayID.isEmpty()) ? membersPlayID.get(entityID) : null;
        return playID;
    }
    
    public static boolean isPlayerPlaying(Integer entityID)
    {
        return (membersPlayID != null && !membersPlayID.isEmpty()) ? membersPlayID.containsKey(entityID) : false;
    }
    
    public static  <T extends EntityLivingBase> boolean isPlayerPlaying(T entityLivingIn)
    {
        return isPlayerPlaying(entityLivingIn.getEntityId());
    }
    
    public static boolean isActivePlayID(Integer playID) { return activePlayIDs != null ? activePlayIDs.contains(playID) : false; }
    
    public static boolean hasPlayID(Integer playID)
    {
        return (membersPlayID != null && !membersPlayID.isEmpty()) ? membersPlayID.containsValue(playID) : false;
    }
    
    public static Set<Integer> getMembersByPlayID(Integer playID) 
    {
        Set<Integer> members = Sets.newHashSet();
        if (membersPlayID != null)
        {
            for(Integer someMember: membersPlayID.keySet())
            {
                if(membersPlayID.get(someMember).equals(playID))
                {
                    members.add(someMember);
                }
            }
        }
        return members;
    }
    
    public static <T extends EntityLivingBase> void stopPlayingPlayer(T entityLivingIn)
    {
        stopPlayingPlayer(entityLivingIn.getEntityId());
    }
    
    public static void stopPlayingPlayer(Integer entityID)
    {
        if (isPlayerPlaying(entityID))
        {
            stopPlayID(PlayManager.getPlayersPlayID(entityID));
        }
    }
    
    public static void stopPlayID(Integer playID)
    {
        if (playID != null) {
            Set<Integer> memberSet = getMembersByPlayID(playID);
            for(Integer member: memberSet)
            {
                if (membersPlayID != null && membersPlayID.containsKey(member))
                {
                    membersPlayID.remove(member, playID);
                }
                if (membersQueuedStatus != null && membersQueuedStatus.containsKey(member))
                {
                    membersQueuedStatus.remove(member);
                }
            }
            dequeuePlayID(playID);
            syncStatus();        
        }
    }
    
    private static void dequeuePlayID(Integer playID) {if (activePlayIDs != null) activePlayIDs.remove(playID);}
    
    private static int getPatch(BlockPos pos, EntityPlayer playerIn, boolean isPlaced)
    {
        if (isPlaced)
        {
            if (playerIn.getEntityWorld().getBlockState(pos).getBlock() instanceof BlockPiano)
            {
                IPlacedInstrument placedInst = (IPlacedInstrument) playerIn.getEntityWorld().getBlockState(pos).getBlock();
                return placedInst.getPatch();
            }
        } else
        {
            IInstrument inst = (IInstrument) playerIn.getHeldItemMainhand().getItem();
            return inst.getPatch(playerIn.getHeldItemMainhand().getMetadata());
        }
        return 0;
    }
    
    private static void syncStatus()
    {
        /** server side */
        GROUPS.setClientPlayStatuses(GROUPS.serializeIntStrMap(membersQueuedStatus));
        GROUPS.setPlayIDMembers(GROUPS.serializeIntIntMap(membersPlayID));
        GROUPS.setActivePlayIDs(GROUPS.serializeIntegerSet(activePlayIDs));
        /** client side */
        PacketDispatcher.sendToAll(new SyncStatusMessage(GROUPS.serializeIntStrMap(membersQueuedStatus), GROUPS.serializeIntIntMap(membersPlayID), GROUPS.serializeIntegerSet(activePlayIDs)));
    }

    private static void queue(Integer playID, Integer memberID, String mml)
    {
        try
        {
            membersMML.put(memberID, mml);
            membersPlayID.put(memberID, playID);
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
            Set<Integer> keys = membersPlayID.keySet();
            Iterator<Integer> it = keys.iterator();
            while (it.hasNext())
            {
                Integer member = (Integer) it.next();
                Integer memberPlayID = membersPlayID.get(member);
                if (memberPlayID.equals(playID))
                {
                    buildMML.append(member).append("=").append(membersMML.get(member)).append("|");
                    membersMML.remove(member);
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
     * @return Vec3d
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
        if (activePlayIDs != null && !activePlayIDs.isEmpty())
        {
            double distance = 0;
            for(Integer playID: activePlayIDs)
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
        if (activePlayIDs != null && (membersPlayID != null && !membersPlayID.isEmpty() && membersPlayID.containsKey(memberID)))
        {
            activePlayIDs.remove(membersPlayID.get(memberID));
        }
        if (membersMML != null && !membersMML.isEmpty() && membersMML.containsKey(memberID))
        {
            membersMML.remove(memberID);
        }
        if (membersPlayID != null && !membersPlayID.isEmpty() && membersPlayID.containsKey(memberID))
        {
            membersPlayID.remove(memberID);
        }
        if (membersQueuedStatus != null && !membersQueuedStatus.isEmpty() && membersQueuedStatus.containsKey(memberID))
        {
            membersQueuedStatus.remove(memberID);
            syncStatus();
        }
    }
    
}
