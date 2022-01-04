package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.network.NetworkLongUtfHelper;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class PlayBlockMusicMessage extends AbstractMessage<PlayBlockMusicMessage>
{
    private static final Logger LOGGER = LogManager.getLogger(PlayBlockMusicMessage.class);
    private final NetworkLongUtfHelper stringHelper = new NetworkLongUtfHelper();
    private int playId = PlayIdSupplier.PlayType.INVALID.getAsInt();
    private BlockPos blockPos;
    private String musicText = "";

    public PlayBlockMusicMessage() { /* NOP */ }

    public PlayBlockMusicMessage(int playId, BlockPos blockPos, String musicText)
    {
        this.playId = playId;
        this.blockPos = blockPos;
        this.musicText = musicText;
    }

    @Override
    public void encode(PlayBlockMusicMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.playId);
        buffer.writeBlockPos(message.blockPos);
        stringHelper.writeLongUtf(buffer, message.musicText);
    }

    @Override
    public PlayBlockMusicMessage decode(PacketBuffer buffer)
    {
        final int playId = buffer.readInt();
        final BlockPos blockPos = buffer.readBlockPos();
        final String musicText = stringHelper.readLongUtf(buffer);

        return new PlayBlockMusicMessage(playId, blockPos, musicText);
    }

    @Override
    public void handle(PlayBlockMusicMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
        {
            LOGGER.debug("PlayBlockMusicMessage playId: {} blockPos: {} hasMML: {}",
                         message.playId, message.blockPos, String.format("%s", message.musicText.contains("MML@")));
            ctx.get().enqueueWork(() ->
                ClientAudio.play(message.playId, message.blockPos, message.musicText));
        }
        ctx.get().setPacketHandled(true);
    }
}
