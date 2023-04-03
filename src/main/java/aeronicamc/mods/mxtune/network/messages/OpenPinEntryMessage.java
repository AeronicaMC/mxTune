package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.gui.Handler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenPinEntryMessage extends AbstractMessage<OpenPinEntryMessage>
{
    private int groupId;
    public OpenPinEntryMessage() { /* NOP */ }

    public OpenPinEntryMessage(int groupId)
    {
        this.groupId = groupId;
    }

    @Override
    public void encode(OpenPinEntryMessage message, PacketBuffer buffer)
    {
        buffer.writeVarInt(message.groupId);
    }

    @Override
    public OpenPinEntryMessage decode(PacketBuffer buffer)
    {
        final int groupId = buffer.readVarInt();
        return new OpenPinEntryMessage(groupId);
    }

    @Override
    public void handle(OpenPinEntryMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(() -> Handler.OpenGuiPinScreen(message.groupId));
        ctx.get().setPacketHandled(true);
    }
}
