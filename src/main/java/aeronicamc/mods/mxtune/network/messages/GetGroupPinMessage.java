package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.gui.group.GuiGroup;
import aeronicamc.mods.mxtune.managers.GroupClient;
import aeronicamc.mods.mxtune.managers.GroupManager;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class GetGroupPinMessage extends AbstractMessage<GetGroupPinMessage>
{
    private String pin = "0000";

    public GetGroupPinMessage() { /* NOP */ }

    public GetGroupPinMessage(String pin)
    {
        this.pin = pin;
    }

    @Override
    public GetGroupPinMessage decode(final PacketBuffer buffer)
    {
        final String pin = buffer.readUtf();
        return new GetGroupPinMessage(pin);
    }

    @Override
    public void encode(final GetGroupPinMessage message, final PacketBuffer buffer)
    {
        buffer.writeUtf(message.pin);
    }

    @Override
    public void handle(final GetGroupPinMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(() ->
                {
                    if (Minecraft.getInstance().screen instanceof GuiGroup)
                    {
                        GroupClient.setPrivatePin(message.pin);
                    }
                });
        else if (ctx.get().getDirection().getReceptionSide().isServer())
        {
            ServerPlayerEntity serverPlayer = ctx.get().getSender();
            if (serverPlayer != null)
                ctx.get().enqueueWork(() -> {
                    PacketDispatcher.sendTo(new GetGroupPinMessage(GroupManager.getGroup(serverPlayer.getId()).getPin()), serverPlayer);
                });
        }
        ctx.get().setPacketHandled(true);
    }
}
