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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.options.PlayerLists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class MusicOptionsMessage extends AbstractServerMessage<MusicOptionsMessage>
{
    private float midiVolume;
    private int muteOption;
    private ArrayList<PlayerLists> blackList, whiteList;
    private byte[] byteBuffer = null;
    
    public MusicOptionsMessage() {}
    
    public MusicOptionsMessage(float midiVolume, int muteOption, ArrayList<PlayerLists> blackList, ArrayList<PlayerLists> whiteList)
    {
        this.midiVolume = midiVolume;
        this.muteOption = muteOption;
        this.blackList = blackList;
        this.whiteList = whiteList;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        this.midiVolume = buffer.readFloat();
        this.muteOption = buffer.readInt();
        try{
            // Deserialize data object from a byte array
            byteBuffer = buffer.readByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer) ;
            ObjectInputStream in = new ObjectInputStream(bis) ;
            whiteList =  (ArrayList<PlayerLists>) in.readObject();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        try{
            // Deserialize data object from a byte array
            byteBuffer = buffer.readByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer) ;
            ObjectInputStream in = new ObjectInputStream(bis) ;
            blackList =  (ArrayList<PlayerLists>) in.readObject();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        buffer.writeFloat(this.midiVolume);
        buffer.writeInt(this.muteOption);
        try{
            // Serialize data object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ObjectOutputStream out = new ObjectOutputStream(bos) ;
            out.writeObject(whiteList);
            out.close();

            // Get the bytes of the serialized object
            byteBuffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        buffer.writeByteArray(byteBuffer);
        try{
            // Serialize data object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ObjectOutputStream out = new ObjectOutputStream(bos) ;
            out.writeObject(blackList);
            out.close();

            // Get the bytes of the serialized object
            byteBuffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        buffer.writeByteArray(byteBuffer);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        MusicOptionsUtil.setMidiVolume(player, this.midiVolume);
        MusicOptionsUtil.setMuteOption(player, this.muteOption);
        MusicOptionsUtil.setBlackList(player, blackList);
        MusicOptionsUtil.setWhiteList(player, whiteList);
    }
}
