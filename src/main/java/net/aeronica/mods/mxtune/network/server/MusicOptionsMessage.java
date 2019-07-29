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

import net.aeronica.mods.mxtune.caps.player.ClassifiedPlayer;
import net.aeronica.mods.mxtune.caps.player.MusicOptionsUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MusicOptionsMessage
{
    
    private final int muteOption;
    private final List<ClassifiedPlayer> blackList;
    private final List<ClassifiedPlayer> whiteList;
    
    public MusicOptionsMessage(final int muteOption, final List<ClassifiedPlayer> blackList, final List<ClassifiedPlayer> whiteList)
    {
        this.muteOption = muteOption;
        this.blackList = blackList;
        this.whiteList = whiteList;
    }

    @SuppressWarnings("unchecked")
    public static MusicOptionsMessage decode(PacketBuffer buffer)
    {
        List<ClassifiedPlayer> whiteListIn = new ArrayList<>();
        List<ClassifiedPlayer> blackListIn = new ArrayList<>();
        int muteOption = buffer.readInt();
        try {
            // Deserialize data object from a byte array
            byte[] byteBuffer = buffer.readByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer) ;
            ObjectInputStream in = new ObjectInputStream(bis);
            whiteListIn = (ArrayList<ClassifiedPlayer>) in.readObject();
            in.close();

            // Deserialize data object from a byte array
            byteBuffer = buffer.readByteArray();
            bis = new ByteArrayInputStream(byteBuffer);
            in = new ObjectInputStream(bis);
            blackListIn = (ArrayList<ClassifiedPlayer>) in.readObject();
            in.close();
        } catch (ClassNotFoundException | IOException e)
        {
            ModLogger.error(e);
        }
        return new MusicOptionsMessage(muteOption, blackListIn, whiteListIn);
    }

    @SuppressWarnings("all")
    public static void encode(final MusicOptionsMessage message, final PacketBuffer buffer)
    {
        buffer.writeInt(message.muteOption);
        try {
            // Serialize data object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject((Serializable) message.whiteList);
            out.close();

            // Get the bytes of the serialized object
            byte[] byteBuffer = bos.toByteArray();
            buffer.writeByteArray(byteBuffer);

            // Serialize data object to a byte array
            bos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bos);
            out.writeObject((Serializable) message.blackList);
            out.close();

            // Get the bytes of the serialized object
            byteBuffer = bos.toByteArray();
            buffer.writeByteArray(byteBuffer);
        } catch (IOException e) {
            ModLogger.error(e);
        }
    }

    public static void handle(final MusicOptionsMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(()->{
                MusicOptionsUtil.setMuteOption(player, message.muteOption);
                MusicOptionsUtil.setBlackList(player, message.blackList);
                MusicOptionsUtil.setWhiteList(player, message.whiteList);
            });
        ctx.get().setPacketHandled(true);
    }
}
