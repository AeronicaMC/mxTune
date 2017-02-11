/**
 * Copyright {2016} Paul Boese aka Aeronica
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
package net.aeronica.mods.mxtune.sound;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.StopPlayMessage;
import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.IStreamListener;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;

@SideOnly(Side.CLIENT)
public enum ClientAudio implements IStreamListener
{

    INSTANCE;
    private static final int THREAD_POOL_SIZE = 10;
    private static final AudioFormat audioFormat3D, audioFormatStereo;
    private static ConcurrentLinkedQueue<Integer> playIDQueue01;
    private static ConcurrentLinkedQueue<Integer> playIDQueue02;
    private static Map<Integer, AudioData> playIDAudioData;
    
    private final static ThreadFactory threadFactory; 
    private final static ExecutorService executorService;
    
    private ClientAudio() {}
    
    static {
        /* Used to track which player/groups queued up music to be played by PlayID */
        playIDQueue01 = new ConcurrentLinkedQueue<Integer>(); // Polled in SoundEventHandler#PlaySoundEvent
        playIDQueue02 = new ConcurrentLinkedQueue<Integer>(); // Polled in CodecPCM
        /* PCM Signed Monaural little endian */
        audioFormat3D = new AudioFormat(48000, 16, 1, true, false);
        /* PCM Signed Stereo little endian */        
        audioFormatStereo = new AudioFormat(48000, 16, 2, true, false);
        playIDAudioData = new HashMap<Integer, AudioData>();
        
        threadFactory = (ThreadFactory) new ThreadFactoryBuilder()
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
    
    public static boolean addPlayIDQueue(int playID) 
    {
        return playIDQueue01.add(playID) && playIDQueue02.add(playID);
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

   public static AudioFormat getAudioFormat(Integer playID)
    {
       AudioData audioData = playIDAudioData.get(playID);       
       return audioData.isClientPlayer() ? audioFormatStereo : audioFormat3D;
    }
    
    public synchronized static void setPlayIDAudioStream(int playID, AudioInputStream audioStream)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null) audioData.setAudioStream(audioStream);
    }
    
    public static void removeEntityAudioData(int playID)
    {
        if ((playIDAudioData.isEmpty() == false) && playIDAudioData.containsKey(playID))
        {
            AudioData audioData = playIDAudioData.get(playID);
            if (audioData.isClientPlayer()) stop(playID);
            playIDAudioData.remove(playID);
        }
        inInit = false;
    }
    
    public synchronized static AudioInputStream getAudioInputStream(int playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        return (audioData != null) ? audioData.getAudioStream() : null;
    }
    
    public static void setPlayIDAudioDataStatus(Integer playID, Status status)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData != null) audioData.setStatus(status);
    }
    
    public static boolean isPlayIDAudioDataWaiting(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData == null) return false;
        return audioData.getStatus() == Status.WAITING;
    }
    
    public static boolean isPlayIDAudioDataError(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData == null) return true;
        return audioData.getStatus() == Status.ERROR;
    }
    
    public static boolean isPlayIDAudioDataReady(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if (audioData == null) return false;
        return audioData.getStatus() == Status.READY;
    }
    
    public static boolean hasPlayID(Integer playID)
    {
        if(playIDAudioData.isEmpty()) return false;
        return playIDAudioData.containsKey(playID);
    }

    public static boolean isPlaying(Integer playID)
    {
        if (hasPlayID(playID))
        {            
            return (inInit==true) ? true : GROUPS.isPlayIDPlaying(playID);
        }
        return false;
    }
    
    public static boolean isClientPlayer(Integer playID)
    {
        AudioData audioData = playIDAudioData.get(playID);
        if(audioData == null) return false;
        return audioData.isClientPlayer();
    }

    private static final String INIT_MML = "PPP=MML@i69t240v0rcegrceg,i72v0reg>c<reg>c,i73v0rg>ce<rg>ce;";
    private static boolean inInit = false;
    /**
     * A cheap and dirty safe initialization that just exercises
     * the audio chain silently. 
     * @param playerIn
     */
    public static void init(EntityPlayer playerIn)
    {
        if(ClientCSDMonitor.canMXTunesPlay())
        {
            Integer playID = 9999;
            inInit = true;
            Integer entityID = playerIn.getEntityId();
            String musicText = new String(INIT_MML).replaceAll("PPP", entityID.toString());
            addPlayIDQueue(playID);
            playIDAudioData.put(playID, new AudioData(playID, musicText, true));        
            executorService.execute(new ThreadedPlay(playID, musicText));
            MXTuneMain.proxy.getMinecraft().getSoundHandler().playSound(new MusicMoving());
        }
    }
 
    public static void play(Integer playID, String musicText)
    {
        if(ClientCSDMonitor.canMXTunesPlay())
        {
            addPlayIDQueue(playID);
            playIDAudioData.put(playID, new AudioData(playID, musicText, GROUPS.isClientPlaying(playID)));        
            executorService.execute(new ThreadedPlay(playID, musicText));
            MXTuneMain.proxy.getMinecraft().getSoundHandler().playSound(new MusicMoving());
        }
    }
    
    private static void stop(Integer playID)
    {
        PacketDispatcher.sendToServer(new StopPlayMessage(playID));
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
    
    @SubscribeEvent
    public void SoundSetupEvent(SoundSetupEvent event) throws SoundSystemException
    {
        SoundSystemConfig.setCodec("nul", CodecPCM.class);
        SoundSystemConfig.setNumberStreamingChannels(8);
        SoundSystemConfig.setNumberNormalChannels(24);
        SoundSystemConfig.setNumberStreamingBuffers(4);
        SoundSystemConfig.addStreamListener(INSTANCE); 
    }

    @SubscribeEvent
    public void PlaySoundEvent(PlaySoundEvent e)
    {
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

    @Override
    public void endOfStream(String sourcename, int queueSize)
    {
        ModLogger.info("ClientAudio endOfStream Source:     " + sourcename);
        ModLogger.info("ClientAudio endOfStream Queue Size: " + queueSize);          
    }
 
}
