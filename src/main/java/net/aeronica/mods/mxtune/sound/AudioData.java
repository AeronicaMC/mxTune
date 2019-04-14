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

import net.aeronica.mods.mxtune.managers.PlayIdSupplier;
import net.aeronica.mods.mxtune.sound.ClientAudio.Status;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.math.BlockPos;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public class AudioData
{
    private final Integer playId;
    private final BlockPos blockPos;
    private final boolean isClientPlayer;
    private final SoundRange soundRange;
    private AudioInputStream audioStream;
    private AudioFormat audioFormat;
    private String uuid;
    private ISound iSound;
    private Status status;
    private final PlayIdSupplier.PlayType playType;
    private final IAudioStatusCallback callback;

    private float volumeFade = 1F;
    private boolean isFading;
    private int fadeTicks;
    private int fadeCounter;

    AudioData(Integer playId, BlockPos blockPos, boolean isClientPlayer, SoundRange soundRange, IAudioStatusCallback callback)
    {
        this.playId = playId;
        this.playType = PlayIdSupplier.getTypeForPlayId(playId);
        this.blockPos = blockPos;
        this.isClientPlayer = isClientPlayer;
        this.soundRange = soundRange;
        this.status = Status.WAITING;
        this.callback = callback;
    }

    public synchronized AudioFormat getAudioFormat()
    {
        return audioFormat;
    }

    public void setAudioFormat(AudioFormat audioFormat)
    {
        synchronized (this)
        {
            this.audioFormat = audioFormat;
        }
    }

    public synchronized Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        synchronized (this)
        {
            if (this.status != status)
            {
                this.status = status;
                if (callback != null)
                    callback.statusCallBack(this.status, playId);
            }
        }
    }

    public Integer getPlayId()
    {
        return playId;
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

    synchronized AudioInputStream getAudioStream()
    {
        return audioStream;
    }

    void setAudioStream(AudioInputStream audioStream)
    {
        synchronized (this)
        {
            this.audioStream = audioStream;
        }
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    synchronized ISound getISound()
    {
        return iSound;
    }

    void setISound(ISound iSound)
    {
        synchronized (this)
        {
            this.iSound = iSound;
        }
    }

    public PlayIdSupplier.PlayType getPlayType()
    {
        return playType;
    }

    public void updateVolumeFade()
    {
        if (isFading)
        {
            fadeCounter--;
            if (fadeCounter > 1)
            {
                volumeFade = (float) fadeTicks / fadeCounter;
            }
            else
            {
                isFading = false;
                setStatus(Status.DONE);
                volumeFade = 0F;
            }
        }
    }

    public void startFadeOut(int seconds)
    {
        fadeTicks = Math.max(Math.abs(seconds * 20), 1);
        fadeCounter = fadeTicks;
        volumeFade = 1F;
        isFading = true;
    }

    public float getFadeMultiplier()
    {
        return volumeFade;
    }

    public boolean isFading()
    {
        return isFading;
    }
}
