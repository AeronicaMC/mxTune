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
import net.minecraft.client.audio.ISound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import static net.aeronica.mods.mxtune.sound.ClientAudio.Status.DONE;
import static net.aeronica.mods.mxtune.sound.ClientAudio.Status.ERROR;


public class AudioData
{
    private final int playId;
    private final BlockPos blockPos;
    private final boolean isClientPlayer;
    private AudioInputStream audioStream;
    private AudioFormat audioFormat;
    private ISound iSound;
    private ClientAudio.Status status;
    private final PlayIdSupplier.PlayType playType;
    private final IAudioStatusCallback callback;

    // Fadeout volume - Not fully utilized. Using Paul's SoundSystem fadeOut method in tandem
    private float volumeFade = 1F;
    private boolean isFading;
    private int fadeTicks;
    private int fadeCounter;

    AudioData(int playId, @Nullable BlockPos blockPos, boolean isClientPlayer, @Nullable IAudioStatusCallback callback)
    {
        this.playId = playId;
        this.playType = PlayIdSupplier.getTypeForPlayId(playId);
        this.blockPos = blockPos;
        this.isClientPlayer = isClientPlayer;
        this.status = ClientAudio.Status.WAITING;
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

    public synchronized ClientAudio.Status getStatus()
    {
        return status;
    }

    public void setStatus(ClientAudio.Status status)
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

    public int getPlayId()
    {
        return playId;
    }

    @Nullable
    BlockPos getBlockPos()
    {
        return blockPos;
    }

    boolean isClientPlayer()
    {
        return isClientPlayer;
    }

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

    @Nullable
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
            if (fadeCounter > 0)
            {
                volumeFade = (float) fadeCounter / fadeTicks;
            }
            else
            {
                isFading = false;
                volumeFade = 0F;
            }
        }
    }

    public void startFadeOut(int seconds)
    {
        // If a fade out is already in progress we will not change it.
        if (!isFading && status != ERROR && status != DONE)
        {
            fadeTicks = Math.max(Math.abs(seconds * 20), 1);
            fadeCounter = fadeTicks;
            volumeFade = 1F;
            isFading = true;
        }
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
