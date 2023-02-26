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

public class CreateSheetMusicMessage extends AbstractMessage<CreateSheetMusicMessage>
{
    private static final Logger LOGGER = LogManager.getLogger(CreateSheetMusicMessage.class);
    private String musicTitle;
    private String extraText;
    private String musicText;
    private String instrumentId;
    private boolean error;

    public CreateSheetMusicMessage() { /* NOP */ }

    public CreateSheetMusicMessage(final String musicTitle, final String extraText, final String musicText, String instrumentId)
    {
        this.musicTitle = musicTitle;
        this.extraText = extraText;
        this.musicText = musicText;
        this.instrumentId = instrumentId;
        this.error = false;
    }

    public CreateSheetMusicMessage(final String musicTitle, String extraText, final String musicText, String instrumentId, final boolean error)
    {
        this.musicTitle = musicTitle;
        this.extraText = extraText;
        this.musicText = musicText;
        this.instrumentId = instrumentId;
        this.error = error;
    }

    @Override
    public CreateSheetMusicMessage decode(final PacketBuffer buffer)
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
        final String instrumentId = buffer.readUtf();
        return new CreateSheetMusicMessage(musicTitle, extraText, musicText != null ? musicText : "", instrumentId, error);
    }

    @Override
    public void encode(final CreateSheetMusicMessage message, final PacketBuffer buffer)
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
        buffer.writeUtf(message.instrumentId);
        buffer.writeBoolean(message.error);
    }

    @Override
    public void handle(final CreateSheetMusicMessage message, final Supplier<NetworkEvent.Context> ctx)
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
                        ItemStack sheetMusic = new ItemStack(ModItems.SHEET_MUSIC.get());
                        final String[] instrumentIds = new String[1];
                        instrumentIds[0] = message.instrumentId;
                        if (SheetMusicHelper.writeIMusic(sheetMusic, message.musicTitle, message.extraText, message.musicText, instrumentIds))
                        {
                            sPlayer.inventory.removeItem(sPlayer.inventory.selected, 1);
                            if (!sPlayer.inventory.add(sheetMusic.copy()))
                                sPlayer.drop(sheetMusic, true, false);
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
