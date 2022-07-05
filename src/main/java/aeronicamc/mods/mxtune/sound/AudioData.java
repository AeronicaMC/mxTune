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
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nullable;
import javax.sound.midi.Sequence;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import static aeronicamc.mods.mxtune.util.SheetMusicHelper.formatDuration;


public class AudioData
{
    private final Minecraft mc = Minecraft.getInstance();
    private static final Vector3d MAX_VECTOR3D = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);

    private AudioInputStream audioStream;
    private AudioFormat audioFormat;
    private ISound iSound;
    private Sequence sequence;
    private ClientAudio.Status status;

    private long processTimeMS;
    private final int durationSeconds;
    private final int removalSeconds;
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
     * @param durationSeconds length of tune (includes four seconds buffer to account for decaying audio).
     * @param secondsElapsed in the audio stream.
     * @param processTimeMS in milliseconds for server packet to reach the client.
     * @param playId for this stream.
     * @param entityId of the audio source.
     * @param isClientPlayer the audio source. Used stereo vs 3D audio selection.
     * @param callback is optional and is used to notify a class that implements the {@link IAudioStatusCallback}
     */
    AudioData(int durationSeconds, int secondsElapsed, long processTimeMS, int playId, int entityId, boolean isClientPlayer, @Nullable IAudioStatusCallback callback)
    {
        this.durationSeconds = durationSeconds;
        this.removalSeconds = durationSeconds + 4;
        this.processTimeMS = processTimeMS;
        this.secondsElapsed = secondsElapsed;
        this.playId = playId;
        this.playType = PlayIdSupplier.getTypeForPlayId(playId);
        this.entityId = entityId;
        this.isClientPlayer = isClientPlayer;
        this.audioFormat = isClientPlayer ? ClientAudio.AUDIO_FORMAT_STEREO : ClientAudio.AUDIO_FORMAT_3D;
        this.status = ClientAudio.Status.YIELD;
        this.callback = callback;
    }

    void addProcessTimeMS(long ms)
    {
        // processTimeMS += ms;
    }

    void setProcessTimeMS(long ms)
    {
        // processTimeMS = ms;
    }

    int applyProcessTimeToElapsedTime()
    {
        // secondsElapsed += Math.round((float) processTimeMS / 1000.0);
        processTimeMS = 0;
        return secondsElapsed;
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

    synchronized int getSecondsElapsed()
    {
        return applyProcessTimeToElapsedTime();
    }

    void tick()
    {
        secondsElapsed++;
    }

    boolean isActive()
    {
        return durationSeconds >= (secondsElapsed);
    }

    boolean canRemove()
    {
        return removalSeconds < (secondsElapsed);
    }

    int getPlayId()
    {
        return playId;
    }

    int getEntityId()
    {
        return entityId;
    }

    boolean isClientPlayer()
    {
        return isClientPlayer;
    }

    AudioInputStream getAudioStream()
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
    ISound getISound()
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

    Sequence getSequence()
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

    PlayIdSupplier.PlayType getPlayType()
    {
        return playType;
    }

    void updateVolumeFade()
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
            if (!isFading && !ClientAudio.isDoneStatus(status))
            {
                fadeTicks = Math.max(Math.abs(seconds * 20), 5);
                fadeCounter = fadeTicks;
                volumeFade = 1F;
                isFading = true;
            }
        }
    }

    float getFadeMultiplier()
    {
        return fadeIn ? 1.0F - volumeFade : volumeFade;
    }

    boolean isFading()
    {
        return isFading;
    }

    /**
     * Stops Vanilla audio, and if the tune is not yielding will set change the status to DONE, signalling the end of the tune.
     */
    void expire()
    {
        volumeFade = 0F;
        isFading = false;
        if ((status != ClientAudio.Status.YIELD) || isEntityRemoved())
            setStatus(ClientAudio.Status.DONE);
        ClientAudio.stop(iSound);
    }

    void yield()
    {
        if (ClientAudio.isPlayingStatus(status))
        {
            setStatus(ClientAudio.Status.YIELD);
            startFadeInOut(1, false);
        }
    }

    void resume()
    {
        setStatus(ClientAudio.Status.WAITING);
        ClientAudio.submitAudioData(this);
    }

    int getRemainingDuration()
    {
        return Math.max(durationSeconds - secondsElapsed, 0);
    }

    float getProgress()
    {
        return Math.max(Math.min(1F / ((durationSeconds +0.1F) / (secondsElapsed + 0.1F)), 1F), 0F);
    }

    double getDistanceTo()
    {
        return mc.player != null ? mc.player.getPosition(mc.getDeltaFrameTime()).distanceTo(getEntityPosition()) : Double.MAX_VALUE;
    }

    private Vector3d getEntityPosition()
    {
        Entity entity;
        if ((mc.player != null) && (mc.player.level != null) && (((entity = mc.player.level.getEntity(entityId))) != null))
        {
            return entity.getPosition(mc.getDeltaFrameTime());
        }
        return MAX_VECTOR3D;
    }

    boolean isEntityRemoved()
    {
        return !(mc.level != null && mc.level.getEntity(entityId) != null);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("isClientPlayer", isClientPlayer)
                .append("entityId", entityId)
                .append("playId", playId)
                .append("secondsToSkip", processTimeMS)
                .append("secondsElapsed", secondsElapsed)
                .append("durationSeconds", durationSeconds)
                .append("removalSeconds", removalSeconds)
                .append("status", status)
                .append("callback", callback)
                .build();
    }

    public ITextComponent getInfo()
    {
        String name = mc.level != null && mc.level.getEntity(entityId) != null ? mc.level.getEntity(entityId).getName().getString() : "[*ERROR*]";
        return new StringTextComponent(
                String.format("[%s] %s, E:%06d, P:%06d, T:%04d, RD:%s PCT:%01.2f, dist:%05.2f",
                              status, name, entityId, playId, secondsElapsed, formatDuration(getRemainingDuration()), getProgress(), getDistanceTo())).withStyle(TextFormatting.WHITE);
    }
}
