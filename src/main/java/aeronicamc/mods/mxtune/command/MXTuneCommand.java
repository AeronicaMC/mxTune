package aeronicamc.mods.mxtune.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class MXTuneCommand
{
    public MXTuneCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal("mxtune")
                    .then(CommandMusic.register())
       );
    }
}
