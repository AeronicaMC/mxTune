package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
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
        final ModelFile musicBlockModel = models().withExistingParent(registryName(ModBlocks.MUSIC_BLOCK.get()).getPath(), mcLoc("block/cube"))
                .texture("particle", modLoc("block/music_block"))
                .texture("down", modLoc("block/music_block"))
                .texture("up", modLoc("block/music_block"))
                .texture("east", modLoc("block/music_block"))
                .texture("west", modLoc("block/music_block"))
                .texture("north", modLoc("block/inv_test_block_front"))
                .texture("south", modLoc("block/music_block"));

        getVariantBuilder(ModBlocks.MUSIC_BLOCK.get())
                .forAllStates(state -> {
                    Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    return ConfiguredModel.builder()
                            .modelFile(musicBlockModel)
                            .rotationY(((int)direction.toYRot() + 180) % 360)
                            .build();
                });

        simpleBlockItem(ModBlocks.MUSIC_BLOCK.get());
        simpleBlock(ModBlocks.MUSIC_VENUE_TOOL_BLOCK.get());
        simpleBlockItem(ModBlocks.MUSIC_VENUE_TOOL_BLOCK.get());
    }
}
