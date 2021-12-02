package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.items.SheetMusicAgePropertyGetter;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

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

        ItemModelBuilder parentModel = withExistingParent(SHEET_MUSIC.getId().getPath(), mcLoc("generated"))
                .texture("layer0", "item/sheet_music");

        IntStream.range(0, 3)
                .mapToObj(index -> {
                    final ItemModelBuilder subModel = withExistingParent(SHEET_MUSIC.getId().toString() + "_age" + index, mcLoc(SHEET_MUSIC.getId().toString()))
                            .texture("layer0", "item/" + SHEET_MUSIC.getId().getPath() + "_age" + index);

                    return Pair.of(index, subModel);
                })
                .forEachOrdered(child ->
                    parentModel
                            .override()
                            .predicate(SheetMusicAgePropertyGetter.NAME, child.getKey() * 40)
                            .model(child.getValue())
                            .end()
                );

        registerMultiInstModels(this);
    }

    private void registerMultiInstModels(ItemModelProvider itemModelProvider)
    {
        INSTRUMENT_ITEMS.forEach(
            (key, value) -> itemModelProvider.withExistingParent(value.getId().getPath(), mcLoc("generated"))
                .texture("layer0", String.format("item/%s", SoundFontProxyManager.getName(key))));
    }
}
