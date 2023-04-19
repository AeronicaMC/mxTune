package aeronicamc.mods.mxtune.caps.venues;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.caps.SerializableCapabilityProvider;
import aeronicamc.mods.mxtune.caps.player.PlayerNexusProvider;
import aeronicamc.mods.mxtune.util.Misc;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class MusicVenueProvider
{
    private static final Logger LOGGER = LogManager.getLogger(MusicVenueProvider.class);

    @CapabilityInject(IMusicVenues.class)
    public static Capability<IMusicVenues> musicVenuesCapability = Misc.nonNullInjected();

    private MusicVenueProvider() { /* NOP */ }

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "stage_area");

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IMusicVenues.class, new Capability.IStorage<IMusicVenues>()
        {
            @Nullable
            @Override
            public INBT writeNBT(Capability<IMusicVenues> capability, final IMusicVenues instance, Direction side)
            {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<IMusicVenues> capability, final IMusicVenues instance, Direction side, INBT nbt)
            {
                if (!(instance instanceof MusicVenues))
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                instance.deserializeNBT(nbt);
            }
        }, MusicVenues::new);
    }

    public static LazyOptional<IMusicVenues> getMusicVenues(final World world)
    {
        return world.getCapability(musicVenuesCapability, null);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class EventHandler
    {
        private static int counter;

        @SubscribeEvent
        public static void event(final AttachCapabilitiesEvent<World> event)
        {
            event.addCapability(ID, new SerializableCapabilityProvider<>(musicVenuesCapability, null, new MusicVenues(event.getObject())));
        }

        @SubscribeEvent
        public static void event(final TickEvent.WorldTickEvent event)
        {
            if(event.phase.equals(TickEvent.Phase.END) && (counter++ % 10 == 0))
                updateServerEntityVenue(event);
        }

        private static void updateServerEntityVenue(TickEvent.WorldTickEvent event)
        {
            event.world.players().forEach(
                    player ->
                    {
                        PlayerNexusProvider.getNexus(player).ifPresent(
                                nexus ->
                                {
                                    EntityVenueState pvs = MusicVenueHelper.getEntityVenueState(player.level, player.getId());
                                    if (!nexus.getEntityVenueState().equals(pvs))
                                    {
                                        nexus.setEntityVenueState(pvs);
                                        ToolManager.getPlayerTool(player).ifPresent(
                                                tool ->
                                                {
                                                    if (pvs.inVenue() && (pvs.getVenue().getOwnerUUID().equals(player.getUUID()) || player.isCreative() || ToolManager.isOp(player)))
                                                        tool.setToolState(ToolState.Type.REMOVE);
                                                    else if (!pvs.inVenue() && tool.getToolState().equals(ToolState.Type.REMOVE))
                                                        tool.setToolState(ToolState.Type.START);

                                                    ToolManager.sync(player);
                                                });

                                        nexus.setEntityVenueState(pvs);
                                    }
                                });
                    });
        }
    }
}
