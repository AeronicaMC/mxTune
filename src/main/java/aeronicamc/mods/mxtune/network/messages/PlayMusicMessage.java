package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.network.NetworkLongUtfHelper;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Supplier;

public class PlayMusicMessage extends AbstractMessage<PlayMusicMessage>
{
    private static final Logger LOGGER = LogManager.getLogger(PlayMusicMessage.class);
    private final NetworkLongUtfHelper stringHelper = new NetworkLongUtfHelper();
    private int playId = PlayIdSupplier.INVALID;
    private int secondsToSkip;
    private int duration;
    private int entityId;
    private String musicText = "";
    private String dateTimeServer = "";

    public PlayMusicMessage() { /* NOP */ }

    public PlayMusicMessage(int playId, String dateTimeServer, int duration, int secondsToSkip, int entityId, String musicText)
    {
        this.playId = playId;
        this.dateTimeServer = dateTimeServer;
        this.duration = duration;
        this.secondsToSkip = secondsToSkip;
        this.entityId = entityId;
        this.musicText = musicText;
    }

    @Override
    public void encode(PlayMusicMessage message, PacketBuffer buffer)
    {
        buffer.writeVarInt(message.playId);
        buffer.writeUtf(message.dateTimeServer);
        buffer.writeVarInt(message.duration);
        buffer.writeVarInt(message.secondsToSkip);
        buffer.writeVarInt(message.entityId);
        stringHelper.writeLongUtf(buffer, message.musicText);
    }

    @Override
    public PlayMusicMessage decode(PacketBuffer buffer)
    {
        final int playId = buffer.readVarInt();
        final String dateTimeServer = buffer.readUtf();
        final int duration = buffer.readVarInt();
        final int secondsToSkip = buffer.readVarInt();
        final int entityId = buffer.readVarInt();
        final String mml = stringHelper.readLongUtf(buffer);
        return new PlayMusicMessage(playId, dateTimeServer, duration, secondsToSkip, entityId, mml);
    }

    @Override
    public void handle(PlayMusicMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
        {
            ctx.get().enqueueWork(() -> {
                assert Minecraft.getInstance().player != null;
                Entity sender = Minecraft.getInstance().player.level.getEntity(message.entityId);
                String senderName = sender != null ? sender.getDisplayName().getString() : "--Server--";
                LocalDateTime dateTimeClient = LocalDateTime.now(ZoneId.of("GMT0"));
                LocalDateTime dateTimeServer = message.secondsToSkip > 0 ? LocalDateTime.parse(message.dateTimeServer) : dateTimeClient;
                long netTransitTime = Duration.between(dateTimeServer, dateTimeClient).toMillis();
                LOGGER.info("In transit: {} ms, From: {} to: {}", netTransitTime, senderName, Minecraft.getInstance().player.getDisplayName().getString());
                ClientAudio.play(message.duration, message.secondsToSkip, netTransitTime, message.playId, message.entityId, message.musicText);
        });
        }
        ctx.get().setPacketHandled(true);
    }
}
