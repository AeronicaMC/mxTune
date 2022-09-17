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
package aeronicamc.mods.mxtune.sound;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioInputStream;
import java.util.Optional;

public class MML2PCM
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final AudioData audioData;

    MML2PCM(@Nullable AudioData audioData)
    {
        this.audioData = audioData;
    }

    MML2PCM process()
    {
        // Generate PCM Audio Stream from MIDI sequence
        getAudioData().ifPresent(audioData -> {
            ClientAudio.Status status = ClientAudio.Status.PLAY;
            try
            {
                if (!ActiveAudio.hasSequence(audioData.getPlayId()))
                    throw new ModMidiException("SequenceProxy expired");
                Midi2WavRenderer mw = new Midi2WavRenderer();
                AudioInputStream pcmStream = mw.createPCMStream(ActiveAudio.getSequence(audioData.getPlayId()), audioData);
                audioData.setAudioStream(pcmStream);
            } catch (ModMidiException | MidiUnavailableException e)
            {
                status = ClientAudio.Status.ERROR;
                LOGGER.error("MIDI to PCM process: ", e);
            } finally
            {
                audioData.setStatus(status);
            }
        });
        return this;
    }

    Optional<AudioData> getAudioData()
    {
        return Optional.ofNullable(this.audioData);
    }
}
