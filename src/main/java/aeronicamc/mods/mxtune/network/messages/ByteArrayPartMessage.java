package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.network.MultiPacketSerializedObjectManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ByteArrayPartMessage extends AbstractMessage<ByteArrayPartMessage>
{
    UUID serialObjectId;

    // part
    int packetId;
    byte[] bytes;

    public ByteArrayPartMessage() { /* Required by the packetDispatcher */ }

    public ByteArrayPartMessage(UUID serialObjectId, int packetId, byte[] bytes)
    {
        this.serialObjectId = serialObjectId;
        this.packetId = packetId;
        this.bytes = bytes;
    }

    @Override
    public void encode(ByteArrayPartMessage message, PacketBuffer buffer)
    {
        buffer.writeUUID(message.serialObjectId);
        buffer.writeInt(message.packetId);
        buffer.writeByteArray(message.bytes);
    }

    @Override
    public ByteArrayPartMessage decode(PacketBuffer buffer)
    {
        final UUID serialObjectId = buffer.readUUID();
        final int packetId = buffer.readInt();
        final byte[] bytes = buffer.readByteArray();
        return new ByteArrayPartMessage(serialObjectId, packetId, bytes);
    }

    @Override
    public void handle(ByteArrayPartMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->{
                MultiPacketSerializedObjectManager.addPacket(new MultiPacketSerializedObjectManager.SerializedObjectPacket(message.serialObjectId, message.packetId, message.bytes));
            });
        ctx.get().setPacketHandled(true);
    }
}
