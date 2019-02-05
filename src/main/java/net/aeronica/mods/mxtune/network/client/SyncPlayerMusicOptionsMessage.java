/*
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

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.options.ClassifiedPlayer;
import net.aeronica.mods.mxtune.options.IPlayerMusicOptions;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class SyncPlayerMusicOptionsMessage extends AbstractClientMessage<SyncPlayerMusicOptionsMessage>
{
    @CapabilityInject(IPlayerMusicOptions.class)
    private static final Capability<IPlayerMusicOptions> MUSIC_OPTIONS = Util.nonNullInjected();

    private byte propertyID;
    private NBTTagCompound data;
    private boolean disableHud;
    private int positionHud;
    private float sizeHud;
    private int muteOption;
    private String sParam1;
    private String sParam2;
    private String sParam3;
    private List<ClassifiedPlayer> blackList;
    private List<ClassifiedPlayer> whiteList;

    private byte[] byteBuffer = null;

    @SuppressWarnings("unused")
    public SyncPlayerMusicOptionsMessage() {/* Required by the PacketDispatcher */}

    public SyncPlayerMusicOptionsMessage(IPlayerMusicOptions inst, byte propertyID)
    {
        this.propertyID = propertyID;
        switch (propertyID)
        {
        case MusicOptionsUtil.SYNC_ALL:
            this.data = new NBTTagCompound();
            this.data = (NBTTagCompound) MUSIC_OPTIONS.writeNBT(inst, null);
            break;

        case MusicOptionsUtil.SYNC_DISPLAY_HUD:
            this.disableHud = inst.isHudDisabled();
            this.positionHud = inst.getPositionHud();
            this.sizeHud = inst.getSizeHud();
            break;
            
        case MusicOptionsUtil.SYNC_MUTE_OPTION:
            this.muteOption = inst.getMuteOption();
            break;
            
        case MusicOptionsUtil.SYNC_S_PARAMS:
            this.sParam1 = inst.getSParam1();
            this.sParam2 = inst.getSParam2();
            this.sParam3 = inst.getSParam3();
            break;

        case MusicOptionsUtil.SYNC_WHITE_LIST:
            this.whiteList = inst.getWhiteList();
            break;
            
        case MusicOptionsUtil.SYNC_BLACK_LIST:
            this.blackList = inst.getBlackList();
            break;

        default:
        }
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        propertyID = buffer.readByte();
        switch (propertyID)
        {
        case MusicOptionsUtil.SYNC_ALL:
            this.data = buffer.readCompoundTag();
            break;
        case MusicOptionsUtil.SYNC_DISPLAY_HUD:
            this.disableHud = buffer.readBoolean();
            this.positionHud = buffer.readInt();
            this.sizeHud = buffer.readFloat();
            break;
        case MusicOptionsUtil.SYNC_MUTE_OPTION:
           this. muteOption = buffer.readInt();
            break;
        case MusicOptionsUtil.SYNC_S_PARAMS:
            this.sParam1 = ByteBufUtils.readUTF8String(buffer);
            this.sParam2 = ByteBufUtils.readUTF8String(buffer);
            this.sParam3 = ByteBufUtils.readUTF8String(buffer);
            break;
        case MusicOptionsUtil.SYNC_WHITE_LIST:
            this.whiteList = readPlayerList(buffer);
            break;            
        case MusicOptionsUtil.SYNC_BLACK_LIST:
            this.blackList = readPlayerList(buffer);
            break;
        default:        
        }
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeByte(this.propertyID);
        switch (this.propertyID)
        {
        case MusicOptionsUtil.SYNC_ALL:
            buffer.writeCompoundTag(this.data);
            break;
        case MusicOptionsUtil.SYNC_DISPLAY_HUD:
            buffer.writeBoolean(this.disableHud);
            buffer.writeInt(this.positionHud);
            buffer.writeFloat(this.sizeHud);
            break;
        case MusicOptionsUtil.SYNC_MUTE_OPTION:
            buffer.writeInt(this.muteOption);
            break;
        case MusicOptionsUtil.SYNC_S_PARAMS:
            ByteBufUtils.writeUTF8String(buffer, this.sParam1);
            ByteBufUtils.writeUTF8String(buffer, this.sParam2);
            ByteBufUtils.writeUTF8String(buffer, this.sParam3);
            break;
        case MusicOptionsUtil.SYNC_WHITE_LIST:
            writePlayerList(buffer, this.whiteList);
            break;            
        case MusicOptionsUtil.SYNC_BLACK_LIST:
            writePlayerList(buffer, this.blackList);
            break;
        default:
        }
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (player.hasCapability(MUSIC_OPTIONS, null))
        {
            final IPlayerMusicOptions instance = player.getCapability(MUSIC_OPTIONS, null);
            if (instance != null)
                switch (this.propertyID)
                {
                    case MusicOptionsUtil.SYNC_ALL:
                        MUSIC_OPTIONS.readNBT(instance, null, this.data);
                        break;
                    case MusicOptionsUtil.SYNC_DISPLAY_HUD:
                        instance.setHudOptions(disableHud, positionHud, sizeHud);
                        break;
                    case MusicOptionsUtil.SYNC_MUTE_OPTION:
                        instance.setMuteOption(this.muteOption);
                        break;
                    case MusicOptionsUtil.SYNC_S_PARAMS:
                        instance.setSParams(this.sParam1, this.sParam2, this.sParam3);
                        break;
                    case MusicOptionsUtil.SYNC_WHITE_LIST:
                        instance.setWhiteList(this.whiteList);
                        break;
                    case MusicOptionsUtil.SYNC_BLACK_LIST:
                        instance.setBlackList(this.blackList);
                        break;
                    default:
                }
        }
    }

    @SuppressWarnings("unchecked")
    private List<ClassifiedPlayer> readPlayerList(PacketBuffer buffer)
    {
        List<ClassifiedPlayer> playerList = Collections.emptyList();
        try{
            // Deserialize data object from a byte array
            byteBuffer = buffer.readByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer) ;
            ObjectInputStream in = new ObjectInputStream(bis) ;
            playerList = (List<ClassifiedPlayer>) in.readObject();
            in.close();            
        } catch (ClassNotFoundException | IOException e)
        {
            ModLogger.error(e);
        }
        return playerList;
    }

    @SuppressWarnings("all")
    private void writePlayerList(PacketBuffer buffer, List<ClassifiedPlayer> playerListIn)
    {
        try{
            // Serialize data object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ObjectOutputStream out = new ObjectOutputStream(bos) ;
            out.writeObject((Serializable)playerListIn);
            out.close();

            // Get the bytes of the serialized object
            byteBuffer = bos.toByteArray();
            buffer.writeByteArray(byteBuffer);
        } catch (IOException e) {
            ModLogger.error(e);
        }
    }
}
