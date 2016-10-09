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

import javax.sound.sampled.AudioInputStream;

import net.aeronica.mods.mxtune.sound.ClientAudio.Status;

public class AudioData
{
    private final Integer entityID;
    private final String mml;
    private AudioInputStream audioStream;
    private Status status;
    
    public AudioData(Integer entityID, String mml)
    {
        this.entityID = entityID;
        this.mml = mml;
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

    public Integer getEntityID()
    {
        return entityID;
    }

    public String getMml()
    {
        return mml;
    }

    public AudioInputStream getAudioStream()
    {
        return audioStream;
    }

    public void setAudioStream(AudioInputStream audioStream)
    {
        this.audioStream = audioStream;
    }
    
}
