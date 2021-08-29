package aeronicamc.mods.mxtune.network;

import aeronicamc.mods.mxtune.caps.LivingEntityModCapProvider;
import aeronicamc.mods.mxtune.Reference;
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

public class LivingEntityModCapSync
{
    private static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
    private final int playId;

    public LivingEntityModCapSync(int playId)
    {
        this.playId = playId;
    }

    public static LivingEntityModCapSync decode(final PacketBuffer buffer)
    {
        final int playId = buffer.readInt();
        LOGGER.debug("LivingEntityModCapSync#decode playId: {}", playId);
        return new LivingEntityModCapSync(playId);
    }

    public static void encode(final LivingEntityModCapSync message, final PacketBuffer buffer)
    {
        buffer.writeInt(message.playId);
    }

    public static void handle(final LivingEntityModCapSync message, final Supplier<NetworkEvent.Context> ctx)
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
