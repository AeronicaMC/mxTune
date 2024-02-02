package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.network.MultiPacketStringManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class StringPartMessage extends AbstractMessage<StringPartMessage>
{
    UUID partStringId;

    // part
    int packetIndex;
    String partString;

    public StringPartMessage() { /* Required by the packetDispatcher */ }

    public StringPartMessage(UUID partStringId, int packetIndex, String partString)
    {
        this.partStringId = partStringId;
        this.packetIndex = packetIndex;
        this.partString = partString;
    }

    @Override
    public void encode(StringPartMessage message, PacketBuffer buffer)
    {
        buffer.writeUUID(message.partStringId);
        buffer.writeInt(message.packetIndex);
        buffer.writeUtf(message.partString);
    }

    @Override
    public StringPartMessage decode(PacketBuffer buffer)
    {
        final UUID serialObjectId = buffer.readUUID();
        final int packetId = buffer.readInt();
        final String bytes = buffer.readUtf();
        return new StringPartMessage(serialObjectId, packetId, bytes);
    }

    @Override
    public void handle(StringPartMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->
                    MultiPacketStringManager
                            .addPacket(new MultiPacketStringManager
                                    .StringPartPacket(message.partStringId, message.packetIndex, message.partString)));
        ctx.get().setPacketHandled(true);
    }
}
