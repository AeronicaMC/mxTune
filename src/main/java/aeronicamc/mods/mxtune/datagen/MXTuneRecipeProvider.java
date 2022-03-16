package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.datagen.crafting.recipe.EnhancedShapedRecipeBuilder;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapelessRecipeBuilder;
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
