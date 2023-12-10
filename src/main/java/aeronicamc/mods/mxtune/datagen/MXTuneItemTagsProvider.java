package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModBlocks;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.init.ModTags;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class MXTuneItemTagsProvider extends ItemTagsProvider
{
    public MXTuneItemTagsProvider(DataGenerator pGenerator, BlockTagsProvider pBlockTagsProvider, @Nullable ExistingFileHelper existingFileHelper)
    {
        super(pGenerator, pBlockTagsProvider, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
        tag(ModTags.Items.TOOLS_WRENCH).add(ModItems.WRENCH.get());
        tag(ModTags.Items.INSTRUMENTS).add(ModItems.MULTI_INST.get());
        tag(ModTags.Items.MUSIC_MACHINES).add(ModBlocks.MUSIC_BLOCK.get().asItem());
    }
}