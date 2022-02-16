package aeronicamc.mods.mxtune.caps.player;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.PlayerNexusSync;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class PlayerNexus implements IPlayerNexus
{
    private static final Logger LOGGER = LogManager.getLogger(PlayerNexus.class);
    private int playId = PlayIdSupplier.PlayType.INVALID.getAsInt();
    private WeakReference<PlayerEntity> playerWeakRef;

    PlayerNexus() { /* NOP */ }

    PlayerNexus(@Nullable final PlayerEntity playerEntity)
    {
        ReferenceQueue<PlayerEntity> playerEntityReferenceQueue = new ReferenceQueue<>();
        this.playerWeakRef = new WeakReference<>(playerEntity, playerEntityReferenceQueue);
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

    @Nullable
    @Override
    public INBT serializeNBT()
    {
        return IntNBT.valueOf(getPlayId());
    }

    @Override
    public void deserializeNBT(@Nullable INBT nbt)
    {
        if (nbt != null)
            setPlayId(((IntNBT) nbt).getAsInt());
    }

    @Override
    public void sync()
    {
        PlayerEntity playerEntity = playerWeakRef.get();
        if (playerEntity != null && !playerEntity.level.isClientSide())
        {
            PacketDispatcher.sendToDimension(new PlayerNexusSync(playId, playerEntity.getId()), playerEntity.level.dimension());
            LOGGER.debug("sync: playId {}, entityId {}", playId, playerEntity.getId());
        }
    }
}
