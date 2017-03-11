/*
 * JFugue - API for Music Programming
 * Copyright (C) 2003-2008  Karl Helgason and David Koelle
 *
 * http://www.jfugue.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package net.aeronica.mods.mxtune.sound;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.sun.media.sound.AudioSynthesizer;

import net.aeronica.mods.mxtune.util.MIDISystemUtil;

/*
 * user: Paul Boese aka Aeronica
 * removed the JFugue specific pattern signature methods
 */
@SuppressWarnings("restriction")
public class Midi2WavRenderer
{
    private Synthesizer synth;
    
    public Midi2WavRenderer() throws MidiUnavailableException, InvalidMidiDataException, IOException
    {
        this.synth = MidiSystem.getSynthesizer();
    }

    private Soundbank loadSoundbank(File soundbankFile) throws MidiUnavailableException, InvalidMidiDataException, IOException
    {
        Soundbank soundbank = MidiSystem.getSoundbank(soundbankFile);
        if (!synth.isSoundbankSupported(soundbank)) {
            throw new IOException("Soundbank not supported by synthesizer");
        }
        return soundbank;
    }
    
    private Soundbank loadSoundbank(URL soundbankFile) throws MidiUnavailableException, InvalidMidiDataException, IOException
    {
        Soundbank soundbank = MidiSystem.getSoundbank(soundbankFile);
        if (!synth.isSoundbankSupported(soundbank)) {
            throw new IOException("Soundbank not supported by synthesizer");
        }
        return soundbank;
    }
    
    /**
     * Creates a WAV file based on the Sequence, using the sounds from the specified soundbank; 
     * to prevent memory problems, this method asks for an array of patches (instruments) to load.
     *  
     * @param soundbankFile
     * @param patches
     * @param sequence
     * @param outputFile
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    public void createWavFile(File soundbankFile, int[] patches, Sequence sequence, File outputFile) throws MidiUnavailableException, InvalidMidiDataException, IOException
    {
        // Load soundbank
        Soundbank soundbank = loadSoundbank(soundbankFile);
        
        // Open the Synthesizer and load the requested instruments
        this.synth.open();
        this.synth.unloadAllInstruments(soundbank);
        for (int patch : patches) {
            this.synth.loadInstrument(soundbank.getInstrument(new Patch(0, patch)));
        }
        
        createWavFile(sequence, outputFile);
    }
    
    /**
     * Creates a WAV file based on the Sequence, using the default soundbank.
     *  
     * @param sequence
     * @param outputFile
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    public void createWavFile(Sequence sequence, File outputFile) throws MidiUnavailableException, InvalidMidiDataException, IOException
    {
        AudioSynthesizer synth = findAudioSynthesizer();
        if (synth == null) {
            throw new IOException("No AudioSynthesizer was found!");
        }

        AudioFormat format = new AudioFormat(96000, 24, 2, true, false);
        Map<String, Object> p = new HashMap<String, Object>();
        p.put("interpolation", "sinc");
        p.put("max polyphony", "1024");
        AudioInputStream stream = synth.openStream(format, p);

        // Play Sequence into AudioSynthesizer Receiver.
        Receiver receiver = synth.getReceiver();
        double total = send(sequence, receiver);

        // Calculate how long the WAVE file needs to be.
        long len = (long) (stream.getFormat().getFrameRate() * (total + 4));
        stream = new AudioInputStream(stream, stream.getFormat(), len);

        // Write WAVE file to disk.
        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, outputFile);

        receiver.close();
        this.synth.close();
    }

    /**
     * Creates a PCM stream based on the Sequence, patches and audio format, using the default soundbank.
     *  
     * @param sequence
     * @param patches
     * @paran format
     * @param outputStream
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     * @throws IOException
     */
    public AudioInputStream createPCMStream(Sequence sequence, Integer[] patches, AudioFormat format) throws MidiUnavailableException, InvalidMidiDataException, IOException
    {
       
        Soundbank soundbank = loadSoundbank(MIDISystemUtil.getMXTuneSB());
        this.synth.open();
                
        AudioSynthesizer aSynth = findAudioSynthesizer();
        if (aSynth == null) {
            throw new IOException("No AudioSynthesizer was found!");
        }
        
        Map<String, Object> p = new HashMap<String, Object>();
        p.put("interpolation", "sinc");
        p.put("max polyphony", "1024");
        AudioInputStream outputStream = aSynth.openStream(format, p);

        Soundbank defsbk = aSynth.getDefaultSoundbank();
        if (defsbk != null)
            aSynth.unloadAllInstruments(defsbk);
        
        aSynth.loadAllInstruments(soundbank);
        aSynth.unloadAllInstruments(soundbank);
        for (int patch : patches) {
            aSynth.loadInstrument(soundbank.getInstrument(new Patch(0, patch)));
        }
        
        // Play Sequence into AudioSynthesizer Receiver.
        Receiver receiver = aSynth.getReceiver();
        double total = send(sequence, receiver);

        // Calculate how long the WAVE file needs to be.
        long len = (long) (outputStream.getFormat().getFrameRate() * (total + 4));
        outputStream = new AudioInputStream(outputStream, outputStream.getFormat(), len);

        receiver.close();
        this.synth.close();
        return outputStream;
    }


