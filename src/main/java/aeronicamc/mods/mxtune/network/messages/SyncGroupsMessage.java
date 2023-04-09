package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.managers.Group;
import aeronicamc.mods.mxtune.managers.GroupClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class SyncGroupsMessage extends AbstractMessage<SyncGroupsMessage>
{
    private Map<Integer, Group> groupMap = new HashMap<>();

    public SyncGroupsMessage() { /* NOP */ }

    public SyncGroupsMessage(Map<Integer, Group> groups)
    {
        groupMap = groups;
    }

    @Override
    public void encode(SyncGroupsMessage message, PacketBuffer buffer)
    {
        final int groupCount = message.groupMap.size();
        buffer.writeInt(groupCount);
        message.groupMap.forEach( (key, group) -> {
                final int groupId = group.getGroupId();
                buffer.writeInt(groupId);
                final int leader = group.getLeader();
                buffer.writeInt(leader);
                final int playId = group.getPlayId();
                buffer.writeInt(playId);
                final int duration = group.getMaxDuration();
                buffer.writeInt(duration);
                final Group.Mode mode = group.getMode();
                buffer.writeEnum(mode);
                final Set<Integer> members = group.getMembers();
                final int memberCount = members.size();
                buffer.writeVarInt(memberCount);
                members.forEach(buffer::writeInt);
                          });
    }

    @Override
    public SyncGroupsMessage decode(PacketBuffer buffer)
    {
        final Map<Integer, Group> groupMap = new HashMap<>();
        final int groupCount = buffer.readInt();
        for (int g = 0 ; g < groupCount; g++)
        {
            final int groupId = buffer.readInt();
            final int leader = buffer.readInt();
            final int playId = buffer.readInt();
            final int duration = buffer.readInt();
            final Group.Mode mode = buffer.readEnum(Group.Mode.class);
            Group group = new Group(groupId, leader);
            group.setPlayId(playId);
            group.setPartDuration(duration);
            group.setMode(mode);
            final int memberCount = buffer.readVarInt();
            for (int m = 0; m < memberCount; m++)
            {
                final int member = buffer.readInt();
                group.getMembers().add(member);
            }
            groupMap.put(group.getGroupId(), group);
        }
        return new SyncGroupsMessage(groupMap);
    }

    @Override
    public void handle(SyncGroupsMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
        {
            ctx.get().enqueueWork(() ->
                                  {
                                      final Optional<World> optionalWorld = LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide());
                                      optionalWorld.ifPresent(world -> GroupClient.setGroups(message.groupMap));
                                  });
        }
        ctx.get().setPacketHandled(true);
    }
}
