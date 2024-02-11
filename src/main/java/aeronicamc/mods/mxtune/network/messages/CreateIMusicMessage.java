package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.init.ModSoundEvents;
import aeronicamc.mods.mxtune.items.MusicPaperItem;
import aeronicamc.mods.mxtune.network.MultiPacketStringHelper;
import aeronicamc.mods.mxtune.util.Misc;
import aeronicamc.mods.mxtune.util.MusicType;
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
import java.util.Arrays;
import java.util.function.Supplier;

public class CreateIMusicMessage extends AbstractMessage<CreateIMusicMessage>
{
    private static final Logger LOGGER = LogManager.getLogger(CreateIMusicMessage.class);
    private String musicTitle;
    private String musicText;
    private byte[] extraData;
    private String[] partInstrumentIds;
    private MusicType musicType;
    private boolean error;

    public CreateIMusicMessage() { /* NOP */ }

    public CreateIMusicMessage(final String musicTitle, final byte[] extraData, final String musicText, final String[] partInstrumentIds, MusicType musicType)
    {
        this.musicTitle = musicTitle;
        this.extraData = extraData;
        this.musicText = musicText;
        this.partInstrumentIds = partInstrumentIds;
        this.musicType = musicType;
        this.error = false;
    }

    public CreateIMusicMessage(final String musicTitle, final byte[] extraData, final String musicText, final String[] partInstrumentIds, MusicType musicType, final boolean error)
    {
        this(musicTitle, extraData, musicText, partInstrumentIds, musicType);
        this.error = error;
    }

    @Override
    public CreateIMusicMessage decode(final PacketBuffer buffer)
    {
        final String musicTitle = buffer.readUtf();
        final byte[] extraData = buffer.readByteArray();
        String musicText = "";
        musicText = MultiPacketStringHelper.readLongString(buffer);
        final String[] partInstrumentIds = nbtToStringArray(buffer.readNbt());
        final MusicType musicType = buffer.readEnum(MusicType.class);
        return new CreateIMusicMessage(musicTitle, extraData, musicText != null ? musicText : "", partInstrumentIds, musicType, musicText == null);
    }

    @Override
    public void encode(final CreateIMusicMessage message, final PacketBuffer buffer)
    {
        buffer.writeUtf(message.musicTitle);
        buffer.writeByteArray(message.extraData);
        MultiPacketStringHelper.writeLongString(buffer, message.musicText);
        buffer.writeNbt(stringArrayToNBT(message.partInstrumentIds));
        buffer.writeEnum(message.musicType);
        buffer.writeBoolean(message.error);
    }

    @Override
    public void handle(final CreateIMusicMessage message, final Supplier<NetworkEvent.Context> ctx)
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
                        final ItemStack musicItem;
                        if (message.musicType.equals(MusicType.SCORE))
                            musicItem = new ItemStack(ModItems.MUSIC_SCORE.get());
                        else
                            musicItem = new ItemStack(ModItems.SHEET_MUSIC.get());
                        if (SheetMusicHelper.writeIMusic(musicItem, message.musicTitle, (message.extraData), message.musicText, message.partInstrumentIds, message.musicType, sPlayer.getUUID(), sPlayer.getDisplayName().getString()))
                        {
                            sPlayer.inventory.removeItem(sPlayer.inventory.selected, message.partInstrumentIds.length);
                            if (!sPlayer.inventory.add(musicItem.copy()))
                                sPlayer.drop(musicItem, true, false);
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
        int[] index = { 0 };
        nbt.putInt("length", strings.length);
        Arrays.stream(strings).sequential().forEach( string -> nbt.putString(String.format("%d", index[0]++), string));
        return nbt;
    }

    private static String[] nbtToStringArray(@Nullable CompoundNBT nbt)
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
