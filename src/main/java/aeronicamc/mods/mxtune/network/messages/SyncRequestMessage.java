package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.caps.player.IPlayerNexus;
import aeronicamc.mods.mxtune.caps.player.PlayerNexusProvider;
import aeronicamc.mods.mxtune.caps.venues.IMusicVenues;
import aeronicamc.mods.mxtune.caps.venues.MusicVenueProvider;
import aeronicamc.mods.mxtune.managers.GroupManager;
import aeronicamc.mods.mxtune.managers.PlayManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncRequestMessage extends AbstractMessage<SyncRequestMessage>
{
    public SyncRequestMessage() { /* NOP */ }

    @Override
    public void encode(SyncRequestMessage message, PacketBuffer buffer) { /* NOP */ }

    @Override
    public SyncRequestMessage decode(PacketBuffer buffer) {
        return new SyncRequestMessage();
    }

    @Override
    public void handle(SyncRequestMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->{

                ServerPlayerEntity sPlayer = ctx.get().getSender();
                if (sPlayer != null)
                {
                    PlayerNexusProvider.getNexus(sPlayer).ifPresent(IPlayerNexus::sync);
                    MusicVenueProvider.getMusicVenues(sPlayer.level).ifPresent(IMusicVenues::sync);
                    PlayManager.sendMusicTo(sPlayer, sPlayer);
                    GroupManager.syncTo(sPlayer);
                }
            });
        ctx.get().setPacketHandled(true);
    }
}
