package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.caps.player.PlayerNexusProvider;
import aeronicamc.mods.mxtune.items.StageToolItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class StageToolMessage extends AbstractMessage<StageToolMessage>
{
    private final BlockPos blockPos;
    private final StageOperation stageOperation;
    private final String name;

    public StageToolMessage(){
        this.blockPos = BlockPos.ZERO;
        this.stageOperation = StageOperation.RESET;
        this.name = "";
    }

    public StageToolMessage(BlockPos blockPos, StageOperation stageOperation){
        this.blockPos = blockPos;
        this.stageOperation = stageOperation;
        this.name = "";
    }

    public StageToolMessage(String name){
        this.blockPos = BlockPos.ZERO;
        this.stageOperation = StageOperation.NAME;
        this.name = name;
    }

    @Override
    public void encode(StageToolMessage message, PacketBuffer buffer)
    {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeEnum(message.stageOperation);
        buffer.writeUtf(message.name);
    }

    @Override
    public StageToolMessage decode(PacketBuffer buffer)
    {
        final BlockPos blockPos = buffer.readBlockPos();
        final StageOperation stageOperation = buffer.readEnum(StageOperation.class);
        final String name = buffer.readUtf();
        return (stageOperation.equals(StageOperation.NAME)) ? new StageToolMessage(name): new StageToolMessage(blockPos, stageOperation);
    }

    @Override
    public void handle(StageToolMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->{

                ServerPlayerEntity sPlayer = ctx.get().getSender();
                if (sPlayer != null && !sPlayer.getMainHandItem().isEmpty() && sPlayer.getMainHandItem().getItem() instanceof StageToolItem)
                {
                    World level = sPlayer.level;
                    PlayerNexusProvider.getPerPlayerOptions(sPlayer).ifPresent(nexus -> {
                        switch (message.stageOperation)
                        {
                            case CORNER1:
                            case CORNER2:
                            case PERFORMERS:
                            case AUDIENCE:
                                if (level.isLoaded(message.blockPos))
                                    nexus.setStagePos(message.stageOperation, message.blockPos);
                                break;
                            case NAME:
                                nexus.setStageName(message.name);
                                break;
                            case APPLY:
                                break;
                            case RESET:
                                break;
                            case REMOVE:
                                break;
                            default:
                        }
                    });
                }
            });
        ctx.get().setPacketHandled(true);
    }

    public enum StageOperation
    {
        CORNER1, CORNER2, PERFORMERS, AUDIENCE, NAME, APPLY, RESET, REMOVE
    }
}
