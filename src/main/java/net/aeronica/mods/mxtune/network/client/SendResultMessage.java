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

import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class SendResultMessage
{
    private final boolean errorResult;
    private final ITextComponent component;

    public SendResultMessage(final ITextComponent component, final Boolean errorResult)
    {
        this.component = component;
        this.errorResult = errorResult;
    }

    public static SendResultMessage decode(final PacketBuffer buffer)
    {
        ITextComponent component = ITextComponent.Serializer.fromJson(buffer.readString(32767));
        boolean errorResult = buffer.readBoolean();
        return new SendResultMessage(Objects.requireNonNull(component), errorResult);
    }

    public static void encode(final SendResultMessage message, final PacketBuffer buffer)
    {
        buffer.writeString(ITextComponent.Serializer.toJson(message.component));
        buffer.writeBoolean(message.errorResult);
    }

    public static void handle(final SendResultMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(()->ModLogger.debug("Error: %s, error %s", message.component.getFormattedText(), message.errorResult));
        ctx.get().setPacketHandled(true);
    }
}
