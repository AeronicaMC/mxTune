package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.datagen.crafting.recipe.EnhancedShapedRecipeBuilder;
import aeronicamc.mods.mxtune.init.ModBlocks;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.init.ModTags;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.data.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static aeronicamc.mods.mxtune.init.ModItems.MULTI_INST;

public class MXTuneRecipeProvider extends RecipeProvider
{

    public MXTuneRecipeProvider(DataGenerator pGenerator)
    {
        super(pGenerator);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> recipeConsumer)
    {
        {
            ShapelessRecipeBuilder.shapeless(ModItems.MUSIC_PAPER.get(), 16)
                    .requires(Items.PAPER, 4)
                    .requires(Tags.Items.DYES_BLACK)
                    .unlockedBy("has_paper", has(Items.PAPER))
                    .unlockedBy("has_black_dye", has(Tags.Items.DYES_BLACK))
                    .save(recipeConsumer);
        }

        {
            ItemStack flutePan = new ItemStack(MULTI_INST.get());
            ((IInstrument)flutePan.getItem()).setPatch(flutePan, SoundFontProxyManager.getIndexById("flute_pan"));

            EnhancedShapedRecipeBuilder.Vanilla.shapedRecipe(flutePan)
                    .pattern("LSL")
                    .pattern("SSS")
                    .define('S', Items.SUGAR_CANE)
                    .define('L', Tags.Items.LEATHER)
                    .unlockedBy("has_sugarcane", has(Items.SUGAR_CANE))
                    .unlockedBy("has_leather", has(Tags.Items.LEATHER))
                    .save(recipeConsumer, new ResourceLocation(Reference.MOD_ID, "flute_pan"));
        }

        {
            ShapelessRecipeBuilder.shapeless(ModItems.MUSIC_VENUE_TOOL.get(), 1)
                    .requires(ModTags.Items.INSTRUMENTS)
                    .requires(Tags.Items.RODS_WOODEN)
                    .unlockedBy("has_instrument", has(ModTags.Items.INSTRUMENTS))
                    .unlockedBy("has_rods_wooden", has(Tags.Items.RODS_WOODEN))
                    .save(recipeConsumer);
        }

        {
            ShapedRecipeBuilder.shaped(ModItems.WRENCH.get(), 1)
                    .pattern(" S ")
                    .pattern(" SS")
                    .pattern("S  ")
                    .define('S', Tags.Items.RODS)
                    .unlockedBy("has_rods_wooden", has(Tags.Items.RODS_WOODEN))
                    .unlockedBy("has_music_machines", has(ModTags.Items.MUSIC_MACHINES))
                    .save(recipeConsumer);
        }

        {
            ShapedRecipeBuilder.shaped(ModBlocks.MUSIC_BLOCK.get().asItem(), 1)
                    .pattern("IPI")
                    .pattern("ARA")
                    .pattern("AAA")
                    .define('R', Tags.Items.DUSTS_REDSTONE)
                    .define('P', Items.GLASS_PANE)
                    .define('A', Items.ACACIA_PLANKS)
                    .define('I', Tags.Items.NUGGETS_GOLD)
                    .unlockedBy("has_dusts_redstone", has(Tags.Items.DUSTS_REDSTONE))
                    .unlockedBy("has_glass_panes", has(Items.GLASS_PANE))
                    .unlockedBy("has_nuggets_iron", has(Tags.Items.NUGGETS_GOLD))
                    .save(recipeConsumer);
        }
    }

    /**
     * Gets a name for this provider, to use in logging.
     */
    @Override
    public String getName()
    {
        return Reference.MOD_NAME + " " + super.getName();
    }
}
