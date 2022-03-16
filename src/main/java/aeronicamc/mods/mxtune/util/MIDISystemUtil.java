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
package aeronicamc.mods.mxtune.util;

import aeronicamc.libs.mml.parser.MMLUtil;
import aeronicamc.mods.mxtune.MXTune;
import aeronicamc.mods.mxtune.Reference;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sun.media.sound.AudioSynthesizer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.midi.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("restriction")
public enum MIDISystemUtil
{
    ;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String NO_SOUND_BANK = "No SoundBank Loaded"; // I18n.format("mxtune.msu.no_sound_bank_loaded");
    private static MidiDevice.Info bestSynthInfo = null;
    private static Synthesizer bestSynth = null;
    private static Soundbank mxTuneSoundBank = null;
    private static boolean synthAvailable = false;
    private static boolean soundBankAvailable = false;
    private static boolean midiAvailable = false;
    private static final ResourceLocation SOUND_FONT = new ResourceLocation(Reference.MOD_ID, "synth/mxtune_v3.sf2");
    private static final List<Instrument> instrumentCache = new ArrayList<>();
    private static final BiMap<Integer, Integer> packedPresetToInstrumentCacheIndex =  HashBiMap.create();
    private static BiMap<Integer, Integer> instrumentCacheIndexToPackedPreset;

    public static void mxTuneInit()
    {
        MidiDevice.Info[] midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
        List<MidiDevice.Info> synthInfos = new ArrayList<>();
        MidiDevice device = null;
        int maxPolyphony = 0;
        Synthesizer testSynth = null;

        for (MidiDevice.Info aMidiDeviceInfo : midiDeviceInfo)
        {
            try
            {
                device = MidiSystem.getMidiDevice(aMidiDeviceInfo);
            } catch (MidiUnavailableException e)
            {
                LOGGER.error(e);
                midiAvailable = false;
            } finally
            {
                if (device != null)
                    device.close();
            }
            if (device instanceof AudioSynthesizer)
            {
                synthInfos.add(aMidiDeviceInfo);
                synthAvailable = true;
            }
        }
        for (MidiDevice.Info info: synthInfos)
        {
            LOGGER.info(info.getName());
            LOGGER.info(info.getDescription());
            LOGGER.info(info.getVendor());
            LOGGER.info(info.getVersion());
            try
            {
                testSynth = (AudioSynthesizer) MidiSystem.getMidiDevice(info);
            } catch (MidiUnavailableException e)
            {
                LOGGER.error(e);
                synthAvailable = false;
            }
            finally
            {
                if (synthAvailable && (testSynth != null) && (testSynth.getMaxPolyphony() > maxPolyphony))
                    {
                        maxPolyphony = testSynth.getMaxPolyphony();
                        bestSynthInfo = info;
                        bestSynth =  testSynth;
                    }
                if (testSynth != null)
                    testSynth.close();
            }
        }
        if (bestSynth != null && synthAvailable)
        {
            try
            {
                mxTuneSoundBank = MidiSystem.getSoundbank(getMXTuneSoundBankURL());
            } catch (InvalidMidiDataException | IOException e)
            {
                LOGGER.error(e);
                mxTuneSoundBank = null;
            }
            if (mxTuneSoundBank != null)
            {
                Instrument[] inst = mxTuneSoundBank.getInstruments();

                /* This is workaround for a java.sound.midi system bug */
                soundBankAvailable = !mxTuneSoundBank.getName().isEmpty();
                LOGGER.info("--- " + (mxTuneSoundBank.getName().isEmpty()? "*No Name*" : mxTuneSoundBank.getName() ) + " ---");
                LOGGER.info("Number of instruments: " + inst.length);
                for (Instrument i: inst) LOGGER.info("({}, {}) {}", i.getPatch().getBank(), i.getPatch().getProgram(), i.getName());
                
            }
        }
        if (bestSynth != null && synthAvailable && soundBankAvailable)
        {
            LOGGER.info(bestSynthInfo.getName());
            LOGGER.info(bestSynthInfo.getDescription());
            LOGGER.info(bestSynthInfo.getVendor());
            LOGGER.info(bestSynthInfo.getVersion());
            LOGGER.info("MaxPolyphony: " + bestSynth.getMaxPolyphony() + ", MaxReceivers: " + ((bestSynth.getMaxReceivers() == -1) ? "Unlimited" : bestSynth.getMaxReceivers()));
            LOGGER.info("Synthsizer Available: ?         " + synthAvailable);
            LOGGER.info("Default Sound Bank Available: ? " + soundBankAvailable);
            midiAvailable = true;
        } else
        {
            LOGGER.error("WARNING - Default Synthesizer available? : " + synthAvailable);
            LOGGER.error("WARNING - Default Sound Bank available?  : " + soundBankAvailable);
            LOGGER.error("WARNING - MIDI System is missing resources! mxTune cannot function properly!");

            midiAvailable = false;
        }
        initInstrumentCache();
    }

    
    public static boolean midiUnavailable() { return !midiAvailable; }

