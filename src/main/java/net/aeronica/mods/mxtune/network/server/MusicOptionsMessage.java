/**
 * Aeronica's mxTune MOD
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
package net.aeronica.mods.mxtune.network.server;

import java.io.IOException;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.capabilities.IPlayerMusicOptions;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class MusicOptionsMessage extends AbstractServerMessage<MusicOptionsMessage>
{
    private float midiVolume;
    private int muteOption;
    
    public MusicOptionsMessage() {}
    
    public MusicOptionsMessage(float midiVolume, int muteOption)
    {
        this.midiVolume = midiVolume;
        this.muteOption = muteOption;
    }
    
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        this.midiVolume = buffer.readFloat();
        this.muteOption = buffer.readInt();
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        buffer.writeFloat(this.midiVolume);
        buffer.writeInt(this.muteOption);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        IPlayerMusicOptions props = player.getCapability(MXTuneMain.MUSIC_OPTIONS, null);
        props.setMidiVolume(player, this.midiVolume);
        props.setMuteOption(player, this.muteOption);
    }
}
