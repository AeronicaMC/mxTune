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
package net.aeronica.mods.mxtune.options;

import java.util.ArrayList;
import java.util.List;

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.SyncPlayerMusicOptionsMessage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class PlayerMusicDefImpl implements IPlayerMusicOptions
{
    @CapabilityInject(IPlayerMusicOptions.class)
    private static final Capability<IPlayerMusicOptions> MUSIC_OPTIONS = null;
    
    /** Music Options*/
    private int muteOption;
    /** HUD Options */
    private boolean disableHud =  false;
    private int positionHud;
    private float sizeHud = 0.5F;
    /** Strings for passing parameters from server to client: for a GUI for example */
    private String sParam1, sParam2, sParam3;
    private List<PlayerLists> whiteList, blackList;

    public PlayerMusicDefImpl()
    {
        this.muteOption = 0;
        this.disableHud = false;
        this.positionHud = 0;
        this.sizeHud = 0.5F;
        this.sParam1 = this.sParam2 = this.sParam3 = "";
        this.whiteList = new ArrayList<PlayerLists>();
        this.blackList = new ArrayList<PlayerLists>();
    }
    
    public PlayerMusicDefImpl(EntityLivingBase entity)
    {
        this.muteOption = 0;
        this.disableHud = false;
        this.positionHud = 0;
        this.sizeHud = 0.5F;
        this.sParam1 = this.sParam2 = this.sParam3 = "";
        this.whiteList = new ArrayList<PlayerLists>();
        this.blackList = new ArrayList<PlayerLists>();
    }

    @Override
    public void clearAll(EntityPlayer playerIn)
    {
        this.muteOption = 0;
        this.disableHud = false;
        this.positionHud = 0;
        this.sizeHud = 0.5F;
        this.sParam1 = this.sParam2 = this.sParam3 = "";
        this.syncAll(playerIn);
    }

    @Override
    public void setHudOptions(EntityPlayer playerIn, boolean disableHud, int positionHud, float sizeHud)
    {
        this.disableHud = disableHud;
        this.positionHud = positionHud;
        this.sizeHud = sizeHud;
        this.sync(playerIn, SYNC_DISPLAY_HUD); 
    }

    @Override
    public void setHudOptions(boolean disableHud, int positionHud, float sizeHud)
    {
        this.disableHud = disableHud;
        this.positionHud = positionHud;
        this.sizeHud = sizeHud;
    }

    @Override
    public boolean isHudDisabled() {return this.disableHud;}

    @Override
    public int getPositionHud() {return this.positionHud;}

    @Override
    public float getSizeHud() {return this.sizeHud;}

    @Override
    public void setSParams(EntityPlayer playerIn, String sParam1, String sParam2, String sParam3)
    {
        this.sParam1 = sParam1;
        this.sParam2 = sParam2;
        this.sParam3 = sParam3;
        this.sync(playerIn, SYNC_SPARAMS);
    }

    @Override
    public void setSParams(String sParam1, String sParam2, String sParam3)
    {
        this.sParam1 = sParam1;
        this.sParam2 = sParam2;
        this.sParam3 = sParam3;
    }
    
    @Override
    public String getSParam1() {return sParam1;}

    @Override
    public String getSParam2() {return sParam2;}

    @Override
    public String getSParam3() {return sParam3;}

    @Override
    public void setMuteOption(EntityPlayer playerIn, int muteOptionIn) {this.muteOption = muteOptionIn; sync(playerIn, SYNC_MUTE_OPTION);}

    @Override
    public void setMuteOption(int muteOptionIn) {this.muteOption = muteOptionIn;}

    @Override
    public int getMuteOption() {return muteOption;}
    
    @Override
    public void setWhiteList(List<PlayerLists> list) {this.whiteList = list;}

    @Override
    public void setWhiteList(EntityPlayer playerIn, List<PlayerLists> list) {this.whiteList = list; sync(playerIn, SYNC_WHITE_LIST);}

    @Override
    public List<PlayerLists> getWhiteList() {return new ArrayList<PlayerLists>(this.whiteList);}

    @Override
    public void setBlackList(EntityPlayer playerIn, List<PlayerLists> list) {this.blackList = list; sync(playerIn, SYNC_BLACK_LIST);}

    @Override
    public void setBlackList(List<PlayerLists> list) {this.blackList = list;}

    @Override
    public List<PlayerLists> getBlackList() {return new ArrayList<PlayerLists>(this.blackList);}

    public static final byte SYNC_ALL = 0;
    public static final byte SYNC_DISPLAY_HUD = 1;
    public static final byte SYNC_MUTE_OPTION = 2;
    public static final byte SYNC_SPARAMS = 3;
    public static final byte SYNC_WHITE_LIST = 4;
    public static final byte SYNC_BLACK_LIST = 5;    
    
    public void syncAll(EntityPlayer playerIn) {sync(playerIn, SYNC_ALL);}
    
    public void sync(EntityPlayer playerIn, byte propertyID)
    {
        if (playerIn != null && !playerIn.getEntityWorld().isRemote)
        {
            PacketDispatcher.sendTo(new SyncPlayerMusicOptionsMessage(playerIn.getCapability(MUSIC_OPTIONS, null), propertyID), (EntityPlayerMP) playerIn);
        }
    }
}
