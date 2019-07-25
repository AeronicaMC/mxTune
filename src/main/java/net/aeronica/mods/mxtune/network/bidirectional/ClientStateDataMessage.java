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
package net.aeronica.mods.mxtune.network.bidirectional;

import net.aeronica.mods.mxtune.managers.ClientFileManager;
import net.aeronica.mods.mxtune.managers.records.RecordType;
import net.aeronica.mods.mxtune.network.IMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.aeronica.mods.mxtune.status.ClientStateData;
import net.aeronica.mods.mxtune.status.ServerCSDManager;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientStateDataMessage implements IMessage
{
    private final ClientStateData csd;
    private final long serverIdUuidMSB;
    private final long serverIdUuidLSB;

    public ClientStateDataMessage(final UUID serverID)
    {
        this.csd = new ClientStateData();
        this.serverIdUuidMSB = serverID.getMostSignificantBits();
        this.serverIdUuidLSB = serverID.getLeastSignificantBits();
    }

    public ClientStateDataMessage(final ClientStateData csd)
    {
        this.csd = csd;
        this.serverIdUuidMSB = 0;
        this.serverIdUuidLSB = 0;
    }

    private ClientStateDataMessage(final ClientStateData csd, final UUID serverID)
    {
        this.csd = csd;
        this.serverIdUuidMSB = serverID.getMostSignificantBits();
        this.serverIdUuidLSB = serverID.getLeastSignificantBits();
    }

    public static ClientStateDataMessage decode(PacketBuffer buffer)
    {
        ClientStateData csd = readCSD(buffer);
        long serverIdUuidMSB = buffer.readLong();
        long serverIdUuidLSB = buffer.readLong();
        UUID uuid = new UUID(serverIdUuidMSB, serverIdUuidLSB);
        return new ClientStateDataMessage(csd, uuid);
    }

    public static void encode(final ClientStateDataMessage message, final PacketBuffer buffer)
    {
        writeCSD(buffer, message.csd);
        buffer.writeLong(message.serverIdUuidMSB);
        buffer.writeLong(message.serverIdUuidLSB);
    }

    public static void handle(final ClientStateDataMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ServerPlayerEntity player = ctx.get().getSender();
        if (ctx.get().getDirection().getReceptionSide().isClient())
        {
            handleClientSide(player, message, ctx);
        } else
        {
            handleServerSide(player, message, ctx);
        }
    }

    private static void handleClientSide(final ServerPlayerEntity playerIn, final ClientStateDataMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(()->
            {
              ClientCSDMonitor.collectAndSend();
              MIDISystemUtil.onPlayerLoggedInModStatus(playerIn);
              ClientFileManager.setCachedServerID(message.serverIdUuidMSB, message.serverIdUuidLSB);
              PacketDispatcher.sendToServer(new GetBaseDataListsMessage(CallBackManager.register(ClientFileManager.INSTANCE), RecordType.PLAY_LIST));
            });
        ctx.get().setPacketHandled(true);
    }

    private static void handleServerSide(final ServerPlayerEntity playerIn, final ClientStateDataMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(()->ServerCSDManager.updateState(playerIn, message.csd));
        ctx.get().setPacketHandled(true);
    }

    public static ClientStateData readCSD(PacketBuffer buffer)
    {
        return new ClientStateData(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }
    
    public static void writeCSD(PacketBuffer buffer, ClientStateData csd)
    {
        buffer.writeBoolean(csd.isMidiAvailable());
        buffer.writeBoolean(csd.isMasterVolumeOn());
        buffer.writeBoolean(csd.isMxtuneVolumeOn());
    }
}
