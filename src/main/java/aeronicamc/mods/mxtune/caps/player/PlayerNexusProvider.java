package aeronicamc.mods.mxtune.caps.player;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.caps.SerializableCapabilityProvider;
import aeronicamc.mods.mxtune.util.Misc;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;


public final class PlayerNexusProvider
{
    private static final Logger LOGGER = LogManager.getLogger(PlayerNexusProvider.class);

    @CapabilityInject(IPlayerNexus.class)
    public static final Capability<IPlayerNexus> NEXUS_CAPABILITY = Misc.nonNullInjected();

    private PlayerNexusProvider() { /* NOP */ }

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "nexus");

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IPlayerNexus.class, new Capability.IStorage<IPlayerNexus>()
        {
            @Nullable
            @Override
            public INBT writeNBT(final Capability<IPlayerNexus> capability, final IPlayerNexus instance, final Direction side)
            {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(final Capability<IPlayerNexus> capability, final IPlayerNexus instance, final Direction side, final INBT nbt)
            {
                if (!(instance instanceof PlayerNexus))
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");

                instance.deserializeNBT(nbt);
            }
        }, () -> null);
    }

    public static LazyOptional<IPlayerNexus> getPerPlayerOptions(final LivingEntity entity)
    {
        return entity.getCapability(NEXUS_CAPABILITY, null);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class EventHandler
    {
        @SubscribeEvent
        public static void event(final AttachCapabilitiesEvent<Entity> event)
        {
            if (event.getObject() instanceof PlayerEntity)
            {
                final PlayerEntity playerEntity = (PlayerEntity) event.getObject();
                final PlayerNexus perPlayerOptions = new PlayerNexus(playerEntity);
                event.addCapability(ID, new SerializableCapabilityProvider<>(NEXUS_CAPABILITY, null, perPlayerOptions));
                event.addListener(() -> getPerPlayerOptions(playerEntity).invalidate());
                //LOGGER.debug("AttachCapabilitiesEvent: {}", playerEntity);
            }
        }

        /**
         * Copy the player's playId when they respawn after dying or returning from the end.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void event(final PlayerEvent.Clone event)
        {
            if (event.getPlayer() instanceof ServerPlayerEntity)
            {
                event.getOriginal().revive(); // gighertz workaround for MCForge #5956 PlayerEvent.Clone Capability Provider is invalid
                getPerPlayerOptions(event.getOriginal()).ifPresent(oldLivingEntityCap ->
                {
                 getPerPlayerOptions(event.getPlayer()).ifPresent(newLivingEntityCap ->
                    {
                        newLivingEntityCap.setPlayId(oldLivingEntityCap.getPlayId());
                        newLivingEntityCap.sync();
                        LOGGER.debug("Clone: oldPId:{}, newPId:{}, {}", oldLivingEntityCap.getPlayId(), newLivingEntityCap.getPlayId(), event.getPlayer());
                    });
                });
            }
        }
    }
}
