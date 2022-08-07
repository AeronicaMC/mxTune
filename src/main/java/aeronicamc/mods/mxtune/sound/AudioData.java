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


import aeronicamc.mods.mxtune.caps.venues.MusicVenueHelper;
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
import java.util.Objects;

import static aeronicamc.mods.mxtune.util.SheetMusicHelper.formatDuration;


public class AudioData implements Cloneable
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

    private boolean fadeToStop;
    private boolean dead;

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
        this.processTimeMS = 0; //processTimeMS;
        this.secondsElapsed = secondsElapsed;
        this.playId = playId;
        this.playType = PlayIdSupplier.getTypeForPlayId(playId);
        this.entityId = entityId;
        this.isClientPlayer = isClientPlayer;
        boolean inVenue = MusicVenueHelper.getEntityVenueState(Objects.requireNonNull(mc.level), entityId).inVenue();
        this.audioFormat = (isClientPlayer || inVenue) ? ClientAudio.AUDIO_FORMAT_STEREO : ClientAudio.AUDIO_FORMAT_3D;
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
                if (callback != null && !dead)
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
        return (removalSeconds < secondsElapsed) || fadeToStop;
    }

    int getPlayId()
    {
        return !dead ? playId : PlayIdSupplier.INVALID;
    }

    int getEntityId()
    {
        return entityId;
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
                if (!fadeIn || fadeToStop)
                    expire();
            }
        }
    }

    /**
     * Start a fade-in or fade-out. Fade-in is set true only in the {@link PCMAudioStream} notifyOnInputStreamAvailable() method.
     * @param seconds   duration of the fade in seconds. 5 MAX.
     * @param fadeIn    set true for fade-in mode.
     * @param stop
     */
    void startFadeInOut(int seconds, boolean fadeIn, boolean stop)
    {
        synchronized (this)
        {
            // If a fade out is already in progress we will not change it.
            this.fadeIn = fadeIn;
            this.fadeToStop = stop;
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
        if ((status != ClientAudio.Status.YIELD) || isEntityRemoved() || fadeToStop)
            setStatus(ClientAudio.Status.DONE);
        ClientAudio.stop(iSound);
    }

    void yield()
    {
        if (ClientAudio.isPlayingStatus(status))
        {
            setStatus(ClientAudio.Status.YIELD);
            startFadeInOut(1, false, false);
        }
    }

    void resume()
    {
        if (!this.dead && status.equals(ClientAudio.Status.YIELD))
        {
            AudioData clone = (AudioData) this.clone();
            clone.setStatus(ClientAudio.Status.WAITING);
            this.dead = true;
            this.startFadeInOut(1, false, true);
            ActiveAudio.addEntry(clone);
            ClientAudio.submitAudioData(clone);
        }
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

    /**
     * Creates and returns a copy of this object.  The precise meaning
     * of "copy" may depend on the class of the object. The general
     * intent is that, for any object {@code x}, the expression:
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * will be true, and that the expression:
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * will be {@code true}, but these are not absolute requirements.
     * While it is typically the case that:
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * will be {@code true}, this is not an absolute requirement.
     * <p>
     * By convention, the returned object should be obtained by calling
     * {@code super.clone}.  If a class and all of its superclasses (except
     * {@code Object}) obey this convention, it will be the case that
     * {@code x.clone().getClass() == x.getClass()}.
     * <p>
     * By convention, the object returned by this method should be independent
     * of this object (which is being cloned).  To achieve this independence,
     * it may be necessary to modify one or more fields of the object returned
     * by {@code super.clone} before returning it.  Typically, this means
     * copying any mutable objects that comprise the internal "deep structure"
     * of the object being cloned and replacing the references to these
     * objects with references to the copies.  If a class contains only
     * primitive fields or references to immutable objects, then it is usually
     * the case that no fields in the object returned by {@code super.clone}
     * need to be modified.
     * <p>
     * The method {@code clone} for class {@code Object} performs a
     * specific cloning operation. First, if the class of this object does
     * not implement the interface {@code Cloneable}, then a
     * {@code CloneNotSupportedException} is thrown. Note that all arrays
     * are considered to implement the interface {@code Cloneable} and that
     * the return type of the {@code clone} method of an array type {@code T[]}
     * is {@code T[]} where T is any reference or primitive type.
     * Otherwise, this method creates a new instance of the class of this
     * object and initializes all its fields with exactly the contents of
     * the corresponding fields of this object, as if by assignment; the
     * contents of the fields are not themselves cloned. Thus, this method
     * performs a "shallow copy" of this object, not a "deep copy" operation.
     * <p>
     * The class {@code Object} does not itself implement the interface
     * {@code Cloneable}, so calling the {@code clone} method on an object
     * whose class is {@code Object} will result in throwing an
     * exception at run time.
     *
     * @return a clone of this instance.
     * @throws CloneNotSupportedException if the object's class does not
     *                                    support the {@code Cloneable} interface. Subclasses
     *                                    that override the {@code clone} method can also
     *                                    throw this exception to indicate that an instance cannot
     *                                    be cloned.
     * @see Cloneable
     */
    @Override
    protected Object clone()
    {
        try {
            return (AudioData) super.clone();
        } catch (CloneNotSupportedException e)
        {
            return new AudioData(this.durationSeconds, this.secondsElapsed, this.processTimeMS, this.getPlayId(), this.getEntityId(), this.isClientPlayer, this.callback);
        }
    }
}
