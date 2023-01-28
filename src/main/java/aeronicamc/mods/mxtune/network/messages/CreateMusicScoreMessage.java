package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.init.ModSoundEvents;
import aeronicamc.mods.mxtune.items.MusicPaperItem;
import aeronicamc.mods.mxtune.network.NetworkSerializedHelper;
import aeronicamc.mods.mxtune.util.Misc;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.function.Supplier;

public class CreateMusicScoreMessage extends AbstractMessage<CreateMusicScoreMessage>
{
    private static final Logger LOGGER = LogManager.getLogger(CreateMusicScoreMessage.class);
    private String musicTitle;
    private String musicText;
    private String extraText;
    private int[] partInstrumentIndexes;
    private boolean error;

    public CreateMusicScoreMessage() { /* NOP */ }

    public CreateMusicScoreMessage(final String musicTitle, final String extraText, final String musicText, final int[] partInstrumentIndexes)
    {
        this.musicTitle = musicTitle;
        this.extraText = extraText;
        this.musicText = musicText;
        this.partInstrumentIndexes = partInstrumentIndexes;
        this.error = false;
    }

    public CreateMusicScoreMessage(final String musicTitle, final String extraText, final String musicText, final int[] partInstrumentIndexes, final boolean error)
    {
        this(musicTitle, extraText, musicText, partInstrumentIndexes);
        this.error = error;
    }

    @Override
    public CreateMusicScoreMessage decode(final PacketBuffer buffer)
    {
        final String musicTitle = buffer.readUtf();
        final String extraText = buffer.readUtf();
        String musicText = "";
        boolean error = false;
        try
        {
            musicText = (String) NetworkSerializedHelper.readSerializedObject(buffer);
        } catch (IOException e)
        {
            LOGGER.error("unable to decode string", e);
            error = true;
        }
        final int[] partInstrumentIndexes = buffer.readVarIntArray(16);
        LOGGER.debug(String.format("%s, buffer.readLongArray: %d", musicTitle, partInstrumentIndexes.length));
        return new CreateMusicScoreMessage(musicTitle, extraText, musicText != null ? musicText : "", partInstrumentIndexes , error);
    }

    @Override
    public void encode(final CreateMusicScoreMessage message, final PacketBuffer buffer)
    {
        buffer.writeUtf(message.musicTitle);
        buffer.writeUtf(message.extraText);
        try
        {
            NetworkSerializedHelper.writeSerializedObject(buffer, message.musicText);
        } catch (IOException e)
        {
            LOGGER.warn("unable to encode string", e);
            buffer.writeUtf("");
            buffer.writeBoolean(true);
            return;
        }
        buffer.writeVarIntArray(message.partInstrumentIndexes);
        buffer.writeBoolean(message.error);
    }

    @Override
    public void handle(final CreateMusicScoreMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->{

                ServerPlayerEntity sPlayer = ctx.get().getSender();
                assert sPlayer != null;
                if (message.error)
                {
                    LOGGER.warn("network error");
                    sPlayer.sendMessage(new TranslationTextComponent("errors.mxtune.sheet_music_write_failure"), sPlayer.getUUID());
                    Misc.audiblePingPlayer(sPlayer, ModSoundEvents.FAILURE.get());

                } else if (!sPlayer.getMainHandItem().isEmpty() && sPlayer.getMainHandItem().getItem() instanceof MusicPaperItem)
                    {
                        ItemStack musicScore = new ItemStack(ModItems.MUSIC_SCORE.get());
                        if (SheetMusicHelper.writeIMusic(musicScore, message.musicTitle, (message.extraText), message.musicText, message.partInstrumentIndexes))
                        {
                            sPlayer.inventory.removeItem(sPlayer.inventory.selected, message.partInstrumentIndexes.length);
                            if (!sPlayer.inventory.add(musicScore.copy()))
                                sPlayer.drop(musicScore, true, false);
                        }
                        else
                        {
                            sPlayer.sendMessage(new TranslationTextComponent("errors.mxtune.mml_server_side_validation_failure"), sPlayer.getUUID());
                            Misc.audiblePingPlayer(sPlayer, ModSoundEvents.FAILURE.get());
                        }
                    }
                });
        ctx.get().setPacketHandled(true);
    }
}
