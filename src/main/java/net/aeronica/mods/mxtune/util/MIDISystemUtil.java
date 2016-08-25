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
package net.aeronica.mods.mxtune.util;

import java.util.Vector;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

import net.aeronica.mods.mxtune.mml.MMLManager;

public class MIDISystemUtil
{
    private MIDISystemUtil() {}
    private static class MIDISystemUtilHolder {public static final MIDISystemUtil INSTANCE = new MIDISystemUtil();}

    public static MIDISystemUtil getInstance() {return MIDISystemUtilHolder.INSTANCE;}

    private static MidiDevice.Info[] midiDeviceInfo = null;
    MidiDevice.Info bestSynthInfo = null;
    private static Synthesizer bestSynth = null;

    public void mxTuneInit()
    {
        midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
        Vector<MidiDevice.Info> synthInfos = new Vector<MidiDevice.Info>();
        MidiDevice device = null;
        int maxPolyphony = 0;
        Synthesizer testSynth = null;
        boolean synthAvailable = true;
        
   
        for (int i = 0; i < midiDeviceInfo.length; i++) {
            try {
                device = MidiSystem.getMidiDevice(midiDeviceInfo[i]);
            } catch (MidiUnavailableException e) {
                  // Handle or throw exception...
            }
            if (device instanceof Synthesizer) {
                synthInfos.add(midiDeviceInfo[i]);
            }
        }
        for (MidiDevice.Info info: synthInfos)
        {
            ModLogger.logInfo(info.getName());
            ModLogger.logInfo(info.getDescription());
            ModLogger.logInfo(info.getVendor());
            ModLogger.logInfo(info.getVersion());
            try
            {
                testSynth = (Synthesizer) MidiSystem.getMidiDevice(info);
            } catch (MidiUnavailableException e)
            {
                synthAvailable = false;
                e.printStackTrace();
            }
            finally
            {
                if (synthAvailable)
                {
                    if (testSynth.getMaxPolyphony() > maxPolyphony)
                    {
                        maxPolyphony = testSynth.getMaxPolyphony();
                        bestSynthInfo = info;
                        bestSynth =  testSynth;
                    }
                }
            }
        }
        if (bestSynth != null && synthAvailable)
        {
            ModLogger.logInfo(bestSynthInfo.getName());
            ModLogger.logInfo(bestSynthInfo.getDescription());
            ModLogger.logInfo(bestSynthInfo.getVendor());
            ModLogger.logInfo(bestSynthInfo.getVersion());
            ModLogger.logInfo("bestSynth: MaxPolyphony: " + bestSynth.getMaxPolyphony() + ", MaxReceivers: " + ((bestSynth.getMaxReceivers() == -1) ? "Unlimited" : bestSynth.getMaxReceivers()));
            ModLogger.logInfo("bestSynth: Open? " + bestSynth.isOpen());
        } else
        {
            ModLogger.logInfo("WARNING - MIDI Synthesizer is unavailable! mxTune cannot function properly!");
            synthAvailable = false;
        }
        MMLManager.getInstance().mmlInit();
    }
}