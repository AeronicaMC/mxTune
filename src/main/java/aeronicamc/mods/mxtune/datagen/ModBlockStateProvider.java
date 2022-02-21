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

public class ModBlockStateProvider extends BlockStateProvider
{
    private static final Logger LOGGER = LogManager.getLogger();

    public ModBlockStateProvider(DataGenerator gen, String modId, ExistingFileHelper exFileHelper)
    {
        super(gen, modId, exFileHelper);
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
        return Reference.MOD_NAME + "BlockStates";
    }

    @Override
    protected void registerStatesAndModels()
    {
        simpleBlock(ModBlocks.MUSIC_VENUE_TOOL_BLOCK.get());
        simpleBlockItem(ModBlocks.MUSIC_VENUE_TOOL_BLOCK.get());
    }
}
