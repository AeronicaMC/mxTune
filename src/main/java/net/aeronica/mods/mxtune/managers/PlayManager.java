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

import com.google.common.collect.Sets;
import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.blocks.IMusicPlayer;
import net.aeronica.mods.mxtune.blocks.IPlacedInstrument;
import net.aeronica.mods.mxtune.config.MXTuneConfig;
import net.aeronica.mods.mxtune.inventory.IInstrument;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.*;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.aeronica.mods.mxtune.managers.PlayIdSupplier.PlayType;
import static net.aeronica.mods.mxtune.util.SheetMusicUtil.*;

public class PlayManager
{
    private static final Map<Integer, String> membersMML = new HashMap<>();
    private static final HashMap<Integer, Integer> membersQueuedStatus = new HashMap<>();
    private static final HashMap<Integer, Integer> membersPlayID = new HashMap<>();
    private static final Set<Integer> activePlayIDs = Sets.newHashSet();

    private PlayManager() { /* NOP */ }

    private static int getNextPlayID()
    {
        return PlayType.PLAYERS.getAsInt();
    }

    private static void setPlaying(Integer playerID) {membersQueuedStatus.put(playerID, GroupHelper.PLAYING);}

    private static void setQueued(Integer playerID) {membersQueuedStatus.put(playerID, GroupHelper.QUEUED);}

    /**
     * For playing music from an Item
     * @param playerIn who is playing
     * @return a unique play id
     */
    public static int playMusic(PlayerEntity playerIn)
    {
        return playMusic(playerIn, null, false);
    }
 
    /**
     * For playing music from a block/placed instrument 
     * @param playerIn who is playing
     * @param pos position of block instrument
     * @return a unique play id
     */
    public static int playMusic(PlayerEntity playerIn, BlockPos pos)
    {
        return playMusic(playerIn, pos, true);
    }

