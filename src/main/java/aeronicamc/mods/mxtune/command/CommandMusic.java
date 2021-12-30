package aeronicamc.mods.mxtune.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CommandMusic
{
    static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("music")
            .requires(cs -> cs.hasPermission(0)) //permission
            .executes(ctx ->
                  {
                      return 0;
                  }
         );
    }
}
