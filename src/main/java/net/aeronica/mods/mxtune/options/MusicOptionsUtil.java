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

import net.aeronica.mods.mxtune.util.Util;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ConstantConditions")
public class MusicOptionsUtil
{

    @CapabilityInject(IPlayerMusicOptions.class)
    private static final Capability<IPlayerMusicOptions> MUSIC_OPTIONS = Util.nonNullInjected();
    
    private MusicOptionsUtil() {}
    
    public static void setHudOptions(EntityPlayer playerIn, boolean disableHud, int positionHud, float sizeHud)
    {
        getImpl(playerIn).setHudOptions(playerIn, disableHud, positionHud, sizeHud);
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
        return getImpl(playerIn).getMuteOption() == MusicOptionsUtil.EnumMuteOptions.ALL.getMetadata();
    }

    public static void setMuteOption(EntityPlayer playerIn, int muteOptionIn)
    {
        getImpl(playerIn).setMuteOption(playerIn, muteOptionIn);
    }

    private static MusicOptionsUtil.EnumMuteOptions getMuteOptionEnum(EntityPlayer playerIn)
    {
        return MusicOptionsUtil.EnumMuteOptions.byMetadata(getImpl(playerIn).getMuteOption());
    }
    
    public static int getMuteOption(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getMuteOption();
    }
    
    public static void setSParams(EntityPlayer playerIn, String sParam1, String sParam2, String sParam3)
    {
        getImpl(playerIn).setSParams(playerIn, sParam1, sParam2, sParam3);
    }
    
    public static String getSParam1(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getSParam1();
    }

    public static String getSParam2(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getSParam2();
    }

    public static String getSParam3(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getSParam3();
    }
    
    public static void setBlackList(EntityPlayer playerIn, List<PlayerLists> blackList)
    {
        getImpl(playerIn).setBlackList(playerIn, blackList);
    }

    public static List<PlayerLists> getBlackList(EntityPlayer playerIn)
    {
        return getImpl(playerIn).getBlackList();
    }
    
    public static void setWhiteList(EntityPlayer playerIn, List<PlayerLists> whiteList)
    {
        getImpl(playerIn).setWhiteList(playerIn, whiteList);
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
    public static boolean isPlayerMuted(EntityPlayer playerIn, EntityPlayer otherPlayer)
    {
        boolean result = false;
        if (playerIn != null && otherPlayer != null)
        {
            switch (getMuteOptionEnum(playerIn))
            {
            case OFF:
                break;
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
        }
        return result;
    }
    
    public enum EnumMuteOptions implements IStringSerializable
    {
        OFF(0, "mxtune.gui.musicOptions.muteOption.off"),
        OTHERS(1, "mxtune.gui.musicOptions.muteOption.others"),
        BLACKLIST(2, "mxtune.gui.musicOptions.muteOption.blacklist"),
        WHITELIST(3, "mxtune.gui.musicOptions.muteOption.whitelist"),
        ALL(4, "mxtune.gui.musicOptions.muteOption.all");

        private final int meta;
        private final String translateKey;
        private static final EnumMuteOptions[] META_LOOKUP = new EnumMuteOptions[values().length];

        EnumMuteOptions(int meta, String translateKey)
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

        public static EnumMuteOptions byMetadata(int metaIn)
        {
            int meta = metaIn;
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
