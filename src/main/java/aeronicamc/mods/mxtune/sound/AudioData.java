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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nullable;
import javax.sound.midi.Sequence;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;


public class AudioData
{
    private final LoggedTimer loggedTimer = new LoggedTimer();
    private final Minecraft mc = Minecraft.getInstance();

    private AudioInputStream audioStream;
    private AudioFormat audioFormat;
    private ISound iSound;
    private Sequence sequence;
    private ClientAudio.Status status;

    private final int secondsToSkip;
    private final int durationSeconds;
    private final int removalSeconds;
    private final long netTransitTime;
    private final int playId;
    private final PlayIdSupplier.PlayType playType;
    private final int entityId;
    private final boolean isClientPlayer;
    private final IAudioStatusCallback callback;

    private int secondsElapsed;
    private float volumeFade = 1F;
    private boolean fadeIn;
    private boolean isFading;
    private int fadeTicks;
    private int fadeCounter;

    /**
     * All the data needed to generate and manage the audio stream except the musicText which is passed to the parser only.
     * @param durationSeconds
     * @param secondsToSkip forward in the audio stream.
     * @param netTransitTime in milliseconds for server packet to reach the client.
     * @param playId for this stream.
     * @param entityId of the audio source.
     * @param isClientPlayer the audio source. Used stereo vs 3D audio selection and ISound type.
     * @param callback is optional and is used to notify a class that implements the {@link IAudioStatusCallback}
     */
    AudioData(int durationSeconds, int secondsToSkip, long netTransitTime, int playId, int entityId, boolean isClientPlayer, @Nullable IAudioStatusCallback callback)
    {
        this.durationSeconds = durationSeconds;
        this.removalSeconds = durationSeconds + 2;
        this.secondsToSkip = secondsToSkip;
        this.netTransitTime = netTransitTime;
        this.playId = playId;
        this.playType = PlayIdSupplier.getTypeForPlayId(playId);
        this.entityId = entityId;
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

    void tickDuration()
    {
        ++secondsElapsed;
    }

    int getSecondsElapsed()
    {
        return secondsElapsed;
    }

    boolean isActive()
    {
        return durationSeconds >= secondsElapsed;
    }

    boolean canRemove()
    {
        return removalSeconds < secondsElapsed;
    }

    synchronized int getPlayId()
    {
        return playId;
    }

    synchronized int getEntityId()
    {
        return entityId;
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

    @Nullable
    synchronized Sequence getSequence()
    {
        return sequence;
    }

    public void setSequence(Sequence sequence)
    {
        synchronized (this)
        {
            this.sequence = sequence;
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
                    volumeFade = (fadeCounter + mc.getDeltaFrameTime()) / fadeTicks;
                }
                else
                {
                    isFading = false;
                    volumeFade = 0F;
                    if (!fadeIn)
                        expire();
                }
            }
        }
    }

    /**
     * Start a fade-in or fade-out. Fade-in is set true only in the {@link PCMAudioStream} notifyOnInputStreamAvailable() method.
     * @param seconds   duration of the fade in seconds. 5 MAX.
     * @param fadeIn    set true for fade-in mode.
     */
    void startFadeInOut(int seconds, boolean fadeIn)
    {
        synchronized (this)
        {
            // If a fade out is already in progress we will not change it.
            this.fadeIn = fadeIn;
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
        return fadeIn ? 1.0F - volumeFade : volumeFade;
    }

    synchronized boolean isFading()
    {
        return isFading;
    }

    synchronized void expire()
    {
        volumeFade = 0F;
        isFading = false;
        secondsElapsed = Integer.MAX_VALUE / 2;
        ClientAudio.stop(playId);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("isClientPlayer", isClientPlayer)
                .append("entityId", entityId)
                .append("playId", playId)
                .append("secondsElapsed", secondsElapsed)
                .append("durationSeconds", durationSeconds)
                .append("removalSeconds", removalSeconds)
                .append("status", status)
                .append("callback", callback)
                .build();
    }
}
