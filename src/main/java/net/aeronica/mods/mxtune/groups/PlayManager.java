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

    private static Map<String, String> membersMML = new HashMap<String, String>();
    private static Map<String, String> groupsMembers = new HashMap<String, String>();
    private static Map<String, String> playStatus = new HashMap<String, String>();

    private void setPlaying(String playerName) {playStatus.put(playerName, GROUPS.PLAYING.name());}

    private void setQueued(String playerName) {playStatus.put(playerName, GROUPS.QUEUED.name());}

    
    @SuppressWarnings("unused")
    private void setDone(String playerName) {if (playStatus.containsKey(playStatus)) playStatus.remove(playerName);}

    /**
     * 
     * @param playerIn
     * @param pos       position of block instrument
     * @param isPlaced  true is this is a block instrument
     */
    public void playMusic(EntityPlayer playerIn, BlockPos pos, boolean isPlaced)
    {
        ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(pos, playerIn, isPlaced);
        if (sheetMusic != null)
        {
            NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("MusicBook");
            if (contents != null)
            {
                String playerName = playerIn.getDisplayName().getUnformattedText();
                String title = sheetMusic.getDisplayName();
                String mml = contents.getString("MML");

                mml = mml.replace("MML@", "MML@I" + getPatch(pos, playerIn, isPlaced));
                ModLogger.logInfo("MML Title: " + title);
                ModLogger.logInfo("MML Sub25: " + mml.substring(0, (mml.length() >= 25 ? 25 : mml.length())));

                if (GROUPS.getMembersGroupID(playerIn.getDisplayName().getUnformattedText()) == null)
                {
                    /** Solo Play */
                    PlayManager.getInstance().playSolo(playerIn, title, mml, playerName, isPlaced);
                    ModLogger.logInfo("playMusic playSolo");
                } else
                {
                    /** Jam Play */
                    PlayManager.getInstance().queueJam(playerIn, title, mml, playerName, isPlaced);
                    ModLogger.logInfo("playMusic queueJam");
                }                
            }
        }
    }

    private void playSolo(EntityPlayer playerIn, String title, String mml, String playerName, boolean isPlaced)
    {
        PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playerName, title, mml, isPlaced);
        PacketDispatcher.sendToAllAround(packetPlaySolo, playerIn.dimension, playerIn.posX, playerIn.posY, playerIn.posZ, ModConfig.getListenerRange());
        setPlaying(playerName);
        syncStatus();
    }
    
    private void queueJam(EntityPlayer playerIn, String title, String mml, String playerName, boolean isPlaced)
    {
        String groupID = GroupManager.getInstance().getMembersGroupID(playerName);
        /** Queue members parts */
        this.queue(groupID, playerName, mml);
        PacketDispatcher.sendTo(new QueueJamMessage("queue", "only", isPlaced), (EntityPlayerMP) playerIn);
        /** Only send the groups MML when the leader starts the JAM */
        if (GroupManager.getInstance().isLeader(playerIn.getDisplayName().getUnformattedText()))
        {
            mml = this.getMML(groupID);
            PacketDispatcher.sendToAllAround(new PlayJamMessage(mml, groupID), playerIn.dimension, playerIn.posX, playerIn.posY, playerIn.posZ, ModConfig.getListenerRange());
            syncStatus();
        }
    }

    public boolean isPlayerPlaying(String playID) {return (GROUPS.getIndex(playID) & 2) == 2; }
    /**
     * Stop the playing MML for the specified playID (player name).
     * 
     * @param playID - The players name - e.g. (EntityPlayer) player.getDisplayName().getUnformattedText()
     */
    public void stopMusic(String playID)
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
            Set<String> keys = playStatus.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext())
            {
                String playerName = (String) it.next();
                buildStatus = buildStatus + playerName + "=" + playStatus.get(playerName) + " ";
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

    private void queue(String groupID, String memberName, String mml)
    {
        try
        {
            membersMML.put(memberName, mml);
            groupsMembers.put(memberName, groupID);
            setQueued(memberName);
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
     * @return
     */
    private String getMML(String groupID)
    {
        String buildMML = " ";
        try
        {
            Set<String> keys = groupsMembers.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext())
            {
                String member = (String) it.next();
                String group = groupsMembers.get(member);
                if (group.equalsIgnoreCase(groupID))
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
     * @param memberName
     */
    public void dequeueMember(String memberName)
    {
        if (membersMML != null && !membersMML.isEmpty() && membersMML.containsKey(memberName))
        {
            membersMML.remove(memberName);
        }
        if (groupsMembers != null && !groupsMembers.isEmpty() && groupsMembers.containsKey(memberName))
        {
            groupsMembers.remove(memberName);
        }
        if (playStatus != null && !playStatus.isEmpty() && playStatus.containsKey(memberName))
        {
            playStatus.remove(memberName);
            syncStatus();
        }
    }
}
