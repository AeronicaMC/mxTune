package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.network.PacketBufferLongUtfHelper;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayMusicMessage extends AbstractMessage<PlayMusicMessage>
{
    private final PacketBufferLongUtfHelper stringHelper = new PacketBufferLongUtfHelper();
    private int playId = PlayIdSupplier.INVALID;
    private int secondsElapsed;
    private int duration;
    private int entityId;
    private String musicText = "";
    private String dateTimeServer = "";

    public PlayMusicMessage() { /* NOP */ }

    public PlayMusicMessage(int playId, String dateTimeServer, int duration, int secondsElapsed, int entityId, String musicText)
    {
        this.playId = playId;
        this.dateTimeServer = dateTimeServer;
        this.duration = duration;
        this.secondsElapsed = secondsElapsed;
        this.entityId = entityId;
        this.musicText = musicText;
    }

    @Override
    public void encode(PlayMusicMessage message, PacketBuffer buffer)
    {
        buffer.writeVarInt(message.playId);
        buffer.writeUtf(message.dateTimeServer);
        buffer.writeVarInt(message.duration);
        buffer.writeVarInt(message.secondsElapsed);
        buffer.writeVarInt(message.entityId);
        stringHelper.writeLongUtf(buffer, message.musicText);
    }

    @Override
    public PlayMusicMessage decode(PacketBuffer buffer)
    {
        final int playIdDecode = buffer.readVarInt();
        final String dateTimeServerDecode = buffer.readUtf();
        final int durationDecode = buffer.readVarInt();
        final int secondsToSkipDecode = buffer.readVarInt();
        final int entityIdDecode = buffer.readVarInt();
        final String mml = stringHelper.readLongUtf(buffer);
        return new PlayMusicMessage(playIdDecode, dateTimeServerDecode, durationDecode, secondsToSkipDecode, entityIdDecode, mml);
    }

    @Override
    public void handle(PlayMusicMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
        {
            ctx.get().enqueueWork(() ->
                    ClientAudio.play(message.duration, message.secondsElapsed, message.playId, message.entityId, message.musicText));
        }
        ctx.get().setPacketHandled(true);
    }
}
