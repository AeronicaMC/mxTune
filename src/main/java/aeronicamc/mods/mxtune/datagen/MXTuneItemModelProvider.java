package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

import static aeronicamc.mods.mxtune.init.ModItems.*;

public class MXTuneItemModelProvider extends ItemModelProvider
{
    public MXTuneItemModelProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper)
    {
        super(generator, modid, existingFileHelper);
    }

    @Nonnull
    @Override
    public String getName()
    {
        return Reference.MOD_NAME + "ItemModels";
    }

    @Override
    protected void registerModels()
    {
        withExistingParent(MUSIC_PAPER.getId().getPath(), mcLoc("generated"))
                .texture("layer0", "item/music_paper");

        withExistingParent(SHEET_MUSIC.getId().getPath(), mcLoc("generated"))
                .texture("layer0", "item/sheet_music");

        registerMultiInstModels(this);
    }

    private void registerMultiInstModels(ItemModelProvider itemModelProvider)
    {
        INSTRUMENT_ITEMS.forEach(
            (key, value) -> itemModelProvider.withExistingParent(value.getId().getPath(), mcLoc("generated"))
                .texture("layer0", String.format("item/%s", SoundFontProxyManager.getName(key))));
    }
}
