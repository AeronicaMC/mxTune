/*
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

import com.google.common.collect.Sets;
import net.aeronica.mods.mxtune.blocks.IMusicPlayer;
import net.aeronica.mods.mxtune.blocks.IPlacedInstrument;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.*;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.sound.SoundRange;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * The SERVER side class for managing playing
 * FIXME: Refactor this class along with GROUPS and GroupManager and the associated packet messages
 * @author Paul Boese aka Aeronica
 *
 */
public class PlayManager
{
    private static final Map<Integer, String> membersMML = new HashMap<>();
    private static final HashMap<Integer, Integer> membersQueuedStatus = new HashMap<>();
    private static final HashMap<Integer, Integer> membersPlayID = new HashMap<>();
    private static final Set<Integer> activePlayIDs = Sets.newHashSet();
    private static int uniquePlayID = 1;
    
    /*
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
     * @param playerIn who is playing
     * @return a unique play id
     */
    @Nullable
    public static Integer playMusic(EntityPlayer playerIn)
    {
        return playMusic(playerIn, null, false);
    }
 
    /**
     * For playing music from a block/placed instrument 
     * @param playerIn who is playing
     * @param pos position of block instrument
     * @return a unique play id
     */
    @Nullable
    public static Integer playMusic(EntityPlayer playerIn, BlockPos pos)
    {
        return playMusic(playerIn, pos, true);
    }

