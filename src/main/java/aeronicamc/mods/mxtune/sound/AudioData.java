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
package aeronicamc.mods.mxtune.sound;


import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.util.LoggedTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;


public class AudioData
{
    private final int playId;
    private final int secondsToSkip;
    private final BlockPos blockPos;
    private final boolean isClientPlayer;
    private AudioInputStream audioStream;
    private AudioFormat audioFormat;
    private ISound iSound;
    private ClientAudio.Status status;
    private final PlayIdSupplier.PlayType playType;
    private final IAudioStatusCallback callback;
    private final long netTransitTime;
    private final LoggedTimer loggedTimer = new LoggedTimer();
    private final Minecraft mc = Minecraft.getInstance();

    // TODO: Fadeout volume - Not fully implemented.
    private float volumeFade = 1F;
    private boolean isFading;
    private int fadeTicks;
    private int fadeCounter;

    /**
     * All the data needed to generate and manage the audio stream except the musicText which is passed to the parser only.
     * @param secondsToSkip forward in the audio stream.
     * @param netTransitTime in milliseconds for server packet to reach the client.
     * @param playId for this stream.
     * @param blockPos of the audio source.
     * @param isClientPlayer the audio source. Used stereo vs 3D audio selection and ISound type.
     * @param callback is optional and is used to notify a class that implements the {@link IAudioStatusCallback}
     */
    AudioData(int secondsToSkip, long netTransitTime, int playId, @Nullable BlockPos blockPos, boolean isClientPlayer, @Nullable IAudioStatusCallback callback)
    {
        this.secondsToSkip = secondsToSkip;
        this.netTransitTime = netTransitTime;
        this.playId = playId;
        this.playType = PlayIdSupplier.getTypeForPlayId(playId);
        this.blockPos = blockPos;
        this.isClientPlayer = isClientPlayer;
        this.status = ClientAudio.Status.WAITING;
        this.callback = callback;
    }

    synchronized AudioFormat getAudioFormat()
    {
        return audioFormat;
    }

    void setAudioFormat(AudioFormat audioFormat)
    {
        synchronized (this)
        {
            this.audioFormat = audioFormat;
            loggedTimer.start("Generate AudioData");
        }
    }

    synchronized ClientAudio.Status getStatus()
    {
        return status;
    }

    void setStatus(ClientAudio.Status status)
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

    synchronized long getSecondsToSkip()
    {
        loggedTimer.stop();
        return secondsToSkip > 0 ? Math.round(
            ((double)secondsToSkip) + (((double)loggedTimer.getTimeElapsed() + (double) netTransitTime) / 1000f)) : 0L;
    }

    synchronized int getPlayId()
    {
        return playId;
    }

    @Nullable
    synchronized BlockPos getBlockPos()
    {
        return blockPos;
    }

    synchronized boolean isClientPlayer()
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

    synchronized PlayIdSupplier.PlayType getPlayType()
    {
        return playType;
    }

    void updateVolumeFade()
    {
        synchronized (this)
        {
            if (isFading)
            {
                fadeCounter--;
                if (fadeCounter > 0)
                {
                    volumeFade = (float) (fadeCounter + mc.getDeltaFrameTime()) / fadeTicks;
                }
                else
                {
                    isFading = false;
                    volumeFade = 0F;
                    ClientAudio.queueAudioDataRemoval(playId);
                }
            }
        }
    }

    void startFadeOut(int seconds)
    {
        synchronized (this)
        {
            // If a fade out is already in progress we will not change it.
            if (!isFading && !(status == ClientAudio.Status.ERROR || status == ClientAudio.Status.DONE))
            {
                fadeTicks = Math.max(Math.abs(seconds * 20), 5);
                fadeCounter = fadeTicks;
                volumeFade = 1F;
                isFading = true;
            }
        }
    }

    synchronized float getFadeMultiplier()
    {
        return volumeFade;
    }

    synchronized boolean isFading()
    {
        return isFading;
    }
}
