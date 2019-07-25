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

import net.aeronica.mods.mxtune.network.IMessage;
import net.aeronica.mods.mxtune.network.server.ChunkToolMessage;
import net.aeronica.mods.mxtune.options.ClassifiedPlayer;
import net.aeronica.mods.mxtune.options.IPlayerMusicOptions;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class SyncPlayerMusicOptionsMessage implements IMessage
{
    @CapabilityInject(IPlayerMusicOptions.class)
    private static final Capability<IPlayerMusicOptions> MUSIC_OPTIONS = Miscellus.nonNullInjected();

    private final int propertyID;
    private CompoundNBT data;
    private final boolean disableHud;
    private final int positionHud;
    private final float sizeHud;
    private final int muteOption;
    private final String sParam1;
    private final String sParam2;
    private final String sParam3;
    private final List<ClassifiedPlayer> blackList;
    private final List<ClassifiedPlayer> whiteList;
    private final boolean allowMusicOp;
    private final GUID selectedAreaGuid;
    private final boolean ctrlKeyDown;
    private final ChunkToolMessage.Operation operation;

    public SyncPlayerMusicOptionsMessage(final IPlayerMusicOptions inst, final int propertyID)
    {
        this.propertyID = propertyID;
        switch (propertyID)
        {
            case MusicOptionsUtil.SYNC_ALL:
                this.data = new CompoundNBT();
                this.data = (CompoundNBT) MUSIC_OPTIONS.writeNBT(inst, null);
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

            case MusicOptionsUtil.SYNC_MUSIC_OP:
                this.allowMusicOp = inst.isMxTuneServerUpdateAllowed();
                break;

            case MusicOptionsUtil.SYNC_SELECTED_PLAY_LIST_GUID:
                this.selectedAreaGuid = inst.getSelectedPlayListGuid();
                break;

            case MusicOptionsUtil.SYNC_CTRL_KEY_DOWN:
                this.ctrlKeyDown = inst.isCtrlKeyDown();
                break;

            case MusicOptionsUtil.SYNC_CHUNK_OPERATION:
                this.operation = inst.getChunkToolOperation();
                break;
            default:
        }
    }

    public static SyncPlayerMusicOptionsMessage decode(final PacketBuffer buffer)
    {
        int propertyID = buffer.readInt();
        switch (propertyID)
        {
            case MusicOptionsUtil.SYNC_ALL:
                 CompoundNBT data = buffer.readCompoundTag();
                break;
            case MusicOptionsUtil.SYNC_DISPLAY_HUD:
                boolean disableHud = buffer.readBoolean();
                int  positionHud = buffer.readInt();
                float sizeHud = buffer.readFloat();
                break;
            case MusicOptionsUtil.SYNC_MUTE_OPTION:
                int muteOption = buffer.readInt();
                break;
            case MusicOptionsUtil.SYNC_S_PARAMS:
                String sParam1 = buffer.readString();
                String sParam2 = buffer.readString();
                String sParam3 = buffer.readString();
                break;
            case MusicOptionsUtil.SYNC_WHITE_LIST:
                List<ClassifiedPlayer> whiteList = readPlayerList(buffer);
                break;
            case MusicOptionsUtil.SYNC_BLACK_LIST:
                List<ClassifiedPlayer> blackList = readPlayerList(buffer);
                break;
            case MusicOptionsUtil.SYNC_MUSIC_OP:
                boolean allowMusicOp = buffer.readBoolean();
                break;
            case MusicOptionsUtil.SYNC_SELECTED_PLAY_LIST_GUID:
                long ddddSigBits = buffer.readLong();
                long ccccSigBits = buffer.readLong();
                long bbbbSigBits = buffer.readLong();
                long aaaaSigBits = buffer.readLong();
                GUID selectedAreaGuid = new GUID(ddddSigBits, ccccSigBits, bbbbSigBits, aaaaSigBits);
                break;
            case MusicOptionsUtil.SYNC_CTRL_KEY_DOWN:
                boolean ctrlKeyDown = buffer.readBoolean();
                break;
            case MusicOptionsUtil.SYNC_CHUNK_OPERATION:
                ChunkToolMessage.Operation operation = buffer.readEnumValue(ChunkToolMessage.Operation.class);
                break;
            default:
        }
        return new SyncPlayerMusicOptionsMessage();
    }

    public static void encode(final SyncPlayerMusicOptionsMessage message, final PacketBuffer buffer)
    {
        buffer.writeInt(message.propertyID);
        switch (message.propertyID)
        {
            case MusicOptionsUtil.SYNC_ALL:
                buffer.writeCompoundTag(message.data);
                break;
            case MusicOptionsUtil.SYNC_DISPLAY_HUD:
                buffer.writeBoolean(message.disableHud);
                buffer.writeInt(message.positionHud);
                buffer.writeFloat(message.sizeHud);
                break;
            case MusicOptionsUtil.SYNC_MUTE_OPTION:
                buffer.writeInt(message.muteOption);
                break;
            case MusicOptionsUtil.SYNC_S_PARAMS:
                buffer.writeString(message.sParam1);
                buffer.writeString(message.sParam2);
                buffer.writeString(message.sParam3);
                break;
            case MusicOptionsUtil.SYNC_WHITE_LIST:
                writePlayerList(buffer, message.whiteList);
                break;
            case MusicOptionsUtil.SYNC_BLACK_LIST:
                writePlayerList(buffer, message.blackList);
                break;
            case MusicOptionsUtil.SYNC_MUSIC_OP:
                buffer.writeBoolean(message.allowMusicOp);
                break;
            case MusicOptionsUtil.SYNC_SELECTED_PLAY_LIST_GUID:
                buffer.writeLong(message.selectedAreaGuid.getDdddSignificantBits());
                buffer.writeLong(message.selectedAreaGuid.getCcccSignificantBits());
                buffer.writeLong(message.selectedAreaGuid.getBbbbSignificantBits());
                buffer.writeLong(message.selectedAreaGuid.getAaaaSignificantBits());
                break;
            case MusicOptionsUtil.SYNC_CTRL_KEY_DOWN:
                buffer.writeBoolean(message.ctrlKeyDown);
                break;
            case MusicOptionsUtil.SYNC_CHUNK_OPERATION:
                buffer.writeEnumValue(message.operation);
                break;
            default:
        }
    }

    public static void handle(final SyncPlayerMusicOptionsMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && ctx.get().getDirection().getReceptionSide().isClient())
        if (player.hasCapability(MUSIC_OPTIONS, null))
        {
            final LazyOptional<IPlayerMusicOptions> instance = player.getCapability(MUSIC_OPTIONS, null);
            if (instance != null)
                switch (this.propertyID)
                {
                    case MusicOptionsUtil.SYNC_ALL:
                        MUSIC_OPTIONS.readNBT(instance, null, message.data);
                        break;
                    case MusicOptionsUtil.SYNC_DISPLAY_HUD:
                        instance.setHudOptions(disableHud, positionHud, sizeHud);
                        break;
                    case MusicOptionsUtil.SYNC_MUTE_OPTION:
                        instance.setMuteOption(muteOption);
                        break;
                    case MusicOptionsUtil.SYNC_S_PARAMS:
                        instance.setSParams(sParam1, sParam2, sParam3);
                        break;
                    case MusicOptionsUtil.SYNC_WHITE_LIST:
                        instance.setWhiteList(whiteList);
                        break;
                    case MusicOptionsUtil.SYNC_BLACK_LIST:
                        instance.setBlackList(blackList);
                        break;
                    case MusicOptionsUtil.SYNC_MUSIC_OP:
                        instance.setMxTuneServerUpdateAllowed(allowMusicOp);
                        break;
                    case MusicOptionsUtil.SYNC_SELECTED_PLAY_LIST_GUID:
                        instance.setSelectedPlayListGuid(selectedAreaGuid);
                        break;
                    case MusicOptionsUtil.SYNC_CTRL_KEY_DOWN:
                        instance.setCtrlKey(ctrlKeyDown);
                        break;
                    case MusicOptionsUtil.SYNC_CHUNK_OPERATION:
                        instance.setChunkToolOperation(operation);
                        break;
                    default:
                }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ClassifiedPlayer> readPlayerList(PacketBuffer buffer)
    {
        List<ClassifiedPlayer> playerList = Collections.emptyList();
        byte[] byteBuffer;
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
    private static void writePlayerList(PacketBuffer buffer, List<ClassifiedPlayer> playerListIn)
    {
        byte[] byteBuffer = null;
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
