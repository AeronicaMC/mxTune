package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.managers.GroupClient;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.SyncRequestMessage;
import aeronicamc.mods.mxtune.render.RenderHelper;
import aeronicamc.mods.mxtune.render.entity.InfoRenderer;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class ClientEvents
{
    private ClientEvents() { /* NOOP */ }

    @SubscribeEvent
    public static void event(ClientPlayerNetworkEvent.LoggedInEvent event)
    {
        GroupClient.clear();
        RenderHelper.getOverlayItemGui().clear();
        PacketDispatcher.sendToServer(new SyncRequestMessage());
    }

    @SubscribeEvent
    public static void event(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        RenderHelper.getOverlayItemGui().clear();
        InfoRenderer.getInstance().clearInfoRendererInstances();
    }

    @SubscribeEvent
    public static void event(ClientPlayerNetworkEvent.RespawnEvent event)
    {
        GroupClient.clear();
        ClientAudio.stopAll();
        RenderHelper.getOverlayItemGui().clear();
        PacketDispatcher.sendToServer(new SyncRequestMessage());
    }
}
