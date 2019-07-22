/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.network.bidirectional;

import net.aeronica.mods.mxtune.managers.ServerFileManager;
import net.aeronica.mods.mxtune.managers.records.BaseData;
import net.aeronica.mods.mxtune.managers.records.RecordType;
import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.util.CallBack;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.Notify;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GetBaseDataListsMessage<E extends BaseData> extends AbstractMessage<GetBaseDataListsMessage<E>>
{
    private RecordType recordType;
    private byte[] byteBuffer = null;
    private long callbackUuidMSB = 0;
    private long callbackUuidLSB = 0;
    private ITextComponent message = new TranslationTextComponent("mxtune.no_error", "");
    private UUID callbackUuid;
    private List<E> listBaseData;
    private boolean readError = false;

    public GetBaseDataListsMessage() { /* Required by the PacketDispatcher */ }

    public GetBaseDataListsMessage(UUID callback, RecordType recordType)
    {
        this.recordType = recordType;
        callbackUuidMSB = callback.getMostSignificantBits();
        callbackUuidLSB = callback.getLeastSignificantBits();
    }

    public GetBaseDataListsMessage(List<E> listBaseData, UUID callbackUuid, RecordType recordType, ITextComponent message)
    {
        this.recordType = recordType;
        this.listBaseData = listBaseData;
        this.message = message;
        this.callbackUuidMSB = callbackUuid.getMostSignificantBits();
        this.callbackUuidLSB = callbackUuid.getLeastSignificantBits();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void decode(PacketBuffer buffer)
    {
        recordType = buffer.readEnumValue(RecordType.class);
        callbackUuidMSB = buffer.readLong();
        callbackUuidLSB = buffer.readLong();
        this.message = ITextComponent.Serializer.jsonToComponent(buffer.readString(32767));
        callbackUuid = new UUID (callbackUuidMSB, callbackUuidLSB);
        try {
            // Deserialize data object from a byte array
            byteBuffer = buffer.readByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer) ;
            ObjectInputStream in = new ObjectInputStream(bis);
            listBaseData =  (ArrayList<E>) in.readObject();
            in.close();

        } catch (ClassNotFoundException | IOException e)
        {
            readError = true;
            ModLogger.error(e);
        }
    }

    @Override
    protected void encode(PacketBuffer buffer)
    {
        buffer.writeEnumValue(recordType);
        buffer.writeLong(callbackUuidMSB);
        buffer.writeLong(callbackUuidLSB);
        buffer.writeString(ITextComponent.Serializer.componentToJson(message));
        try {
            // Serialize data object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject((Serializable)listBaseData);
            out.close();

            // Get the bytes of the serialized object
            byteBuffer = bos.toByteArray();
            buffer.writeByteArray(byteBuffer);

        } catch (IOException e) {
            ModLogger.error(e);
        }
    }

    @Override
    public void handle(PlayerEntity player, Side side)
    {
        if (side.isClient())
        {
            handleClientSide();
        } else
        {
            handleServerSide((ServerPlayerEntity) player);
        }
    }

    private void handleClientSide()
    {
        CallBack callBack = CallBackManager.getCaller(callbackUuid);
        if (callBack == null) return;
        Notify notify = CallBackManager.getNotified(callbackUuid);
        if (!readError)
        {
            callBack.onResponse(listBaseData, recordType);
            if (notify != null)
                notify.onNotify(recordType);
        }
        else
                callBack.onFailure(message.appendText("/n").appendSibling(new TranslationTextComponent("mxtune.error.network_data_error", "CLIENT Read Error.")));
    }

    @SuppressWarnings("unchecked")
    private void handleServerSide(ServerPlayerEntity playerMP)
    {
        switch (recordType)
        {
            case MXT:
                break;
            case PLAY_LIST:
                PacketDispatcher.sendTo(new GetBaseDataListsMessage(ServerFileManager.getPlayLists(), callbackUuid, recordType, new TranslationTextComponent("mxtune.no_error", "SERVER")), playerMP);
                break;
            case SONG:
                break;
            case SONG_PROXY:
                PacketDispatcher.sendTo(new GetBaseDataListsMessage(ServerFileManager.getSongProxies(), callbackUuid, recordType, new TranslationTextComponent("mxtune.no_error", "SERVER")), playerMP);
                break;
            default:
        }
    }
}
