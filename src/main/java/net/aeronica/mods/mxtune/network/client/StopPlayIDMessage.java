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
package net.aeronica.mods.mxtune.network.client;

import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class StopPlayIDMessage
{
    private final int playID;

    public StopPlayIDMessage(int playID)
    {
        this.playID = playID;
    }
    

    public static StopPlayIDMessage decode(final PacketBuffer buffer)
    {
        int playID = buffer.readInt();
        return new StopPlayIDMessage(playID);
    }


    public static void encode(final StopPlayIDMessage message, final PacketBuffer buffer)
    {
        buffer.writeInt(message.playID);
    }


    public static void handle(final StopPlayIDMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(()->ClientAudio.queueAudioDataRemoval(message.playID));
        ctx.get().setPacketHandled(true);
    }
}
