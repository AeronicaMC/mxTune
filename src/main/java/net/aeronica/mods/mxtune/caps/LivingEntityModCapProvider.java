package net.aeronica.mods.mxtune.caps;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.AntiNull;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
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
    private static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

    @CapabilityInject(ILivingEntityModCap.class)
    public static final Capability<ILivingEntityModCap> LIVING_ENTITY_MOD_CAP_CAPABILITY = AntiNull.nonNullInjected();

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
                instance.setPlayId(((IntNBT) nbt).getAsInt());
            }
        }, () -> new LivingEntityModCap(null));
    }

    public static LazyOptional<ILivingEntityModCap> getLivingEntityModCap(final LivingEntity entity)
    {
        return entity.getCapability(LIVING_ENTITY_MOD_CAP_CAPABILITY, null);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    private static class EventHandler
    {
        @SubscribeEvent
        public static void attachCapabilities(final AttachCapabilitiesEvent<Entity> event)
        {
            if ((event.getObject() instanceof LivingEntity) && !(event.getObject() instanceof PlayerEntity))
            {
                final LivingEntityModCap livingEntityModCap = new LivingEntityModCap((LivingEntity) event.getObject());
                event.addCapability(ID, new SerializableCapabilityProvider<>(LIVING_ENTITY_MOD_CAP_CAPABILITY, null, livingEntityModCap));
                event.addListener(()->getLivingEntityModCap((LivingEntity) event.getObject()).invalidate());
                LOGGER.debug("LivingEntityModCapProvider#attachCapabilities: {}", ((LivingEntity)event.getObject()));
            }
        }

        @SubscribeEvent
        public static void attachPlayerCapabilities(final AttachCapabilitiesEvent<Entity> event)
        {
            if (event.getObject() instanceof PlayerEntity)
            {
                final LivingEntityModCap livingEntityModCap = new LivingEntityModCap((PlayerEntity) event.getObject());
                event.addCapability(ID, new SerializableCapabilityProvider<>(LIVING_ENTITY_MOD_CAP_CAPABILITY, null, livingEntityModCap));
                event.addListener(()->getLivingEntityModCap((LivingEntity) event.getObject()).invalidate());
                LOGGER.debug("LivingEntityModCapProvider#attachPlayerCapabilities: {}", (event.getObject()));
            }
        }

        /**
         * Copy the player's playId when they respawn after dying or returning from the end.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void playerClone(final PlayerEvent.Clone event)
        {
            event.getOriginal().revive(); // gighertz workaround for MCForge #5956 PlayerEvent.Clone Capability Provider is invalid
            getLivingEntityModCap(event.getOriginal()).ifPresent(oldLivingEntityCap -> {
                getLivingEntityModCap(event.getPlayer()).ifPresent(newLivingEntityCap -> {
                    if (!event.isWasDeath() || event.getPlayer().level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || event.getOriginal().isSpectator())
                    {
                        newLivingEntityCap.setPlayId(oldLivingEntityCap.getPlayId());
                        LOGGER.debug("LivingEntityModCapProvider#PlayerEvent.Clone: oldPId:{}, newPId{}, {}", oldLivingEntityCap.getPlayId(), newLivingEntityCap.getPlayId(), event.getPlayer());
                    }
                });
            });
        }

        /**
         * Synchronise a player's playId to watching clients when they change dimensions.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void playerChangeDimension(final PlayerEvent.PlayerChangedDimensionEvent event)
        {
            getLivingEntityModCap(event.getPlayer()).ifPresent(ILivingEntityModCap::synchronise);
            LOGGER.debug("LivingEntityModCapProvider#PlayerChangedDimensionEvent: {}", event.getPlayer());
        }
    }
}
