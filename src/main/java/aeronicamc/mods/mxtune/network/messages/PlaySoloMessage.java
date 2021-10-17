package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlaySoloMessage extends AbstractMessage<PlaySoloMessage>
{
    private int playId = PlayIdSupplier.INVALID;
    private int entityId;
    private String musicText = "";

    public PlaySoloMessage() { /* NOP */ }

    public PlaySoloMessage(int playId, int entityId, String musicText)
    {
        this.playId = playId;
        this.entityId = entityId;
        this.musicText = musicText;
    }

    @Override
    public void encode(PlaySoloMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.playId);
        buffer.writeInt(message.entityId);
        buffer.writeUtf(message.musicText);
    }

    @Override
    public PlaySoloMessage decode(PacketBuffer buffer)
    {
        final int playId = buffer.readInt();
        final int entityId = buffer.readInt();
        final String mml = buffer.readUtf();
        return new PlaySoloMessage(playId, entityId, mml);
    }

    @Override
    public void handle(PlaySoloMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
        {
            ctx.get().enqueueWork(() ->
                ClientAudio.play(message.playId, message.entityId, message.musicText));
        }
        ctx.get().setPacketHandled(true);
    }
}
