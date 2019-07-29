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
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ManageGroupMessage
{
    private final int operation;
    private final int groupID;
    private final int memberID;

    // TODO: Queue the group/member information server side, and only send a true/false or cmd/index as appropriate.
    // Store in the MusicOptions capability. The management commands will need to be rethought and
    // actions based on server side state. Client should only send action requests with owner validation.
    // i.e. only the leader can make those changes. The GroupManger does some validation already but it needs to be
    // reviewed.
    public ManageGroupMessage(final int operation, final int groupID, final int memberName)
    {
        this.operation = operation;
        this.groupID = groupID;
        this.memberID = memberName;
    }

    public static ManageGroupMessage decode(final PacketBuffer buffer)
    {
        int operation = buffer.readInt();
        int groupID = buffer.readInt();
        int memberID = buffer.readInt();
        return new ManageGroupMessage(operation, groupID, memberID);
    }

    public static void encode(final ManageGroupMessage message, final PacketBuffer buffer)
    {
        buffer.writeInt(message.operation);
        buffer.writeInt(message.groupID);
        buffer.writeInt(message.memberID);
    }

    public static void handle(final ManageGroupMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(()->{
                switch (message.operation)
                {
                    case GroupHelper.GROUP_ADD:
                        GroupManager.addGroup(message.memberID);
                        break;
                    case GroupHelper.MEMBER_ADD:
                        GroupManager.addMember(message.groupID, message.memberID);
                        break;
                    case GroupHelper.MEMBER_REMOVE:
                        GroupManager.removeMember(message.memberID);
                        break;
                    case GroupHelper.MEMBER_PROMOTE:
                        GroupManager.setLeader(message.memberID);
                        break;
                    default:
                }
            });
        ctx.get().setPacketHandled(true);
    }
}
