package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.caps.LivingEntityModCapProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class LivingEntityModCapSync extends AbstractMessage<LivingEntityModCapSync>
{
    private static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);
    private int playId;
    private int entityId;

    public LivingEntityModCapSync() { /* NOP */ }

    public LivingEntityModCapSync(int playId, int entityId)
    {
        this.playId = playId;
        this.entityId = entityId;
    }

    @Override
    public LivingEntityModCapSync decode(final PacketBuffer buffer)
    {
        final int playId = buffer.readInt();
        final int entityId = buffer.readInt();
        LOGGER.debug("playId: {}", playId);
        return new LivingEntityModCapSync(playId, entityId);
    }

    @Override
    public void encode(final LivingEntityModCapSync message, final PacketBuffer buffer)
    {
        LOGGER.debug("playId: {}", message.playId);
        buffer.writeInt(message.playId);
        buffer.writeInt(message.entityId);
    }

    @Override
    public void handle(final LivingEntityModCapSync message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT)
            ctx.get().enqueueWork(() ->
                {
                    World level = Minecraft.getInstance().level;
                    if (level != null)
                    {
                        final LivingEntity livingEntity = (LivingEntity) level.getEntity(message.entityId);
                        if (livingEntity != null)
                            LivingEntityModCapProvider.getLivingEntityModCap(livingEntity).ifPresent(
                                livingEntityCap -> livingEntityCap.setPlayId(message.playId));
                    }
                });
        ctx.get().setPacketHandled(true);
    }
}
