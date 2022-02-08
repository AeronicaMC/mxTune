package aeronicamc.mods.mxtune.caps.player;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.PerPlayerOptionsSync;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class PerPlayerOptions implements IPerPlayerOptions
{
    private int playId = PlayIdSupplier.PlayType.INVALID.getAsInt();
    private final LivingEntity entity;

    PerPlayerOptions(@Nullable final LivingEntity entity)
    {
        this.entity = entity;
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
    public void deserializeNBT(INBT nbt)
    {
        setPlayId(((IntNBT) nbt).getAsInt());
    }

    @Override
    public void sync()
    {
        if (entity == null) return;
        World world = entity.level;
        if (world.isClientSide) return;
        RegistryKey<World> dimension = world.dimension();
        PacketDispatcher.sendToDimension(new PerPlayerOptionsSync(playId, entity.getId()), dimension);
    }
}
