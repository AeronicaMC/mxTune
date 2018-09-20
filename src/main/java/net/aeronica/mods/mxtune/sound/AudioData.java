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

import net.aeronica.mods.mxtune.sound.ClientAudio.Status;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.math.BlockPos;

import javax.sound.sampled.AudioInputStream;

public class AudioData
{
    private final Integer playID;
    private final BlockPos blockPos;
    private final boolean isClientPlayer;
    private AudioInputStream audioStream;
    private String uuid;
    private ISound iSound;
    private Status status;
    
    public AudioData(Integer entityID, BlockPos blockPos, boolean isClientPlayer)
    {
        this.playID = entityID;
        this.blockPos = blockPos;
        this.isClientPlayer = isClientPlayer;
        this.status = Status.WAITING;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public Integer getPlayID()
    {
        return playID;
    }
    
    public BlockPos getBlockPos()
    {
        return blockPos;
    }
    
    public boolean isClientPlayer()
    {
        return isClientPlayer;
    }
    
    public AudioInputStream getAudioStream()
    {
        return audioStream;
    }

    public void setAudioStream(AudioInputStream audioStream)
    {
        this.audioStream = audioStream;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public ISound getiSound()
    {
        return iSound;
    }

    public void setiSound(ISound iSound)
    {
        this.iSound = iSound;
    }
}
