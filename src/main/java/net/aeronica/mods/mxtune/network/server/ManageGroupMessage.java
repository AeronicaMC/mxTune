/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
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
package net.aeronica.mods.mxtune.network.server;

import net.aeronica.mods.mxtune.managers.GroupHelper;
import net.aeronica.mods.mxtune.managers.GroupManager;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class ManageGroupMessage extends AbstractServerMessage<ManageGroupMessage>
{
    private int operation;
    private Integer groupID;
    private Integer memberID;

    @SuppressWarnings("unused")
    public ManageGroupMessage() {/* Required by the PacketDispatcher */}

    // TODO: Queue the group/member information server side, and only send a true/false or cmd/index as appropriate.
    // Store in the MusicOptions capability. The management commands will need to be rethought and
    // actions based on server side state. Client should only send action requests with owner validation.
    // i.e. only the leader can make those changes. The GroupManger does some validation already but it needs to be
    // reviewed.
    public ManageGroupMessage(int operation, Integer groupID, Integer memberName)
    {
        this.operation = operation;
        this.groupID = groupID;
        this.memberID = memberName;
    }

    @Override
    protected void decode(PacketBuffer buffer)
    {
        operation = buffer.readInt();
        groupID = buffer.readInt();
        memberID = buffer.readInt();
    }

    @Override
    protected void encode(PacketBuffer buffer)
    {
        if (groupID == null) groupID = -1;
        buffer.writeInt(operation);
        buffer.writeInt(groupID);
        buffer.writeInt(memberID);
    }

    @Override
    public void handle(PlayerEntity player, Side side)
    {
        switch (operation)
        {
        case GroupHelper.GROUP_ADD:
            GroupManager.addGroup(memberID);
            break;
        case GroupHelper.MEMBER_ADD:
            GroupManager.addMember(groupID, memberID);
            break;
        case GroupHelper.MEMBER_REMOVE:
            GroupManager.removeMember(memberID);
            break;
        case GroupHelper.MEMBER_PROMOTE:
            GroupManager.setLeader(memberID);
            break;
        default:
        }
    }
}
