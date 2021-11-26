package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.items.MusicPaperItem;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CreateSheetMusicMessage extends AbstractMessage<CreateSheetMusicMessage>
{
    private String musicTitle;
    private String musicText;

    public CreateSheetMusicMessage() { /* NOP */ }

    public CreateSheetMusicMessage(final String musicTitle, final String musicText)
    {
        this.musicTitle = musicTitle;
        this.musicText = musicText;
    }

    @Override
    public CreateSheetMusicMessage decode(final PacketBuffer buffer)
    {
        musicTitle = buffer.readUtf();
        musicText = buffer.readUtf();

        return new CreateSheetMusicMessage(musicTitle, musicText);
    }

    @Override
    public void encode(final CreateSheetMusicMessage message, final PacketBuffer buffer)
    {
        buffer.writeUtf(message.musicTitle);
        buffer.writeUtf(message.musicText);
    }

    @Override
    public void handle(final CreateSheetMusicMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->{

                ServerPlayerEntity sPlayer = ctx.get().getSender();
                assert sPlayer != null;
                if (!sPlayer.getMainHandItem().isEmpty() && sPlayer.getMainHandItem().getItem() instanceof MusicPaperItem)
                    {
                        ItemStack sheetMusic = new ItemStack(ModItems.SHEET_MUSIC.get());
                        if (SheetMusicHelper.writeSheetMusic(sheetMusic, musicTitle, musicText))
                        {
                            sPlayer.inventory.removeItem(sPlayer.inventory.selected, 1);
                            if (!sPlayer.inventory.add(sheetMusic.copy()))
                                sPlayer.drop(sheetMusic, true, false);
                        }
                        else
                        {
                            sPlayer.sendMessage(new TranslationTextComponent("mxtune.status.mml_server_side_validation_failure"), sPlayer.getUUID());
                            // Miscellus.audiblePingPlayer(sPlayer, SoundEvents.BLOCK_ANVIL_PLACE);
                        }
                    }
                });
        ctx.get().setPacketHandled(true);
    }
}
