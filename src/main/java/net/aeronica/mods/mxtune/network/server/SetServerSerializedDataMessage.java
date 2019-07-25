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

package net.aeronica.mods.mxtune.network.server;

import net.aeronica.mods.mxtune.managers.ServerFileManager;
import net.aeronica.mods.mxtune.managers.records.PlayList;
import net.aeronica.mods.mxtune.managers.records.RecordType;
import net.aeronica.mods.mxtune.mxt.MXTuneFile;
import net.aeronica.mods.mxtune.network.IMessage;
import net.aeronica.mods.mxtune.network.NetworkSerializedHelper;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.SendResultMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.ResultMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

public class SetServerSerializedDataMessage implements IMessage
{
    private final RecordType recordType;
    private final Serializable baseData;
    private final long ddddSigBits;
    private final long ccccSigBits;
    private final long bbbbSigBits;
    private final long aaaaSigBits;
    private final GUID dataTypeUuid;

    /**
     * Client Submission for data type
     * @param guidType data type unique id
     * @param recordType data type
     * @param baseData data
     */
    public SetServerSerializedDataMessage(GUID guidType, RecordType recordType, Serializable baseData)
    {
        this.recordType = recordType;
        this.baseData = baseData;
        ddddSigBits = guidType.getDdddSignificantBits();
        ccccSigBits = guidType.getCcccSignificantBits();
        bbbbSigBits = guidType.getBbbbSignificantBits();
        aaaaSigBits = guidType.getAaaaSignificantBits();
        this.dataTypeUuid = guidType;
    }

    public static SetServerSerializedDataMessage decode(final PacketBuffer buffer)
    {
        RecordType recordType = buffer.readEnumValue(RecordType.class);
        Serializable baseData = NetworkSerializedHelper.readSerializedObject(buffer);
        long ddddSigBits = buffer.readLong();
        long ccccSigBits = buffer.readLong();
        long bbbbSigBits = buffer.readLong();
        long aaaaSigBits = buffer.readLong();
        GUID dataTypeUuid = new GUID(ddddSigBits, ccccSigBits, bbbbSigBits, aaaaSigBits);
        return new SetServerSerializedDataMessage(dataTypeUuid, recordType, Objects.requireNonNull(baseData));
    }

    public static void encode(final SetServerSerializedDataMessage message, final PacketBuffer buffer)
    {
        buffer.writeEnumValue(message.recordType);
        NetworkSerializedHelper.writeSerializedObject(buffer, message.baseData);
        buffer.writeLong(message.ddddSigBits);
        buffer.writeLong(message.ccccSigBits);
        buffer.writeLong(message.bbbbSigBits);
        buffer.writeLong(message.aaaaSigBits);
    }

    public static void handle(final SetServerSerializedDataMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && ctx.get().getDirection().getReceptionSide().isServer()) ctx.get().enqueueWork(()->{
            ResultMessage resultMessage = ResultMessage.NO_ERROR;
            if (MusicOptionsUtil.isMxTuneServerUpdateAllowed(player))
            {
                switch (message.recordType)
                {
                    case PLAY_LIST:
                        PlayList playList = (PlayList)message.baseData;
                        resultMessage = ServerFileManager.setPlayList(message.dataTypeUuid, playList);
                        ModLogger.debug("PLAY_LIST Serialized Test: pass %s", message.dataTypeUuid.equals(playList.getGUID()));
                        break;
                    case MXT:
                        MXTuneFile mxTuneFile = (MXTuneFile) message.baseData;
                        resultMessage = ServerFileManager.setMXTFile(message.dataTypeUuid, mxTuneFile);
                        ModLogger.debug("MXT Serialized Test: pass %s", message.dataTypeUuid.equals(mxTuneFile.getGUID()));
                        break;
                    default:
                        resultMessage = new ResultMessage(true, new TranslationTextComponent("mxtune.error.unexpected_type", message.recordType.name()));
                }
            }
            else
                PacketDispatcher.sendTo(new SendResultMessage((new TranslationTextComponent("mxtune.warning.set_server_data_not_allowed")), true), player);

            if (resultMessage.hasError())
                PacketDispatcher.sendTo(new SendResultMessage(resultMessage.getMessage(), resultMessage.hasError()), player);
        });
        ctx.get().setPacketHandled(true);
    }
}
