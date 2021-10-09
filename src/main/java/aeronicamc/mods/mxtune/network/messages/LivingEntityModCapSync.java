package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.caps.LivingEntityModCapProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.Supplier;

public class LivingEntityModCapSync extends AbstractMessage<LivingEntityModCapSync>
{
    private static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);
    int playId = -1;

    public LivingEntityModCapSync() { /* NOP */ }

    public LivingEntityModCapSync(int playId)
    {
        this.playId = playId;
    }

    @Override
    public LivingEntityModCapSync decode(final PacketBuffer buffer)
    {
        final int playId = buffer.readInt();
        LOGGER.debug("playId: {}", playId);
        return new LivingEntityModCapSync(playId);
    }

    @Override
    public void encode(final LivingEntityModCapSync message, final PacketBuffer buffer)
    {
        LOGGER.debug("playId: {}", message.playId);
        buffer.writeInt(message.playId);
    }

    @Override
    public void handle(final LivingEntityModCapSync message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
            ctx.get().enqueueWork(() ->
                {
                    final Optional<World> optionalWorld = LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide());
                    final LivingEntity livingEntity = ctx.get().getSender();
                    if (livingEntity != null)
                        optionalWorld.ifPresent(world -> LivingEntityModCapProvider.getLivingEntityModCap(livingEntity).ifPresent(livingEntityCap ->
                            livingEntityCap.setPlayId(message.playId)));
                });
        ctx.get().setPacketHandled(true);
    }
}