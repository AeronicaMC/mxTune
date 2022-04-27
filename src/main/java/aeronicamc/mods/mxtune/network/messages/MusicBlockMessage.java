package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.blocks.MusicBlockEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MusicBlockMessage extends AbstractMessage<MusicBlockMessage>
{
    BlockPos blockPos;
    int signals;

    public MusicBlockMessage()
    {
        this.blockPos = BlockPos.ZERO;
        this.signals = 0;
    }

    public MusicBlockMessage(BlockPos blockPos, int signals)
    {
        this.blockPos = blockPos;
        this.signals = signals;
    }

    @Override
    public void encode(MusicBlockMessage message, PacketBuffer buffer)
    {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeVarInt(message.signals);
    }

    @Override
    public MusicBlockMessage decode(PacketBuffer buffer)
    {
        return new MusicBlockMessage(buffer.readBlockPos(), buffer.readVarInt());
    }

    @Override
    public void handle(MusicBlockMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isServer())
            ctx.get().enqueueWork(() ->{

                ServerPlayerEntity sPlayer = ctx.get().getSender();
                if (sPlayer != null && sPlayer.level.isLoaded(message.blockPos))
                {
                    TileEntity blockEntity = sPlayer.level.getBlockEntity(message.blockPos);
                    if (blockEntity instanceof MusicBlockEntity)
                    {
                        ((MusicBlockEntity) blockEntity).setRearRedstoneInputEnabled((message.signals & 0x0001) > 0);
                        ((MusicBlockEntity) blockEntity).setLeftRedstoneOutputEnabled((message.signals & 0x0002) > 0);
                        ((MusicBlockEntity) blockEntity).setRightRedstoneOutputEnabled((message.signals & 0x0004) > 0);
                    }

                }
            });
        ctx.get().setPacketHandled(true);
    }
}
