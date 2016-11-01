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
import net.aeronica.mods.mxtune.network.client.PlayJamMessage;
import net.aeronica.mods.mxtune.network.client.PlaySoloMessage;
import net.aeronica.mods.mxtune.network.client.SyncStatusMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.sound.PlayStatusUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

// Notes: For saving to disk use UUIDs. For client-server communication use getEntityID. Done.
// UUID does not work on the client.
public class PlayManager
{
    /** Don't allow any other class to instantiate the PlayManager */
    private PlayManager() {}
    private static class PlayManagerHolder {public static final PlayManager INSTANCE = new PlayManager();}
    public static PlayManager getInstance() {return PlayManagerHolder.INSTANCE;}

    private static Map<Integer, String> membersMML;
    private static Map<Integer, Integer> groupsMembers;
    private static Map<Integer, String> playStatus;
    private static Integer playID;

    static {
        membersMML = new HashMap<Integer, String>();
        groupsMembers = new HashMap<Integer, Integer>();
        playStatus = new HashMap<Integer, String>();
        playID = 0;
    }
    
    public static Integer getNextPlayID()
    {
        return playID++;
    }

    private static void setPlaying(Integer playerID) {playStatus.put(playerID, GROUPS.PLAYING.name());}

    private static void setQueued(Integer playerID) {playStatus.put(playerID, GROUPS.QUEUED.name());}

    
    @SuppressWarnings("unused")
    private static void setDone(Integer playerID) {if (playStatus.containsKey(playStatus)) playStatus.remove(playerID);}

    /**
     * 
     * @param playerIn
     * @param pos       position of block instrument
     * @param isPlaced  true is this is a block instrument
     */
    public static void playMusic(EntityPlayer playerIn, BlockPos pos, boolean isPlaced)
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
                    playSolo(playerIn, title, mml, playerID, pos, isPlaced);
                    ModLogger.debug("playMusic playSolo");
                } else
                {
                    /** Jam Play */
                    queueJam(playerIn, title, mml, playerID);
                    ModLogger.debug("playMusic queueJam");
                }                
            }
        }
    }

    private static void playSolo(EntityPlayer playerIn, String title, String mml, Integer playerID, BlockPos pos, boolean isPlaced)
    {
        PlaySoloMessage packetPlaySolo = new PlaySoloMessage(playerID, title, mml, pos, isPlaced);
        PacketDispatcher.sendToAllAround(packetPlaySolo, playerIn.dimension, playerIn.posX, playerIn.posY, playerIn.posZ, ModConfig.getListenerRange());
        setPlaying(playerID);
        syncStatus();
    }
    
    private static void queueJam(EntityPlayer playerIn, String title, String mml, Integer playerID)
    {
        Integer groupID = GROUPS.getMembersGroupID(playerID);
        /** Queue members parts */
        queue(groupID, playerID, mml);
        // PacketDispatcher.sendTo(new QueueJamMessage("queue", "only", isPlaced), (EntityPlayerMP) playerIn);
        /** Only send the groups MML when the leader starts the JAM */
        if (GROUPS.isLeader(playerID))
        {
            mml = getMML(groupID);
            BlockPos pos = getMedianPos(groupID);
            PacketDispatcher.sendToAllAround(new PlayJamMessage(mml, playerID, pos, true), playerIn.dimension, pos.getX(), pos.getY(), pos.getZ(), ModConfig.getListenerRange());
            setPlaying(playerID);
            syncStatus();
        }
    }

    public static boolean isPlayerPlaying(Integer EntityID) {return (GROUPS.getIndex(EntityID) & 2) == 2; }
    public static boolean isPlayerQueued(Integer EntityID) {return (GROUPS.getIndex(EntityID) & 1) == 1; }
    
    /**
     * Stop the playing MML for the specified playID (player name).
     * 
     * @param EntityID - The players entity id
     */
    public static void stopMusic(Integer EntityID)
    {
        Integer entityGroup = GROUPS.getMembersGroupID(EntityID);
        ModLogger.logInfo("PM.stopMusic: member: " + EntityID);
        if (isPlayerPlaying(EntityID) & (entityGroup == null))
        {
            ModLogger.logInfo("  PM.stopMusic: player: " + EntityID + ", group: " + entityGroup);
            dequeueMember(EntityID);
        }
        if (isPlayerQueued(EntityID))
        {
            ModLogger.logInfo("  PM.stopMusic: queued party member: " + EntityID + ", group: " + entityGroup);
            dequeueMember(EntityID);
        }
        if (isPlayerPlaying(EntityID) & (entityGroup != null))
        {
            ModLogger.logInfo("  PM.stopMusic: playing party member: " + EntityID + ", group: " + entityGroup);
            for(Integer member: GROUPS.getClientMembers().keySet())
            {   
                if(GROUPS.getMembersGroupID(member) == entityGroup)
                {
                    EntityPlayer player = (EntityPlayer) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getEntityByID(member);
                    ModLogger.logInfo("    PM.stopMusic member: " + member);
                    dequeueMember(member);
                    PlayStatusUtil.setPlaying(player, false);
                }
            }
        }
    }
    
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

    private static void queue(Integer groupID, Integer playerID, String mml)
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
    private static String getMML(Integer groupID)
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

    private static BlockPos getMedianPos(Integer groupID)
    {
        int x, y, z, count; x = y = z = count = 0;
        BlockPos pos;
        ModLogger.logInfo("getMedianPos");
       
        for(Integer member: GROUPS.getClientMembers().keySet())
        {   
            if(GROUPS.getMembersGroupID(member) == groupID)
            {
                EntityPlayer player = (EntityPlayer) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getEntityByID(member);
                x = x + player.getPosition().getX();
                y = y + player.getPosition().getY();
                z = z + player.getPosition().getZ();
                count++;
                ModLogger.logInfo("  getMedianPos player:" + player + ", x:" + x + ", count: " + count);
            }
        }            
        
        if (count == 0) return new BlockPos(0,0,0);
        x/=count;
        y/=count;
        z/=count;
        pos = new BlockPos(x,y,z);
        ModLogger.logInfo("" + pos);
        return pos;
    }

    /**
     * Used by the GroupMananger to purge unused/aborted Jam data
     * 
     * @param memberID
     */
    public static void dequeueMember(Integer memberID)
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
