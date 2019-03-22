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
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class GetServerDataMessage extends AbstractMessage<GetServerDataMessage>
{
    private boolean errorResult = false;
    private boolean fileError = false;
    public enum GetType {AREA, PLAY_LIST, MUSIC}
    private GetType type = GetType.AREA;
    private NBTTagCompound dataCompound = new NBTTagCompound();
    private long dataTypeUuidMSB = 0;
    private long dataTypeUuidLSB = 0;
    private UUID dataTypeUuid;
    private int playId = PlayType.INVALID;

    @SuppressWarnings("unused")
    public GetServerDataMessage() { /* Required by the PacketDispatcher */ }

    /**
     * Client Request for data type
     * @param uuidType data type unique id
     * @param type data type
     */
    public GetServerDataMessage(UUID uuidType, GetType type)
    {
        this.type = type;
        dataTypeUuidMSB = uuidType.getMostSignificantBits();
        dataTypeUuidLSB = uuidType.getLeastSignificantBits();
    }

    /**
     * Client Request for data type using playId which will cause the client to start playing the song after it is
     * received by the client.
     * @param uuidType data type unique id
     * @param type data type
     * @param playId to use for the song. Only valid for the MUSIC type
     */
    public GetServerDataMessage(UUID uuidType, GetType type, int playId)
    {
        this.type = type;
        dataTypeUuidMSB = uuidType.getMostSignificantBits();
        dataTypeUuidLSB = uuidType.getLeastSignificantBits();
        this.playId = playId;
    }

    /**
     * Server response with data
     * @param uuidType data type unique id
     * @param type data type
     * @param playId to use for the song or the INVALID id (default if not specified in the client request)
     * @param dataCompound provided data
     */
    private GetServerDataMessage(UUID uuidType, GetType type, int playId, NBTTagCompound dataCompound, boolean errorResult)
    {
        this.type = type;
        dataTypeUuidMSB = uuidType.getMostSignificantBits();
        dataTypeUuidLSB = uuidType.getLeastSignificantBits();
        this.playId = playId;
        this.dataCompound = dataCompound;
        this.errorResult = errorResult;
    }
    
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        this.type = buffer.readEnumValue(GetType.class);
        this.dataCompound = buffer.readCompoundTag();
        this.dataTypeUuidMSB = buffer.readLong();
        this.dataTypeUuidLSB = buffer.readLong();
        this.errorResult = buffer.readBoolean();
        this.playId = buffer.readInt();
        dataTypeUuid = new UUID(dataTypeUuidMSB, dataTypeUuidLSB);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeEnumValue(type);
        buffer.writeCompoundTag(dataCompound);
        buffer.writeLong(dataTypeUuidMSB);
        buffer.writeLong(dataTypeUuidLSB);
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
            ModLogger.error(type + " file: " + dataTypeUuid.toString() + ".dat does not exist on the server");
        switch(type)
        {
            case AREA:
                ClientFileManager.addArea(dataTypeUuid, dataCompound, errorResult);
                break;
            case PLAY_LIST:
                ClientFileManager.addPlayList(dataTypeUuid, dataCompound, errorResult);
                break;
            case MUSIC:
                ClientFileManager.addMusic(dataTypeUuid, dataCompound, errorResult);
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
            case PLAY_LIST:
                dataCompound = getDataCompoundFromFile(FileHelper.SERVER_PLAYLISTS_FOLDER, dataTypeUuid);
                break;
            case MUSIC:
                dataCompound = getDataCompoundFromFile(FileHelper.SERVER_MUSIC_FOLDER, dataTypeUuid);
                break;
            default:
        }
        PacketDispatcher.sendTo(new GetServerDataMessage(dataTypeUuid, type, playId, dataCompound, fileError), (EntityPlayerMP) playerIn);
    }

    private NBTTagCompound getDataCompoundFromFile(String folder, UUID dataTypeUuid)
    {
        Path path;
        NBTTagCompound emptyData = new NBTTagCompound();
        Boolean fileExists = FileHelper.fileExists(folder, dataTypeUuid.toString() + ".dat", Side.SERVER);
        if (!fileExists)
        {
            path = Paths.get(folder, dataTypeUuid.toString() + ".dat");
            ModLogger.error(path.toString() + " not found!");
            fileError = true;
            return emptyData;
        }

        try
        {
            path = FileHelper.getCacheFile(folder, dataTypeUuid.toString() + ".dat", Side.SERVER);
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
