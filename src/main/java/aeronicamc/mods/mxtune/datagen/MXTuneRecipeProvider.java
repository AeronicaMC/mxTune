package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModItems;
import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

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
            ShapedRecipeBuilder.shaped(ModItems.MULTI_INST.get())
                    .pattern("BBB")
                    .pattern("SLS")
                    .pattern("SSS")
                    .define('B', Tags.Items.BONES)
                    .define('S', Tags.Items.RODS_WOODEN)
                    .define('L', Tags.Items.LEATHER)
                    .unlockedBy("has_bone", has(Tags.Items.BONES))
                    .unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN))
                    .unlockedBy("has_leather", has(Tags.Items.LEATHER))
                    .save(recipeConsumer, new ResourceLocation(Reference.MOD_ID, "accordion_from_bones_sticks_and_leather"));
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
