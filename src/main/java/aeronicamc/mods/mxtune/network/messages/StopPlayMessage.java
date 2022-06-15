package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class StopPlayMessage extends AbstractMessage<StopPlayMessage>
{
    private int playId = PlayIdSupplier.INVALID;
    private boolean stopAll;

    public StopPlayMessage()
    {
        this.stopAll = true;
    }

    public StopPlayMessage(int playId)
    {
        this.playId = playId;
        this.stopAll = false;
    }

    @Override
    public void encode(StopPlayMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.playId);
    }

    @Override
    public StopPlayMessage decode(PacketBuffer buffer)
    {
        final int playId = buffer.readInt();
        return new StopPlayMessage(playId);
    }

    @Override
    public void handle(StopPlayMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
        {
            ctx.get().enqueueWork(() -> {
                if (message.stopAll)
                    ClientAudio.stopAll();
                else
                    ClientAudio.fadeOut(message.playId, 1);
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