    /**
     * Find available AudioSynthesizer.
     */
    public static AudioSynthesizer findAudioSynthesizer() throws MidiUnavailableException 
    {
        // First check if default synthesizer is AudioSynthesizer.
        Synthesizer synth = MidiSystem.getSynthesizer();
        if (synth instanceof AudioSynthesizer) {
            return (AudioSynthesizer)synth;
        }

        // If default synthesizer is not AudioSynthesizer, check others.
        MidiDevice.Info[] midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < midiDeviceInfo.length; i++) {
            MidiDevice dev = MidiSystem.getMidiDevice(midiDeviceInfo[i]);
            if (dev instanceof AudioSynthesizer) {
                return (AudioSynthesizer) dev;
            }
        }

        // No AudioSynthesizer was found, return null.
        return null;
    }

    /**
     * Send entry MIDI Sequence into Receiver using timestamps.
     */
    public double send(Sequence seq, Receiver recv) 
    {
        float divtype = seq.getDivisionType();
        assert (seq.getDivisionType() == Sequence.PPQ);
        Track[] tracks = seq.getTracks();
        int[] trackspos = new int[tracks.length];
        int mpq = 500000;
        int seqres = seq.getResolution();
        long lasttick = 0;
        long curtime = 0;
        while (true) {
            MidiEvent selevent = null;
            int seltrack = -1;
            for (int i = 0; i < tracks.length; i++) {
                int trackpos = trackspos[i];
                Track track = tracks[i];
                if (trackpos < track.size()) {
                    MidiEvent event = track.get(trackpos);
                    if (selevent == null
                            || event.getTick() < selevent.getTick()) {
                        selevent = event;
                        seltrack = i;
                    }
                }
            }
            if (seltrack == -1)
                break;
            trackspos[seltrack]++;
            long tick = selevent.getTick();
            if (divtype == Sequence.PPQ)
                curtime += ((tick - lasttick) * mpq) / seqres;
            else
                curtime = (long) ((tick * 1000000.0 * divtype) / seqres);
            lasttick = tick;
            MidiMessage msg = selevent.getMessage();
            if (msg instanceof MetaMessage) {
                if (divtype == Sequence.PPQ)
                    if (((MetaMessage) msg).getType() == 0x51) {
                        byte[] data = ((MetaMessage) msg).getData();
                        mpq = ((data[0] & 0xff) << 16)
                                | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                    }
            } else {
                if (recv != null)
                    recv.send(msg, curtime);
            }
        }
        return curtime / 1000000.0;
    }

}