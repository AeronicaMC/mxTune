package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.datagen.loot.MXTuneLootTableProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IDataProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * Registers this mod's {@link IDataProvider}s.
 *
 * @author Choonster
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Bus.MOD)
public class ModDataProviders
{
    @SubscribeEvent
    public static void registerDataProviders(final GatherDataEvent event)
    {
        final DataGenerator dataGenerator = event.getGenerator();
        final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if (event.includeClient())
        {
            dataGenerator.addProvider(new MXTuneLanguageProvider(dataGenerator));
            dataGenerator.addProvider(new MXTuneSoundDefinitionsProvider(dataGenerator, existingFileHelper));

            final MXTuneItemModelProvider itemModelProvider = new MXTuneItemModelProvider(dataGenerator, existingFileHelper);
            dataGenerator.addProvider(itemModelProvider);

            // Let BlockState provider see generated item models by passing its existing file helper
            dataGenerator.addProvider(new MXTuneBlockStateProvider(dataGenerator, itemModelProvider.existingFileHelper));
        }

        if (event.includeServer())
        {
            dataGenerator.addProvider(new MXTuneRecipeProvider(dataGenerator));
            dataGenerator.addProvider(new MXTuneLootTableProvider(dataGenerator));

            final MXTuneBlockTagsProvider blockTagsProvider = new MXTuneBlockTagsProvider(dataGenerator, existingFileHelper);
            dataGenerator.addProvider(blockTagsProvider);
            dataGenerator.addProvider(new MXTuneItemTagsProvider(dataGenerator, blockTagsProvider, existingFileHelper));
        }
    }
}
