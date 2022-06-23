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
package aeronicamc.mods.mxtune.sound;

import aeronicamc.mods.mxtune.util.LoggedTimer;
import aeronicamc.mods.mxtune.util.MIDISystemUtil;
import com.sun.media.sound.AudioSynthesizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.sound.midi.*;
import javax.sound.sampled.AudioInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * user: Paul Boese aka Aeronica
 * <Li>removed the JFugue specific pattern signature methods.</Li>
 * <Li>Added an option to skip forward N seconds of audio.</Li>
 * <P>Skip forward in the send method instead!</P>
 * <P>Deprecated ref: {@link <A="https://stackoverflow.com/questions/52595473/java-start-audio-playback-at-x-position">Java: Start Audio Playback at X Position</A></P>}
 */
@SuppressWarnings("restriction")
public class Midi2WavRenderer implements Receiver
{
    private static final Logger LOGGER = LogManager.getLogger(Midi2WavRenderer.class);

    public Midi2WavRenderer() { /* NOP */ }

    /**
     * Creates a PCM stream based on the Sequence, patches and audio format, using the default soundbank.
     * Note: This uses the mxTune soundfont only
     *  
     * @param sequence the MIDI sequence to be rendered
     * @param audioData the audio format for rendering
     * @throws MidiUnavailableException potential exception
     * @throws ModMidiException potential exception
     * @return the AudioInputStream
     */
    AudioInputStream createPCMStream(Sequence sequence, AudioData audioData) throws ModMidiException, MidiUnavailableException
    {
        LoggedTimer timer = new LoggedTimer();
        timer.start(String.format("%d: Render Audio", audioData.getPlayId()));
        Soundbank mxTuneSoundBank = MIDISystemUtil.getMXTuneSoundBank();
        AudioSynthesizer audioSynthesizer = findAudioSynthesizer();
        if (audioSynthesizer == null) {
            throw new ModMidiException("No AudioSynthesizer was found!");
        }
        
        Map<String, Object> p = new HashMap<>();
        p.put("interpolation", "sinc");
        p.put("max polyphony", "1024");
        AudioInputStream outputStream = audioSynthesizer.openStream(audioData.getAudioFormat(), p);

        Soundbank defaultSoundBank = audioSynthesizer.getDefaultSoundbank();
        if (defaultSoundBank != null)
            audioSynthesizer.unloadAllInstruments(defaultSoundBank);
        
        audioSynthesizer.loadAllInstruments(mxTuneSoundBank);

        // Play Sequence into AudioSynthesizer Receiver.
        Receiver receiver = audioSynthesizer.getReceiver();
        int skipTime = audioData.getSecondsElapsed();
        double total = send(sequence, receiver, skipTime); // add 1 to account for render time
        LOGGER.info("");
        // Calculate how long the WAVE file needs to be and add 4 seconds to account for sustained notes
        long len = (long) (outputStream.getFormat().getFrameRate() * (total + 4));
        outputStream = new AudioInputStream(outputStream, outputStream.getFormat(), len);

        receiver.close();
        LOGGER.info("Skipping {} Seconds", skipTime);
        timer.stop();
        audioData.setProcessTimeMS(timer.getTimeElapsed());
        return outputStream;
    }

    /**
     * Find available AudioSynthesizer.
     */
    @Nullable
    private AudioSynthesizer findAudioSynthesizer() throws MidiUnavailableException
    {
        // First check if default synthesizer is AudioSynthesizer.
        Synthesizer synth = MidiSystem.getSynthesizer();
        if (synth instanceof AudioSynthesizer) {
            return (AudioSynthesizer)synth;
        }

        // If default synthesizer is not AudioSynthesizer, check others.
        MidiDevice.Info[] midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info aMidiDeviceInfo : midiDeviceInfo)
        {
            MidiDevice dev = MidiSystem.getMidiDevice(aMidiDeviceInfo);
            if (dev instanceof AudioSynthesizer)
            {
                return (AudioSynthesizer) dev;
            }
        }

