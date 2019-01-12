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
package net.aeronica.mods.mxtune.sound;

import net.aeronica.mods.mxtune.sound.ClientAudio.Status;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.math.BlockPos;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public class AudioData
{
    private final Integer playID;
    private final BlockPos blockPos;
    private final boolean isClientPlayer;
    private final SoundRange soundRange;
    private AudioInputStream audioStream;
    private AudioFormat audioFormat;
    private String uuid;
    private ISound iSound;
    private Status status;
    
    AudioData(Integer entityID, BlockPos blockPos, boolean isClientPlayer, SoundRange soundRange)
    {
        this.playID = entityID;
        this.blockPos = blockPos;
        this.isClientPlayer = isClientPlayer;
        this.soundRange = soundRange;
        this.status = Status.WAITING;
    }

    public AudioFormat getAudioFormat()
    {
        return audioFormat;
    }

    public void setAudioFormat(AudioFormat audioFormat)
    {
        this.audioFormat = audioFormat;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public Integer getPlayID()
    {
        return playID;
    }
    
    BlockPos getBlockPos()
    {
        return blockPos;
    }
    
    boolean isClientPlayer()
    {
        return isClientPlayer;
    }

    SoundRange getSoundRange() { return soundRange; }
    
    AudioInputStream getAudioStream()
    {
        return audioStream;
    }

    void setAudioStream(AudioInputStream audioStream)
    {
        this.audioStream = audioStream;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    ISound getISound()
    {
        return iSound;
    }

    void setISound(ISound iSound)
    {
        this.iSound = iSound;
    }
}
