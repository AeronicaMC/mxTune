/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
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
package net.aeronica.mods.mxtune.network.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.options.IPlayerMusicOptions;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.options.PlayerLists;
import net.aeronica.mods.mxtune.options.PlayerMusicDefImpl;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 
 * A packet to send ALL data stored in your extended properties to the client.
 * This is handy if you only need to send your data once per game session or all
 * of your data needs to be synchronized together; it's also handy while first
 * starting, since you only need one packet for everything - however, you should
 * NOT use such a packet in your final product!!!
 * 
 * Each packet should handle one thing and one thing only, in order to minimize
 * network traffic as much as possible. There is no point sending 20+ fields'
 * worth of data when you just need the current mana amount; conversely, it's
 * foolish to send 20 packets for all the data when the player first loads, when
 * you could send it all in one single packet.
 * 
 * TL;DR - make separate packets for each piece of data, and one big packet for
 * those times when you need to send everything.
 *
 */
public class SyncPlayerMusicOptionsMessage extends AbstractClientMessage<SyncPlayerMusicOptionsMessage>
{
    private byte propertyID;
    private NBTTagCompound data;
    private float midiVolume;
    private int muteOption;
    private String sParam1, sParam2, sParam3;
    private ArrayList<PlayerLists> blackList, whiteList;

    private byte[] byteBuffer = null;
    
    // The basic, no-argument constructor MUST be included to use the new
    // automated handling
    public SyncPlayerMusicOptionsMessage() {}

    // We need to initialize our data, so provide a suitable constructor:
    public SyncPlayerMusicOptionsMessage(IPlayerMusicOptions inst, byte propertyID)
    {
        this.propertyID = propertyID;
        switch (propertyID)
        {
        case PlayerMusicDefImpl.SYNC_ALL:
            /** create a new tag compound */
            this.data = new NBTTagCompound();
            /** and save our player's data into it */
            this.data = (NBTTagCompound) MusicOptionsUtil.MUSIC_OPTIONS.writeNBT(inst, null);
            break;

        case PlayerMusicDefImpl.SYNC_MIDI_VOLUME:
            this.midiVolume = inst.getMidiVolume();
            break;
            
        case PlayerMusicDefImpl.SYNC_MUTE_OPTION:
            this.muteOption = inst.getMuteOption();
            break;
            
        case PlayerMusicDefImpl.SYNC_SPARAMS:
            this.sParam1 = inst.getSParam1();
            this.sParam2 = inst.getSParam2();
            this.sParam3 = inst.getSParam3();
            break;

        case PlayerMusicDefImpl.SYNC_WHITE_LIST:
            this.whiteList = inst.getWhiteList();
            break;
            
        case PlayerMusicDefImpl.SYNC_BLACK_LIST:
            this.blackList = inst.getBlackList();
            break;

        default:
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        propertyID = buffer.readByte();
        switch (propertyID)
        {
        case PlayerMusicDefImpl.SYNC_ALL:
            this.data = buffer.readNBTTagCompoundFromBuffer();
            break;
        case PlayerMusicDefImpl.SYNC_MIDI_VOLUME:
            this.midiVolume = buffer.readFloat();
            break;
        case PlayerMusicDefImpl.SYNC_MUTE_OPTION:
           this. muteOption = buffer.readInt();
            break;
        case PlayerMusicDefImpl.SYNC_SPARAMS:
            this.sParam1 = ByteBufUtils.readUTF8String(buffer);
            this.sParam2 = ByteBufUtils.readUTF8String(buffer);
            this.sParam3 = ByteBufUtils.readUTF8String(buffer);
            break;
        case PlayerMusicDefImpl.SYNC_WHITE_LIST:
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
            break;            
        case PlayerMusicDefImpl.SYNC_BLACK_LIST:
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
            break;
        default:        
        }
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        buffer.writeByte(this.propertyID);
        switch (this.propertyID)
        {
        case PlayerMusicDefImpl.SYNC_ALL:
            buffer.writeNBTTagCompoundToBuffer(this.data);
            break;
        case PlayerMusicDefImpl.SYNC_MIDI_VOLUME:
            buffer.writeFloat(this.midiVolume);
            break;
        case PlayerMusicDefImpl.SYNC_MUTE_OPTION:
            buffer.writeInt(this.muteOption);
            break;
        case PlayerMusicDefImpl.SYNC_SPARAMS:
            ByteBufUtils.writeUTF8String(buffer, this.sParam1);
            ByteBufUtils.writeUTF8String(buffer, this.sParam2);
            ByteBufUtils.writeUTF8String(buffer, this.sParam3);
            break;
        case PlayerMusicDefImpl.SYNC_WHITE_LIST:
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
            break;            
        case PlayerMusicDefImpl.SYNC_BLACK_LIST:
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
            break;
        default:
        }
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (side.isClient())
        {
            ModLogger.logInfo("Synchronizing Music Options player extended properties data on CLIENT");
            final IPlayerMusicOptions inst = player.getCapability(MusicOptionsUtil.MUSIC_OPTIONS, null);
            switch (this.propertyID)
            {
            case PlayerMusicDefImpl.SYNC_ALL:
                MusicOptionsUtil.MUSIC_OPTIONS.readNBT(inst, null, this.data);
                break;
            case PlayerMusicDefImpl.SYNC_MIDI_VOLUME:
                inst.setMidiVolume(this.midiVolume);
                break;
            case PlayerMusicDefImpl.SYNC_MUTE_OPTION:
                inst.setMuteOption(this.muteOption);
                break;
            case PlayerMusicDefImpl.SYNC_SPARAMS:
                inst.setSParams(this.sParam1, this.sParam2, this.sParam3);
                break;
            case PlayerMusicDefImpl.SYNC_WHITE_LIST:
                inst.setWhiteList(this.whiteList);
                break;                
            case PlayerMusicDefImpl.SYNC_BLACK_LIST:
                inst.setBlackList(this.blackList);
                break;
            default:
            }
        }
    }
}