        // No AudioSynthesizer was found, return null.
        return null;
    }

    public double getSequenceInSeconds(Sequence sequence) throws ModMidiException
    {
        return send(sequence, this, 0L);
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {/* NOP */}

    @Override
    public void close() {/* NOP */}

    /**
     * Send entry MIDI Sequence into Receiver using timestamps.<p>
     * <p>Added a simpler and FASTER way to skip forward in the tune. Do it while rendering instead of after.
     *
     * @param seq Sequence to send
     * @param recv  the Receiver of the Sequence
     * @param SkipSeconds Seconds to skip forward
     * @return Total Length in seconds
     * @throws ModMidiException
     */
    private double send(@Nullable Sequence seq, @Nullable Receiver recv, long SkipSeconds) throws ModMidiException
    {
        if (seq == null) return 0D;
        boolean skipping = SkipSeconds > 2L;
        long skipTime = (long) (1000000.0 * SkipSeconds);
        boolean oneTime = false;

        float divisionType = seq.getDivisionType();
        Track[] tracks = seq.getTracks();
        int[] tracksPos = new int[tracks.length];
        int mpq = 500000;
        int seqResolution = seq.getResolution();
        long lastTick = 0;
        long currentTime = 0;

        while (true) {
            MidiEvent selectedEvent = null;
            int selectedTrack = -1;
            for (int i = 0; i < tracks.length; i++) {
                int trackPos = tracksPos[i];
                Track track = tracks[i];
                if (trackPos < track.size()) {
                    MidiEvent event = track.get(trackPos);
                    if (selectedEvent == null
                            || event.getTick() < selectedEvent.getTick()) {
                        selectedEvent = event;
                        selectedTrack = i;
                    }
                }
            }
            if (selectedTrack == -1)
                break;
            tracksPos[selectedTrack]++;
            if (selectedEvent == null)
                throw new ModMidiException("Null MidiEvent in 'send' method: " +seq);
            long tick = selectedEvent.getTick();
            if ((int)divisionType == (int)Sequence.PPQ)
                currentTime += ((tick - lastTick) * mpq) / seqResolution;
            else
                currentTime = (long) ((tick * 1000000.0 * divisionType) / seqResolution);
            lastTick = tick;
            MidiMessage msg = selectedEvent.getMessage();
            if (msg instanceof MetaMessage) {
                if ((int)divisionType == (int)Sequence.PPQ)
                    if (((MetaMessage) msg).getType() == 0x51) {
                        byte[] data = ((MetaMessage) msg).getData();
                        mpq = ((data[0] & 0xff) << 16)
                                | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                    }
            } else {
                if (recv != null)
                {
                    // Skip forward, but keep the program settings messages. Send other messages only after skipTime.
                    if (skipping && ((currentTime < 11000) || (currentTime >= skipTime)))
                        recv.send(msg, (currentTime < 11000) ? currentTime : currentTime - skipTime);

                    // Special case: Send "All Notes Off" to each channel before we start sending to the receiver.
                    else if (skipping && !oneTime && (currentTime <= (skipTime - 100000)))
                    {
                        oneTime = true;
                        for (int channel = 0; channel < 16; channel++)
                        {
                            ShortMessage msgLoc = new ShortMessage();
                            try
                            {
                                // All Notes Off
                                msgLoc.setMessage(ShortMessage.CONTROL_CHANGE, channel, 120 & 0x7F, 0 & 0x7F);
                            } catch (InvalidMidiDataException e)
                            {
                                LOGGER.error("Failed to create All Notes Off message.");
                            }
                            recv.send(msgLoc, currentTime);
                        }
                    }
                    // Normal no skip operation. Send everything
                    else if (!skipping)
                        recv.send(msg, currentTime);
                }
            }
        }
        return currentTime / 1000000.0;
    }
}
