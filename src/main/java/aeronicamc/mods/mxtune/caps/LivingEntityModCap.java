package aeronicamc.mods.mxtune.caps;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.network.LivingEntityModCapSync;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class LivingEntityModCap implements ILivingEntityModCap
{
    private int playId = PlayIdSupplier.INVALID;
    private final LivingEntity entity;

    LivingEntityModCap(@Nullable final LivingEntity entity)
    {
        this.entity = entity;
    }

    @Override
    public void setPlayId(int playId)
    {
        this.playId = playId;
        synchronise();
    }

    @Override
    public int getPlayId()
    {
        return playId;
    }


    @Override
    public void synchronise()
    {
        if (entity == null) return;
        World world = entity.level;
        if (world.isClientSide) return;
        RegistryKey<World> dimension = world.dimension();
        PacketDispatcher.sendToDimension(new LivingEntityModCapSync(playId), dimension);
    }
}
