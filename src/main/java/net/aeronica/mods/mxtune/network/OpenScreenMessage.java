package net.aeronica.mods.mxtune.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static net.aeronica.mods.mxtune.gui.Handler.openTestScreen;

public class OpenScreenMessage
{
    private final SM screen;

    public OpenScreenMessage(SM screen)
    {
        this.screen = screen;
    }

    public static net.aeronica.mods.mxtune.network.OpenScreenMessage decode(final PacketBuffer buffer)
    {
        final SM screen = buffer.readEnum(SM.class);
        return new net.aeronica.mods.mxtune.network.OpenScreenMessage(screen);
    }

    public static void encode(final net.aeronica.mods.mxtune.network.OpenScreenMessage message, final PacketBuffer buffer)
    {
        buffer.writeEnum(message.screen);
    }

    public static void handle(final net.aeronica.mods.mxtune.network.OpenScreenMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        if (ctx.get().getDirection().getReceptionSide().isClient())
            ctx.get().enqueueWork(() ->
                {
                    switch (message.screen)
                    {
                        case TEST_ONE:
                            openTestScreen();
                            break;
                        case TEST_TWO:
                            openTestScreen();
                            break;
                    }
                });
        ctx.get().setPacketHandled(true);
    }

    public enum SM
    {
        TEST_ONE, TEST_TWO;
    }
}
