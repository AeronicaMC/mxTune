package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.managers.GroupManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SendPinEntryMessage extends AbstractMessage<SendPinEntryMessage>
{
    private String pin;
    public SendPinEntryMessage() { /* NOP */ }

    public SendPinEntryMessage(String pin)
    {
        this.pin = pin;
    }

    @Override
    public void encode(SendPinEntryMessage message, PacketBuffer buffer)
    {
        buffer.writeUtf(message.pin);
    }

    @Override
    public SendPinEntryMessage decode(PacketBuffer buffer)
    {
        final String pinDecode = buffer.readUtf();
        return new SendPinEntryMessage(pinDecode);
    }

    @Override
    public void handle(SendPinEntryMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
        {
            ServerPlayerEntity serverPlayer = ctx.get().getSender();
            if (serverPlayer != null)
                ctx.get().enqueueWork(() -> GroupManager.handlePin(serverPlayer, message.pin));
        }
        ctx.get().setPacketHandled(true);
    }
}
