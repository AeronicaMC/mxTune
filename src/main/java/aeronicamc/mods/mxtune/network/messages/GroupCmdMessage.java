package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.gui.group.GuiGroup;
import aeronicamc.mods.mxtune.managers.GroupClient;
import aeronicamc.mods.mxtune.managers.GroupManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class GroupCmdMessage extends AbstractMessage<GroupCmdMessage>
{
    private final String pin;
    private final Cmd cmd;
    private final int taggedMemberId;

    public GroupCmdMessage()
    {
        pin = "0000";
        cmd = Cmd.Nil;
        taggedMemberId = 0;
    }

    public GroupCmdMessage(@Nullable String pin, Cmd cmd, int taggedMemberId)
    {
        this.pin = pin != null ? pin : "0000";
        this.cmd = cmd;
        this.taggedMemberId = taggedMemberId;
    }

    @Override
    public GroupCmdMessage decode(final PacketBuffer buffer)
    {
        final String pin = buffer.readUtf();
        final Cmd cmd = buffer.readEnum(GroupCmdMessage.Cmd.class);
        final int memberId = buffer.readInt();
        return new GroupCmdMessage(pin, cmd, memberId);
    }

    @Override
    public void encode(final GroupCmdMessage message, final PacketBuffer buffer)
    {
        buffer.writeUtf(message.pin);
        buffer.writeEnum(message.cmd);
        buffer.writeInt(message.taggedMemberId);
    }

    @Override
    public void handle(final GroupCmdMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(() ->
                {
                    if (Minecraft.getInstance().screen instanceof GuiGroup)
                    {
                        GroupClient.setPrivatePin(message.pin, message.cmd);
                    }
                });
        else if (ctx.get().getDirection().getReceptionSide().isServer())
        {
            ServerPlayerEntity serverPlayer = ctx.get().getSender();
            if (serverPlayer != null)
                ctx.get().enqueueWork(() -> GroupManager.handleGroupCmd(serverPlayer, message.cmd, message.taggedMemberId));
        }
        ctx.get().setPacketHandled(true);
    }

    public enum Cmd
    {
        Nil, CloseGui, Disband, Pin, ModePin, ModeOpen, NewPin, Promote, Remove
    }
}
