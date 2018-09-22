/*
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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.PlayStoppedMessage;
import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.entity.EntityPlayerSP;
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
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientAudio
{
    private static SoundHandler handler;
    private static SoundSystem sndSystem;
    private static MusicTicker musicTicker;

    private static final int THREAD_POOL_SIZE = 4;
    private static final AudioFormat audioFormat3D;
    private static final AudioFormat audioFormatStereo;
    private static ConcurrentLinkedQueue<Integer> playIDQueue01;
    private static ConcurrentLinkedQueue<Integer> playIDQueue02;
    private static ConcurrentLinkedQueue<Integer> playIDQueue03;
    private static ConcurrentHashMap<Integer, AudioData> playIDAudioData;

    private static ExecutorService executorService = null;
    private static ThreadFactory threadFactory = null;

    private static int counter = 0;
    private static boolean vanillaMusicPaused = false;

    private static final int MAX_STREAM_CHANNELS = 16;
    private static final int DESIRED_STREAM_CHANNELS = 8;

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
    }

    private ClientAudio() { /* Nothing to do */ }
       
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
    
    public static Integer peekPlayIDQueue01()
    {
        return playIDQueue01.peek();
    }

    static Integer pollPlayIDQueue02()
    {
        return playIDQueue02.poll();
    }
    
    public static Integer peekPlayIDQueue02()
    {
        return playIDQueue02.peek();
    }

    private static Integer pollPlayIDQueue03()
    {
        return playIDQueue03.poll();
    }
    
    private static Integer peekPlayIDQueue03()
    {
        return playIDQueue03.peek();
    }
    
    static AudioFormat getAudioFormat(Integer playID)
    {
       AudioData audioData = playIDAudioData.get(playID);
       if (audioData == null)
           return null;
       return audioData.isClientPlayer() ? audioFormatStereo : audioFormat3D;
    }
    
    static synchronized void setPlayIDAudioStream(Integer playID, AudioInputStream audioStream)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null)
            audioData.setAudioStream(audioStream);
    }
    
    private static synchronized void setUuid(Integer playID, String uuid)
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

    private static synchronized void setiSound(Integer playID, ISound iSound)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null)
            audioData.setiSound(iSound);
    }

    public static ISound getiSound(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData.getiSound();
    }

    public static BlockPos getBlockPos(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData.getBlockPos();
    }

    static void removePlayIDAudioData(Integer playID)
    {
        if (playIDAudioData.containsKey(playID))
        {
            AudioData audioData = playIDAudioData.get(playID);
            if (audioData != null)
            {
                if (audioData.isClientPlayer())
                    notify(playID);
                else if (!audioData.isClientPlayer() && audioData.getBlockPos() != null)
                    notify(playID, true);
            }

            try
            {
                audioData.getAudioStream().close();
            } catch (IOException e)
            {
                ModLogger.error(e);
            }

            playIDAudioData.remove(playID);
        }
    }
    
    static AudioInputStream getAudioInputStream(int playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData != null ? audioData.getAudioStream() : null;
    }
    
    static synchronized void setPlayIDAudioDataStatus(Integer playID, Status status)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null)
            audioData.setStatus(status);
    }
    
    public static boolean isPlayIDAudioDataWaiting(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData != null && audioData.getStatus() == Status.WAITING;
    }
    
    static boolean isPlayIDAudioDataError(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData == null || audioData.getStatus() == Status.ERROR;
    }
    
    static boolean isPlayIDAudioDataReady(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return audioData != null && audioData.getStatus() == Status.READY;
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

    public static synchronized void play(Integer playID, String musicText)
    {
        startThreadFactory();
        if(ClientCSDMonitor.canMXTunesPlay())
        {
            addPlayIDQueue(playID);
            playIDAudioData.putIfAbsent(playID, new AudioData(playID, null, GROUPS.isClientPlaying(playID)));
            executorService.execute(new ThreadedPlay(playID, musicText));
            MXTuneMain.proxy.getMinecraft().getSoundHandler().playSound(new MusicMoving());
            stopVanillaMusic();
        }
    }

    public static synchronized void play(Integer playID, BlockPos pos, String musicText)
    {
        startThreadFactory();
        if(ClientCSDMonitor.canMXTunesPlay())
        {
            addPlayIDQueue(playID);
            playIDAudioData.putIfAbsent(playID, new AudioData(playID, pos, false));
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

    private static void notify(Integer playID, boolean isBlockEntity)
    {
        if (playID != null)
            PacketDispatcher.sendToServer(new PlayStoppedMessage(playID, isBlockEntity));
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
        private final Integer playID;
        private final String musicText;

        ThreadedPlay(Integer entityID, String musicText)
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

    // Copied from vanilla 1.11.2 MusicTicker class
    private static void stopMusic()
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
        ModLogger.info("ClientAudio stopVanillaMusic - PAUSED on %d active sessions.", playIDAudioData.mappingCount());
        setVanillaMusicPaused(true);
        stopMusic();
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
            for (ConcurrentHashMap.Entry<Integer, AudioData> entry : playIDAudioData.entrySet())
            {
                AudioData audioData = entry.getValue();
                if ((audioData.getStatus().equals(Status.ERROR) || audioData.getStatus().equals(Status.DONE)) &&
                        !handler.isSoundPlaying(audioData.getiSound()))
                {
                    playIDAudioData.remove(entry.getKey());
                    if (audioData.isClientPlayer())
                        notify(entry.getValue().getPlayID());
                    else if (!audioData.isClientPlayer() && audioData.getBlockPos() != null)
                        notify(entry.getValue().getPlayID(), true);

                    ModLogger.info("updateClientAudio: Status:Error or Done!");
                }
            }
            if(isVanillaMusicPaused() && playIDAudioData.mappingCount() == 0)
            {
                resumeVanillaMusic();
                setVanillaMusicPaused(false);
            }  else if (playIDAudioData.mappingCount() > 0)
            {
                // don't allow the timer to counter down while ClientAudio sessions are playing
                setVanillaMusicTimer(Integer.MAX_VALUE);
            }
            // Remove inactive playIDs
            for (ConcurrentHashMap.Entry<Integer, AudioData> entry : playIDAudioData.entrySet())
            {
                if (!GROUPS.getActivePlayIDs().contains(entry.getKey()))
                {
                    stop(entry.getKey());
                    playIDAudioData.remove(entry.getKey());
                    ModLogger.info("updateClientAudio: Active playID removed");
                }
            }
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
        }
    }
    
    private static void cleanup()   
    {
        setVanillaMusicPaused(false);
        KeySetView<Integer, AudioData> keys = playIDAudioData.keySet();
        keys.forEach(ClientAudio::stop);
    }
    
    @SubscribeEvent
    public static void onJoinWorld(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityPlayerSP && event.getEntity().getEntityId() == MXTuneMain.proxy.getClientPlayer().getEntityId())
        {
            cleanup();
            ModLogger.info("ClientAudio EntityJoinWorldEvent: %s", event.getEntity().getName());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawnEvent(PlayerRespawnEvent event)
    {
        cleanup();
        ModLogger.info("ClientAudio PlayerRespawnEvent: %s", event.player.getName());
    }

    @SubscribeEvent
    public static void onEvent(ClientTickEvent event)
    {
        if (event.side == Side.CLIENT && event.phase == TickEvent.Phase.END && counter++ % 5 == 0)
        {
            /* once every 1/4 second */
            updateClientAudio();
        }
    }
    
    @SubscribeEvent
    public static void soundSetupEvent(SoundSetupEvent event) throws SoundSystemException
    {
        SoundSystemConfig.setCodec("nul", CodecPCM.class);
        ModLogger.info("Sound Setup Event %s", event);
        configureSound();
    }
    
    @SubscribeEvent
    public static void PlaySoundEvent(PlaySoundEvent e)
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
                else if (ClientAudio.getBlockPos(playID) == null)
                {
                    /*
                     * Moving music source for hand held or worn instruments. 
                     */
                    e.setResultSound(new MusicMoving(playID));
                }
                else
                {
                    e.setResultSound(new MusicPositioned(playID, ClientAudio.getBlockPos(playID)));
                    ModLogger.info("PlaySoundEvent MusicPositioned playID: %d, pos: %s, isPlayer: %s",
                                   playID, ClientAudio.getBlockPos(playID), ClientAudio.isClientPlayer(playID));
                }
            }
        }
    }

    /**
     * This event hook associates the internal sound UUID with the
     * mxTune sound event.
     */
    @SubscribeEvent
    public static void PlayStreamingSourceEvent(PlayStreamingSourceEvent e)
    {
        if (e.getSound().getSoundLocation().equals(ModSoundEvents.PCM_PROXY.getSoundName())) {
            if (ClientAudio.peekPlayIDQueue03() != null)
            {
                Integer playID = ClientAudio.pollPlayIDQueue03();
                ClientAudio.setUuid(playID, e.getUuid());
                ClientAudio.setiSound(playID, e.getSound());
                ModLogger.info("ClientAudio PlayStreamingSourceEvent: uuid: %s, ISound: %s", e.getUuid(), e.getSound());
            }
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
