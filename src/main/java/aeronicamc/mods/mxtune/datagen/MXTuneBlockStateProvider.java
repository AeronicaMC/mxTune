package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.blocks.MusicBlock;
import aeronicamc.mods.mxtune.init.ModBlocks;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class MXTuneBlockStateProvider extends BlockStateProvider
{
    public MXTuneBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper)
    {
        super(gen, Reference.MOD_ID, exFileHelper);
    }

    private ResourceLocation registryName(final Block block) {
        return Preconditions.checkNotNull(block.getRegistryName(), "Block %s has a null registry name", block);
    }

    @SuppressWarnings("unused")
    private void simpleBlockItem(Block block)
    {
        simpleBlockItem(block, models().getExistingFile(registryName(block)));
    }

    @Override
    public String getName() {
        return Reference.MOD_NAME + " BlockStates";
    }

    @Override
    protected void registerStatesAndModels()
    {
        // Using non-generated BlockBench models. Load them using getExistingFile...
        final ModelFile musicBlockModel = models().getExistingFile(modLoc("music_block"));
        final ModelFile musicBlockModelPLaying = models().getExistingFile(modLoc("music_block_playing"));

        getVariantBuilder(ModBlocks.MUSIC_BLOCK.get())
                .forAllStatesExcept(state -> {
                    Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (state.getValue(MusicBlock.PLAYING).equals(false))
                        return ConfiguredModel.builder()
                                .modelFile(musicBlockModel)
                                .rotationY(((int)direction.toYRot() + 180) % 360)
                                .build();
                    else
                        return ConfiguredModel.builder()
                                .modelFile(musicBlockModelPLaying)
                                .rotationY(((int)direction.toYRot() + 180) % 360)
                                .build();
                }, MusicBlock.POWERED);

        simpleBlockItem(ModBlocks.MUSIC_BLOCK.get(), musicBlockModel);
    }
}
