package aeronicamc.mods.mxtune.caps;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.util.Misc;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
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


public final class LivingEntityModCapProvider
{
    private static final Logger LOGGER = LogManager.getLogger(LivingEntityModCapProvider.class);

    @CapabilityInject(ILivingEntityModCap.class)
    public static final Capability<ILivingEntityModCap> LIVING_ENTITY_MOD_CAP_CAPABILITY = Misc.nonNullInjected();

    private LivingEntityModCapProvider() { /* NOP */ }

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "living_mod_cap");

    public static void register()
    {
        CapabilityManager.INSTANCE.register(ILivingEntityModCap.class, new Capability.IStorage<ILivingEntityModCap>()
        {
            @Nullable
            @Override
            public INBT writeNBT(final Capability<ILivingEntityModCap> capability, final ILivingEntityModCap instance, final Direction side)
            {
                return IntNBT.valueOf(instance.getPlayId());
            }

            @Override
            public void readNBT(final Capability<ILivingEntityModCap> capability, final ILivingEntityModCap instance, final Direction side, final INBT nbt)
            {
                if (!(instance instanceof LivingEntityModCap))
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");

                instance.setPlayId(((IntNBT) nbt).getAsInt());
            }
        }, () -> new LivingEntityModCap(null));
    }

    public static LazyOptional<ILivingEntityModCap> getLivingEntityModCap(final LivingEntity entity)
    {
        return entity.getCapability(LIVING_ENTITY_MOD_CAP_CAPABILITY, null);
    }

    // TODO: Fix the network errors caused by a null here -> LOGGER.debug("AttachCapabilitiesEvent: {}", (event.getObject()));
    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class EventHandler
    {
        @SubscribeEvent
        public static void event(final AttachCapabilitiesEvent<Entity> event)
        {
            if (event.getObject() instanceof PlayerEntity)
            {
                final PlayerEntity playerEntity = (PlayerEntity) event.getObject();
                final LivingEntityModCap livingEntityModCap = new LivingEntityModCap(playerEntity);
                event.addCapability(ID, new SerializableCapabilityProvider<>(LIVING_ENTITY_MOD_CAP_CAPABILITY, null, livingEntityModCap));
                event.addListener(() -> getLivingEntityModCap(playerEntity).invalidate());
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
                getLivingEntityModCap(event.getOriginal()).ifPresent(oldLivingEntityCap ->
                {
                 getLivingEntityModCap(event.getPlayer()).ifPresent(newLivingEntityCap ->
                    {
                        newLivingEntityCap.setPlayId(oldLivingEntityCap.getPlayId());
                        newLivingEntityCap.synchronize();
                        LOGGER.debug("Clone: oldPId:{}, newPId:{}, {}", oldLivingEntityCap.getPlayId(), newLivingEntityCap.getPlayId(), event.getPlayer());
                    });
                });
            }
        }

        /**
         * Synchronise a player's playId to watching clients when they change dimensions.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void event(final PlayerEvent.PlayerChangedDimensionEvent event)
        {
            getLivingEntityModCap(event.getPlayer()).ifPresent(ILivingEntityModCap::synchronize);
            LOGGER.debug("PlayerChangedDimensionEvent: {}", event.getPlayer());
        }

        @SubscribeEvent
        public static void event(PlayerEvent.PlayerLoggedInEvent event)
        {
            getLivingEntityModCap(event.getPlayer()).ifPresent(ILivingEntityModCap::synchronize);
            LOGGER.debug("PlayerLoggedInEvent: {}", event.getPlayer());
        }

        @SubscribeEvent
        public static void event(PlayerEvent.PlayerRespawnEvent event)
        {
            getLivingEntityModCap(event.getPlayer()).ifPresent(ILivingEntityModCap::synchronize);
            LOGGER.debug("PlayerRespawnEvent: {}", event.getPlayer());
        }
    }
}
