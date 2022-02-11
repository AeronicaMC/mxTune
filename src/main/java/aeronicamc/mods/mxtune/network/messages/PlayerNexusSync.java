package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.caps.player.PlayerNexusProvider;
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

public class PlayerNexusSync extends AbstractMessage<PlayerNexusSync>
{
    private static final Logger LOGGER = LogManager.getLogger(PlayerNexusSync.class);
    private final int playId;
    private final int entityId;

    public PlayerNexusSync()
    {
        this.playId = -1;
        this.entityId = Integer.MIN_VALUE;
    }

    public PlayerNexusSync(final int playId, final int entityId)
    {
        this.playId = playId;
        this.entityId = entityId;
    }

    @Override
    public PlayerNexusSync decode(final PacketBuffer buffer)
    {
        final int playId = buffer.readInt();
        final int entityId = buffer.readInt();
        LOGGER.debug("playId: {}", playId);
        return new PlayerNexusSync(playId, entityId);
    }

    @Override
    public void encode(final PlayerNexusSync message, final PacketBuffer buffer)
    {
        LOGGER.debug("playId: {}", message.playId);
        buffer.writeInt(message.playId);
        buffer.writeInt(message.entityId);
    }

    @Override
    public void handle(final PlayerNexusSync message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
            ctx.get().enqueueWork(() ->
                {
                    final Optional<World> optionalWorld = LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide());
                    optionalWorld.ifPresent(
                            world -> {
                                final LivingEntity livingEntity = (LivingEntity) world.getEntity(message.entityId);
                                if (livingEntity != null)
                                    PlayerNexusProvider.getPerPlayerOptions(livingEntity).ifPresent(
                                            playerOptions -> playerOptions.setPlayId(message.playId));
                    });
                });
        ctx.get().setPacketHandled(true);
    }
}