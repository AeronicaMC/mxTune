/**
 * Copyright {2016-2017} Paul Boese aka Aeronica
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.PlayStoppedMessage;
import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;

@SideOnly(Side.CLIENT)
public enum ClientAudio
{

    INSTANCE;
    static SoundHandler handler;
    static SoundManager sndManager;
    static SoundSystem sndSystem;
    static MusicTicker musicTicker;
    
    static final int THREAD_POOL_SIZE = 2;
    static final AudioFormat audioFormat3D;
    static final AudioFormat audioFormatStereo;
    static ConcurrentLinkedQueue<Integer> playIDQueue01;
    static ConcurrentLinkedQueue<Integer> playIDQueue02;
    static ConcurrentLinkedQueue<Integer> playIDQueue03;
    static ConcurrentHashMap<Integer, AudioData> playIDAudioData;
    
    final static ExecutorService executorService;
    final static ThreadFactory threadFactory; 
    
    static int count = 0;
    static boolean vanillaMusicPaused = false;
    
    static final int MAX_STREAM_CHANNELS = 16;
    static final int SOUND_QUEUE_SLACK = 6;
    static int normalChannelCount = 0;
    static int streamChannelCount = 0;
    
    static {
        sndSystem = null;
        /* Used to track which player/groups queued up music to be played by PlayID */
        playIDQueue01 = new ConcurrentLinkedQueue<>(); // Polled in ClientAudio#PlaySoundEvent
        playIDQueue02 = new ConcurrentLinkedQueue<>(); // Polled in CodecPCM
        playIDQueue03 = new ConcurrentLinkedQueue<>(); // Polled in ClientAudio#playStreamingSourceEvent
        /* PCM Signed Monaural little endian */
        audioFormat3D = new AudioFormat(48000, 16, 1, true, false);
        /* PCM Signed Stereo little endian */        
        audioFormatStereo = new AudioFormat(48000, 16, 2, true, false);
        playIDAudioData = new ConcurrentHashMap<>();
        
        threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("mxTune-ClientAudio-%d")
                .setDaemon(true)
                .setPriority(Thread.NORM_PRIORITY)
                .build();
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE, (java.util.concurrent.ThreadFactory) threadFactory);
    }

       
    public enum Status
    {
        WAITING, READY, ERROR;
    }
    
    public static synchronized boolean addPlayIDQueue(int playID) 
    {
        return playIDQueue01.add(playID) && playIDQueue02.add(playID) && playIDQueue03.add(playID);
    }
    
    public static Integer pollPlayIDQueue01()
    {
        return playIDQueue01.poll();
    }
    
    public static Integer peekPlayIDQueue01()
    {
        return playIDQueue01.peek();
    }

    public static Integer pollPlayIDQueue02()
    {
        return playIDQueue02.poll();
    }
    
    public static Integer peekPlayIDQueue02()
    {
        return playIDQueue02.peek();
    }

    public static Integer pollPlayIDQueue03()
    {
        return playIDQueue03.poll();
    }
    
    public static Integer peekPlayIDQueue03()
    {
        return playIDQueue03.peek();
    }
    
   public static AudioFormat getAudioFormat(Integer playID)
    {
       AudioData audioData = playIDAudioData.get(playID);
       if (audioData == null)
           return null;
       return audioData.isClientPlayer() ? audioFormatStereo : audioFormat3D;
    }
    
    public static synchronized void setPlayIDAudioStream(Integer playID, AudioInputStream audioStream)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null)
            audioData.setAudioStream(audioStream);
    }
    
    public static synchronized void setUuid(Integer playID, String uuid)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null)
            audioData.setUuid(uuid);
    }
    
    public static String getUuid(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);       
        return audioData.getUuid();
    }
    
    public static void removePlayIDAudioData(int playID)
    {
        if (playIDAudioData.containsKey(playID))
        {
            AudioData audioData = playIDAudioData.get(playID);
            if (audioData.isClientPlayer())
                notify(playID);
            playIDAudioData.remove(playID);
        }
    }
    
    public static AudioInputStream getAudioInputStream(int playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData != null ? audioData.getAudioStream() : null;
    }
    
    public static synchronized void setPlayIDAudioDataStatus(Integer playID, Status status)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null)
            audioData.setStatus(status);
    }
    
    public static boolean isPlayIDAudioDataWaiting(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData != null ? audioData.getStatus() == Status.WAITING : false;
    }
    
    public static boolean isPlayIDAudioDataError(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData != null ? audioData.getStatus() == Status.ERROR : true;
    }
    
    public static boolean isPlayIDAudioDataReady(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData != null ? audioData.getStatus() == Status.READY : false;
    }
    
    public static boolean hasPlayID(Integer playID)
    {
        return (!playIDAudioData.isEmpty() && playID != null) ? playIDAudioData.containsKey(playID) : false;
    }
    
    public static boolean isClientPlayer(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData != null ? audioData.isClientPlayer() : false;
    }

    public static synchronized void play(Integer playID, String musicText)
    {
        if(ClientCSDMonitor.canMXTunesPlay())
        {
            addPlayIDQueue(playID);
            playIDAudioData.putIfAbsent(playID, new AudioData(playID, musicText, GROUPS.isClientPlaying(playID)));        
            executorService.execute(new ThreadedPlay(playID, musicText));
            MXTuneMain.proxy.getMinecraft().getSoundHandler().playSound(new MusicMoving());
            stopVanillaMusic();
        }
    }
    
    private static void notify(Integer playID)
    {
        if (playID != null)
            PacketDispatcher.sendToServer(new PlayStoppedMessage(playID));
    }
    
    public static void stop(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null && sndSystem != null && audioData.getUuid() != null)
            sndSystem.fadeOut(audioData.getUuid(), null, 100);
    }
    
    private static class ThreadedPlay implements Runnable
    {
        private final Integer playID;
        private final String musicText;

        public ThreadedPlay(Integer entityID, String musicText)
        {
            this.playID = entityID;
            this.musicText = musicText;
        }

        @Override
        public void run()
        {
            MML2PCM p = new MML2PCM();
            p.process(playID, musicText);
        }
    }

    private static void stopVanillaMusic()
    {
        ModLogger.info("ClientAudio stopVanillaMusic - PAUSED on %d active sessions.", playIDAudioData.mappingCount());
        setVanillaMusicPaused(true);
        musicTicker.stopMusic();
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
        if (sndSystem != null && playIDAudioData != null)
        { 
            if(isVanillaMusicPaused() && playIDAudioData.mappingCount() == 0)
            {
                resumeVanillaMusic();
                setVanillaMusicPaused(false);
            }  else if (playIDAudioData.mappingCount() > 0)
            {
                // don't allow the timer to count down while ClientAudio sessions are playing
                setVanillaMusicTimer(Integer.MAX_VALUE);
            }
            // Remove inactive playIDs
            for (ConcurrentHashMap.Entry<Integer, AudioData> entry : playIDAudioData.entrySet())
            {
                if (!GROUPS.getActivePlayIDs().contains(entry.getKey()))
                {
                    stop(entry.getKey());
                    playIDAudioData.remove(entry.getKey());
                }
            }
        }
    }

    private static void init()
    {
        if (sndSystem == null || sndSystem.randomNumberGenerator == null)
        {
            handler = Minecraft.getMinecraft().getSoundHandler();
            sndManager = handler.sndManager;
            sndSystem = sndManager.sndSystem;
            musicTicker = Minecraft.getMinecraft().getMusicTicker();
            setVanillaMusicPaused(false);
        }
    }
    
    private static void cleanup()   
    {
        setVanillaMusicPaused(false);
        KeySetView<Integer, AudioData> keys = playIDAudioData.keySet();
        keys.forEach(k -> stop(k));
    }
    
    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityPlayerSP && event.getEntity().getEntityId() == MXTuneMain.proxy.getClientPlayer().getEntityId())
        {
            cleanup();
            ModLogger.info("ClientAudio EntityJoinWorldEvent: %s", event.getEntity().getName());
        }
    }

    @SubscribeEvent
    public void onPlayerRespawnEvent(PlayerRespawnEvent event)
    {
        cleanup();
        ModLogger.info("ClientAudio PlayerRespawnEvent: %s", event.player.getName());
    }

    @SubscribeEvent
    public void onEvent(ClientTickEvent event)
    {
        if (event.side == Side.CLIENT && event.phase == TickEvent.Phase.END && count % 40 == 0)
        {
            /* once every 2 seconds */
            updateClientAudio();
        }
    }
    
    @SubscribeEvent
    public void soundSetupEvent(SoundSetupEvent event) throws SoundSystemException
    {
        SoundSystemConfig.setCodec("nul", CodecPCM.class);
        ModLogger.info("Sound Setup Event %s", event);
        configureSound();
    }
    
    @SubscribeEvent
    public void PlaySoundEvent(PlaySoundEvent e)
    {
        init();
        /* Testing for a the PCM_PROXY sound. For playing MML though the MML->PCM ClientAudio chain */
        if (e.getSound().getSoundLocation().equals(ModSoundEvents.PCM_PROXY.getSoundName()))
        {
            Integer playID;
            if ((playID = ClientAudio.pollPlayIDQueue01()) != null)
            {
                if (ClientAudio.isClientPlayer(playID))
                {
                    /*
                     * ThePlayer(s) hear their own music without any 3D distance
                     * effects applied. Not using the built-in background music
                     * feature here because it's managed by vanilla and might interrupt
                     * the players music. Doing this also eliminates a pulsing effect
                     * that occurs when the player moves and 3D sound system updates
                     * the sound position.
                     */
                    e.setResultSound(new MusicBackground(playID));
                }
                else
                {
                    /*
                     * Moving music source for hand held or worn instruments. 
                     */
                    e.setResultSound(new MusicMoving(playID));
                }
            }
        }
    }

    /**
     * This event hook associates the internal sound UUID with the
     * mxTune sound event.
     */
    @SubscribeEvent
    public void PlayStreamingSourceEvent(PlayStreamingSourceEvent e)
    {
        if (e.getSound().getSoundLocation().equals(ModSoundEvents.PCM_PROXY.getSoundName())) {
            if (ClientAudio.peekPlayIDQueue03() != null)
                ClientAudio.setUuid(ClientAudio.pollPlayIDQueue03(), e.getUuid());
        }
        ModLogger.info("ClientAudio PlayStreamingSourceEvent: uuid: %s, ISound: %s", e.getUuid(), e.getSound());
    }

    /*
     * This section Poached from Dynamic Surroundings
     */
    public int currentSoundCount() {
        return sndManager.playingSounds.size();
    }

    public boolean canFitSound() {
        return currentSoundCount() < (normalChannelCount - SOUND_QUEUE_SLACK);
    }

    private static void configureSound() {
        int totalChannels = -1;

        try {
            final boolean create = !AL.isCreated();
            if (create)
                AL.create();
            final IntBuffer ib = BufferUtils.createIntBuffer(1);
            ALC10.alcGetInteger(AL.getDevice(), ALC11.ALC_MONO_SOURCES, ib);
            totalChannels = ib.get(0);
            if (create)
                AL.destroy();
        } catch (final Throwable e) {
            ModLogger.error(e);
        }

        normalChannelCount = ModConfig.getNormalSoundChannelCount();
        streamChannelCount = ModConfig.getStreamingSoundChannelCount();

        if (ModConfig.getAutoConfigureChannels() && totalChannels > 64) {
            totalChannels = ((totalChannels + 1) * 3) / 4;
            streamChannelCount = Math.min(totalChannels / 5, MAX_STREAM_CHANNELS);
            normalChannelCount = totalChannels - streamChannelCount;
        }

        ModLogger.info("Sound channels: %d normal, %d streaming (total avail: %s)", normalChannelCount, streamChannelCount,
                totalChannels == -1 ? "UNKNOWN" : Integer.toString(totalChannels));
        SoundSystemConfig.setNumberNormalChannels(normalChannelCount);
        SoundSystemConfig.setNumberStreamingChannels(streamChannelCount);
    }

}
