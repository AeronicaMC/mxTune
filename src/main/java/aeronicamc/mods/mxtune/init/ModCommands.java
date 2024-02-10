package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.command.MXTuneCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModCommands
{
    private ModCommands() { /* NOOP */ }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event)
    {
        new MXTuneCommand(event.getDispatcher());
    }
}
