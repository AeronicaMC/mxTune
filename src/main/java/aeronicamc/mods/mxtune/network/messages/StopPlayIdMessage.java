package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class StopPlayIdMessage extends AbstractMessage<StopPlayIdMessage>
{
    private int playId = PlayIdSupplier.PlayType.INVALID.getAsInt();

    public StopPlayIdMessage() { /* NOP */ }

    public StopPlayIdMessage(int playId)
    {
        this.playId = playId;
    }

    @Override
    public void encode(StopPlayIdMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.playId);
    }

    @Override
    public StopPlayIdMessage decode(PacketBuffer buffer)
    {
        final int playId = buffer.readInt();
        return new StopPlayIdMessage(playId);
    }

    @Override
    public void handle(StopPlayIdMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
        {
            ctx.get().enqueueWork(() ->
                ClientAudio.fadeOut(message.playId, 3));
        }
        ctx.get().setPacketHandled(true);
    }
}
