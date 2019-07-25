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
import net.aeronica.mods.mxtune.network.IMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.util.CallBack;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.Notify;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class GetBaseDataListsMessage<E extends BaseData> implements IMessage
{
    private final RecordType recordType;
    private final long callbackUuidMSB;
    private final long callbackUuidLSB;
    private final UUID callbackUuid;
    private final List<E> listBaseData;
    private final boolean readError;

    public GetBaseDataListsMessage(final UUID callback, final RecordType recordType)
    {
        this.recordType = recordType;
        this.callbackUuidMSB = callback.getMostSignificantBits();
        this.callbackUuidLSB = callback.getLeastSignificantBits();
        this.callbackUuid = callback;
        this.listBaseData = new ArrayList<>();
        this.readError = false;
    }

    public GetBaseDataListsMessage(final List<E> listBaseData, final UUID callbackUuid, final RecordType recordType, boolean readError)
    {
        this.recordType = recordType;
        this.listBaseData = listBaseData;
        this.callbackUuidMSB = callbackUuid.getMostSignificantBits();
        this.callbackUuidLSB = callbackUuid.getLeastSignificantBits();
        this.callbackUuid = callbackUuid;
        this.readError = readError;
    }

    @SuppressWarnings("unchecked")
    public static GetBaseDataListsMessage decode(final PacketBuffer buffer)
    {
        RecordType recordType = buffer.readEnumValue(RecordType.class);
        long callbackUuidMSB = buffer.readLong();
        long callbackUuidLSB = buffer.readLong();
        UUID callbackUuid = new UUID (callbackUuidMSB, callbackUuidLSB);
        boolean readError = false;

        List<BaseData> listBaseData = new ArrayList<>();
        byte[] byteBuffer;
        try {
            // Deserialize data object from a byte array
            byteBuffer = buffer.readByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer) ;
            ObjectInputStream in = new ObjectInputStream(bis);
            listBaseData = (ArrayList<BaseData>) in.readObject();
            in.close();

        } catch (ClassNotFoundException | IOException e)
        {
            readError = true;
            ModLogger.error(e);
        }
        return new GetBaseDataListsMessage(listBaseData, callbackUuid, recordType, readError);
    }

    public static void encode(final GetBaseDataListsMessage message, final PacketBuffer buffer)
    {
        buffer.writeEnumValue(message.recordType);
        buffer.writeLong(message.callbackUuidMSB);
        buffer.writeLong(message.callbackUuidLSB);

        byte[] byteBuffer;
        try {
            // Serialize data object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject((Serializable)message.listBaseData);
            out.close();

            // Get the bytes of the serialized object
            byteBuffer = bos.toByteArray();
            buffer.writeByteArray(byteBuffer);

        } catch (IOException e) {
            ModLogger.error(e);
        }
    }

    public static void handle(final GetBaseDataListsMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (ctx.get().getDirection().getReceptionSide().isClient())
        {
            handleClientSide(message,ctx);
        } else
        {
            handleServerSide(message, ctx, Objects.requireNonNull(player));
        }
    }

    private static void handleClientSide(final GetBaseDataListsMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(()->{
            CallBack callBack = CallBackManager.getCaller(message.callbackUuid);
            if (callBack == null) return;
            Notify notify = CallBackManager.getNotified(message.callbackUuid);
            if (!message.readError)
            {
                callBack.onResponse(message.listBaseData, message.recordType);
                if (notify != null)
                    notify.onNotify(message.recordType);
            }
            else
                callBack.onFailure(new TranslationTextComponent("mxtune.error.network_data_error", "CLIENT Read Error."));
        });
        ctx.get().setPacketHandled(true);
    }

    @SuppressWarnings("unchecked")
    private static void handleServerSide(final GetBaseDataListsMessage message, final Supplier<NetworkEvent.Context> ctx, ServerPlayerEntity player)
    {
        ctx.get().enqueueWork(()->{
            switch (message.recordType)
            {
                case MXT:
                    break;
                case PLAY_LIST:
                    PacketDispatcher.sendTo(new GetBaseDataListsMessage(ServerFileManager.getPlayLists(), message.callbackUuid, message.recordType, false), player);
                    break;
                case SONG:
                    break;
                case SONG_PROXY:
                    PacketDispatcher.sendTo(new GetBaseDataListsMessage(ServerFileManager.getSongProxies(), message.callbackUuid, message.recordType, false), player);
                    break;
                default:
            }
        });

    }
}
