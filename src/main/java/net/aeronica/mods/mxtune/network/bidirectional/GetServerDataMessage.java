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
import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GetServerDataMessage extends AbstractMessage<GetServerDataMessage>
{
    private boolean errorResult = false;
    private boolean fileError = false;
    public enum GetType {AREA, MUSIC}
    private GetType type = GetType.AREA;
    private NBTTagCompound dataCompound = new NBTTagCompound();
    private long ddddSigBits;
    private long ccccSigBits;
    private long bbbbSigBits;
    private long aaaaSigBits;
    private GUID dataTypeUuid;
    private int playId = PlayType.INVALID;

    @SuppressWarnings("unused")
    public GetServerDataMessage() { /* Required by the PacketDispatcher */ }

    /**
     * Client Request for data type
     * @param guidType data type unique id
     * @param type data type
     */
    public GetServerDataMessage(GUID guidType, GetType type)
    {
        this.type = type;
        ddddSigBits = guidType.getDdddSignificantBits();
        ccccSigBits = guidType.getCcccSignificantBits();
        bbbbSigBits = guidType.getBbbbSignificantBits();
        aaaaSigBits = guidType.getAaaaSignificantBits();
    }

    /**
     * Client Request for data type using playId which will cause the client to start playing the song after it is
     * received by the client.
     * @param guidType data type unique id
     * @param type data type
     * @param playId to use for the song. Only valid for the MUSIC type
     */
    public GetServerDataMessage(GUID guidType, GetType type, int playId)
    {
        this.type = type;
        ddddSigBits = guidType.getDdddSignificantBits();
        ccccSigBits = guidType.getCcccSignificantBits();
        bbbbSigBits = guidType.getBbbbSignificantBits();
        aaaaSigBits = guidType.getAaaaSignificantBits();
        this.playId = playId;
    }

    /**
     * Server response with data
     * @param guidType data type unique id
     * @param type data type
     * @param playId to use for the song or the INVALID id (default if not specified in the client request)
     * @param dataCompound provided data
     */
    private GetServerDataMessage(GUID guidType, GetType type, int playId, NBTTagCompound dataCompound, boolean errorResult)
    {
        this.type = type;
        ddddSigBits = guidType.getDdddSignificantBits();
        ccccSigBits = guidType.getCcccSignificantBits();
        bbbbSigBits = guidType.getBbbbSignificantBits();
        aaaaSigBits = guidType.getAaaaSignificantBits();
        this.playId = playId;
        this.dataCompound = dataCompound;
        this.errorResult = errorResult;
    }
    
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        this.type = buffer.readEnumValue(GetType.class);
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
    protected void write(PacketBuffer buffer)
    {
        buffer.writeEnumValue(type);
        buffer.writeCompoundTag(dataCompound);
        buffer.writeLong(ddddSigBits);
        buffer.writeLong(ccccSigBits);
        buffer.writeLong(bbbbSigBits);
        buffer.writeLong(aaaaSigBits);
        buffer.writeBoolean(errorResult);
        buffer.writeInt(playId);
    }

    @Override
    public void process(EntityPlayer player, Side side)
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
            ModLogger.error(type + " file: " + dataTypeUuid.toString() + FileHelper.EXTENSION_DAT + " does not exist on the server");
        switch(type)
        {
            case AREA:
                ClientFileManager.addArea(dataTypeUuid, dataCompound, errorResult);
                break;
            case MUSIC:
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
    private void handleServerSide(EntityPlayer playerIn)
    {
        switch(type)
        {
            case AREA:
                dataCompound = getDataCompoundFromFile(FileHelper.SERVER_AREAS_FOLDER, dataTypeUuid);
                break;
            case MUSIC:
                dataCompound = getDataCompoundFromFile(FileHelper.SERVER_MUSIC_FOLDER, dataTypeUuid);
                break;
            default:
        }
        PacketDispatcher.sendTo(new GetServerDataMessage(dataTypeUuid, type, playId, dataCompound, fileError), (EntityPlayerMP) playerIn);
    }

    private NBTTagCompound getDataCompoundFromFile(String folder, GUID dataTypeGuid)
    {
        Path path;
        NBTTagCompound emptyData = new NBTTagCompound();
        Boolean fileExists = FileHelper.fileExists(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_DAT, Side.SERVER);
        if (!fileExists)
        {
            path = Paths.get(folder, dataTypeGuid.toString() + FileHelper.EXTENSION_DAT);
            ModLogger.error(path.toString() + " not found!");
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
}
