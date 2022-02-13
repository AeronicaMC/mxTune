package aeronicamc.mods.mxtune.caps.player;

import aeronicamc.mods.mxtune.caps.stages.StageAreaData;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.PlayerNexusSync;
import aeronicamc.mods.mxtune.network.messages.StageToolMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.UUID;

public class PlayerNexus implements IPlayerNexus
{
    private int playId = PlayIdSupplier.PlayType.INVALID.getAsInt();
    private WeakReference<PlayerEntity> playerWeakRef;
    private final StageAreaData stageToolData = new StageAreaData(BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO, "Set Name", UUID.randomUUID());

    PlayerNexus() { /* NOP */ }

    PlayerNexus(@Nullable final PlayerEntity playerEntity)
    {
        ReferenceQueue<PlayerEntity> playerEntityReferenceQueue = new ReferenceQueue<>();
        this.playerWeakRef = new WeakReference<>(playerEntity, playerEntityReferenceQueue);
        stageToolData.setOwnerUUID(playerEntity != null ? Objects.requireNonNull(playerEntity).getUUID() : UUID.randomUUID());
    }

    @Override
    public void setStagePos(StageToolMessage.StageOperation operation, BlockPos blockPos)
    {
        switch (operation)
        {
            case CORNER1:
                stageToolData.setStartPos(blockPos);
                break;
            case CORNER2:
                stageToolData.setEndPos(blockPos);
                break;
            case PERFORMERS:
                stageToolData.setPerformerSpawn(blockPos);
            case AUDIENCE:
                stageToolData.setAudienceSpawn(blockPos);
            default:
        }
    }

    @Override
    public void setStageName(String name)
    {
        stageToolData.setTitle(StringUtils.normalizeSpace(name));
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
        PlayerEntity playerEntity = playerWeakRef.get();
        if (playerEntity != null && !playerEntity.level.isClientSide)
        {
            World world = playerEntity.level;
            RegistryKey<World> dimension = world.dimension();
            PacketDispatcher.sendToDimension(new PlayerNexusSync(playId, playerEntity.getId()), dimension);
        }
    }
}
