package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.SyncRequestMessage;
import aeronicamc.mods.mxtune.render.entity.InfoRenderer;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class ClientEvents
{
    private static final Logger LOGGER = LogManager.getLogger(ClientEvents.class);

    @SubscribeEvent
    public static void event(ClientPlayerNetworkEvent.LoggedInEvent event)
    {
        PacketDispatcher.sendToServer(new SyncRequestMessage());
    }

    @SubscribeEvent
    public static void event(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        InfoRenderer.getInstance().clearInfoRendererInstances();
    }

    @SubscribeEvent
    public static void event(ClientPlayerNetworkEvent.RespawnEvent event)
    {
        LOGGER.debug("RespawnEvent: {}", event.getPlayer());
        ClientAudio.stopAll();
        PacketDispatcher.sendToServer(new SyncRequestMessage());
    }
}
