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
import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.groups.GroupHelper;
import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;

import javax.sound.sampled.AudioFormat;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public enum ClientAudio implements IResourceManagerReloadListener
{
    INSTANCE;
    public static final Object THREAD_SYNC = new Object();
    private static SoundHandler handler;
    private static SoundSystem sndSystem;
    private static MusicTicker musicTicker;

    private static final int THREAD_POOL_SIZE = 2;
    /* PCM Signed Monaural little endian */
    private static final AudioFormat audioFormat3D = new AudioFormat(48000, 16, 1, true, false);
    /* PCM Signed Stereo little endian */
    private static final AudioFormat audioFormatStereo = new AudioFormat(48000, 16, 2, true, false);
    /* Used to track which player/groups queued up music to be played by PlayID */
    private static Queue<Integer> playIDQueue01 = new ConcurrentLinkedQueue<>(); // Polled in ClientAudio#PlaySoundEvent
    private static Queue<Integer> playIDQueue02 = new ConcurrentLinkedQueue<>(); // Polled in CodecPCM
    private static Queue<Integer> playIDQueue03 = new ConcurrentLinkedQueue<>(); // Polled in ClientAudio#playStreamingSourceEvent
    private static Map<Integer, AudioData> playIDAudioData = new ConcurrentHashMap<>();

    private static ExecutorService executorService = null;
    private static ThreadFactory threadFactory = null;

    private static int counter = 0;
    private static Queue<Integer> delayedAudioDataRemovalQueue = new ConcurrentLinkedDeque<>();

    private static boolean vanillaMusicPaused = false;

    private static final int MAX_STREAM_CHANNELS = 16;
    private static final int DESIRED_STREAM_CHANNELS = 8;

    private ClientAudio() { /* NOP */ }
       
    private static void startThreadFactory()
    {
        if (threadFactory == null)
        {
            threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat("mxTune-ClientAudio-%d")
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
    
    private static synchronized void addPlayIDQueue(int playID)
    {
        if (playIDQueue01.add(playID) && playIDQueue02.add(playID)) {playIDQueue03.add(playID);}
    }
    
    private static Integer pollPlayIDQueue01()
    {
        return playIDQueue01.poll();
    }

    static Integer pollPlayIDQueue02()
    {
        return playIDQueue02.poll();
    }

    private static Integer pollPlayIDQueue03()
    {
        return playIDQueue03.poll();
    }
    
    private static Integer peekPlayIDQueue03()
    {
        return playIDQueue03.peek();
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

    private static synchronized void setISound(Integer playID, ISound iSound)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null)
            audioData.setISound(iSound);
    }

    private static BlockPos getBlockPos(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData.getBlockPos();
    }

    private static SoundRange getSoundRange(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData.getSoundRange();
    }
    
    static boolean hasPlayID(Integer playID)
    {
        return (!playIDAudioData.isEmpty() && playID != null) && playIDAudioData.containsKey(playID);
    }
    
    private static boolean isClientPlayer(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData != null && audioData.isClientPlayer();
    }

    /**
     * For players.
     * @param playID unique submission identifier.
     * @param musicText MML string
     */
    public static synchronized void play(Integer playID, String musicText)
    {
        play(playID, null, musicText, GroupHelper.isClientPlaying(playID), SoundRange.NORMAL);
    }

    /**
     * For TileEntities placed in the world.
     * @param playID unique submission identifier.
     * @param pos block position in the world
     * @param musicText MML string
     * @param soundRange defines the attenuation: NORMAL or INFINITY respectively
     */
    public static synchronized void play(Integer playID, BlockPos pos, String musicText, SoundRange soundRange)
    {
        play(playID, pos, musicText, false, soundRange);
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

    private static void play(Integer playID, BlockPos pos, String musicText, boolean isClient, SoundRange soundRange)
    {
        startThreadFactory();
        if(ClientCSDMonitor.canMXTunesPlay() && playID != null)
        {
            addPlayIDQueue(playID);
            AudioData audioData = new AudioData(playID, pos, isClient, soundRange);
            setAudioFormat(audioData);
            AudioData result = playIDAudioData.putIfAbsent(playID, audioData);
            if (result != null)
            {
                ModLogger.warn("ClientAudio#play: playID: %s has already been submitted", playID);
                return;
            }
            executorService.execute(new ThreadedPlay(audioData, musicText));
            MXTune.proxy.getMinecraft().getSoundHandler().playSound(new MovingMusic());
            stopVanillaMusic();
        } else
        {
            ModLogger.warn("ClientAudio#play(Integer playID, BlockPos pos, String musicText): playID is null!");
        }
    }

    public static void stop(Integer playID)
    {
        synchronized (SoundSystemConfig.THREAD_SYNC)
        {
            AudioData audioData = playIDAudioData.get(playID);
            if (audioData != null && sndSystem != null && audioData.getUuid() != null)
                sndSystem.fadeOut(audioData.getUuid(), null, 100);
        }
    }
    
    private static class ThreadedPlay implements Runnable
    {
        private final AudioData audioData;
        private final String musicText;

        ThreadedPlay(AudioData audioData, String musicText)
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
        ModLogger.info("ClientAudio stopVanillaMusic - PAUSED on %d active sessions.", playIDAudioData.size());
        setVanillaMusicPaused(true);
        stopVanillaMusicTicker();
        setVanillaMusicTimer(Integer.MAX_VALUE);
    }

    private static void resumeVanillaMusic()
    {
        ModLogger.info("ClientAudio resumeVanillaMusic - RESUMED");
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
                if (!GroupHelper.getActivePlayIDs().contains(entry.getKey()))
                {
                    // Stopping playing audio takes 100 milliseconds. e.g. SoundSystem fadeOut(<source>, <delay in ms>)
                    // To prevent audio clicks/pops we have the wait at least that amount of time
                    // before removing the AudioData instance for this playID.
                    // Therefore the removal is queued for 250 milliseconds.
                    // e.g. the client tick setup to trigger once every 1/4 second.
                    stop(entry.getKey());
                    queueAudioDataRemoval(entry.getKey());
                    ModLogger.info("updateClientAudio: AudioData for playID queued for removal");
                }
            }
        }
    }

    private static void removeQueuedAudioData()
    {
        while (!delayedAudioDataRemovalQueue.isEmpty())
            if (delayedAudioDataRemovalQueue.peek() != null)
                playIDAudioData.remove(Objects.requireNonNull(delayedAudioDataRemovalQueue.poll()));
    }

    private static void queueAudioDataRemoval(Integer playID)
    {
        if (playID != null)
            delayedAudioDataRemovalQueue.add(playID);
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
        }
    }
    
    private static void cleanup()   
    {
        setVanillaMusicPaused(false);
        playIDAudioData.keySet().forEach(ClientAudio::queueAudioDataRemoval);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        ModLogger.info("Restarting mxTune");
        cleanup();
        configureSound();
        init();
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
        if (event.side == Side.CLIENT && event.phase == TickEvent.Phase.END && counter++ % 5 == 0)
        {
            /* once every 1/4 second */
            updateClientAudio();
        }
    }
    
    @SubscribeEvent
    public static void event(SoundSetupEvent event) throws SoundSystemException
    {
        SoundSystemConfig.setCodec("nul", CodecPCM.class);
        ModLogger.info("Sound Setup Event: associate the \"nul\" extension with CodecPCM.");
        ModLogger.info("Sound Streaming Buffer Size: %d", SoundSystemConfig.getStreamingBufferSize());
        configureSound();
    }
    
    @SubscribeEvent
    public static void event(PlaySoundEvent e)
    {
        init();
        /* Testing for a the PCM_PROXY sound. For playing MML though the MML->PCM ClientAudio chain */
        if (e.getSound().getSoundLocation().equals(ModSoundEvents.PCM_PROXY.getSoundName()))
        {
            Integer playID = pollPlayIDQueue01();
            if (playID != null)
            {
                if (isClientPlayer(playID))
                {
                    // ** ThePlayer [this client] **
                    // hears their own music without any 3D distance effects applied.
                    e.setResultSound(new MusicClient(playID));
                }
                else if (getBlockPos(playID) == null)
                {
                    // ** The MUSIC the OTHER players are playing **
                    // Moving music source for hand held or worn instruments
                    // The Spinet Piano although a placed instrument still needs a player to sit on it is
                    // included here.
                    e.setResultSound(new MovingMusic(playID));
                }
                else
                {
                    // ** Musical Machines - Juke Boxes, Band Amp, Yet-to-be-announced stuff... **
                    e.setResultSound(new MusicPositioned(playID, getBlockPos(playID), getSoundRange(playID)));
                    ModLogger.debug("PlaySoundEvent MusicPositioned playID: %d, pos: %s, isPlayer: %s",
                                   playID, getBlockPos(playID), isClientPlayer(playID));
                }
            }
        }
    }

    /**
     * This event hook associates the internal sound UUID with the
     * mxTune sound event.
     */
    @SubscribeEvent
    public static void event(PlayStreamingSourceEvent e)
    {
        if (e.getSound().getSoundLocation().equals(ModSoundEvents.PCM_PROXY.getSoundName()) &&
                ClientAudio.peekPlayIDQueue03() != null)
        {
            Integer playID = ClientAudio.pollPlayIDQueue03();
            ClientAudio.setUuid(playID, e.getUuid());
            ClientAudio.setISound(playID, e.getSound());
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
