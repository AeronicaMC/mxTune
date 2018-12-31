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
package net.aeronica.mods.mxtune.options;

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.SyncPlayerMusicOptionsMessage;
import net.aeronica.mods.mxtune.util.Util;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ConstantConditions")
public class MusicOptionsUtil
{
    public static final byte SYNC_ALL = 0;
    public static final byte SYNC_DISPLAY_HUD = 1;
    public static final byte SYNC_MUTE_OPTION = 2;
    public static final byte SYNC_S_PARAMS = 3;
    public static final byte SYNC_WHITE_LIST = 4;
    public static final byte SYNC_BLACK_LIST = 5;

    @CapabilityInject(IPlayerMusicOptions.class)
    private static final Capability<IPlayerMusicOptions> MUSIC_OPTIONS = Util.nonNullInjected();
    
    private MusicOptionsUtil() {}
    
    public static void setHudOptions(EntityPlayer playerIn, boolean disableHud, int positionHud, float sizeHud)
    {
        getImpl(playerIn).setHudOptions(disableHud, positionHud, sizeHud);
        sync(playerIn, SYNC_DISPLAY_HUD);
    }

    public static boolean isHudDisabled(EntityPlayer playerIn)
    {
        return getImpl(playerIn).isHudDisabled();
    }
    
    public static int getPositionHUD(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getPositionHud();
    }

    public static float getSizeHud(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getSizeHud();
    }
    
    public static boolean isMuteAll(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getMuteOption() == MusicOptionsUtil.EnumMuteOptions.ALL.getIndex();
    }

    public static void setMuteOption(EntityPlayer playerIn, int muteOptionIn)
    {
        getImpl(playerIn).setMuteOption(muteOptionIn);
        sync(playerIn, SYNC_MUTE_OPTION);
    }

    private static MusicOptionsUtil.EnumMuteOptions getMuteOptionEnum(EntityPlayer playerIn)
    {
        return MusicOptionsUtil.EnumMuteOptions.byIndex(getImpl(playerIn).getMuteOption());
    }
    
    public static int getMuteOption(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getMuteOption();
    }
    
    public static void setSParams(EntityPlayer playerIn, String sParam1, String sParam2, String sParam3)
    {
        getImpl(playerIn).setSParams(sParam1, sParam2, sParam3);
        sync(playerIn, SYNC_S_PARAMS);
    }
    
    public static String getSParam1(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getSParam1();
    }

    @SuppressWarnings("unused")
    public static String getSParam2(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getSParam2();
    }

    @SuppressWarnings("unused")
    public static String getSParam3(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getSParam3();
    }
    
    public static void setBlackList(EntityPlayer playerIn, List<PlayerLists> blackList)
    {
        getImpl(playerIn).setBlackList(blackList);
        sync(playerIn, SYNC_BLACK_LIST);
    }

    public static List<PlayerLists> getBlackList(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getBlackList();
    }
    
    public static void setWhiteList(EntityPlayer playerIn, List<PlayerLists> whiteList)
    {
        getImpl(playerIn).setWhiteList(whiteList);
        sync(playerIn, SYNC_WHITE_LIST);
    }

    public static List<PlayerLists> getWhiteList(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getWhiteList();
    }

    @Nullable
    private static IPlayerMusicOptions getImpl(EntityPlayer player)
    {
        IPlayerMusicOptions bardActionImpl;
        if (player.hasCapability(Objects.requireNonNull(MUSIC_OPTIONS), null))
            bardActionImpl =  player.getCapability(MUSIC_OPTIONS, null);
        else
            throw new RuntimeException("IBardAction capability is null");
        return bardActionImpl;
    }

    /*
     * GuiHudAdjust positionHud temporary value for use when adjusting the Hud.
     */
    private static int adjustPositionHud = 0;
    public static int getAdjustPositionHud() {return adjustPositionHud;}
    public static void setAdjustPositionHud(int posHud) {adjustPositionHud = posHud;}
    
    private static float adjustSizeHud = 1.0F;
    public static void setAdjustSizeHud(float sizeHud) {adjustSizeHud=sizeHud;}
    public static float getAdjustSizeHud() {return adjustSizeHud;}

    /**
     * Mute per the muteOptions setting taking care to not mute THEPLAYER (playerIn) except for case ALL
     * 
     * @param playerIn this persons mute setting
     * @param otherPlayer is the other person muted
     * @return true if muted
     */
    public static boolean playerNotMuted(@Nullable EntityPlayer playerIn, @Nullable EntityPlayer otherPlayer)
    {
        boolean result = false;
        if (playerIn != null && otherPlayer != null)
        {
            switch (getMuteOptionEnum(playerIn))
            {
            case OFF:
                break;
            case ALL:
                result = true;
                break;
            case OTHERS:
                result = !playerIn.equals(otherPlayer);
                break;
            case WHITELIST:
                result = !isPlayerInList(playerIn, otherPlayer, getWhiteList(playerIn));
                break;
            case BLACKLIST:
                result = isPlayerInList(playerIn, otherPlayer, getBlackList(playerIn));
                break;
            default:
            }
        }
        return !result;
    }

    private static boolean isPlayerInList(EntityPlayer playerIn, EntityPlayer otherPlayer, List<PlayerLists> playerList)
    {
        boolean inList = false;
        if (!playerIn.equals(otherPlayer))
            for (PlayerLists w : playerList)
                if (w.getUuid().equals(otherPlayer.getUniqueID()))
                {
                    inList = true;
                    break;
                }
        return inList;
    }

    public enum EnumMuteOptions implements IStringSerializable
    {
        OFF(0, "mxtune.gui.musicOptions.muteOption.off"),
        OTHERS(1, "mxtune.gui.musicOptions.muteOption.others"),
        BLACKLIST(2, "mxtune.gui.musicOptions.muteOption.blacklist"),
        WHITELIST(3, "mxtune.gui.musicOptions.muteOption.whitelist"),
        ALL(4, "mxtune.gui.musicOptions.muteOption.all");

        private final int index;
        private final String translateKey;
        private static final EnumMuteOptions[] INDEX_LOOKUP = new EnumMuteOptions[values().length];

        EnumMuteOptions(int index, String translateKey)
        {
            this.index = index;
            this.translateKey = translateKey;
        }
        
        public int getIndex() {return this.index;}
        
        static
        {
            for (EnumMuteOptions value : values())
            {
                INDEX_LOOKUP[value.getIndex()] = value;
            }
        }

        public static EnumMuteOptions byIndex(int indexIn)
        {
            int index = indexIn;
            if (index < 0 || index >= INDEX_LOOKUP.length)
            {
                index = 0;
            }
            return INDEX_LOOKUP[index];
        }
        
        @Override
        public String toString(){return I18n.format(this.translateKey);}  

        @Override
        public String getName() {return this.translateKey;}        
    }

    /**
     * Sync all properties for the specified player to the client.
     *
     * @param playerIn synchronize this players music options
     */
    static void syncAll(EntityPlayer playerIn)
    {
        sync(playerIn, SYNC_ALL);
    }

    /**
     * Sync the specified property ID for the specified player
     * to the client.
     *
     * @param playerIn synchronize this players music options
     * @param propertyID to synchronize
     */
    public static void sync(EntityPlayer playerIn, byte propertyID)
    {
        if (!playerIn.getEntityWorld().isRemote)
        {
            PacketDispatcher.sendTo(new SyncPlayerMusicOptionsMessage(playerIn.getCapability(Objects.requireNonNull(MUSIC_OPTIONS), null), propertyID), (EntityPlayerMP) playerIn);
        }
    }
}
