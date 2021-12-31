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
                .then(CommandMusicDump.register())
                .then(CommandMusicLoad.register());
    }

    private static class CommandMusicDump
    {
        static ArgumentBuilder<CommandSource, ?> register()
        {
            return Commands.literal("dump")
                .requires(cs->cs.hasPermission(4)) //permission
                .executes(ctx ->
                    {
                        ctx.getSource().sendSuccess(
                            new TranslationTextComponent("commands.mxtune.music.dump",
                                String.format("%d", ModDataStore.dumpToFile())), true);
                        return 0;
                    }
                );
        }
    }

    private static class CommandMusicLoad
    {
        static ArgumentBuilder<CommandSource, ?> register()
        {
            return Commands.literal("load")
                .requires(cs->cs.hasPermission(4)) //permission
                .executes(ctx ->
                    {
                        ctx.getSource().sendSuccess(
                            new TranslationTextComponent("commands.mxtune.music.load",
                                String.format("%d", ModDataStore.loadDumpFile())), true);
                        return 0;
                    }
                );
        }
    }
}
