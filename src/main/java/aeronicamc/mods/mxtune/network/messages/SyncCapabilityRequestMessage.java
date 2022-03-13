package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.caps.player.IPlayerNexus;
import aeronicamc.mods.mxtune.caps.player.PlayerNexusProvider;
import aeronicamc.mods.mxtune.caps.venues.IMusicVenues;
import aeronicamc.mods.mxtune.caps.venues.MusicVenueProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncCapabilityRequestMessage extends AbstractMessage<SyncCapabilityRequestMessage>
{
    public SyncCapabilityRequestMessage() { /* NOP */ }

    @Override
    public void encode(SyncCapabilityRequestMessage message, PacketBuffer buffer) { /* NOP */ }

    @Override
    public SyncCapabilityRequestMessage decode(PacketBuffer buffer) {
        return new SyncCapabilityRequestMessage();
    }

    @Override
    public void handle(SyncCapabilityRequestMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->{

                ServerPlayerEntity sPlayer = ctx.get().getSender();
                if (sPlayer != null)
                {
                    PlayerNexusProvider.getNexus(sPlayer).ifPresent(IPlayerNexus::sync);
                    MusicVenueProvider.getMusicVenues(sPlayer.level).ifPresent(IMusicVenues::sync);
                }
            });
        ctx.get().setPacketHandled(true);
    }
}
