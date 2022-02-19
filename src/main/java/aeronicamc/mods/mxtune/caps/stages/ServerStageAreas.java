package aeronicamc.mods.mxtune.caps.stages;

import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.StageAreaSyncMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerStageAreas implements IServerStageAreas
{
    private static final Logger LOGGER = LogManager.getLogger(ServerStageAreas.class);
    private World level;
    private final List<StageAreaData> stageAreas = new CopyOnWriteArrayList<>();

    private int someInt;

    ServerStageAreas() { /* NOP */ }

    ServerStageAreas(World level)
    {
        this();
        this.level = level;
    }

    private Optional<World> getLevel()
    {
        return Optional.of(level);
    }

    @Override
    public void addArea(StageAreaData stageAreaData)
    {
        stageAreas.add(stageAreaData);
    }

    @Override
    public List<StageAreaData> getStageAreas()
    {
        return stageAreas;
    }

    @Override
    public int getInt()
    {
        return someInt;
    }

    @Override
    public void setInt(Integer someInt)
    {
        this.someInt = someInt;
        sync();
    }

    public void sync()
    {
        getLevel().ifPresent(level -> {
            if (!level.isClientSide())
            {
                PacketDispatcher.sendToDimension(new StageAreaSyncMessage(serializeNBT()), level.dimension());
                LOGGER.debug("sync: someInt {}, stageAreas {}", this.someInt, this.stageAreas.size());
            }
        });
    }

    @Override
    public INBT serializeNBT()
    {
        final CompoundNBT cNbt = new CompoundNBT();
        ListNBT listnbt = new ListNBT();
        stageAreas.forEach(stageArea -> NBTDynamicOps.INSTANCE.withEncoder(StageAreaData.CODEC)
                .apply(stageArea).result().ifPresent(listnbt::add));

        cNbt.put("stageAreas", listnbt);
        cNbt.putInt("someInt", getInt());
        return cNbt;
    }

    @Override
    public void deserializeNBT(@Nullable INBT nbt)
    {
        CompoundNBT cNbt = ((CompoundNBT) nbt);
        if (cNbt != null && cNbt.contains("stageAreas"))
        {
            ListNBT listnbt = cNbt.getList("stageAreas", Constants.NBT.TAG_COMPOUND);
            stageAreas.clear();
            listnbt.forEach(stageAreaNBT -> NBTDynamicOps.INSTANCE.withParser(StageAreaData.CODEC)
                    .apply(stageAreaNBT).result().ifPresent(stageAreas::add));

            setInt(cNbt.getInt("someInt"));
        }
    }
}
