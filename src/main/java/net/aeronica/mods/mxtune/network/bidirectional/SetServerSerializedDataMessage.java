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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.managers.ServerFileManager;
import net.aeronica.mods.mxtune.managers.records.Area;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.network.NetworkSerializedHelper;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.SendResultMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.ResultMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.io.Serializable;

public class SetServerSerializedDataMessage extends AbstractMessage.AbstractServerMessage<SetServerSerializedDataMessage>
{
    private SetType type = SetType.AREA;
    private Serializable baseData;
    private long ddddSigBits;
    private long ccccSigBits;
    private long bbbbSigBits;
    private long aaaaSigBits;
    private GUID dataTypeUuid = Reference.EMPTY_GUID;

    @SuppressWarnings("unused")
    public SetServerSerializedDataMessage() { /* Required by the PacketDispatcher */ }

    /**
     * Client Submission for data type
     * @param guidType data type unique id
     * @param type data type
     * @param baseData
     */
    public SetServerSerializedDataMessage(GUID guidType, SetType type , Serializable baseData)
    {
        this.type = type;
        this.baseData = baseData;
        ddddSigBits = guidType.getDdddSignificantBits();
        ccccSigBits = guidType.getCcccSignificantBits();
        bbbbSigBits = guidType.getBbbbSignificantBits();
        aaaaSigBits = guidType.getAaaaSignificantBits();
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        this.type = buffer.readEnumValue(SetType.class);
        this.baseData = NetworkSerializedHelper.readSerializedObject(buffer);
        ddddSigBits = buffer.readLong();
        ccccSigBits = buffer.readLong();
        bbbbSigBits = buffer.readLong();
        aaaaSigBits = buffer.readLong();
        dataTypeUuid = new GUID(ddddSigBits, ccccSigBits, bbbbSigBits, aaaaSigBits);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        buffer.writeEnumValue(type);
        NetworkSerializedHelper.writeSerializedObject(buffer, baseData);
        buffer.writeLong(ddddSigBits);
        buffer.writeLong(ccccSigBits);
        buffer.writeLong(bbbbSigBits);
        buffer.writeLong(aaaaSigBits);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        ResultMessage resultMessage = ResultMessage.NO_ERROR;
        if (MusicOptionsUtil.isMxTuneServerUpdateAllowed(player))
        {
            switch (type)
            {
                case AREA:
                    Area area = (Area)baseData;
                    resultMessage = ServerFileManager.setArea(dataTypeUuid, area);
                    ModLogger.debug("BACKGROUND Serialized Test: pass %s", dataTypeUuid.equals(area.getGUID()));
                    break;
                case MUSIC:
                    Song song = (Song)baseData;
                    resultMessage = ServerFileManager.setSong(dataTypeUuid, song);
                    ModLogger.debug("MUSIC Serialized Test: pass %s", dataTypeUuid.equals(song.getGUID()));
                    break;
                default:
                    resultMessage = new ResultMessage(true, new TextComponentTranslation("mxtune.error.unexpected_type", type.name()));
            }
        }
        else
            PacketDispatcher.sendTo(new SendResultMessage((new TextComponentTranslation("mxtune.warning.set_server_data_not_allowed")), true), (EntityPlayerMP) player);

        if (resultMessage.hasError())
            PacketDispatcher.sendTo(new SendResultMessage(resultMessage.getMessage(), resultMessage.hasError()), (EntityPlayerMP) player);

    }
    public enum SetType {AREA, MUSIC}
}
