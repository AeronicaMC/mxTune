package aeronicamc.mods.mxtune.caps;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

/**
 * A simple implementation of {@link ICapabilityProvider} that supports a single {@link Capability} handler instance.
 *
 * @author Choonster
 */
public class SimpleCapabilityProvider<H> implements ICapabilityProvider
{
    /**
     * The {@link Capability} instance to provide the handler for.
     */
    protected final Capability<H> capability;

    /**
     * The {@link Direction} to provide the handler for.
     */
    protected final Direction facing;

    /**
     * The handler instance to provide.
     */
    protected final H instance;

    /**
     * A lazy optional containing handler instance to provide.
     */
    protected final LazyOptional<H> lazyOptional;

    public SimpleCapabilityProvider(final Capability<H> capability, @Nullable final Direction facing, @Nullable final H instance) {
        this.capability = capability;
        this.facing = facing;

        this.instance = instance;

        if (this.instance != null) {
            lazyOptional = LazyOptional.of(() -> this.instance);
        } else {
            lazyOptional = LazyOptional.empty();
        }
    }

    /**
     * Retrieves the handler for the capability requested on the specific side.
     * The return value CAN be null if the object does not support the capability.
     * The return value CAN be the same for multiple faces.
     *
     * @param capability The capability to check
     * @param facing     The Side to check from:
     *                   CAN BE NULL. Null is defined to represent 'internal' or 'self'
     * @return A lazy optional containing the handler, if this object supports the capability.
     */
    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, @Nullable final Direction facing) {
        return getCapability().orEmpty(capability, lazyOptional);
    }

    /**
     * Get the {@link Capability} instance to provide the handler for.
     *
     * @return The Capability instance
     */
    public final Capability<H> getCapability() {
        return capability;
    }

    /**
     * Get the {@link Direction} to provide the handler for.
     *
     * @return The Direction to provide the handler for
     */
    @Nullable
    public Direction getFacing() {
        return facing;
    }

    /**
     * Get the handler instance.
     *
     * @return A lazy optional containing the handler instance
     */
    @Nullable
    public final H getInstance() {
        return instance;
    }

}
