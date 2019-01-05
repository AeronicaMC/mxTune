/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
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

package net.aeronica.libs.mml.core;

import java.util.ArrayList;
import java.util.List;

/*
 * MML Note Velocity Compressor
 * Make soft tunes louder and decreases dynamic range. Makes loud tunes less so. Aims for a happy medium and
 * overall good experience for the listener.
 */
public class NoteVolumeCompressor
{
    private List<MObject> mmlObjectOut = new ArrayList<>();
    private List<MObject> mmlObjectsIn;
    private int minVolume;
    private int maxVolume;

    NoteVolumeCompressor(List<MObject> mmlObjectsIn)
    {
       this. mmlObjectsIn = mmlObjectsIn;
    }

    List<MObject> processVolumes()
    {
        getMinMaxVolumes();
        applyCompression();
        return mmlObjectOut;
    }

    private void getMinMaxVolumes()
    {
        for (MObject mmo : mmlObjectsIn)
            if (mmo.getType() == MObject.Type.DONE)
            {
                minVolume = mmo.getMinVolume();
                maxVolume = mmo.getMaxVolume();
            }
    }

    private int scaleBetween(int unscaledNum, int minAllowed, int maxAllowed, int min, int max) {
        if ((max - min) == 0)
        {
            return clamp(unscaledNum, minAllowed, maxAllowed);
        }
        return (maxAllowed - minAllowed) * (unscaledNum - min) / (max - min)  + minAllowed;
    }

    private int clamp(int value, int min, int max) { return Math.max(Math.min(max, value), min); }

    private int computeCompression(int volumeIn)
    {
        int newVolume = clamp(scaleBetween(volumeIn, 84, 127, minVolume, maxVolume), 0, 127);
        return newVolume;
    }

    private void applyCompression()
    {
        for (MObject mmo: mmlObjectsIn)
        {
            if (mmo.getType() == MObject.Type.NOTE)
            {
                int volume = computeCompression(mmo.getNoteVolume());
                mmlObjectOut.add(new MObject.MObjectBuilder(MObject.Type.NOTE)
                                         .midiNote(mmo.getMidiNote())
                                         .startingTicks(mmo.getStartingTicks())
                                         .lengthTicks(mmo.getLengthTicks())
                                         .text(mmo.getText())
                                         .volume(volume)
                                         .build());
            }
            else
            {
                mmlObjectOut.add(mmo);
            }
        }
    }
}
