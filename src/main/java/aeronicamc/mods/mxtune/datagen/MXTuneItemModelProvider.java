package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

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
//        getBuilder(ModItems.MUSIC_PAPER.getId().getPath())
//                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
//                .texture("layer0", modLoc("item/music_paper"));

        withExistingParent(ModItems.MUSIC_PAPER.getId().getPath(), mcLoc("handheld"))
                .texture("layer0", "item/music_paper");

        withExistingParent(ModItems.MULTI_INST.getId().getPath(), mcLoc("handheld"))
                .texture("layer0", "item/multi_inst");
    }

}
