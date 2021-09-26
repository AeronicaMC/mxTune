package aeronicamc.mods.mxtune.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class AbstractMessage<T extends  AbstractMessage<T>>
{
    public abstract void encode(T message, PacketBuffer buffer);

    public abstract T decode(PacketBuffer buffer);

    public abstract void handle(T message, Supplier<NetworkEvent.Context> ctx);
}
