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
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class SyncGroupMessage extends AbstractClientMessage<SyncGroupMessage>
{

    private String groups, members;

    public SyncGroupMessage() {this.groups = this.members = new String();}

    public SyncGroupMessage(String groups, String members)
    {
        this.groups = groups;
        this.members = members;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        this.groups = ByteBufUtils.readUTF8String(buffer);
        this.members = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeUTF8String(buffer, groups);
        ByteBufUtils.writeUTF8String(buffer, members);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        /** Client side - build - replace the groups and members hashes */
        if (side.isClient())
        {
            ModLogger.debug("+++Server PacketGroupSync doAction on behalf of " + player.getDisplayName() + " +++");
            ModLogger.debug("  +++ Groups:  " + groups);
            ModLogger.debug("  +++ members: " + members);

            GROUPS.clientGroups = GROUPS.splitToHashMap(groups);
            GROUPS.clientMembers = GROUPS.splitToHashMap(members);
        }
    }
}
