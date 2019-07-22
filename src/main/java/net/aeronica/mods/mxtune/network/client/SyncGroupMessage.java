/*
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

import net.aeronica.mods.mxtune.managers.GroupHelper;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class SyncGroupMessage extends AbstractClientMessage<SyncGroupMessage>
{
    private String groups;
    private String members;

    @SuppressWarnings("unused")
    public SyncGroupMessage() {/* Required by the PacketDispatcher */}

    public SyncGroupMessage(String groups, String members)
    {
        this.groups = groups;
        this.members = members;
    }

    @Override
    protected void decode(PacketBuffer buffer)
    {
        this.groups = ByteBufUtils.readUTF8String(buffer);
        this.members = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void encode(PacketBuffer buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, groups);
        ByteBufUtils.writeUTF8String(buffer, members);
    }

    @Override
    public void handle(PlayerEntity player, Side side)
    {
        GroupHelper.setClientGroups(groups);
        GroupHelper.setClientMembers(members);
        GroupHelper.setGroupsMembers(members);
    }
}
