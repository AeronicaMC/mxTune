package aeronicamc.mods.mxtune.network.messages;

import aeronicamc.mods.mxtune.gui.Handler;
import aeronicamc.mods.mxtune.managers.GroupManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenScreenMessage extends AbstractMessage<OpenScreenMessage>
{
    private SM screen = null;

    public OpenScreenMessage() { /* NOP */ }

    public OpenScreenMessage(SM screen)
    {
        this.screen = screen;
    }

    @Override
    public OpenScreenMessage decode(final PacketBuffer buffer)
    {
        final SM screen = buffer.readEnum(SM.class);
        return new OpenScreenMessage(screen);
    }

    @Override
    public void encode(final OpenScreenMessage message, final PacketBuffer buffer)
    {
        buffer.writeEnum(message.screen);
    }

    @Override
    public void handle(final OpenScreenMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(() ->
                {
                    switch (message.screen)
                    {
                        case GROUP_OPEN:
                            Handler.OpenGuiGroupScreen();
                            break;
                        case TEST_ONE:
                            Handler.openTestScreen();
                            break;
                        case TEST_TWO:
                            break;
                        default:
                    }
                });
        else if (ctx.get().getDirection().getReceptionSide().isServer())
        {
            ServerPlayerEntity serverPlayer = ctx.get().getSender();
            if (serverPlayer != null)
                ctx.get().enqueueWork(() -> {
                    switch (message.screen)
                    {
                        case GROUP_CHECK:
                            GroupManager.handleGroupCheck(serverPlayer, message.screen);
                            break;
                        case TEST_ONE:
                        case TEST_TWO:
                        default:
                    }
                });
        }
        ctx.get().setPacketHandled(true);
    }

    public enum SM
    {
        GROUP_CHECK, GROUP_OPEN, TEST_ONE, TEST_TWO;
    }
}
