/*
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
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
package net.aeronica.mods.mxtune.network.server;

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.aeronica.mods.mxtune.options.ClassifiedPlayer;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MusicOptionsMessage extends AbstractServerMessage<MusicOptionsMessage>
{
    
    private int muteOption;
    private List<ClassifiedPlayer> blackList;
    private List<ClassifiedPlayer> whiteList;
    private byte[] byteBuffer = null;
    private boolean canProcess = true;

    @SuppressWarnings("unused")
    public MusicOptionsMessage() {/* Required by the PacketDispatcher */}
    
    public MusicOptionsMessage(int muteOption, List<ClassifiedPlayer> blackList, List<ClassifiedPlayer> whiteList)
    {
        this.muteOption = muteOption;
        this.blackList = blackList;
        this.whiteList = whiteList;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void decode(PacketBuffer buffer)
    {
        this.muteOption = buffer.readInt();
        try {
            // Deserialize data object from a byte array
            byteBuffer = buffer.readByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer) ;
            ObjectInputStream in = new ObjectInputStream(bis);
            whiteList =  (ArrayList<ClassifiedPlayer>) in.readObject();
            in.close();

            // Deserialize data object from a byte array
            byteBuffer = buffer.readByteArray();
            bis = new ByteArrayInputStream(byteBuffer);
            in = new ObjectInputStream(bis);
            blackList =  (ArrayList<ClassifiedPlayer>) in.readObject();
            in.close();
        } catch (ClassNotFoundException | IOException e)
        {
            canProcess = false;
            ModLogger.error(e);
        }

    }

    @Override
    @SuppressWarnings("all")
    protected void encode(PacketBuffer buffer)
    {
        buffer.writeInt(this.muteOption);
        try {
            // Serialize data object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject((Serializable) whiteList);
            out.close();

            // Get the bytes of the serialized object
            byteBuffer = bos.toByteArray();
            buffer.writeByteArray(byteBuffer);

            // Serialize data object to a byte array
            bos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bos);
            out.writeObject((Serializable) blackList);
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
        if (canProcess)
        {
            MusicOptionsUtil.setMuteOption(player, muteOption);
            MusicOptionsUtil.setBlackList(player, blackList);
            MusicOptionsUtil.setWhiteList(player, whiteList);
        }
    }
}
