/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
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
package net.aeronica.mods.mxtune.options;

import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class MusicOptionsUtil
{
    private MusicOptionsUtil() {}
    private static class MusicOptionsUtilHolder {private static final MusicOptionsUtil INSTANCE = new MusicOptionsUtil();}
    public static MusicOptionsUtil getInstance() {return MusicOptionsUtilHolder.INSTANCE;}

    @CapabilityInject(IPlayerMusicOptions.class)
    public static final Capability<IPlayerMusicOptions> MUSIC_OPTIONS = null;

    public static void setMidiVolume(EntityPlayer playerIn, float volumeIn) {playerIn.getCapability(MUSIC_OPTIONS, null).setMidiVolume(playerIn, volumeIn);}
    
    public static float getMidiVolume(EntityPlayer playerIn) {return playerIn.getCapability(MUSIC_OPTIONS, null).getMidiVolume();}
    
    public static boolean isMuteAll(EntityPlayer playerIn) {return playerIn.getCapability(MUSIC_OPTIONS, null).getMuteOption() == MusicOptionsUtil.EnumMuteOptions.ALL.getMetadata();}

    public static void setMuteOption(EntityPlayer playerIn, int muteOptionIn) {playerIn.getCapability(MUSIC_OPTIONS, null).setMuteOption(playerIn, muteOptionIn);}
    
    public static MusicOptionsUtil.EnumMuteOptions getMuteOptionEnum(EntityPlayer playerIn)
    {
        return MusicOptionsUtil.EnumMuteOptions.byMetadata(playerIn.getCapability(MUSIC_OPTIONS, null).getMuteOption());
    }
    
    public static int getMuteOption(EntityPlayer playerIn) {return playerIn.getCapability(MUSIC_OPTIONS, null).getMuteOption();}
    
    public static void setSParams(EntityPlayer playerIn, String sParam1, String sParam2, String sParam3)
    {
        playerIn.getCapability(MUSIC_OPTIONS, null).setSParams(playerIn, sParam1, sParam2, sParam3);
    }
    
    public static String getSParam1(EntityPlayer playerIn) {return playerIn.getCapability(MUSIC_OPTIONS, null).getSParam1();}

    public static String getSParam2(EntityPlayer playerIn) {return playerIn.getCapability(MUSIC_OPTIONS, null).getSParam2();}

    public static String getSParam3(EntityPlayer playerIn) {return playerIn.getCapability(MUSIC_OPTIONS, null).getSParam3();}
    
    public static void setBlackList(EntityPlayer playerIn, List<PlayerLists> blackList) {playerIn.getCapability(MUSIC_OPTIONS, null).setBlackList(playerIn, blackList);}

    public static List<PlayerLists> getBlackList(EntityPlayer playerIn) {return playerIn.getCapability(MUSIC_OPTIONS, null).getBlackList();}
    
    public static void setWhiteList(EntityPlayer playerIn, List<PlayerLists> whiteList) {playerIn.getCapability(MUSIC_OPTIONS, null).setWhiteList(playerIn, whiteList);}

    public static List<PlayerLists> getWhiteList(EntityPlayer playerIn) {return playerIn.getCapability(MUSIC_OPTIONS, null).getWhiteList();}
    
    /**
     * Mute per the muteOptions setting taking care to not mute THEPLAYER (playerIn) except for case ALL
     * 
     * @param playerIn
     * @param otherPlayer
     * @return true if muted
     */
    public static boolean getMuteResult(EntityPlayer playerIn, EntityPlayer otherPlayer)
    {
        boolean result = false;
        switch (getMuteOptionEnum(playerIn))
        {
        case OFF:
            result = false; break;
        case ALL:
            result = true; break;
        case OTHERS:
            result = !playerIn.equals(otherPlayer); break;
        case WHITELIST:
        {
            if (!playerIn.equals(otherPlayer))
            {
                boolean flag = true;
                for (PlayerLists w : getWhiteList(playerIn))
                {
                    if (w.getUuid().equals(otherPlayer.getUniqueID()))
                    {
                        flag = false;
                        break;
                    }
                }
                result = flag;
            } else
            {
                result = false;
            }
            break;
        }
        case BLACKLIST:
        {
            if (!playerIn.equals(otherPlayer))
            {
                boolean flag = false;
                for (PlayerLists w : getBlackList(playerIn))
                {
                    if (w.getUuid().equals(otherPlayer.getUniqueID()))
                    {
                        flag = true;
                        break;
                    }
                }
                result = flag;
            } else
            {
                result = false;
            }
            break;
        }
        default:
        }
        return result;
    }
    
    public static void dumpAllPlayers()
    {
        //MinecraftServer minecraftServer = MXTuneMain.proxy.getMinecraftServer();
        String[] pdat =  FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getAvailablePlayerDat();
        GameProfile gp = null;
        for (String n : pdat)
        {
            ModLogger.logInfo("playerDump#Player.dat:  " + n);
            try {
                gp = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(UUID.fromString(n));      
            }
            catch(IllegalArgumentException e) {  
            }
            finally {
                if (gp != null) {
                    ModLogger.logInfo("playerDump#Name:        " + gp.getName());
                    ModLogger.logInfo("playerDump#UUID:        " + gp.getId());
                    ModLogger.logInfo("playerDump#Properties:  " + gp.getProperties());
                    ModLogger.logInfo("playerDump#isComplete:  " + gp.isComplete());
                    ModLogger.logInfo("playerDump#isLegacy:    " + gp.isLegacy());
                }
                else
                    ModLogger.logInfo("playerDump#GameProfile: Invalid UUID string: " + n); 
            }
        }
    }
    
    public static enum EnumMuteOptions implements IStringSerializable
    {
        OFF(0, "mxtune.gui.musicOptions.muteOption.off"),
        OTHERS(1, "mxtune.gui.musicOptions.muteOption.others"),
        BLACKLIST(2, "mxtune.gui.musicOptions.muteOption.blacklist"),
        WHITELIST(3, "mxtune.gui.musicOptions.muteOption.whitelist"),
        ALL(4, "mxtune.gui.musicOptions.muteOption.all");

        private final int meta;
        private final String translateKey;
        private static final EnumMuteOptions[] META_LOOKUP = new EnumMuteOptions[values().length];

        private EnumMuteOptions(int meta, String translateKey)
        {
            this.meta = meta;
            this.translateKey = translateKey;
        }
        
        public int getMetadata() {return this.meta;}
        
        static
        {
            for (EnumMuteOptions value : values())
            {
                META_LOOKUP[value.getMetadata()] = value;
            }
        }

        public static EnumMuteOptions byMetadata(int meta)
        {
            if (meta < 0 || meta >= META_LOOKUP.length)
            {
                meta = 0;
            }
            return META_LOOKUP[meta];
        }
        
        @Override
        public String toString(){return I18n.format(this.translateKey);}  

        @Override
        public String getName() {return this.translateKey;}        
    }
}
