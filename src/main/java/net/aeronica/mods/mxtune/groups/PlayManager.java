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

import net.aeronica.mods.mxtune.blocks.BlockPiano;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.StopPlayMessage;
import net.aeronica.mods.mxtune.network.client.PlayJamMessage;
import net.aeronica.mods.mxtune.network.client.PlaySoloMessage;
import net.aeronica.mods.mxtune.network.client.QueueJamMessage;
import net.aeronica.mods.mxtune.network.client.SyncStatusMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

// Notes: For saving to disk use UUIDs. For client-server communication use getEntityID. Done.
// UUID does not work on the client.
public class PlayManager
{
    /** Don't allow any other class to instantiate the PlayManager */
    private PlayManager() {}
    private static class PlayManagerHolder {public static final PlayManager INSTANCE = new PlayManager();}
    public static PlayManager getInstance() {return PlayManagerHolder.INSTANCE;}

    private static Map<Integer, String> membersMML = new HashMap<Integer, String>();
    private static Map<Integer, Integer> groupsMembers = new HashMap<Integer, Integer>();
    private static Map<Integer, String> playStatus = new HashMap<Integer, String>();

    private void setPlaying(Integer playerID) {playStatus.put(playerID, GROUPS.PLAYING.name());}

    private void setQueued(Integer playerID) {playStatus.put(playerID, GROUPS.QUEUED.name());}

    
    @SuppressWarnings("unused")
    private void setDone(Integer playerID) {if (playStatus.containsKey(playStatus)) playStatus.remove(playerID);}

    /**
     * 
     * @param playerIn
     * @param pos       position of block instrument
     * @param isPlaced  true is this is a block instrument
     */
    public void playMusic(EntityPlayer playerIn, BlockPos pos, boolean isPlaced)
    {
        if (MusicOptionsUtil.isMuteAll(playerIn)) return;
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

                if (GROUPS.getMembersGroupID(playerID) == null)
                {
                    /** Solo Play */
                    PlayManager.getInstance().playSolo(playerIn, title, mml, playerID, isPlaced);
                    ModLogger.debug("playMusic playSolo");
                } else
                {
                    /** Jam Play */
                    PlayManager.getInstance().queueJam(playerIn, title, mml, playerID, isPlaced);
                    ModLogger.debug("playMusic queueJam");
                }                
            }
        }
    }

    private void playSolo(EntityPlayer playerIn, String title, String mml, Integer playerID, boolean isPlaced)
    {
        PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playerID, title, mml, isPlaced);
        PacketDispatcher.sendToAllAround(packetPlaySolo, playerIn.dimension, playerIn.posX, playerIn.posY, playerIn.posZ, ModConfig.getListenerRange());
        setPlaying(playerID);
        syncStatus();
    }
    
    private void queueJam(EntityPlayer playerIn, String title, String mml, Integer playerID, boolean isPlaced)
    {
        Integer groupID = GROUPS.getMembersGroupID(playerID);
        /** Queue members parts */
        this.queue(groupID, playerID, mml);
        PacketDispatcher.sendTo(new QueueJamMessage("queue", "only", isPlaced), (EntityPlayerMP) playerIn);
        /** Only send the groups MML when the leader starts the JAM */
        if (GROUPS.isLeader(playerID))
        {
            mml = this.getMML(groupID);
            PacketDispatcher.sendToAllAround(new PlayJamMessage(mml, groupID), playerIn.dimension, playerIn.posX, playerIn.posY, playerIn.posZ, ModConfig.getListenerRange());
            syncStatus();
        }
    }

    public boolean isPlayerPlaying(Integer playID) {return (GROUPS.getIndex(playID) & 2) == 2; }
    /**
     * Stop the playing MML for the specified playID (player name).
     * 
     * @param playID - The players name - e.g. (EntityPlayer) player.getDisplayName().getUnformattedText()
     */
    public void stopMusic(Integer playID)
    {
        if (isPlayerPlaying(playID))
        {
            this.dequeueMember(playID);
            PacketDispatcher.sendToAll(new StopPlayMessage(playID));
        }
    }
    
    private int getPatch(BlockPos pos, EntityPlayer playerIn, boolean isPlaced)
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
    
    private void syncStatus()
    {
        String buildStatus = " ";
        try
        {
            Set<Integer> keys = playStatus.keySet();
            Iterator<Integer> it = keys.iterator();
            while (it.hasNext())
            {
                Integer playerID = (Integer) it.next();
                buildStatus = buildStatus + playerID + "=" + playStatus.get(playerID) + " ";
            }
        } catch (Exception e)
        {
            ModLogger.logError(e.getLocalizedMessage());
            e.printStackTrace();
        }
        /** server side */
        GROUPS.setClientPlayStatuses(buildStatus.trim());
        /** client side */
        PacketDispatcher.sendToAll(new SyncStatusMessage(buildStatus.trim()));
    }

    private void queue(Integer groupID, Integer playerID, String mml)
    {
        try
        {
            membersMML.put(playerID, mml);
            groupsMembers.put(playerID, groupID);
            setQueued(playerID);
            syncStatus();
        } catch (Exception e)
        {
            ModLogger.logError(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns a string in Map ready format. e.g.
     * "mamberName=MML@... memberName=MML@..."
     * 
     * @param groupID
     * @return string in Map ready format.
     */
    private String getMML(Integer groupID)
    {
        String buildMML = " ";
        try
        {
            Set<Integer> keys = groupsMembers.keySet();
            Iterator<Integer> it = keys.iterator();
            while (it.hasNext())
            {
                Integer member = (Integer) it.next();
                Integer group = groupsMembers.get(member);
                if (group.equals(groupID))
                {
                    buildMML = buildMML + member + "=" + membersMML.get(member) + " ";
                    it.remove();
                    membersMML.remove(member);
                    setPlaying(member);
                }
            }
        } catch (Exception e)
        {
            ModLogger.logError(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return buildMML.trim();
    }

    /**
     * Used by the GroupMananger to purge unused/aborted Jam data
     * 
     * @param memberID
     */
    public void dequeueMember(Integer memberID)
    {
        if (membersMML != null && !membersMML.isEmpty() && membersMML.containsKey(memberID))
        {
            membersMML.remove(memberID);
        }
        if (groupsMembers != null && !groupsMembers.isEmpty() && groupsMembers.containsKey(memberID))
        {
            groupsMembers.remove(memberID);
        }
        if (playStatus != null && !playStatus.isEmpty() && playStatus.containsKey(memberID))
        {
            playStatus.remove(memberID);
            syncStatus();
        }
    }
}
