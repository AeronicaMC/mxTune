package aeronicamc.mods.mxtune.events;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.caps.stages.event.StageEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.DEDICATED_SERVER)
public class StageEventHandler
{
    private static final Logger LOGGER = LogManager.getLogger(StageEventHandler.class);

    @SubscribeEvent
    public static void event(StageEvent.SelectPosition event)
    {
        switch (event.getToolState())
        {
            case Corner1:
            case Corner2:
            case Corner1Edit:
            case Corner2Edit:
            case AudienceSpawn:
            case AudienceSpawnEdit:
            case StageSpawn:
            case StageSpawnEdit:
            default:
                LOGGER.debug("SelectPosition event: {}", event.getToolState());
        }
    }

}