    /**
     * For playing music from a block, e.g. Band Amp.
     * @param worldIn the world of course
     * @param pos position of block instrument
     * @param soundRange defines the attenuation: NORMAL or INFINITY respectively
     * @return a unique play id
     */
    @Nullable
    public static Integer playMusic(World worldIn, BlockPos pos, SoundRange soundRange)
    {
        Integer playID = null;
        IMusicPlayer musicPlayer;
        if (worldIn.getBlockState(pos).getBlock() instanceof IMusicPlayer)
        {
            musicPlayer = (IMusicPlayer) worldIn.getBlockState(pos).getBlock();
            String mml = musicPlayer.getMML(worldIn, pos);
            if (mml.contains("MML"))
            {
                playID = getNextPlayID();
                activePlayIDs.add(playID);
                syncStatus();
                PlayBlockMusicMessage playBlockMusicMessage = new PlayBlockMusicMessage(playID, pos, mml, soundRange);
                PacketDispatcher.sendToAllAround(playBlockMusicMessage, worldIn.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), ModConfig.getListenerRange());
            }
        }
        return playID;
    }

    /**
     * For playing music
     * @param playerIn who is playing
     * @param pos position of block instrument
     * @param isPlaced true is this is a block instrument
     * @return a unique play id or null if unable to play
     */
    @Nullable
    public static Integer playMusic(EntityPlayer playerIn, BlockPos pos, boolean isPlaced)
    {
        if (MusicOptionsUtil.isMuteAll(playerIn))
            return null;
        ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(pos, playerIn, isPlaced);
        if (!sheetMusic.isEmpty())
        {
            NBTTagCompound contents = null;
            if (sheetMusic.getTagCompound() != null)
            {
                contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("SheetMusic");
            }
            if (contents != null && !contents.isEmpty())
            {
                Integer playerID = playerIn.getEntityId();
                String title = sheetMusic.getDisplayName();
                String mml = contents.getString("MML");

                mml = mml.replace("MML@", "MML@I" + getPackedPreset(pos, playerIn, isPlaced));
                ModLogger.debug("MML Title: " + title);
                ModLogger.debug("MML Sub25: " + mml.substring(0, mml.length() >= 25 ? 25 : mml.length()));

                if (GroupManager.getMembersGroupID(playerID) == null)
                {
                    /* Solo Play */
                    ModLogger.debug("playMusic playSolo");
                    return playSolo(playerIn, mml, playerID);
                }
                else
                {
                    /* Jam Play */
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
        String musicText = getMappedMML(playID);
        activePlayIDs.add(playID);
        syncStatus();
        PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playID, musicText);
        PacketDispatcher.sendToAllAround(packetPlaySolo, playerIn.dimension, playerIn.posX, playerIn.posY, playerIn.posZ, ModConfig.getListenerRange());
        return playID;
    }
    
    private static Integer queueJam(EntityPlayer playerIn, String mml, Integer playerID)
    {
        Integer groupsPlayID = getGroupsPlayID(playerID);
        /* Queue members parts */
        queue(groupsPlayID, playerID, mml);
        syncStatus();
        /* Only send the groups MML when the leader starts the JAM */
        if (GroupManager.isLeader(playerID))
        {
            String musicText = getMappedMML(groupsPlayID);
            Vec3d pos = GROUPS.getMedianPos(groupsPlayID);
            activePlayIDs.add(groupsPlayID);
            syncStatus();
            resetGroupsPlayID(playerID);
            PlayJamMessage playJamMessage = new PlayJamMessage(playerID, groupsPlayID, musicText);
            PacketDispatcher.sendToAllAround(playJamMessage, playerIn.dimension, pos.x, pos.y, pos.z, ModConfig.getListenerRange());
        }
        return groupsPlayID;
    }

    /**
     * Reset the groups PlayID to null.
     * It's only needed for queuing the MML parts and should be used when the leader kicks off the session.
     * 
     * @param membersID of some group
     */
    private static void resetGroupsPlayID(Integer membersID)
    {
        GroupManager.Group membersGroup = GroupManager.getMembersGroup(membersID);
        if (membersGroup!=null)
            membersGroup.setPlayID(null);
    }
    
    /**
     * Generate a new PlayID if this is the first member to queue, or return the existing one.
     * This assumes the member is already been validated as a member of the group
     * 
     * @param membersID of some group
     * @return a unique playID or null if something went wrong
     */
    @Nullable
    private static Integer getGroupsPlayID(Integer membersID)
    {
        GroupManager.Group membersGroup = GroupManager.getMembersGroup(membersID);
        Integer playID = null;
        if (membersGroup!=null)
            if (membersGroup.getPlayID() == null)
            {
                playID = getNextPlayID();
                membersGroup.setPlayID(playID);
            }
            else
                playID = membersGroup.getPlayID();
        return playID;
    }

    @Nullable
    private static Integer getPlayersPlayID(Integer entityID)
    {
        return (entityID != null) ? membersPlayID.get(entityID) : null;
    }
    
    private static boolean isPlayerPlaying(Integer entityID)
    {
        return entityID != null && membersPlayID.containsKey(entityID);
    }
    
    public static  <T extends EntityLivingBase> boolean isPlayerPlaying(T entityLivingIn)
    {
        return isPlayerPlaying(entityLivingIn.getEntityId());
    }

    public static boolean isActivePlayID(@Nullable Integer playID) { return playID != null && activePlayIDs.contains(playID); }
    
    public static boolean hasPlayID(@Nullable Integer playID)
    {
        return playID != null && membersPlayID.containsValue(playID);
    }
    
    private static Set<Integer> getMembersByPlayID(@Nullable Integer playID)
    {
        Set<Integer> members = Sets.newHashSet();
        if (playID != null)
            for (Integer someMember : membersPlayID.keySet())
                if (membersPlayID.get(someMember).equals(playID))
                    members.add(someMember);

        return members;
    }
    
    public static <T extends EntityLivingBase> void playingEnded(T entityLivingIn, @Nullable Integer playID)
    {
        if (isPlayerPlaying(entityLivingIn)){
            membersPlayID.remove(entityLivingIn.getEntityId());
            membersQueuedStatus.remove(entityLivingIn.getEntityId());
            removeActivePlayID(playID);
            syncStatus();
        }
    }
        
    public static <T extends EntityLivingBase> void stopPlayingPlayer(T entityLivingIn)
    {
        stopPlayingPlayer(entityLivingIn.getEntityId());
    }
    
    private static void stopPlayingPlayer(Integer entityID)
    {
        if (isPlayerPlaying(entityID))
        {
            stopPlayID(PlayManager.getPlayersPlayID(entityID));
        }
    }

    public static void stopPlayID(@Nullable Integer playID)
    {
        Set<Integer> memberSet = getMembersByPlayID(playID);
        for(Integer member: memberSet)
        {
            if (membersPlayID.containsKey(member))
            {
                membersPlayID.remove(member, playID);
            }
            membersQueuedStatus.remove(member);
        }
        PacketDispatcher.sendToAll(new StopPlayMessage(playID));
        removeActivePlayID(playID);
        syncStatus();        
    }
    
    private static void removeActivePlayID(Integer playID) {if (playID != null && !activePlayIDs.isEmpty()) activePlayIDs.remove(playID);}
    
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
        /* server side */
        GROUPS.setClientPlayStatuses(GROUPS.serializeIntIntMap(membersQueuedStatus));
        GROUPS.setPlayIDMembers(GROUPS.serializeIntIntMap(membersPlayID));
        GROUPS.setActivePlayIDs(GROUPS.serializeIntegerSet(activePlayIDs));
        /* client side */
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
     * @param playID play ID to map
     * @return string in Map ready format.
     */
    private static String getMappedMML(Integer playID)
    {
        StringBuilder buildMappedMML = new StringBuilder();
        try
        {
            Set<Integer> keys = membersPlayID.keySet();
            for (Integer member : keys)
            {
                if (membersPlayID.get(member).equals(playID))
                {
                    buildMappedMML.append(member).append("=").append(membersMML.get(member)).append("|");
                    membersMML.remove(member);
                    setPlaying(member);
                }
            }
        } catch (Exception e)
        {
            ModLogger.error(e);
        }
        return buildMappedMML.toString();
    }

    /**
     * Called by server tick once every two seconds to calculate the distance between
     * group members and to stop the playID if the distance exceeds stopDistane.
     * 
     * @param stopDistance exceeding the stop distance stops the music
     */
    public static void testStopDistance(double stopDistance)
    {
        for (Integer playID : activePlayIDs)
        {
            if (getMembersByPlayID(playID) != null && !getMembersByPlayID(playID).isEmpty())
            {
                for (Integer memberA : getMembersByPlayID(playID))
                {
                    for (Integer memberB : getMembersByPlayID(playID))
                    {
                        if (!memberA.equals(memberB))
                        {
                            double distance = getMemberVector(memberA).distanceTo(getMemberVector(memberB));
                            if (distance > stopDistance) PlayManager.stopPlayID(playID);
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
     * Used by the GroupManager to purge unused/aborted Jam data
     *
     * @param memberID to be purged
     */
    static void purgeMember(Integer memberID)
    {
        Integer playID = getPlayersPlayID(memberID);
        if (memberID != null)
        {
            if (membersPlayID.containsKey(memberID))
                activePlayIDs.remove(membersPlayID.get(memberID));

            membersMML.remove(memberID);
            membersPlayID.remove(memberID);

            if (membersQueuedStatus.containsKey(memberID))
            {
                membersQueuedStatus.remove(memberID);
                stopPlayID(playID);
            }
        }
    }
}
