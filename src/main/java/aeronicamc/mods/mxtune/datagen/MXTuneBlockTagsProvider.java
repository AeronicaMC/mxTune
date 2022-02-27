package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class MXTuneBlockTagsProvider extends BlockTagsProvider
{
    public MXTuneBlockTagsProvider(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper)
    {
        super(pGenerator, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
        /* NOP */
    }
}
