package aeronicamc.mods.mxtune.command;

import aeronicamc.mods.mxtune.caches.ModDataStore;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandMusic
{
    static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("music")
                .then(CommandConvert.register());
    }

    private static class  CommandConvert
    {
        static ArgumentBuilder<CommandSource, ?> register()
        {
            return Commands.literal("convert")
                    .requires(cs->cs.hasPermission(4)) //permission
                    .executes(ctx ->
                            {
                                ctx.getSource().sendSuccess(
                                        new TranslationTextComponent("commands.mxtune.music.convert",
                                                String.format("%d", ModDataStore.convertDumpToFiles())), true);
                                return 0;
                            }
                    );
        }
    }
}
