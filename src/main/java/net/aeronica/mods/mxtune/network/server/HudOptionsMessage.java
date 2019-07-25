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

import net.aeronica.mods.mxtune.caps.player.MusicOptionsUtil;
import net.aeronica.mods.mxtune.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class HudOptionsMessage implements IMessage
{
    private final int positionHud;
    private final boolean disableHud;
    private final float sizeHud;
    
    public HudOptionsMessage(final int positionHud, final boolean disableHud, final float sizeHud)
    {
        this.positionHud = positionHud;
        this.disableHud = disableHud;
        this.sizeHud = sizeHud;
    }

    public static HudOptionsMessage decode(final PacketBuffer buffer)
    {
        int positionHud = buffer.readInt();
        boolean disableHud = buffer.readBoolean();
        float sizeHud = buffer.readFloat();
        return new HudOptionsMessage(positionHud, disableHud, sizeHud);
    }

    public static void encode(final HudOptionsMessage message, final PacketBuffer buffer)
    {
        buffer.writeInt(message.positionHud);
        buffer.writeBoolean(message.disableHud);
        buffer.writeFloat(message.sizeHud);
    }

    public static void handle(final HudOptionsMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(()->
                MusicOptionsUtil.setHudOptions(player, message.disableHud, message.positionHud, message.sizeHud));
    }
}
