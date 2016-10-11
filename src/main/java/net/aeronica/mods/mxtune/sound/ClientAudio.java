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

import net.minecraft.util.math.BlockPos;


public class ClientAudio
{

    private static AudioFormat audioFormat;
    private static ConcurrentLinkedQueue<Integer> entityIDQueue01;
    private static ConcurrentLinkedQueue<Integer> entityIDQueue02;
    private static Map<Integer, AudioData> entityData;
    
    private final static ThreadFactory threadFactory; 
    private final static ExecutorService executorService; 
    private static  ClientAudio instance;

    private ClientAudio(){instance = new ClientAudio();}
    
    public static ClientAudio getInstance()
    {
        return instance;
    }
    
    static {
        /* Used to track which player/entity want to queue up music to be played */
        entityIDQueue01 = new ConcurrentLinkedQueue<Integer>();
        entityIDQueue02 = new ConcurrentLinkedQueue<Integer>();
        audioFormat = new AudioFormat(48000, 16, 1, true, false);
        entityData = new HashMap<Integer, AudioData>();
        
        threadFactory = (ThreadFactory) new ThreadFactoryBuilder()
                .setNameFormat("mxTune-ClientAudio-%d")
                .setDaemon(true)
                .build();
        executorService = Executors.newFixedThreadPool(10, (java.util.concurrent.ThreadFactory) threadFactory);
    }

       
    public enum Status
    {
        WAITING, READY, ERROR;
    }
    
    public static boolean addEntityIdQueue(int entityId) 
    {
        return entityIDQueue01.add(entityId) && entityIDQueue02.add(entityId);
    }
    
    public static int pollEntityIDQueue01()
    {
        return entityIDQueue01.poll();
    }
    
    public static int peekEntityIDQueue01()
    {
        return entityIDQueue01.peek();
    }

    public static int pollEntityIdQueue02()
    {
        return entityIDQueue02.poll();
    }
    
    public static int peekEntityIdQueue02()
    {
        return entityIDQueue02.peek();
    }

   public static AudioFormat getAudioFormat()
    {
        return audioFormat;
    }
    
    public static void setEntityAudioStream(int entityID, AudioInputStream audioStream)
    {
        AudioData audioData = entityData.get(entityID);
        audioData.setAudioStream(audioStream);
    }
    
    public static void removeEntityAudioData(int entityID)
    {
        if ((entityData.isEmpty() == false) && entityData.containsKey(entityID))
            entityData.remove(entityID);
    }
    
    public static AudioInputStream getAudioInputStream(int entityID)
    {
        AudioData audioData = entityData.get(entityID);
        return audioData.getAudioStream();
    }
    
    public static void setEntityAudioDataStatus(Integer entityID, Status status)
    {
        AudioData audioData = entityData.get(entityID);
        audioData.setStatus(status);
    }
    
    public static boolean isEntityAudioDataWaiting(Integer entityID)
    {
        AudioData audioData = entityData.get(entityID);
        return audioData.getStatus() == Status.WAITING;
    }
    
    public static boolean isEntityAudioDataError(Integer entityID)
    {
        AudioData audioData = entityData.get(entityID);
        return audioData.getStatus() == Status.ERROR;
    }
    
    public static boolean isEntityAudioDataReady(Integer entityID)
    {
        AudioData audioData = entityData.get(entityID);
        return audioData.getStatus() == Status.READY;
    }
    
    public static boolean hasEntity(Integer entityID)
    {
        return entityData.containsKey(entityID);
    }
    
    public static boolean isPlaying(Integer entityID)
    {
        if (hasEntity(entityID))
        {
            AudioData audioData = entityData.get(entityID);
            return PlayStatusUtil.isPlaying(audioData.getPlayer());
        }
        return false;
    }
    
    public static void play(Integer entityID, String musicText, BlockPos pos, boolean isPlaced)
    {
        addEntityIdQueue(entityID);
        entityData.put(entityID, new AudioData(entityID, musicText));        
        executorService.execute(new ThreadedPlay(entityID, musicText, pos, isPlaced));
    }
    
    private static class ThreadedPlay implements Runnable
    {
        private final Integer entityID;
        private final String musicText;
        private final BlockPos pos;
        private boolean isPlaced;

        public ThreadedPlay(Integer entityID, String musicText, BlockPos pos, boolean isPlaced)
        {
            this.entityID = entityID;
            this.musicText = musicText;
            this.pos = pos;
            this.isPlaced = isPlaced;
        }

        @Override
        public void run()
        {
            MML2PCM p = new MML2PCM();
            p.process(entityID, musicText);
        }
    }
}