    /**
     * For playing music from a block, e.g. Band Amp.
     * @param worldIn the world of course
     * @param pos position of block instrument
     * @return a unique play id
     */
    public static int playMusic(World worldIn, BlockPos pos)
    {
        int playID = PlayType.INVALID;
        IMusicPlayer musicPlayer;
        if (worldIn.getBlockState(pos).getBlock() instanceof IMusicPlayer)
        {
            musicPlayer = (IMusicPlayer) worldIn.getTileEntity(pos);
            String mml = musicPlayer.getMML();
            if (mml.contains(KEY_MML))
            {
                playID = getNextPlayID();
                activePlayIDs.add(playID);
                syncStatus();

                DurationTimer.scheduleStop(playID, musicPlayer.getDuration());
                ModLogger.debug("Block/TE MML Sub25: " + mml.substring(0, Math.min(25, mml.length())));
                PlayBlockMusicMessage playBlockMusicMessage = new PlayBlockMusicMessage(playID, pos, mml, musicPlayer.getSoundRange());
                PacketDispatcher.sendToAllAround(playBlockMusicMessage, worldIn.getDimension().getType().getId(), pos.getX(), pos.getY(), pos.getZ(), MXTuneConfig.getListenerRange());
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
    public static int playMusic(PlayerEntity playerIn, BlockPos pos, boolean isPlaced)
    {
        if (MusicOptionsUtil.isMuteAll(playerIn))
            return PlayType.INVALID;
        ItemStack sheetMusic = getSheetMusic(pos, playerIn, isPlaced);
        if (!sheetMusic.isEmpty())
        {
            CompoundNBT contents = null;
            if (sheetMusic.getTag() != null)
            {
                contents = (CompoundNBT) sheetMusic.getTag().getCompound(KEY_SHEET_MUSIC);
            }
            if (contents != null && !contents.isEmpty())
            {
                Integer playerID = playerIn.getEntityId();
                String title = sheetMusic.getDisplayName().getUnformattedComponentText();
                String mml = contents.getString(KEY_MML);
                int duration = contents.getInt(KEY_DURATION);

                mml = mml.replace("MML@", "MML@I" + getPackedPreset(pos, playerIn, isPlaced));
                ModLogger.debug("MML Title: " + title);
                ModLogger.debug("MML Sub25: " + mml.substring(0, Math.min(25, mml.length())));


                if (GroupManager.getMembersGroup(playerID).isEmpty())
                {
                    /* Solo Play */
                    ModLogger.debug("playMusic playSolo");
                    return playSolo(playerIn, mml, duration, playerID);
                }
                else
                {
                    /* Jam Play */
                    ModLogger.debug("playMusic queueJam");
                    return queueJam(playerIn, mml, duration, playerID);
                }
            }
        }
        return PlayType.INVALID;
    }

    private static int playSolo(PlayerEntity playerIn, String mml, int duration, Integer playerID)
    {
        int playID = getNextPlayID();
        queue(playID, playerID, mml);

        DurationTimer.scheduleStop(playID, duration);

        String musicText = getGroupMML(playID);
        activePlayIDs.add(playID);
        syncStatus();
        PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playID, musicText);
        PacketDispatcher.sendToAllAround(packetPlaySolo, playerIn.dimension, playerIn.posX, playerIn.posY, playerIn.posZ, MXTuneConfig.getListenerRange());
        return playID;
    }
    
    @SuppressWarnings("ConstantConditions")
    private static int queueJam(PlayerEntity playerIn, String mml, int duration, Integer membersID)
    {
        int groupsPlayID = getGroupsPlayID(membersID);
        /* Queue members parts */
        queue(groupsPlayID, membersID, mml);
        syncStatus();

        GroupManager.setMembersPartDuration(membersID, duration);
        /* Only send the groups MML when the leader starts the JAM */
        if (GroupManager.isLeader(membersID))
        {
            DurationTimer.scheduleStop(groupsPlayID, GroupManager.getGroupDuration(membersID));

            String musicText = getGroupMML(groupsPlayID);
            BlockPos pos = playerIn.getPosition();
            activePlayIDs.add(groupsPlayID);
            syncStatus();
            resetGroupsPlayID(membersID);
            PlayJamMessage playJamMessage = new PlayJamMessage(membersID, groupsPlayID, musicText);
            PacketDispatcher.sendToAllAround(playJamMessage, playerIn.dimension, pos.getX(), pos.getY(), pos.getZ(), MXTuneConfig.getListenerRange());
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
        Group membersGroup = GroupManager.getMembersGroup(membersID);
        if (!membersGroup.isEmpty())
            membersGroup.setPlayID(PlayType.INVALID);
    }
    
    /**
     * Generate a new PlayID if this is the first member to queue, or return the existing one.
     * This assumes the member is already been validated as a member of the group
     * 
     * @param membersID of some group
     * @return a unique playID or null if something went wrong
     */
    private static int getGroupsPlayID(Integer membersID)
    {
        Group membersGroup = GroupManager.getMembersGroup(membersID);
        int playID = PlayType.INVALID;
        if (!membersGroup.isEmpty())
            if (membersGroup.getPlayID() == PlayType.INVALID)
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
    
    public static  <T extends LivingEntity> boolean isPlayerPlaying(T entityLivingIn)
    {
        return isPlayerPlaying(entityLivingIn.getEntityId());
    }

    public static boolean isActivePlayID(@Nullable Integer playID) { return playID != null && activePlayIDs.contains(playID); }
    
    public static boolean hasPlayID(int playID)
    {
        return playID != PlayType.INVALID && membersPlayID.containsValue(playID);
    }
    
    private static Set<Integer> getMembersByPlayID(int playID)
    {
        Set<Integer> members = Sets.newHashSet();
        if (playID != PlayType.INVALID)
            for (Integer someMember : membersPlayID.keySet())
                if (membersPlayID.get(someMember).equals(playID))
                    members.add(someMember);

        return members;
    }
        
    public static <T extends LivingEntity> void stopPlayingPlayer(T entityLivingIn)
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

    public static void stopPlayID(int playID)
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
        removeActivePlayID(playID);
        PacketDispatcher.sendToAll(new StopPlayIDMessage(playID));
    }
    
    private static void removeActivePlayID(int playID)
    {
        if ((playID != PlayType.INVALID) && !activePlayIDs.isEmpty())
            activePlayIDs.remove(playID);
        syncStatus();
    }
    
    private static int getPackedPreset(BlockPos pos, PlayerEntity playerIn, boolean isPlaced)
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
            packedPreset =  inst.getPatch(playerIn.getHeldItemMainhand());
        }
        return packedPreset;
    }
    
    private static void syncStatus()
    {
        /* server side */
        GroupHelper.setClientPlayStatuses(GroupHelper.serializeIntIntMap(membersQueuedStatus));
        GroupHelper.setPlayIDMembers(GroupHelper.serializeIntIntMap(membersPlayID));
        GroupHelper.setActiveServerManagedPlayIDs(GroupHelper.serializeIntegerSet(activePlayIDs));
        /* client side */
        PacketDispatcher.sendToAll(new SyncStatusMessage(GroupHelper.serializeIntIntMap(membersQueuedStatus), GroupHelper.serializeIntIntMap(membersPlayID), GroupHelper.serializeIntegerSet(activePlayIDs)));
    }

    private static void queue(int playID, Integer memberID, String mml)
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
     * Concatenated MML from all the group members
     * "MML@...;MML@...;MML@...;"
     * 
     * @param playId play ID for the group
     * @return string mmlText ready to parse.
     */
    private static String getGroupMML(int playId)
    {
        StringBuilder mmlText = new StringBuilder();
        try
        {
            Set<Integer> keys = membersPlayID.keySet();
            for (Integer member : keys)
            {
                if (membersPlayID.get(member).equals(playId))
                {
                    mmlText.append(membersMML.get(member));
                    membersMML.remove(member);
                    setPlaying(member);
                }
            }
        } catch (Exception e)
        {
            ModLogger.error(e);
        }
        return mmlText.toString();
    }

    /**
     * Called by server tick once every two seconds to calculate the distance between
     * group members and to stop the playID if the distance exceeds stopDistane.
     * 
     * @param stopDistance exceeding the stop distance stops the music
     */
    public static void testStopDistance(double stopDistance)
    {
        if (!GroupManager.hasActiveGroups()) return;

        for (int playID : activePlayIDs)
        {
            if (!getMembersByPlayID(playID).isEmpty())
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
        PlayerEntity player = MXTune.proxy.getPlayerByEntityID(entityID);
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
