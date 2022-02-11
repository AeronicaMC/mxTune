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
import net.minecraftforge.fml.common.thread.EffectiveSide;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
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

    StageAreaData genTest() {
        return new StageAreaData(
                                 new BlockPos(173,70,-441),
                                 new BlockPos(177,72,-445),
                                 new BlockPos(176,70,-441),
                                 new BlockPos(173,70,-441), "Stage#: " + RandomUtils.nextInt(), UUID.randomUUID());
    }

    @Override
    public void test()
    {
        genTest().getAreaAABB().inflate(0.002);
        stageAreas.add(genTest());
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
        World level = this.levelRef.get();
        if (EffectiveSide.get().isServer() && level != null)
            PacketDispatcher.sendToDimension(new StageAreaSyncMessage(Objects.requireNonNull(serializeNBT())), level.dimension());

        LOGGER.debug("{someInt {}, stageAreas {}", this.someInt, this.stageAreas);
    }

    @Override
    public INBT serializeNBT()
    {
        final CompoundNBT compoundNBT = new CompoundNBT();
        ListNBT listnbt = new ListNBT();
        stageAreas.forEach(stageArea -> NBTDynamicOps.INSTANCE.withEncoder(StageAreaData.CODEC)
                .apply(stageArea).result().ifPresent(listnbt::add));

        compoundNBT.put("stageAreas", listnbt);
        compoundNBT.putInt("someInt", getInt());
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(INBT nbt)
    {
        CompoundNBT compoundNBT = ((CompoundNBT) nbt);
        if (nbt != null)
        {
            if (compoundNBT.contains("stageAreas"))
            {
                ListNBT listnbt = compoundNBT.getList("stageAreas", Constants.NBT.TAG_COMPOUND);
                stageAreas.clear();
                listnbt.forEach(stageAreaNBT -> NBTDynamicOps.INSTANCE.withParser(StageAreaData.CODEC)
                        .apply(stageAreaNBT).result().ifPresent(stageAreas::add));

                setInt(compoundNBT.getInt("someInt"));
            }
        }
    }
}
