package aeronicamc.mods.mxtune.caps.stages;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.caps.SerializableCapabilityProvider;
import aeronicamc.mods.mxtune.util.Misc;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class ServerStageAreaProvider
{
    private static final Logger LOGGER = LogManager.getLogger(ServerStageAreaProvider.class);

    @CapabilityInject(IServerStageAreas.class)
    public static Capability<IServerStageAreas> STAGE_AREA_CAP = Misc.nonNullInjected();

    private ServerStageAreaProvider() { /* NOP */ }

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "stage_area");

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IServerStageAreas.class, new Capability.IStorage<IServerStageAreas>()
        {
            @Nullable
            @Override
            public INBT writeNBT(Capability<IServerStageAreas> capability, final IServerStageAreas instance, Direction side)
            {
                final CompoundNBT[] compoundNBT = {new CompoundNBT()};
                if (instance.getStageAreaData().getDimension().equals(ServerWorld.OVERWORLD))
                    NBTDynamicOps.INSTANCE.withEncoder(StageAreaData.CODEC).apply(instance.getStageAreaData()).result().ifPresent(p -> {
                        compoundNBT[0] = (CompoundNBT) p.copy();
                    });
                compoundNBT[0].putInt("someInt", instance.getInt());
                return compoundNBT[0];
            }

            @Override
            public void readNBT(Capability<IServerStageAreas> capability, final IServerStageAreas instance, Direction side, INBT nbt)
            {
                if (!(instance instanceof ServerStageAreas))
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                if (nbt instanceof CompoundNBT)
                {
                    if (((CompoundNBT) nbt).contains("dimension") && instance.getStageAreaData().getDimension().equals(ServerWorld.OVERWORLD))
                        NBTDynamicOps.INSTANCE.withParser(StageAreaData.CODEC).apply(nbt);
                    instance.setInt( ((CompoundNBT)nbt).getInt("someInt"));
                    LOGGER.debug("readNBT {}", instance.getInt());
                }
            }
        }, ServerStageAreas::new);
    }

    public static LazyOptional<IServerStageAreas> getServerStageAreas(final World world)
    {
        return world.getCapability(STAGE_AREA_CAP, null);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class EventHandler
    {
        @SubscribeEvent
        public static void event(final AttachCapabilitiesEvent<World> event)
        {
            final World world = event.getObject();
            //if (!world.isClientSide() && event.getObject() instanceof ServerWorld)
            {
                final ServerStageAreas serverStageAreas = new ServerStageAreas();
                event.addCapability(ID, new SerializableCapabilityProvider<>(STAGE_AREA_CAP, null, serverStageAreas));
                event.addListener(() -> getServerStageAreas(world).invalidate());
                LOGGER.debug("AttachCapabilitiesEvent<World> {} {}", world, world.dimension());
            }
        }
    }
}
