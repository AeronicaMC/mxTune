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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MIDISystemUtil
{
    private MIDISystemUtil() {}
    private static class MIDISystemUtilHolder {public static final MIDISystemUtil INSTANCE = new MIDISystemUtil();}

    public static MIDISystemUtil getInstance() {return MIDISystemUtilHolder.INSTANCE;}

    private MidiDevice.Info[] midiDeviceInfo = null;
    private MidiDevice.Info bestSynthInfo = null;
    private Synthesizer bestSynth = null;
    private Soundbank soundBank = null;
    private boolean synthAvailable = false;
    private boolean soundBankAvailable = false;
    private boolean midiAvailable = false;
    private int timesToWarn = 10;
    private List<TextComponentString> chatStatus = new ArrayList<TextComponentString>();

    public void mxTuneInit()
    {
        midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
        Vector<MidiDevice.Info> synthInfos = new Vector<MidiDevice.Info>();
        MidiDevice device = null;
        int maxPolyphony = 0;
        Synthesizer testSynth = null;
        chatStatus.clear();
           
        for (int i = 0; i < midiDeviceInfo.length; i++) {
            try {
                device = MidiSystem.getMidiDevice(midiDeviceInfo[i]);
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
                midiAvailable = false;
            }
            if (device instanceof Synthesizer) {
                synthInfos.add(midiDeviceInfo[i]);
                synthAvailable = true;
            }
        }
        for (MidiDevice.Info info: synthInfos)
        {
            ModLogger.debug(info.getName());
            ModLogger.debug(info.getDescription());
            ModLogger.debug(info.getVendor());
            ModLogger.debug(info.getVersion());
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
            soundBank = bestSynth.getDefaultSoundbank();
            if (soundBank != null)
            {
                Instrument[] inst = soundBank.getInstruments();

                /** XXX: This is workaround for a java.sound.midi system bug */
                if (soundBank.getName().isEmpty())
                    soundBankAvailable = false;
                else
                    soundBankAvailable = true;
                ModLogger.info("--- " + (soundBank.getName().isEmpty()? "*No Name*" : soundBank.getName() ) + " ---");
                ModLogger.info("Number of instruments: " + inst.length);
                for (Instrument i: inst) ModLogger.info("       " + i.getName());
                
            }
        }
        if (bestSynth != null && synthAvailable && soundBankAvailable)
        {
            ModLogger.info(bestSynthInfo.getName());
            ModLogger.info(bestSynthInfo.getDescription());
            ModLogger.info(bestSynthInfo.getVendor());
            ModLogger.info(bestSynthInfo.getVersion());
            ModLogger.info("MaxPolyphony: " + bestSynth.getMaxPolyphony() + ", MaxReceivers: " + ((bestSynth.getMaxReceivers() == -1) ? "Unlimited" : bestSynth.getMaxReceivers()));
            ModLogger.info("Synthsizer Available: ?         " + synthAvailable);
            ModLogger.info("Default Sound Bank Available: ? " + soundBankAvailable);
            addStatus(new TextComponentString("[" + MXTuneMain.MODNAME + "] " + TextFormatting.GREEN +I18n.format("mxtune.chat.msu.midiAvailable")));
            midiAvailable = true;
        } else
        {
            ModLogger.error("WARNING - Default Synthesizer available? : " + synthAvailable);
            ModLogger.error("WARNING - Default Sound Bank available?  : " + soundBankAvailable);
            ModLogger.error("WARNING - MIDI System is missing resources! mxTune cannot function properly!");
            addStatus(new TextComponentString("[" + MXTuneMain.MODNAME + "] " + TextFormatting.RED +I18n.format("mxtune.chat.msu.midiNotAvailable")));
            addStatus(new TextComponentString("[" + MXTuneMain.MODNAME + "] " + TextFormatting.YELLOW +I18n.format("mxtune.chat.msu.suggestion.01")));
            addStatus(new TextComponentString("[" + MXTuneMain.MODNAME + "] " + TextFormatting.YELLOW +I18n.format("mxtune.chat.msu.suggestion.02")));

            midiAvailable = false;
        }
    }
    
    private void addStatus(TextComponentString status)
    {
        chatStatus.add(status);
    }
    
    public boolean midiUnavailable() {return !this.midiAvailable;}
    
    public boolean midiUnavailableWarn(EntityPlayer playerIn)
    {
        boolean unAvailable = midiUnavailable();
        if (unAvailable && timesToWarn-- > 0) onPlayerLoggedInModStatus(playerIn);
        return unAvailable;
    }
    
    public void onPlayerLoggedInModStatus(EntityPlayer playerIn)
    {
        if (ModConfig.hideWelcomeStatusMessage() == false)
            for (TextComponentString tcs: chatStatus) {playerIn.sendMessage(tcs);}
    }
    
}