    private static URL getMXTuneSoundBankURL()
    {
        URL file = MXTune.class.getResource("/assets/" + SOUND_FONT.getNamespace() + "/" + SOUND_FONT.getPath());
        LOGGER.info("Sound font path: {}", file);
        return file;
    }
    
    public static Soundbank getMXTuneSoundBank() { return mxTuneSoundBank; }

    private static NullInstrument getNullInstrument()
    {
        return new NullInstrument(null, new Patch(0, 0), NO_SOUND_BANK, NullClass.class);
    }

    @SuppressWarnings("unused")
    private class NullClass
    {
        private int someInt;
        NullClass() { someInt = 0; }
        NullClass(int someInt) { this.someInt = someInt; }

        public int getSomeInt() { return someInt; }
    }

    /* Load MIDI instruments */
    private static void initInstrumentCache()
    {
        if (midiUnavailable() || mxTuneSoundBank == null)
            instrumentCache.add(getNullInstrument());
        else
            Collections.addAll(instrumentCache, mxTuneSoundBank.getInstruments());

        int index = 0;
        int packedPreset;
        for (Instrument instrument : instrumentCache)
        {
            packedPreset = MMLUtil.instrument2PackedPreset(instrument);
            packedPresetToInstrumentCacheIndex.put(packedPreset, index++);
        }

        instrumentCacheIndexToPackedPreset = packedPresetToInstrumentCacheIndex.inverse();
    }

    public static List<Instrument> getInstrumentCacheCopy() { return new ArrayList<>(instrumentCache); }

    public static int getPackedPresetFromInstrumentCacheIndex(int index)
    {
        int packedPreset;
        try {
            packedPreset = instrumentCacheIndexToPackedPreset.get(index);
        }
        catch (NullPointerException | ClassCastException e)
        {
            packedPreset = 0; // Acoustic Piano
        }
        return packedPreset;
    }

    public static int getInstrumentCachedIndexFromPackedPreset(int packedPatch)
    {
        int cacheIndex;
        try {
            cacheIndex = packedPresetToInstrumentCacheIndex.get(packedPatch);
        }
        catch (NullPointerException | ClassCastException e)
        {
            cacheIndex = 0; // Acoustic Piano
        }
        return cacheIndex;
    }

    // A hack is hack is a hack
    // Probably due to my poor understanding of the SoundFont2 specification
    public static String getPatchNameKey(Patch patch)
    {
        for (Instrument inst : instrumentCache)
        {
            if (inst.toString().contains("Drumkit:") && inst.getPatch().getProgram() == patch.getProgram() ||
                ((inst.getPatch().getBank()) / 128  == patch.getBank()) && (inst.getPatch().getProgram() == patch.getProgram()))
                return inst.getName();
        }
        return "--- Error ---";
    }
}
