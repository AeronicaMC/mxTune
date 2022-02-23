package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.init.ModItems;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class ToolManagerSyncMessage extends AbstractMessage<ToolManagerSyncMessage>
{
    private CompoundNBT nbt;

    public ToolManagerSyncMessage(){ /* NOP */ }

    public ToolManagerSyncMessage(@Nullable INBT nbt){
        this.nbt = (CompoundNBT) nbt;
    }

    @Override
    public void encode(ToolManagerSyncMessage message, PacketBuffer buffer)
    {
        buffer.writeNbt(message.nbt);
    }

    @Override
    public ToolManagerSyncMessage decode(PacketBuffer buffer)
    {
        final CompoundNBT cNbt = buffer.readNbt();
        return new ToolManagerSyncMessage(cNbt);
    }

    @Override
    public void handle(ToolManagerSyncMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(() -> {
                final Optional<World> levelOpt = LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide());
                levelOpt.ifPresent(level -> ModItems.MUSIC_VENUE_TOOL.ifPresent(tool-> tool.getToolManager().deserialize(message.nbt)));
            });
        ctx.get().setPacketHandled(true);
    }
}
