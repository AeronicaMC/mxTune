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
package net.aeronica.mods.mxtune.network.server;

import java.io.IOException;

import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.groups.GroupManager;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class ManageGroupMessage extends AbstractServerMessage<ManageGroupMessage>
{
    String operation;
    String groupID;
    String memberName;

    public ManageGroupMessage() {}

    public ManageGroupMessage(String operation, String groupID, String memberName)
    {
        this.operation = operation;
        this.groupID = groupID;
        this.memberName = memberName;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        operation = ByteBufUtils.readUTF8String(buffer);
        groupID = ByteBufUtils.readUTF8String(buffer);
        memberName = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeUTF8String(buffer, operation);
        ByteBufUtils.writeUTF8String(buffer, groupID);
        ByteBufUtils.writeUTF8String(buffer, memberName);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (side == Side.CLIENT) return;
        System.out.println("+++ GroupManage doAction: " + operation);
        switch (GROUPS.valueOf(operation))
        {
        case GROUP_ADD:
            GroupManager.addGroup(memberName);
            break;
        case MEMBER_ADD:
            GroupManager.addMember(groupID, memberName);
            break;
        case MEMBER_REMOVE:
            GroupManager.removeMember(memberName);
            break;
        case MEMBER_PROMOTE:
            GroupManager.setLeader(memberName);
            break;
        default:
        }
    }
}
