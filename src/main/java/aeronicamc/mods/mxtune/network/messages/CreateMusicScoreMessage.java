package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.init.ModSoundEvents;
import aeronicamc.mods.mxtune.items.MusicPaperItem;
import aeronicamc.mods.mxtune.network.NetworkSerializedHelper;
import aeronicamc.mods.mxtune.util.Misc;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;

public class CreateMusicScoreMessage extends AbstractMessage<CreateMusicScoreMessage>
{
    private static final Logger LOGGER = LogManager.getLogger(CreateMusicScoreMessage.class);
    private String musicTitle;
    private String musicText;
    private byte[] extraData;
    private String[] partInstrumentIds;
    private boolean error;

    public CreateMusicScoreMessage() { /* NOP */ }

    public CreateMusicScoreMessage(final String musicTitle, final byte[] extraData, final String musicText, final String[] partInstrumentIds)
    {
        this.musicTitle = musicTitle;
        this.extraData = extraData;
        this.musicText = musicText;
        this.partInstrumentIds = partInstrumentIds;
        this.error = false;
    }

    public CreateMusicScoreMessage(final String musicTitle, final byte[] extraData, final String musicText, final String[] partInstrumentIds, final boolean error)
    {
        this(musicTitle, extraData, musicText, partInstrumentIds);
        this.error = error;
    }

    @Override
    public CreateMusicScoreMessage decode(final PacketBuffer buffer)
    {
        final String musicTitle = buffer.readUtf();
        final byte[] extraData = buffer.readByteArray();
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
        final String[] partInstrumentIds = NBT2StringArray(buffer.readNbt());
        LOGGER.debug(String.format("%s, buffer.readLongArray: %d", musicTitle, partInstrumentIds.length));
        return new CreateMusicScoreMessage(musicTitle, extraData, musicText != null ? musicText : "", partInstrumentIds , error);
    }

    @Override
    public void encode(final CreateMusicScoreMessage message, final PacketBuffer buffer)
    {
        buffer.writeUtf(message.musicTitle);
        buffer.writeByteArray(message.extraData);
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
        buffer.writeNbt(stringArrayToNBT(message.partInstrumentIds));
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
                        if (SheetMusicHelper.writeIMusic(musicScore, message.musicTitle, (message.extraData), message.musicText, message.partInstrumentIds))
                        {
                            sPlayer.inventory.removeItem(sPlayer.inventory.selected, message.partInstrumentIds.length);
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

    private static CompoundNBT stringArrayToNBT(String[] strings)
    {
        CompoundNBT nbt = new CompoundNBT();
        int[] index = new int[1];
        nbt.putInt("length", strings.length);
        Arrays.stream(strings).sequential().forEach( string -> {
            nbt.putString(String.format("%d", index[0]++), string);
        });
        return nbt;
    }

    private static String[] NBT2StringArray(@Nullable CompoundNBT nbt)
    {
        if (nbt != null)
        {
            int length = nbt.getInt("length");
            String[] strings = new String[length];
            for (int i = 0; i < length; i++)
                strings[i] = nbt.getString(String.format("%d", i));
            return strings;
        }
        else
            return new String[0];
    }
}
