package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.items.MusicVenueToolItem;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.SyncRequestMessage;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
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
        /* NOP */
    }

    @SubscribeEvent
    public static void event(ClientPlayerNetworkEvent.RespawnEvent event)
    {
        LOGGER.debug("RespawnEvent: {}", event.getPlayer());
        ClientAudio.stopAll();
        PacketDispatcher.sendToServer(new SyncRequestMessage());
    }

    private static final ITextComponent MUSIC_VENUE_TOOL_BLOCK_HELP = new TranslationTextComponent("tooltip.mxtune.music_venue_tool_block.help_01");
    @SubscribeEvent
    public static void event(ItemTooltipEvent event)
    {
        if (event.getItemStack().getItem() instanceof MusicVenueToolItem)
        {
            event.getToolTip().add(MUSIC_VENUE_TOOL_BLOCK_HELP);
        }
    }
}
