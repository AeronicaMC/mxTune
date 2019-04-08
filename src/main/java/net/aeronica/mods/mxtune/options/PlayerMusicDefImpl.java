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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.GUID;

import java.util.ArrayList;
import java.util.List;

public class PlayerMusicDefImpl implements IPlayerMusicOptions
{
    /** Music Options*/
    private int muteOption;
    /** HUD Options */
    private boolean disableHud;
    private int positionHud;
    private float sizeHud;
    /** Strings for passing parameters from server to client: for a GUI for example */
    private String sParam1;
    private String sParam2;
    private String sParam3;
    private List<ClassifiedPlayer> whiteList;
    private List<ClassifiedPlayer> blackList;
    private boolean soundRangeInfinityAllowed;
    private boolean mxTuneServerUpdateAllowed;
    private GUID selectedAreaGuid;

    PlayerMusicDefImpl()
    {
        this.muteOption = 0;
        this.disableHud = false;
        this.positionHud = 0;
        this.sizeHud = 0.5F;
        this.sParam1 = "";
        this.sParam2 = "";
        this.sParam3 = "";
        this.whiteList = new ArrayList<>();
        this.blackList = new ArrayList<>();
        this.soundRangeInfinityAllowed = false;
        this.mxTuneServerUpdateAllowed = false;
        this.selectedAreaGuid = Reference.EMPTY_GUID;
    }

    @Override
    public void setHudOptions(boolean disableHud, int positionHud, float sizeHud)
    {
        this.disableHud = disableHud;
        this.positionHud = positionHud;
        this.sizeHud = sizeHud;
    }

    @Override
    public boolean isHudDisabled() { return this.disableHud; }

    @Override
    public int getPositionHud() { return this.positionHud; }

    @Override
    public float getSizeHud() { return this.sizeHud; }

    @Override
    public void setSParams(String sParam1, String sParam2, String sParam3)
    {
        this.sParam1 = sParam1;
        this.sParam2 = sParam2;
        this.sParam3 = sParam3;
    }
    
    @Override
    public String getSParam1() { return sParam1; }

    @Override
    public String getSParam2() { return sParam2; }

    @Override
    public String getSParam3() { return sParam3; }

    @Override
    public void setMuteOption(int muteOptionIn) { this.muteOption = muteOptionIn; }

    @Override
    public int getMuteOption() { return muteOption; }
    
    @Override
    public void setWhiteList(List<ClassifiedPlayer> list) { this.whiteList = list; }

    @Override
    public List<ClassifiedPlayer> getWhiteList() { return new ArrayList<>(this.whiteList); }

    @Override
    public void setBlackList(List<ClassifiedPlayer> list) { this.blackList = list; }

    @Override
    public List<ClassifiedPlayer> getBlackList() { return new ArrayList<>(this.blackList); }

    @Override
    public void setSoundRangeInfinityAllowed(boolean isAllowed) { this.soundRangeInfinityAllowed = isAllowed; }

    @Override
    public boolean isSoundRangeInfinityRangeAllowed() { return this.soundRangeInfinityAllowed; }

    @Override
    public void setMxTuneServerUpdateAllowed(boolean isAllowed) { mxTuneServerUpdateAllowed = isAllowed; }

    @Override
    public boolean isMxTuneServerUpdateAllowed() { return mxTuneServerUpdateAllowed; }

    @Override
    public void setSelectedAreaGuid(GUID guidArea) { selectedAreaGuid = guidArea; }

    @Override
    public GUID getSelectedAreaGuid() { return selectedAreaGuid; }
}
