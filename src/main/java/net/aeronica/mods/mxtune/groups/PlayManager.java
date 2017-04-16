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

import net.aeronica.mods.mxtune.blocks.IPlacedInstrument;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.PlayJamMessage;
import net.aeronica.mods.mxtune.network.client.PlaySoloMessage;
import net.aeronica.mods.mxtune.network.client.StopPlayMessage;
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

    static Map<Integer, String> membersMML = new HashMap<>();
    static HashMap<Integer, Integer> membersQueuedStatus = new HashMap<>();
    static HashMap<Integer, Integer> membersPlayID = new HashMap<>();
    static Set<Integer> activePlayIDs = Sets.newHashSet();
    static int uniquePlayID = 1;
    
    /**
     * Play ID's 1 to Integer.MAX, -1 for invalid, 0 for initialization only, null if not set.
     * @return a unique positive play id
     */
    private static int getNextPlayID()
    {
        if (uniquePlayID == Integer.MAX_VALUE)
            uniquePlayID = 1;
        else
            uniquePlayID++;
        return uniquePlayID;
    }

    private static void setPlaying(Integer playerID) {membersQueuedStatus.put(playerID, GROUPS.PLAYING);}

    private static void setQueued(Integer playerID) {membersQueuedStatus.put(playerID, GROUPS.QUEUED);}

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
     * @return a unique play id or null if unable to play
     */
    public static Integer playMusic(EntityPlayer playerIn, BlockPos pos, boolean isPlaced)
    {
        if (MusicOptionsUtil.isMuteAll(playerIn))
            return null;
        ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(pos, playerIn, isPlaced);
        if (sheetMusic != null)
        {
            NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("MusicBook");
            if (contents != null)
            {
                Integer playerID = playerIn.getEntityId();
                String title = sheetMusic.getDisplayName();
                String mml = contents.getString("MML");

                mml = mml.replace("MML@", "MML@I" + getPackedPreset(pos, playerIn, isPlaced));
                ModLogger.debug("MML Title: " + title);
                ModLogger.debug("MML Sub25: " + mml.substring(0, mml.length() >= 25 ? 25 : mml.length()));

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
        if (g!=null)
            g.playID = null;
    }
    
    /**
     * Generate a new PlayID if this is the first member to queue, or return the existing one.
     * This assumes the member is already been validated as a member of the group
     * 
     * @param membersID
     * @return a unique playID or null if something went wrong
     */
    private static Integer getGroupsPlayID(Integer membersID)
    {
        GroupManager.Group g = GroupManager.getMembersGroup(membersID);
        Integer playID = null;
        if (g!=null)
            if (g.playID == null)
                playID = g.playID = getNextPlayID();
            else
                playID = g.playID;
        return playID;
    }

    public static Integer getPlayersPlayID(Integer entityID)
    {
        return (membersPlayID != null && !membersPlayID.isEmpty()) ? membersPlayID.get(entityID) : null;
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
    
    public static <T extends EntityLivingBase> void playingEnded(T entityLivingIn, Integer playID)
    {
        if (isPlayerPlaying(entityLivingIn)){
            if (membersPlayID != null) membersPlayID.remove(entityLivingIn.getEntityId());
            if (membersQueuedStatus != null && membersQueuedStatus.containsKey(entityLivingIn.getEntityId()))
            {
                membersQueuedStatus.remove(entityLivingIn.getEntityId());
            }
            dequeuePlayID(playID);
            syncStatus();
        }
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
        PacketDispatcher.sendToAll(new StopPlayMessage(playID));
        dequeuePlayID(playID);
        syncStatus();        
    }
    
    private static void dequeuePlayID(Integer playID) {if (activePlayIDs != null) activePlayIDs.remove(playID);}
    
    private static int getPackedPreset(BlockPos pos, EntityPlayer playerIn, boolean isPlaced)
    {
        int packedPreset = 0;
        if (isPlaced)
        {
            if (playerIn.getEntityWorld().getBlockState(pos).getBlock() instanceof IPlacedInstrument)
            {
                IPlacedInstrument placedInst = (IPlacedInstrument) playerIn.getEntityWorld().getBlockState(pos).getBlock();
                packedPreset =  placedInst.getPatch();
            }
        } else
        {
            IInstrument inst = (IInstrument) playerIn.getHeldItemMainhand().getItem();
            packedPreset =  inst.getPatch(playerIn.getHeldItemMainhand().getMetadata());
        }
        return packedPreset;
    }
    
    private static void syncStatus()
    {
        /** server side */
        GROUPS.setClientPlayStatuses(GROUPS.serializeIntIntMap(membersQueuedStatus));
        GROUPS.setPlayIDMembers(GROUPS.serializeIntIntMap(membersPlayID));
        GROUPS.setActivePlayIDs(GROUPS.serializeIntegerSet(activePlayIDs));
        /** client side */
        PacketDispatcher.sendToAll(new SyncStatusMessage(GROUPS.serializeIntIntMap(membersQueuedStatus), GROUPS.serializeIntIntMap(membersPlayID), GROUPS.serializeIntegerSet(activePlayIDs)));
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
            ModLogger.error(e);
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
            ModLogger.error(e);
        }
        return buildMML.toString();
    }

    /**
     * 
     * @param playID
     * @return Vec3d
     */
    public static Vec3d getMedianPos(int playID)
    {
        double x, y, z;
        x = y = z = 0;
        int count = 0;
        Vec3d pos;
        for(int member: getMembersByPlayID(playID))
        {   
            EntityPlayer player = (EntityPlayer) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getEntityByID(member);
            if(player == null)
                continue;
            x = x + player.getPositionVector().xCoord;
            y = y + player.getPositionVector().yCoord;
            z = z + player.getPositionVector().zCoord;
            count++;
        }            

        if (count == 0)
            return Vec3d.ZERO;
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
                                double distance = getMemberVector(memberA).distanceTo(getMemberVector(memberB));
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
        Integer playID =  getPlayersPlayID(memberID);
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
            stopPlayID(playID);
        }
    }
    
}
