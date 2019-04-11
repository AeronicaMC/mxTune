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

import net.aeronica.mods.mxtune.util.GUID;

import java.util.List;

public interface IPlayerMusicOptions
{
    void setHudOptions(boolean disableHUD, int positionHud, float sizeHud);
    
    boolean isHudDisabled();
    
    int getPositionHud();
    
    float getSizeHud();
    
    int getMuteOption();

    void setMuteOption(int muteOptionIn);
    
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

    void setWhiteList(List<ClassifiedPlayer> list);
    
    List<ClassifiedPlayer> getWhiteList();

    void setBlackList(List<ClassifiedPlayer> list);
    
    List<ClassifiedPlayer> getBlackList();

    void setSoundRangeInfinityAllowed(boolean isAllowed);

    boolean isSoundRangeInfinityRangeAllowed();

    void setMxTuneServerUpdateAllowed(boolean isAllowed);

    boolean isMxTuneServerUpdateAllowed();

    void setSelectedAreaGuid(GUID guidArea);

    GUID getSelectedAreaGuid();

    void setCtrlKey(boolean isDown);

    boolean isCtrlKeyDown();
}
