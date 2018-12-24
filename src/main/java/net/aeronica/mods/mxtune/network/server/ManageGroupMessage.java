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
package net.aeronica.mods.mxtune.network.server;

import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.groups.GroupManager;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class ManageGroupMessage extends AbstractServerMessage<ManageGroupMessage>
{
    private int operation;
    private Integer groupID;
    private Integer memberID;

    @SuppressWarnings("unused")
    public ManageGroupMessage() {/* Required by the PacketDispatcher */}

    public ManageGroupMessage(int operation, Integer groupID, Integer memberName)
    {
        this.operation = operation;
        this.groupID = groupID;
        this.memberID = memberName;
    }

    @Override
    protected void read(PacketBuffer buffer)
    {
        operation = buffer.readInt();
        groupID = buffer.readInt();
        memberID = buffer.readInt();
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        if (groupID == null) groupID = -1;
        buffer.writeInt(operation);
        buffer.writeInt(groupID);
        buffer.writeInt(memberID);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        switch (operation)
        {
        case GROUPS.GROUP_ADD:
            GroupManager.addGroup(memberID);
            break;
        case GROUPS.MEMBER_ADD:
            GroupManager.addMember(groupID, memberID);
            break;
        case GROUPS.MEMBER_REMOVE:
            GroupManager.removeMember(memberID);
            break;
        case GROUPS.MEMBER_PROMOTE:
            GroupManager.setLeader(memberID);
            break;
        default:
        }
    }
}
