package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.items.StageToolItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class StageToolMessage extends AbstractMessage<StageToolMessage>
{
    private final CompoundNBT stageAreaNbt;
    public StageToolMessage(){
        stageAreaNbt = new CompoundNBT();
    }

    public StageToolMessage(CompoundNBT stageAreaNbt){
        this.stageAreaNbt = stageAreaNbt;
    }

    @Override
    public void encode(StageToolMessage message, PacketBuffer buffer)
    {
        buffer.writeNbt((CompoundNBT) message.stageAreaNbt);
    }

    @Override
    public StageToolMessage decode(PacketBuffer buffer)
    {
        final CompoundNBT stageAreaNbt = buffer.readNbt();
        return new StageToolMessage(stageAreaNbt);
    }

    @Override
    public void handle(StageToolMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->{

                ServerPlayerEntity sPlayer = ctx.get().getSender();
                if (sPlayer != null && !sPlayer.getMainHandItem().isEmpty() && sPlayer.getMainHandItem().getItem() instanceof StageToolItem)
                {
                    // TODO:
                    Object o = message;
                }
            });
        ctx.get().setPacketHandled(true);
    }
}
