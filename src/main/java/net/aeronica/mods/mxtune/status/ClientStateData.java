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
package net.aeronica.mods.mxtune.status;

import java.io.Serializable;

public class ClientStateData implements Serializable
{
    /**
     * Serialized ClientStateData
     */
    private static final long serialVersionUID = 7855748956912934732L;
    private final boolean midiAvailable;
    private final boolean masterVolumeOn;
    private final boolean mxtuneVolumeOn;

    public ClientStateData()
    {
        this.midiAvailable = false;
        this.masterVolumeOn = false;
        this.mxtuneVolumeOn = false;
    }

    public ClientStateData(boolean midiAvailable, boolean masterVolumeOn, boolean mxtuneVolumeOn)
    {
        this.midiAvailable = midiAvailable;
        this.masterVolumeOn = masterVolumeOn;
        this.mxtuneVolumeOn = mxtuneVolumeOn;
    }

    public boolean isMidiAvailable() {return midiAvailable;}

    public boolean isMasterVolumeOn() {return masterVolumeOn;}

    public boolean isMxtuneVolumeOn() {return mxtuneVolumeOn;}
    
    boolean isEqual(ClientStateData csd)
    {
        return csd != null && (this.midiAvailable == csd.midiAvailable && this.masterVolumeOn == csd.masterVolumeOn && this.mxtuneVolumeOn == csd.mxtuneVolumeOn);
    }
    
    public boolean isGood() {return this.midiAvailable && this.masterVolumeOn && this.mxtuneVolumeOn;}
    
    @Override
    public String toString()
    {
        return "{midiAvailable="+midiAvailable+", isMasterVolumeOn="+masterVolumeOn+", mxtuneVolumeOn="+mxtuneVolumeOn+"}";
    }
}

