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

import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.managers.ClientFileManager;
import net.aeronica.mods.mxtune.managers.ClientPlayManager;
import net.aeronica.mods.mxtune.managers.PlayIdSupplier.PlayType;
import net.aeronica.mods.mxtune.managers.records.RecordType;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.mxt.MXTuneFile;
import net.aeronica.mods.mxtune.mxt.MXTuneFileHelper;
import net.aeronica.mods.mxtune.network.IMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.MXTuneException;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class GetServerDataMessage implements IMessage
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final RecordType recordType;
    private final CompoundNBT dataCompound;
    private final long ddddSigBits;
    private final long ccccSigBits;
    private final long bbbbSigBits;
    private final long aaaaSigBits;
    private final GUID dataTypeUuid;
    private boolean errorResult;
    private final int playId;

    private static boolean fileError = false;

    /**
     * Client Request for data recordType
     * @param guidType data recordType unique id
     * @param recordType data recordType
     */
    public GetServerDataMessage(GUID guidType, RecordType recordType)
    {
        this.recordType = recordType;
        this.dataCompound = new CompoundNBT();
        this.ddddSigBits = guidType.getDdddSignificantBits();
        this.ccccSigBits = guidType.getCcccSignificantBits();
        this.bbbbSigBits = guidType.getBbbbSignificantBits();
        this.aaaaSigBits = guidType.getAaaaSignificantBits();
        this.dataTypeUuid = new GUID(ddddSigBits, ccccSigBits,bbbbSigBits, aaaaSigBits);
        this.errorResult = false;
        this.playId = PlayType.INVALID;
    }

    /**
     * Client Request for data recordType using playId which will cause the client to start playing the song after it is
     * received by the client.
     * @param guidType data recordType unique id
     * @param recordType data recordType
     * @param playId to use for the song. Only valid for the MUSIC recordType
     */
    public GetServerDataMessage(GUID guidType, RecordType recordType, int playId)
    {
        this.recordType = recordType;
        this.dataCompound = new CompoundNBT();
        this.ddddSigBits = guidType.getDdddSignificantBits();
        this.ccccSigBits = guidType.getCcccSignificantBits();
        this.bbbbSigBits = guidType.getBbbbSignificantBits();
        this.aaaaSigBits = guidType.getAaaaSignificantBits();
        this.dataTypeUuid = new GUID(ddddSigBits, ccccSigBits,bbbbSigBits, aaaaSigBits);
        this.errorResult = false;
        this.playId = playId;
    }

    /**
     * Server response with data
     * @param guidType data recordType unique id
     * @param recordType data recordType
     * @param playId to use for the song or the INVALID id (default if not specified in the client request)
     * @param dataCompound provided data
     */
    private GetServerDataMessage(final GUID guidType, final RecordType recordType, final int playId, @Nullable final CompoundNBT dataCompound, final boolean errorResult)
    {
        this.recordType = recordType;
        this.dataCompound = dataCompound;
        this.ddddSigBits = guidType.getDdddSignificantBits();
        this.ccccSigBits = guidType.getCcccSignificantBits();
        this.bbbbSigBits = guidType.getBbbbSignificantBits();
        this.aaaaSigBits = guidType.getAaaaSignificantBits();
        this.dataTypeUuid = new GUID(ddddSigBits, ccccSigBits,bbbbSigBits, aaaaSigBits);
        this.errorResult = errorResult;
        this.playId = playId;
    }

    public static GetServerDataMessage decode(PacketBuffer buffer)
    {
        RecordType recordType = buffer.readEnumValue(RecordType.class);
        CompoundNBT dataCompound = buffer.readCompoundTag();
        long ddddSigBits = buffer.readLong();
        long ccccSigBits = buffer.readLong();
        long bbbbSigBits = buffer.readLong();
        long aaaaSigBits = buffer.readLong();
        boolean errorResult = buffer.readBoolean();
        int playId = buffer.readInt();
        GUID dataTypeUuid = new GUID(ddddSigBits, ccccSigBits,bbbbSigBits, aaaaSigBits);
        return new GetServerDataMessage(dataTypeUuid, recordType, playId, dataCompound, errorResult);
    }

    public static void encode(final GetServerDataMessage message, final PacketBuffer buffer)
    {
        buffer.writeEnumValue(message.recordType);
        buffer.writeCompoundTag(message.dataCompound);
        buffer.writeLong(message.ddddSigBits);
        buffer.writeLong(message.ccccSigBits);
        buffer.writeLong(message.bbbbSigBits);
        buffer.writeLong(message.aaaaSigBits);
        buffer.writeBoolean(message.errorResult);
        buffer.writeInt(message.playId);
    }

    public static void handle(final GetServerDataMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            handleClientSide(message, ctx);
        else
            handleServerSide(message, ctx);
    }

    /**
     * Data received from server. errorResult = true if retrieval failed.
     */
    private static void handleClientSide(final GetServerDataMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
            {
                if (message.errorResult)
                    LOGGER.warn("{} file: {}.{}  does not exist on the server", message.recordType, message.dataTypeUuid.toString(), FileHelper.EXTENSION_DAT);
                switch(message.recordType)
                {
                    case PLAY_LIST:
                        ClientFileManager.addPlayList(message.dataTypeUuid, message.dataCompound, message.errorResult);
                        break;
                    case SONG:
                        ClientFileManager.addSong(message.dataTypeUuid, message.dataCompound, message.errorResult);
                        ClientPlayManager.playMusic(message.dataTypeUuid, message.playId);
                        break;
                    default:
                }
            });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Retrieve requested data and send it to the client.
     * @param message from the client.
     * @param ctx network event context
     */
    private static void handleServerSide(final GetServerDataMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null)
            ctx.get().enqueueWork(() ->
                {
                    CompoundNBT dataCompound = null;
                    switch(message.recordType)
                    {
                        case PLAY_LIST:
                            dataCompound = getDataCompoundFromFile(FileHelper.SERVER_PLAY_LISTS_FOLDER, message.dataTypeUuid);
                            break;
                        case SONG:
                            dataCompound = getDataCompoundFromMXTuneFile(FileHelper.SERVER_MUSIC_FOLDER, message.dataTypeUuid);
                            break;
                        default:
                    }
                    PacketDispatcher.sendTo(new GetServerDataMessage(message.dataTypeUuid, message.recordType, message.playId, dataCompound, fileError), (ServerPlayerEntity) player);
                });
        ctx.get().setPacketHandled(true);
    }

    private static CompoundNBT getDataCompoundFromFile(String folder, GUID dataTypeGuid)
    {
        Path path;
        CompoundNBT emptyData = new CompoundNBT();
        Boolean fileExists = FileHelper.fileExists(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_DAT, LogicalSide.SERVER);
        if (!fileExists)
        {
            path = Paths.get(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_DAT);
            LOGGER.warn("{} not found!", path.toString());
            fileError = true;
            return emptyData;
        }

        try
        {
            path = FileHelper.getCacheFile(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_DAT, LogicalSide.SERVER);
        } catch (IOException e)
        {
            LOGGER.error(e);
            fileError = true;
            return emptyData;
        }
        fileError = false;
        return FileHelper.getCompoundFromFile(path);
    }

    /**
     * MXT files are now saved on the server side. This method gets the song compound from the MXT file.
     * @param folder The path to the resource.
     * @param dataTypeGuid The GUID of the resource.
     * @return The Song compound.
     */
    private static CompoundNBT getDataCompoundFromMXTuneFile(String folder, GUID dataTypeGuid)
    {
        Path path;
        CompoundNBT emptyData = new CompoundNBT();
        Boolean fileExists = FileHelper.fileExists(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_MXT, LogicalSide.SERVER);
        if (!fileExists)
        {
            path = Paths.get(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_MXT);
            LOGGER.warn("{} not found!", path.toString());
            fileError = true;
            return emptyData;
        }

        MXTuneFile mxTuneFile;
        try
        {
            path = FileHelper.getCacheFile(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_MXT, LogicalSide.SERVER);
            mxTuneFile = MXTuneFileHelper.getMXTuneFile(path);
            if (mxTuneFile == null)
                throw new MXTuneException(new TranslationTextComponent("mxtune.error.unexpected_data_error", path.toString()).getUnformattedComponentText());
        } catch (MXTuneException | IOException e)
        {
            LOGGER.error(e);
            fileError = true;
            return emptyData;
        }
        fileError = false;

        Song song = MXTuneFileHelper.getSong(mxTuneFile);
        CompoundNBT songData = new CompoundNBT();
        song.writeToNBT(songData);
        return songData;
    }
}
