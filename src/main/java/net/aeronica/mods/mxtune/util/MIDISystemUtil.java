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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import com.sun.media.sound.AudioSynthesizer;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("restriction")
@SideOnly(Side.CLIENT)
public enum MIDISystemUtil
{

    ;
    private static MidiDevice.Info[] midiDeviceInfo = null;
    private static MidiDevice.Info bestSynthInfo = null;
    private static Synthesizer bestSynth = null;
    private static Soundbank mxTuneSoundBank = null;
    private static boolean synthAvailable = false;
    private static boolean soundBankAvailable = false;
    private static boolean midiAvailable = false;
    private static int timesToWarn = 10;
    private static List<TextComponentString> chatStatus = new ArrayList<>();
    private static final ResourceLocation SOUND_FONT = new ResourceLocation(MXTuneMain.MODID, "synth/mxtune.sf2");

    public static void mxTuneInit()
    {
        midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
        List<MidiDevice.Info> synthInfos = new ArrayList<>();
        MidiDevice device = null;
        int maxPolyphony = 0;
        Synthesizer testSynth = null;
        chatStatus.clear();
           
        for (int i = 0; i < midiDeviceInfo.length; i++) {
            try {
                device = MidiSystem.getMidiDevice(midiDeviceInfo[i]);
            } catch (MidiUnavailableException e) {
                ModLogger.error(e);
                midiAvailable = false;
            } finally {
                device.close();
            }
            if (device instanceof AudioSynthesizer) {
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
                testSynth = (AudioSynthesizer) MidiSystem.getMidiDevice(info);
            } catch (MidiUnavailableException e)
            {
                ModLogger.error(e);
                synthAvailable = false;
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
                ModLogger.error(e);
                mxTuneSoundBank = null;
            }
            if (mxTuneSoundBank != null)
            {
                Instrument[] inst = mxTuneSoundBank.getInstruments();

                /** This is workaround for a java.sound.midi system bug */
                if (mxTuneSoundBank.getName().isEmpty())
                    soundBankAvailable = false;
                else
                    soundBankAvailable = true;
                ModLogger.info("--- " + (mxTuneSoundBank.getName().isEmpty()? "*No Name*" : mxTuneSoundBank.getName() ) + " ---");
                ModLogger.info("Number of instruments: " + inst.length);
                for (Instrument i: inst) ModLogger.info("(%5d, %3d) %s", i.getPatch().getBank(), i.getPatch().getProgram(), i.getName());
                
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
    
    private static void addStatus(TextComponentString status)
    {
        chatStatus.add(status);
    }
    
    public static boolean midiUnavailable() {return !midiAvailable;}
    
    public static boolean midiUnavailableWarn(EntityPlayer playerIn)
    {
        boolean unAvailable = midiUnavailable();
        if (unAvailable && timesToWarn-- > 0) onPlayerLoggedInModStatus(playerIn);
        return unAvailable;
    }
    
    public static void onPlayerLoggedInModStatus(EntityPlayer playerIn)
    {
        if (ModConfig.hideWelcomeStatusMessage() == false)
            for (TextComponentString tcs: chatStatus) {playerIn.sendMessage(tcs);}
    }
    
    private static URL getMXTuneSoundBankURL()
    {
        URL file = MXTuneMain.class.getResource("/assets/" + SOUND_FONT.getNamespace() + "/" + SOUND_FONT.getPath());
        ModLogger.debug("Sound font path: %s", file);
        return file;
    }
    
    public static Soundbank getMXTuneSoundBank()
    {
        return mxTuneSoundBank;
    }
    
}
