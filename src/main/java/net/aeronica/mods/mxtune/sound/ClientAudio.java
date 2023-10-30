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
/*
 * Portions of This file are part of Dynamic Surroundings, licensed under
 * the MIT License (MIT).
 *
 * Copyright (c) OreCruncher, Abastro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.aeronica.mods.mxtune.sound;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.managers.GroupHelper;
import net.aeronica.mods.mxtune.managers.PlayIdSupplier;
import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(Side.CLIENT)
public enum ClientAudio implements ISelectiveResourceReloadListener
{
    INSTANCE;
    public static final Object THREAD_SYNC = new Object();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static SoundHandler handler;
    private static SoundSystem sndSystem;
    private static MusicTicker musicTicker;

    private static final int THREAD_POOL_SIZE = 2;
    /* PCM Signed Monaural little endian */
    private static final AudioFormat audioFormat3D = new AudioFormat(48000, 16, 1, true, false);
    /* PCM Signed Stereo little endian */
    private static final AudioFormat audioFormatStereo = new AudioFormat(48000, 16, 2, true, false);
    /* Used to track which player/groups queued up music to be played by PlayID */
    private static final Queue<Integer> playIDQueuePCM = new ConcurrentLinkedQueue<>(); // Polled in CodecPCM
    private static final Queue<Integer> playIDQueueStreamEvent = new ConcurrentLinkedQueue<>(); // Polled in ClientAudio#playStreamingSourceEvent
    private static final Map<Integer, AudioData> playIDAudioData = new ConcurrentHashMap<>();

    private static ExecutorService executorService = null;
    private static ThreadFactory threadFactory = null;

    private static int counter = 0;
    private static final Queue<Integer> delayedAudioDataRemovalQueue = new ConcurrentLinkedDeque<>();

    private static boolean vanillaMusicPaused = false;

    private static final int MAX_STREAM_CHANNELS = 16;
    private static final int DESIRED_STREAM_CHANNELS = 8;

    public static synchronized Set<Integer> getActivePlayIDs()
    {
        return Collections.unmodifiableSet(new HashSet<>(playIDAudioData.keySet()));
    }

    private static void startThreadFactory()
    {
        if (threadFactory == null)
        {
            threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat(Reference.MOD_NAME + " ClientAudio-%d")
                    .setDaemon(true)
                    .setPriority(Thread.NORM_PRIORITY)
                    .build();
            executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE, threadFactory);
        }
    }
    
    public enum Status
    {
        WAITING, READY, ERROR, DONE
    }

    /*
     * The net.aeronica.mods.mxtune.sound package is the only place where a PlayID can be null.
     * This is due mostly to the Queue semantics where if the PlayID does not exist a null is returned.
     */

    private static synchronized void addPlayIDQueue(int playID)
    {
        if (playIDQueuePCM.add(playID)) {playIDQueueStreamEvent.add(playID);}
    }

    @Nullable
    static Integer pollPlayIDQueuePCM()
    {
        return playIDQueuePCM.poll();
    }

    @Nullable
    private static Integer pollPlayIDQueueStreamEvent()
    {
        return playIDQueueStreamEvent.poll();
    }

    @Nullable
    private static Integer peekPlayIDQueueStreamEvent()
    {
        return playIDQueueStreamEvent.peek();
    }

    static AudioData getAudioData(Integer playID)
    {
        synchronized (THREAD_SYNC)
        {
            return playIDAudioData.get(playID);
        }
    }
    
    private static synchronized void setUuid(Integer playID, String uuid)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null)
            audioData.setUuid(uuid);
    }

    static boolean hasPlayID(Integer playID)
    {
        return (!playIDAudioData.isEmpty() && playID != PlayIdSupplier.PlayType.INVALID) && playIDAudioData.containsKey(playID);
    }

    /**
     * For players.
     * @param playID unique submission identifier.
     * @param musicText MML string
     */
    public static void play(int playID, String musicText)
    {
        play(playID, null, musicText, GroupHelper.isClientPlaying(playID), SoundRange.NORMAL, null);
    }

    /**
     * For TileEntities placed in the world.
     * @param playID unique submission identifier.
     * @param pos block position in the world
     * @param musicText MML string
     * @param soundRange defines the attenuation: NATURAL or INFINITY respectively
     */
    public static void play(Integer playID, @Nullable BlockPos pos, String musicText, SoundRange soundRange)
    {
        play(playID, pos, musicText, false, soundRange, null);
    }

    public static void playLocal(int playId, String musicText, IAudioStatusCallback callback)
    {
        play(playId, Minecraft.getMinecraft().player.getPosition(), musicText, true, SoundRange.INFINITY, callback);
    }

    // Determine if audio is 3D spacial or background
    // Players playing solo, or in JAMS hear their own audio without 3D effects or falloff.
    // Musical Automata that have SoundRange.INFINITY will play for all clients without 3D effects or falloff.
    private static void setAudioFormat(AudioData audioData)
    {
        if (audioData.isClientPlayer() || (audioData.getSoundRange() == SoundRange.INFINITY))
            audioData.setAudioFormat(audioFormatStereo);
        else audioData.setAudioFormat(audioFormat3D);
    }

    private static void play(int playID, @Nullable BlockPos pos, String musicText, boolean isClient, SoundRange soundRange, @Nullable IAudioStatusCallback callback)
    {
        startThreadFactory();
        if(ClientCSDMonitor.canMXTunesPlay() && playID != PlayIdSupplier.PlayType.INVALID)
        {
            addPlayIDQueue(playID);
            AudioData audioData = new AudioData(playID, pos, isClient, soundRange, callback);
            setAudioFormat(audioData);
            AudioData result = playIDAudioData.putIfAbsent(playID, audioData);
            if (result != null)
            {
                ModLogger.warn("ClientAudio#play: playID: %s has already been submitted", playID);
                return;
            }
            if (isClient)
                mc.getSoundHandler().playSound(new MusicClient(audioData)); // Players instruments or BGM
            else if (pos == null)
                mc.getSoundHandler().playSound(new MovingMusic(audioData)); // Other players instruments
            else
                mc.getSoundHandler().playSound(new MusicPositioned(audioData)); // Block in-world instruments
            executorService.execute(new CreatePCMAudioStreamFromMML(audioData, musicText));
            stopVanillaMusic();
        } else
        {
            ModLogger.warn("ClientAudio#play(Integer playID, BlockPos pos, String musicText): playID is null!");
        }
    }

    public static void stop(int playID)
    {
        synchronized (SoundSystemConfig.THREAD_SYNC)
        {
            AudioData audioData = playIDAudioData.get(playID);
            if (sndSystem != null && audioData != null && !audioData.getUuid().isEmpty())
            {
                audioData.setStatus(Status.DONE);
                sndSystem.fadeOut(audioData.getUuid(), null, 100);
            }
        }
    }
    
    private static class CreatePCMAudioStreamFromMML implements Runnable
    {
        private final AudioData audioData;
        private final String musicText;

        CreatePCMAudioStreamFromMML(AudioData audioData, String musicText)
        {
            this.audioData = audioData;
            this.musicText = musicText;
        }

        @Override
        public void run()
        {
            MML2PCM mml2PCM = new MML2PCM(audioData, musicText);
            mml2PCM.process();
        }
    }

    // Copied from vanilla 1.11.2 MusicTicker class
    private static void stopVanillaMusicTicker()
    {
        if (musicTicker.currentMusic != null)
        {
            handler.stopSound(musicTicker.currentMusic);
            musicTicker.currentMusic = null;
            musicTicker.timeUntilNextMusic = 0;
        }
    }
    
    private static void stopVanillaMusic()
    {
        ModLogger.debug("ClientAudio stopVanillaMusic - PAUSED on %d active sessions.", playIDAudioData.size());
        setVanillaMusicPaused(true);
        stopVanillaMusicTicker();
        setVanillaMusicTimer(Integer.MAX_VALUE);
    }

    private static void resumeVanillaMusic()
    {
        ModLogger.debug("ClientAudio resumeVanillaMusic - RESUMED");
        setVanillaMusicTimer(100);
    }
       
    private static void setVanillaMusicTimer(int value)
    {
        if (musicTicker != null)
            musicTicker.timeUntilNextMusic = value;
    }
    
    private static void setVanillaMusicPaused(boolean flag)
    {
        vanillaMusicPaused = flag;
    }
    
    private static boolean isVanillaMusicPaused()
    {
        return vanillaMusicPaused;
    }

    private static void updateClientAudio()
    {
        if (sndSystem != null)
        {
            if(isVanillaMusicPaused() && playIDAudioData.isEmpty() )
            {
                resumeVanillaMusic();
                setVanillaMusicPaused(false);
            } else if (!playIDAudioData.isEmpty())
            {
                // don't allow the timer to counter down while ClientAudio sessions are playing
                setVanillaMusicTimer(Integer.MAX_VALUE);
            }
            // Remove inactive playIDs
            removeQueuedAudioData();
            for (Map.Entry<Integer, AudioData> entry : playIDAudioData.entrySet())
            {
                AudioData audioData = entry.getValue();
                Status status = audioData.getStatus();
                if (playIdExpired(audioData.getPlayId()) || status == Status.ERROR || status == Status.DONE)
                {
                    // Stopping playing audio takes 100 milliseconds. e.g. SoundSystem fadeOut(<source>, <delay in ms>)
                    // To prevent audio clicks/pops we have the wait at least that amount of time
                    // before removing the AudioData instance for this playID.
                    // Therefore the removal is queued for 250 milliseconds.
                    // e.g. the client tick setup to trigger once every 1/4 second.
                    queueAudioDataRemoval(entry.getKey());
                    ModLogger.debug("updateClientAudio: AudioData for playID queued for removal");
                }
            }
        }
    }

    private static boolean playIdExpired(int playId)
    {
        return !GroupHelper.getAllPlayIDs().contains(playId);
    }

    private static void removeQueuedAudioData()
    {
        while (!delayedAudioDataRemovalQueue.isEmpty())
            if (delayedAudioDataRemovalQueue.peek() != null)
                playIDAudioData.remove(Objects.requireNonNull(delayedAudioDataRemovalQueue.poll()));
    }

    public static void queueAudioDataRemoval(int playId)
    {
        stop(playId);
        delayedAudioDataRemovalQueue.add(playId);
    }

    private static void updateVolumeFades()
    {
        synchronized (playIDAudioData)
        {
           playIDAudioData.values().forEach(ClientAudio::updateVolumeFade);
        }
    }

    private static void updateVolumeFade(AudioData audioData)
    {
        if (audioData.getStatus() != Status.DONE && audioData.getStatus() != Status.ERROR && audioData.isFading())
        {
            audioData.updateVolumeFade();
            if (audioData.getFadeMultiplier() <= 0.05)
                queueAudioDataRemoval(audioData.getPlayId());
        }
    }

    /**
     * Causes the specified playID to fade out over the specified number of seconds.
     * @param playID the play session to fade.
     * @param seconds to fade out and stop the song. A value of 0 will stop the song immediately.
     */
    public static void fadeOut(int playID, int seconds)
    {
        synchronized (playIDAudioData)
        {
            AudioData audioData = playIDAudioData.get(playID);
            if (sndSystem != null && audioData != null && audioData.getUuid() != null &&
                    seconds > 0)
            {
                sndSystem.fadeOut(audioData.getUuid(), null, Math.max(Math.abs(seconds * 900L), 100L));
                audioData.startFadeOut(seconds);
            }
            else
                queueAudioDataRemoval(playID);
        }
    }

    private static void init()
    {
        if (sndSystem == null || sndSystem.randomNumberGenerator == null)
        {
            handler = Minecraft.getMinecraft().getSoundHandler();
            SoundManager sndManager = handler.sndManager;
            sndSystem = sndManager.sndSystem;
            musicTicker = Minecraft.getMinecraft().getMusicTicker();
            setVanillaMusicPaused(false);
            playIDAudioData.clear();
        }
    }
    
    private static void cleanup()   
    {
        setVanillaMusicPaused(false);
        playIDAudioData.keySet().forEach(ClientAudio::queueAudioDataRemoval);
        playIDQueuePCM.clear();
        playIDQueueStreamEvent.clear();
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, @Nonnull Predicate<IResourceType> resourcePredicate)
    {
        if (resourcePredicate.test(VanillaResourceType.SOUNDS))
        {
            ModLogger.info("Restarting mxTune");
            configureSound();
            init();
        }
    }

    @SubscribeEvent
    public static void event(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityPlayerSP)
        {
            cleanup();
            ModLogger.debug("ClientAudio EntityJoinWorldEvent: %s", event.getEntity().getName());
        }
    }

    @SubscribeEvent
    public static void event(PlayerRespawnEvent event)
    {
        cleanup();
        ModLogger.debug("ClientAudio PlayerRespawnEvent: %s", event.player.getName());
    }

    @SubscribeEvent
    public static void event(ClientTickEvent event)
    {
        if (event.side == Side.CLIENT && event.phase == TickEvent.Phase.END)
        {
            /* once per tick but not if game is paused */
            if (!mc.isGamePaused())
                updateVolumeFades();

            /* once every 1/4 second */
            if (counter++ % 5 == 0)
                updateClientAudio();
        }
    }
    
    @SubscribeEvent
    public static void event(SoundSetupEvent event) throws SoundSystemException
    {
        SoundSystemConfig.setCodec("nul", CodecPCM.class);
        ModLogger.debug("Sound Setup Event: associate the \"nul\" extension with CodecPCM.");
        ModLogger.debug("Sound Streaming Buffer Size: %d", SoundSystemConfig.getStreamingBufferSize());
        configureSound();
    }
    
    @SubscribeEvent
    public static void event(PlaySoundEvent e)
    {
        init();
        ResourceLocation soundLocation = e.getSound().getSoundLocation();
        if (
                (ModConfig.isCreativeMusicDisabled() && soundLocation.equals(SoundEvents.MUSIC_CREATIVE.getSoundName())) ||
                (ModConfig.isCreditsMusicDisabled() && soundLocation.equals(SoundEvents.MUSIC_CREDITS.getSoundName())) ||
                (ModConfig.isDragonMusicDisabled() && soundLocation.equals(SoundEvents.MUSIC_DRAGON.getSoundName())) ||
                (ModConfig.isEndMusicDisabled() && soundLocation.equals(SoundEvents.MUSIC_END.getSoundName())) ||
                (ModConfig.isGameMusicDisabled() && soundLocation.equals(SoundEvents.MUSIC_GAME.getSoundName())) ||
                (ModConfig.isMenuMusicDisabled() && soundLocation.equals(SoundEvents.MUSIC_MENU.getSoundName())) ||
                (ModConfig.isNetherMusicDisabled() && soundLocation.equals(SoundEvents.MUSIC_NETHER.getSoundName())))
            e.setResultSound(null);
    }

    /**
     * This event hook associates the internal sound UUID with the
     * mxTune sound event.
     */
    @SubscribeEvent
    public static void event(PlayStreamingSourceEvent e)
    {
        if (e.getSound().getSoundLocation().equals(ModSoundEvents.PCM_PROXY.getSoundName()) &&
                ClientAudio.peekPlayIDQueueStreamEvent() != null)
        {
            Integer playID = ClientAudio.pollPlayIDQueueStreamEvent();
            ClientAudio.setUuid(playID, e.getUuid());
            ModLogger.debug("ClientAudio PlayStreamingSourceEvent: uuid: %s, ISound: %s", e.getUuid(), e.getSound());
        }
    }

    /*
     * This section Poached from Dynamic Surroundings
     */
    private static void alErrorCheck() {
        final int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR)
            ModLogger.warn("OpenAL error: %d", error);
    }

    private static void configureSound() {
        int totalChannels = -1;

        try {
            final boolean create = !AL.isCreated();
            if (create)
            {
                AL.create();
                alErrorCheck();
            }

            final IntBuffer ib = BufferUtils.createIntBuffer(1);
            ALC10.alcGetInteger(AL.getDevice(), ALC11.ALC_MONO_SOURCES, ib);
            alErrorCheck();
            totalChannels = ib.get(0);

            if (create)
                AL.destroy();

        } catch (final Throwable e) {
            ModLogger.error(e);
        }

        int normalChannelCount = SoundSystemConfig.getNumberNormalChannels();
        int streamChannelCount = SoundSystemConfig.getNumberStreamingChannels();

        if (ModConfig.getAutoConfigureChannels() && (totalChannels > 64) && (streamChannelCount < DESIRED_STREAM_CHANNELS))
        {
            totalChannels = ((totalChannels + 1) * 3) / 4;
            streamChannelCount = Math.min(Math.min(totalChannels / 5, MAX_STREAM_CHANNELS), DESIRED_STREAM_CHANNELS);
            normalChannelCount = totalChannels - streamChannelCount;
        }
        else if ((totalChannels != -1) && ((normalChannelCount + streamChannelCount) >= 32))
        {
            // Try for at least 6 streaming channels if not using auto configure and we expect default SoundSystemConfig settings
            while ( streamChannelCount < 6 )
            {
                if (normalChannelCount > 24)
                {
                    normalChannelCount--;
                    streamChannelCount++;
                }
                else
                    break;
            }
        }

        ModLogger.info("Sound channels: %d normal, %d streaming (total avail: %s)", normalChannelCount, streamChannelCount,
                totalChannels == -1 ? "UNKNOWN" : Integer.toString(totalChannels));
        SoundSystemConfig.setNumberNormalChannels(normalChannelCount);
        SoundSystemConfig.setNumberStreamingChannels(streamChannelCount);
    }
}
