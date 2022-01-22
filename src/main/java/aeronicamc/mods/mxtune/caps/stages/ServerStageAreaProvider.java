package aeronicamc.mods.mxtune.caps.stages;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.util.Misc;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class ServerStageAreaProvider
{
    private static final Logger LOGGER = LogManager.getLogger(ServerStageAreaProvider.class);

    public static Capability<IServerStageAreas> STAGE_AREA_CAP = Misc.nonNullInjected();

    private ServerStageAreaProvider() { /* NOP */ }

    public final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "stage_area");

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IServerStageAreas.class, new Capability.IStorage<IServerStageAreas>()
        {
            @Nullable
            @Override
            public INBT writeNBT(Capability<IServerStageAreas> capability, final IServerStageAreas instance, Direction side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<IServerStageAreas> capability, final IServerStageAreas instance, Direction side, INBT nbt)
            {

            }
        }, ServerStageAreas::new);
    }

    public static LazyOptional<IServerStageAreas> getLivingEntityModCap(final World world)
    {
        return world.getCapability(STAGE_AREA_CAP, null);
    }
}
