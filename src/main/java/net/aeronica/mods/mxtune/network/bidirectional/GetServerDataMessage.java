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
import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.MXTuneException;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GetServerDataMessage extends AbstractMessage<GetServerDataMessage>
{
    private boolean errorResult = false;
    private boolean fileError = false;
    private RecordType recordType = RecordType.PLAY_LIST;
    private CompoundNBT dataCompound = new CompoundNBT();
    private long ddddSigBits;
    private long ccccSigBits;
    private long bbbbSigBits;
    private long aaaaSigBits;
    private GUID dataTypeUuid;
    private int playId = PlayType.INVALID;

    @SuppressWarnings("unused")
    public GetServerDataMessage() { /* Required by the PacketDispatcher */ }

    /**
     * Client Request for data recordType
     * @param guidType data recordType unique id
     * @param recordType data recordType
     */
    public GetServerDataMessage(GUID guidType, RecordType recordType)
    {
        this.recordType = recordType;
        ddddSigBits = guidType.getDdddSignificantBits();
        ccccSigBits = guidType.getCcccSignificantBits();
        bbbbSigBits = guidType.getBbbbSignificantBits();
        aaaaSigBits = guidType.getAaaaSignificantBits();
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
        ddddSigBits = guidType.getDdddSignificantBits();
        ccccSigBits = guidType.getCcccSignificantBits();
        bbbbSigBits = guidType.getBbbbSignificantBits();
        aaaaSigBits = guidType.getAaaaSignificantBits();
        this.playId = playId;
    }

    /**
     * Server response with data
     * @param guidType data recordType unique id
     * @param recordType data recordType
     * @param playId to use for the song or the INVALID id (default if not specified in the client request)
     * @param dataCompound provided data
     */
    private GetServerDataMessage(GUID guidType, RecordType recordType, int playId, CompoundNBT dataCompound, boolean errorResult)
    {
        this.recordType = recordType;
        ddddSigBits = guidType.getDdddSignificantBits();
        ccccSigBits = guidType.getCcccSignificantBits();
        bbbbSigBits = guidType.getBbbbSignificantBits();
        aaaaSigBits = guidType.getAaaaSignificantBits();
        this.playId = playId;
        this.dataCompound = dataCompound;
        this.errorResult = errorResult;
    }
    
    @Override
    protected void decode(PacketBuffer buffer) throws IOException
    {
        this.recordType = buffer.readEnumValue(RecordType.class);
        this.dataCompound = buffer.readCompoundTag();
        ddddSigBits = buffer.readLong();
        ccccSigBits = buffer.readLong();
        bbbbSigBits = buffer.readLong();
        aaaaSigBits = buffer.readLong();
        this.errorResult = buffer.readBoolean();
        this.playId = buffer.readInt();
        dataTypeUuid = new GUID(ddddSigBits, ccccSigBits,bbbbSigBits, aaaaSigBits);
    }

    @Override
    protected void encode(PacketBuffer buffer)
    {
        buffer.writeEnumValue(recordType);
        buffer.writeCompoundTag(dataCompound);
        buffer.writeLong(ddddSigBits);
        buffer.writeLong(ccccSigBits);
        buffer.writeLong(bbbbSigBits);
        buffer.writeLong(aaaaSigBits);
        buffer.writeBoolean(errorResult);
        buffer.writeInt(playId);
    }

    @Override
    public void handle(PlayerEntity player, Side side)
    {
        if (side.isClient())
        {
            handleClientSide();
        } else
        {
            handleServerSide(player);
        }
    }

    /**
     * Data received from server. errorResult = true if retrieval failed.
     */
    private void handleClientSide()
    {
        if (errorResult)
            ModLogger.warn(recordType + " file: " + dataTypeUuid.toString() + FileHelper.EXTENSION_DAT + " does not exist on the server");
        switch(recordType)
        {
            case PLAY_LIST:
                ClientFileManager.addPlayList(dataTypeUuid, dataCompound, errorResult);
                break;
            case SONG:
                ClientFileManager.addSong(dataTypeUuid, dataCompound, errorResult);
                ClientPlayManager.playMusic(dataTypeUuid, playId);
                break;

            default:
        }
    }

    /**
     * Retrieve requested data and send it to the client.
     * @param playerIn the client.
     */
    private void handleServerSide(PlayerEntity playerIn)
    {
        switch(recordType)
        {
            case PLAY_LIST:
                dataCompound = getDataCompoundFromFile(FileHelper.SERVER_PLAY_LISTS_FOLDER, dataTypeUuid);
                break;
            case SONG:
                dataCompound = getDataCompoundFromMXTuneFile(FileHelper.SERVER_MUSIC_FOLDER, dataTypeUuid);
                break;
            default:
        }
        PacketDispatcher.sendTo(new GetServerDataMessage(dataTypeUuid, recordType, playId, dataCompound, fileError), (ServerPlayerEntity) playerIn);
    }

    private CompoundNBT getDataCompoundFromFile(String folder, GUID dataTypeGuid)
    {
        Path path;
        CompoundNBT emptyData = new CompoundNBT();
        Boolean fileExists = FileHelper.fileExists(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_DAT, Side.SERVER);
        if (!fileExists)
        {
            path = Paths.get(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_DAT);
            ModLogger.warn(path.toString() + " not found!");
            fileError = true;
            return emptyData;
        }

        try
        {
            path = FileHelper.getCacheFile(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_DAT, Side.SERVER);
        } catch (IOException e)
        {
            ModLogger.error(e);
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
    private CompoundNBT getDataCompoundFromMXTuneFile(String folder, GUID dataTypeGuid)
    {
        Path path;
        CompoundNBT emptyData = new CompoundNBT();
        Boolean fileExists = FileHelper.fileExists(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_MXT, Side.SERVER);
        if (!fileExists)
        {
            path = Paths.get(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_MXT);
            ModLogger.warn(path.toString() + " not found!");
            fileError = true;
            return emptyData;
        }

        MXTuneFile mxTuneFile;
        try
        {
            path = FileHelper.getCacheFile(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_MXT, Side.SERVER);
            mxTuneFile = MXTuneFileHelper.getMXTuneFile(path);
            if (mxTuneFile == null)
                throw new MXTuneException(new TranslationTextComponent("mxtune.error.unexpected_data_error", path.toString()).getUnformattedComponentText());
        } catch (MXTuneException | IOException e)
        {
            ModLogger.error(e);
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
