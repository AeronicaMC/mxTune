package aeronicamc.mods.mxtune.caps.stages;

import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.StageAreaSyncMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ServerStageAreas implements IServerStageAreas
{
    private static final Logger LOGGER = LogManager.getLogger(ServerStageAreas.class);
    RegistryKey<World> dimension;
    List<StageAreaData> stageAreas = new ArrayList<>();
    StageAreaData stageAreaDataTest = new StageAreaData(World.OVERWORLD,
                                                        new BlockPos(173, 70, -441),
                                                        new BlockPos(177,72,-445),
                                                        new BlockPos(176,70,-441),
                                                        new BlockPos(173,70,-441), "Null Stage", UUID.randomUUID());
    int someInt;

    ServerStageAreas()
    {
        this.stageAreas.add(stageAreaDataTest);
    }

    ServerStageAreas(RegistryKey<World> dimension)
    {
        this();
        this.dimension = dimension;
    }

    @Override
    public void test()
    {
        stageAreas.add(stageAreaDataTest);
    }

    @Override
    public List<StageAreaData> getStageAreas()
    {
        return stageAreas;
    }

    @Override
    public RegistryKey<World> getDimension()
    {
        return this.dimension;
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
        if (EffectiveSide.get().isServer())
        {
            PacketDispatcher.sendToAll(new StageAreaSyncMessage(Objects.requireNonNull(serializeNBT())));
            LOGGER.debug("{someInt {}, stageAreas {}", this.someInt, this.stageAreas);
        } else {
            LOGGER.debug("{someInt {}, stageAreas {}", this.someInt, this.stageAreas);
        }
    }

    @Override
    public INBT serializeNBT()
    {
        final CompoundNBT compoundNBT = new CompoundNBT();
        ListNBT listnbt = new ListNBT();
        for(StageAreaData stageArea : stageAreas) {
            NBTDynamicOps.INSTANCE.withEncoder(StageAreaData.CODEC).apply(stageArea).result().ifPresent(p -> {
                listnbt.add(p.copy());
            });
        }
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
                ListNBT listnbt = compoundNBT.getList("stageAreas", 10);
                stageAreas.clear();
                for (INBT stageAreaNBT : listnbt)
                {
                    NBTDynamicOps.INSTANCE.withParser(StageAreaData.CODEC).apply(stageAreaNBT).result().ifPresent(stageAreaData -> stageAreas.add(stageAreaData));
                }
                setInt(compoundNBT.getInt("someInt"));
            }
        }
    }
}
