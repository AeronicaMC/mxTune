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
    public enum Type {AREA, PLAY_LIST, MUSIC}
    private Type type = Type.AREA;
    private NBTTagCompound dataCompound = new NBTTagCompound();
    private long dataTypeUuidMSB = 0;
    private long dataTypeUuidLSB = 0;
    private UUID dataTypeUuid;

    @SuppressWarnings("unused")
    public GetServerDataMessage() { /* Required by the PacketDispatcher */ }

    /**
     * Client Request for data type
     * @param uuidType data type unique id
     * @param type data type
     */
    public GetServerDataMessage(UUID uuidType, Type type)
    {
        this.type = type;
        dataTypeUuidMSB = uuidType.getMostSignificantBits();
        dataTypeUuidLSB = uuidType.getLeastSignificantBits();
    }

    /**
     * Server response with data
     * @param uuidType data type unique id
     * @param type data type
     * @param dataCompound provided data
     */
    private GetServerDataMessage(UUID uuidType, Type type, NBTTagCompound dataCompound, boolean errorResult)
    {
        this.type = type;
        dataTypeUuidMSB = uuidType.getMostSignificantBits();
        dataTypeUuidLSB = uuidType.getLeastSignificantBits();
        this.dataCompound = dataCompound;
        this.errorResult = errorResult;
    }
    
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        this.type = buffer.readEnumValue(Type.class);
        this.dataCompound = buffer.readCompoundTag();
        this.dataTypeUuidMSB = buffer.readLong();
        this.dataTypeUuidLSB = buffer.readLong();
        this.errorResult = buffer.readBoolean();
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
        PacketDispatcher.sendTo(new GetServerDataMessage(dataTypeUuid, type, dataCompound, fileError), (EntityPlayerMP) playerIn);
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
