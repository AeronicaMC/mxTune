package aeronicamc.mods.mxtune.caps.player;

import aeronicamc.mods.mxtune.caps.venues.EntityVenueState;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.PlayerNexusSync;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;

public class PlayerNexus implements IPlayerNexus
{
    private static final Logger LOGGER = LogManager.getLogger(PlayerNexus.class);
    private int playId = PlayIdSupplier.INVALID;
    private PlayerEntity player;
    private EntityVenueState evs = EntityVenueState.INVALID;

    PlayerNexus() { /* NOP */ }

    PlayerNexus(@Nullable final PlayerEntity playerEntity)
    {
        player = playerEntity;
    }

    private Optional<PlayerEntity> getPlayer()
    {
        return Optional.of(player);
    }

    @Override
    public void setPlayId(int playId)
    {
        this.playId = playId;
        sync();
    }

    @Override
    public int getPlayId()
    {
        return playId;
    }

    @Override
    public EntityVenueState getEntityVenueState()
    {
        return evs;
    }

    @Override
    public void setEntityVenueState(EntityVenueState evs)
    {
        this.evs = evs;
    }

    @Nullable
    @Override
    public INBT serializeNBT()
    {
        return IntNBT.valueOf(getPlayId());
    }

    @Override
    public void deserializeNBT(@Nullable INBT nbt)
    {
        Optional<INBT> oNbt = Optional.ofNullable(nbt);
        oNbt.ifPresent(this::accept);
    }

    private void accept(INBT pNbt)
    {
        setPlayId(((IntNBT) pNbt).getAsInt());
    }

    @Override
    public void sync()
    {
        getPlayer().ifPresent( pPlayer -> {
            if (!pPlayer.level.isClientSide())
            {
                PacketDispatcher.sendToDimension(new PlayerNexusSync(playId, pPlayer.getId()), pPlayer.level.dimension());
            }
        });
    }
}
