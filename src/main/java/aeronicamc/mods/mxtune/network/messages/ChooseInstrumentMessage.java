package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.inventory.MultiInstContainer;
import aeronicamc.mods.mxtune.util.IInstrument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ChooseInstrumentMessage extends AbstractMessage<ChooseInstrumentMessage>
{
    int signals;

    public ChooseInstrumentMessage()
    {
        this.signals = 0;
    }

    public ChooseInstrumentMessage(int index)
    {
        this.signals = index;
    }

    @Override
    public void encode(ChooseInstrumentMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.signals);
    }

    @Override
    public ChooseInstrumentMessage decode(PacketBuffer buffer)
    {
        final int index = buffer.readInt();
        return new ChooseInstrumentMessage(index);
    }

    @Override
    public void handle(ChooseInstrumentMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->{

                ServerPlayerEntity sPlayer = ctx.get().getSender();
                if (sPlayer != null && !sPlayer.getMainHandItem().isEmpty() && sPlayer.containerMenu != null && sPlayer.getMainHandItem().getItem() instanceof IInstrument)
                {
                    ((MultiInstContainer) sPlayer.containerMenu).setSignals(message.signals);
                }
            });
        ctx.get().setPacketHandled(true);
    }
}
