package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.managers.GroupClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class SyncGroupMemberState extends AbstractMessage<SyncGroupMemberState>
{
    private Map<Integer, Integer> memberState = new HashMap<>();

    public SyncGroupMemberState() { /* NOP */ }

    public SyncGroupMemberState(Map<Integer, Integer> memberState)
    {
        this.memberState = memberState;
    }

    @Override
    public void encode(SyncGroupMemberState message, PacketBuffer buffer)
    {
        final int memberCount = message.memberState.size();
        buffer.writeInt(memberCount);
        message.memberState.forEach((member, state) -> {
            buffer.writeInt(member);
            buffer.writeVarInt(state);
        });
    }

    @Override
    public SyncGroupMemberState decode(PacketBuffer buffer)
    {
        final Map<Integer, Integer> memberState = new HashMap<>();
        final int memberCount = buffer.readInt();
        for (int member = 0 ; member < memberCount; member++)
        {
            int memberId = buffer.readInt();
            int state = buffer.readVarInt();
            memberState.put(memberId, state);
        }
        return new SyncGroupMemberState(memberState);
    }

    @Override
    public void handle(SyncGroupMemberState message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
        {
            ctx.get().enqueueWork(() ->
                                  {
                                      final Optional<World> optionalWorld = LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide());
                                      optionalWorld.ifPresent(world -> GroupClient.setMemberState(message.memberState));
                                  });
        }
        ctx.get().setPacketHandled(true);
    }
}
