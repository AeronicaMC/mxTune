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

import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public interface IPlayerMusicOptions
{   
    public void clearAll(EntityPlayer playerIn);
    
    public void setHudOptions(EntityPlayer playerIn, boolean disableHud, int positionHud, float sizeHud);
    
    public void setHudOptions(boolean disableHUD, int positionHud, float sizeHud);
    
    public boolean isHudDisabled();
    
    public int getPositionHud();
    
    public float getSizeHud();
    
    public int getMuteOption();
    
    public void setMuteOption(EntityPlayer playerIn, int muteOptionIn);

    public void setMuteOption(int muteOptionIn);

    /**
     * Strings will be set and sync'd to the specified players client
     * It should be called on the server side only. Used to
     * store and send ad hoc parameters to the client. 
     * 
     * @param playerIn
     * @param sParam1
     * @param sParam2
     * @param sParam3
     */
    public void setSParams(EntityPlayer playerIn, String sParam1, String sParam2, String sParam3);
    
    /**
     * Strings will be set on the side it's called on. Used to
     * store and send ad hoc parameters to the client. 
     * 
     * @param sParam1
     * @param sParam2
     * @param sParam3
     */
    public void setSParams(String sParam1, String sParam2, String sParam3);

    public String getSParam1();

    public String getSParam2();

    public String getSParam3();
    
    public void setWhiteList(EntityPlayer playerIn, List<PlayerLists> list);

    public void setWhiteList(List<PlayerLists> list);
    
    public List<PlayerLists> getWhiteList();

    public void setBlackList(EntityPlayer playerIn, List<PlayerLists> list);

    public void setBlackList(List<PlayerLists> list);
    
    public List<PlayerLists> getBlackList();

    /**
     * Sync all properties for the specified player to the client.
     * 
     * @param playerIn
     */
    public void syncAll(EntityPlayer playerIn);
    
    /**
     * Sync the specified property ID for the specified player
     * to the client.
     * 
     * @param playerIn
     * @param propertyID
     */
    public void sync(EntityPlayer playerIn, byte propertyID);
}
