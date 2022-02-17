package aeronicamc.mods.mxtune.caps.stages;

import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.StageAreaSyncMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerStageAreas implements IServerStageAreas
{
    private static final Logger LOGGER = LogManager.getLogger(ServerStageAreas.class);
    private WeakReference<World> levelRef;
    private final List<StageAreaData> stageAreas = new CopyOnWriteArrayList<>();

    private int someInt;

    ServerStageAreas() { /* NOP */ }

    ServerStageAreas(World level)
    {
        this();
        ReferenceQueue<World> worldReferenceQueue = new ReferenceQueue<>();
        this.levelRef = new WeakReference<>(level, worldReferenceQueue);
    }

    StageAreaData stageAreaTest01() {
        return new StageAreaData(
                new BlockPos(173,70,-441),
                new BlockPos(177,72,-445),
                new BlockPos(176,70,-441),
                new BlockPos(173,70,-441),
                "Stage#: 01",
                UUID.randomUUID());
    }

    StageAreaData stageAreaTest02() {
        return new StageAreaData(
                new BlockPos(169,73,-446),
                new BlockPos(164,69,-451),
                new BlockPos(167,69,-448),
                new BlockPos(166,69,-450),
                "Stage#: 02",
                UUID.randomUUID());
    }

    @Override
    public void test()
    {
        stageAreas.add(stageAreaTest01());
        stageAreas.add(stageAreaTest02());
        sync();
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
        World level = levelRef.get();
        if (level != null && !level.isClientSide())
            {
                PacketDispatcher.sendToDimension(new StageAreaSyncMessage(serializeNBT()), level.dimension());
                LOGGER.debug("sync: someInt {}, stageAreas {}", this.someInt, this.stageAreas.size());
            }
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
