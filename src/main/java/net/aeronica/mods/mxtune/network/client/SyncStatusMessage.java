/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
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
package net.aeronica.mods.mxtune.network.client;

import java.io.IOException;

import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class SyncStatusMessage extends AbstractClientMessage<SyncStatusMessage>
{

    String clientPlayStatuses;
    String playIDMembers;
    String activePlayIDs;

    public SyncStatusMessage() {}

    public SyncStatusMessage(String clientPlayStatuses, String playIDMembers, String activePlayIDs)
    {
        this.clientPlayStatuses = clientPlayStatuses;
        this.playIDMembers = playIDMembers;
        this.activePlayIDs = activePlayIDs;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        clientPlayStatuses = ByteBufUtils.readUTF8String(buffer);
        playIDMembers = ByteBufUtils.readUTF8String(buffer);
        activePlayIDs = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeUTF8String(buffer, clientPlayStatuses);
        ByteBufUtils.writeUTF8String(buffer, playIDMembers);
        ByteBufUtils.writeUTF8String(buffer, activePlayIDs);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        GROUPS.setClientPlayStatuses(clientPlayStatuses);
        GROUPS.setPlayIDMembers(playIDMembers);
        GROUPS.setActivePlayIDs(activePlayIDs);
    }

}
