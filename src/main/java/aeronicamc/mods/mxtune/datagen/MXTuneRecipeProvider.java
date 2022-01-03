package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Items;
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
    }

    /**
     * Gets a name for this provider, to use in logging.
     */
    @Override
    public String getName()
    {
        return Reference.MOD_NAME + " " +super.getName();
    }
}
