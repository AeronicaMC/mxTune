package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.items.ItemMultiInst;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Map;

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
        withExistingParent(ModItems.MUSIC_PAPER.getId().getPath(), mcLoc("generated"))
                .texture("layer0", "item/music_paper");

        withExistingParent(ModItems.SHEET_MUSIC.getId().getPath(), mcLoc("generated"))
                .texture("layer0", "item/sheet_music");

        registerMultiInstModels(this);
    }

    private void registerMultiInstModels(ItemModelProvider itemModelProvider)
    {
        for (Map.Entry<Integer, RegistryObject<ItemMultiInst>> entry : ModItems.MULTI_INST.entrySet())
        {
            itemModelProvider.withExistingParent(entry.getValue().getId().getPath(), mcLoc("generated"))
                    .texture("layer0", "item/" + SoundFontProxyManager.getName(entry.getKey()));
        }
    }
}
