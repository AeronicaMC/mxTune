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
package net.aeronica.mods.mxtune.options;

import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public interface IPlayerMusicOptions
{
    void setHudOptions(EntityPlayer playerIn, boolean disableHud, int positionHud, float sizeHud);
    
    void setHudOptions(boolean disableHUD, int positionHud, float sizeHud);
    
    boolean isHudDisabled();
    
    int getPositionHud();
    
    float getSizeHud();
    
    int getMuteOption();
    
    void setMuteOption(EntityPlayer playerIn, int muteOptionIn);

    void setMuteOption(int muteOptionIn);

    /**
     * Strings will be set and sync'd to the specified players client
     * It should be called on the server side only. Used to
     * store and send ad hoc parameters to the client. 
     * 
     * @param playerIn specific player to affect
     * @param sParam1 general purpose string parameter
     * @param sParam2 general purpose string parameter
     * @param sParam3 general purpose string parameter
     */
    void setSParams(EntityPlayer playerIn, String sParam1, String sParam2, String sParam3);
    
    /**
     * Strings will be set on the side it's called on. Used to
     * store and send ad hoc parameters to the client. 
     * 
     * @param sParam1 general purpose string parameter
     * @param sParam2 general purpose string parameter
     * @param sParam3 general purpose string parameter
     */
    void setSParams(String sParam1, String sParam2, String sParam3);

    String getSParam1();

    String getSParam2();

    String getSParam3();
    
    void setWhiteList(EntityPlayer playerIn, List<PlayerLists> list);

    void setWhiteList(List<PlayerLists> list);
    
    List<PlayerLists> getWhiteList();

    void setBlackList(EntityPlayer playerIn, List<PlayerLists> list);

    void setBlackList(List<PlayerLists> list);
    
    List<PlayerLists> getBlackList();

    /**
     * Sync all properties for the specified player to the client.
     * 
     * @param playerIn synchronize this players music options
     */
    void syncAll(EntityPlayer playerIn);
    
    /**
     * Sync the specified property ID for the specified player
     * to the client.
     *
     * @param playerIn synchronize this players music options
     * @param propertyID to synchronize
     */
    void sync(EntityPlayer playerIn, byte propertyID);
}
