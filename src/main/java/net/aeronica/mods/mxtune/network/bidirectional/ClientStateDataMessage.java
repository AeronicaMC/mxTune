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
import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.status.ClientCSDMonitor;
import net.aeronica.mods.mxtune.status.ClientStateData;
import net.aeronica.mods.mxtune.status.ServerCSDManager;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class ClientStateDataMessage extends AbstractMessage<ClientStateDataMessage>
{
    private ClientStateData csd = new ClientStateData();
    private long serverIdUuidMSB = 0;
    private long serverIdUuidLSB = 0;

    public ClientStateDataMessage() { /* Required by the PacketDispatcher */ }

    public ClientStateDataMessage(UUID serverID)
    {
        serverIdUuidMSB = serverID.getMostSignificantBits();
        serverIdUuidLSB = serverID.getLeastSignificantBits();
    }

    public ClientStateDataMessage(ClientStateData csd)
    {
        this.csd = csd;
    }
    
    @Override
    protected void read(PacketBuffer buffer)
    {
        this.csd = readCSD(buffer);
        this.serverIdUuidMSB = buffer.readLong();
        this.serverIdUuidLSB = buffer.readLong();
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        writeCSD(buffer, csd);
        buffer.writeLong(serverIdUuidMSB);
        buffer.writeLong(serverIdUuidLSB);
    }

    @Override
    public void process(PlayerEntity player, Side side)
    {
        if (side.isClient())
        {
            handleClientSide(player);
        } else
        {
            handleServerSide(player);
        }
    }

    private void handleClientSide(PlayerEntity playerIn)
    {
        ClientCSDMonitor.collectAndSend();
        MIDISystemUtil.onPlayerLoggedInModStatus(playerIn);
        ClientFileManager.setCachedServerID(serverIdUuidMSB, serverIdUuidLSB);
        PacketDispatcher.sendToServer(new GetBaseDataListsMessage(CallBackManager.register(ClientFileManager.INSTANCE), RecordType.PLAY_LIST));
    }

    private void handleServerSide(PlayerEntity playerIn)
    {
        ServerCSDManager.updateState(playerIn, csd);
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
