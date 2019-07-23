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

import net.aeronica.mods.mxtune.managers.ClientPlayManager;
import net.aeronica.mods.mxtune.network.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ResetClientPlayEngine implements IMessage
{
    public ResetClientPlayEngine() { /* NOP */ }

    public static ResetClientPlayEngine decode(final PacketBuffer buffer)
    {
        return new ResetClientPlayEngine();
    }

    public static void encode(final ResetClientPlayEngine message, final PacketBuffer buffer)
    {
        // NOP
    }

    public static void handle(final ResetClientPlayEngine message, final Supplier<NetworkEvent.Context> ctx)
    {
        // TODO: Make a more defined process that's more like full client audio chain reset.
        if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
            ctx.get().enqueueWork(() -> new Thread(() ->
                {
                    try
                    {
                        Thread.sleep(1000);
                    } catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                    }
                    ClientPlayManager.reset();
                }).start());
        ctx.get().setPacketHandled(true);
    }
}
