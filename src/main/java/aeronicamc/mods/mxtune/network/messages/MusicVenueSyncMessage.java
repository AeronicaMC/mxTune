package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.caps.venues.MusicVenueProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class MusicVenueSyncMessage extends AbstractMessage<MusicVenueSyncMessage>
{
    private INBT stageAreaNbt = new CompoundNBT();

    public MusicVenueSyncMessage() { /* NOP */ }

    public MusicVenueSyncMessage(@Nullable final INBT stageAreaNbt)
    {
        this.stageAreaNbt = stageAreaNbt;
    }

    @Override
    public MusicVenueSyncMessage decode(final PacketBuffer buffer)
    {
        return new MusicVenueSyncMessage(buffer.readNbt());
    }

    @Override
    public void encode(final MusicVenueSyncMessage message, final PacketBuffer buffer)
    {
        buffer.writeNbt((CompoundNBT) message.stageAreaNbt);
    }

    @Override
    public void handle(final MusicVenueSyncMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
        {
            ctx.get().enqueueWork(
                    () -> {
                        final Optional<World> optionalWorld = LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide());
                        optionalWorld.ifPresent(
                                world -> MusicVenueProvider.getMusicVenues(world)
                                        .ifPresent(
                                                stageAreas -> stageAreas.deserializeNBT(message.stageAreaNbt)));
                    });
        }
        ctx.get().setPacketHandled(true);
    }
}
