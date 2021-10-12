package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

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
            // TODO look at TestMod3 for passing MXTuneItemModelProvider to the MXTuneBlockStateProvider
            dataGenerator.addProvider(new MXTuneLanguageProvider(dataGenerator));
            dataGenerator.addProvider(new MXTuneItemModelProvider(dataGenerator, Reference.MOD_ID, existingFileHelper));
        }

        if (event.includeServer())
        {
            // TODO
        }
    }
}
