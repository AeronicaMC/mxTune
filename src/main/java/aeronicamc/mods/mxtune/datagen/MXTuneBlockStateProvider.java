package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModBlocks;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MXTuneBlockStateProvider extends BlockStateProvider
{
    private static final Logger LOGGER = LogManager.getLogger();

    public MXTuneBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper)
    {
        super(gen, Reference.MOD_ID, exFileHelper);
    }

    private ResourceLocation registryName(final Block block) {
        return Preconditions.checkNotNull(block.getRegistryName(), "Block %s has a null registry name", block);
    }

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
//        final ModelFile musicBlockModel = models().orientable(
//                        ModBlocks.MUSIC_BLOCK.getId().getPath(),
//                        modLoc("block/music_block_side"),
//                        modLoc("block/music_block_front"),
//                        modLoc("block/music_block"))
//                .texture("particle", modLoc("block/music_block"));
//
//        final ModelFile musicBlockModelPLaying = models().orientable(
//                        ModBlocks.MUSIC_BLOCK.getId().getPath() + "_playing",
//                        modLoc("block/music_block_side"),
//                        modLoc("block/music_block_playing_front"),
//                        modLoc("block/music_block"))
//                .texture("particle", modLoc("block/music_block"));

//        getVariantBuilder(ModBlocks.MUSIC_BLOCK.get())
//                .forAllStatesExcept(state -> {
//                    Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
//                    if (state.getValue(MusicBlock.PLAYING).equals(false))
//                        return ConfiguredModel.builder()
//                                .modelFile(models().getExistingFile(new ResourceLocation(Reference.MOD_ID, "block/music_block")))
//                                .rotationY(((int)direction.toYRot() + 180) % 360)
//                                .build();
//                    else
//                        return ConfiguredModel.builder()
//                                .modelFile(models().getExistingFile(new ResourceLocation(Reference.MOD_ID, "block/music_block_playing")))
//                                .rotationY(((int)direction.toYRot() + 180) % 360)
//                                .build();
//                }, MusicBlock.POWERED);

        //simpleBlockItem(ModBlocks.MUSIC_BLOCK.get(), (models().getExistingFile(new ResourceLocation(Reference.MOD_ID, "block/music_block"))));
        simpleBlock(ModBlocks.MUSIC_VENUE_TOOL_BLOCK.get());
        simpleBlockItem(ModBlocks.MUSIC_VENUE_TOOL_BLOCK.get());
    }
}
