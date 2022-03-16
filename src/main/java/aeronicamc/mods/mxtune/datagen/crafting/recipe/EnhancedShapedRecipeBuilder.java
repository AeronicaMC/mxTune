package aeronicamc.mods.mxtune.datagen.crafting.recipe;

import aeronicamc.mods.mxtune.util.RegistryUtil;
import com.google.common.base.Preconditions;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * An extension of {@link ShapedRecipeBuilder} that allows the recipe result to have NBT and a custom group name for
 * the recipe advancement.
 *
 * @author Choonster
 */
public class EnhancedShapedRecipeBuilder<
        RECIPE extends ShapedRecipe,
        BUILDER extends EnhancedShapedRecipeBuilder<RECIPE, BUILDER>
        > extends ShapedRecipeBuilder {
    private static final Method ENSURE_VALID = ObfuscationReflectionHelper.findMethod(ShapedRecipeBuilder.class, /* ensureValid */ "func_200463_a", ResourceLocation.class);
    private static final Field ADVANCEMENT = ObfuscationReflectionHelper.findField(ShapedRecipeBuilder.class, /* advancement */ "field_200479_f");
    private static final Field GROUP = ObfuscationReflectionHelper.findField(ShapedRecipeBuilder.class, /* group */ "field_200480_g");
    private static final Field ROWS = ObfuscationReflectionHelper.findField(ShapedRecipeBuilder.class, /* rows */ "field_200477_d");
    private static final Field KEY = ObfuscationReflectionHelper.findField(ShapedRecipeBuilder.class, /* key */ "field_200478_e");

    protected final ItemStack result;
    protected final IRecipeSerializer<? extends RECIPE> serializer;
    protected String itemGroup;

    protected EnhancedShapedRecipeBuilder(final ItemStack result, final IRecipeSerializer<? extends RECIPE> serializer) {
        super(result.getItem(), result.getCount());
        this.result = result;
        this.serializer = serializer;
    }

    /**
     * Sets the item group name to use for the recipe advancement. This allows the result to be an item without an
     * item group, e.g. minecraft:spawner.
     *
     * @param group The group name
     * @return This builder
     */
    public BUILDER itemGroup(final String group) {
        itemGroup = group;
        return (BUILDER) this;
    }

    /**
     * Adds a key to the recipe pattern.
     */
    @Override
    public BUILDER define(final Character symbol, final ITag<Item> tagIn) {
        return (BUILDER) super.define(symbol, tagIn);
    }

    /**
     * Adds a key to the recipe pattern.
     */
    @Override
    public BUILDER define(final Character symbol, final IItemProvider itemIn) {
        return (BUILDER) super.define(symbol, itemIn);
    }

    /**
     * Adds a key to the recipe pattern.
     */
    @Override
    public BUILDER define(final Character symbol, final Ingredient ingredientIn) {
        return (BUILDER) super.define(symbol, ingredientIn);
    }

    /**
     * Adds a new entry to the patterns for this recipe.
     */
    @Override
    public BUILDER pattern(final String pattern) {
        return (BUILDER) super.pattern(pattern);
    }

    /**
     * Adds a criterion needed to unlock the recipe.
     */
    @Override
    public BUILDER unlockedBy(final String name, final ICriterionInstance criterion) {
        return (BUILDER) super.unlockedBy(name, criterion);
    }

    @Override
    public BUILDER group(final String group) {
        return (BUILDER) super.group(group);
    }

    /**
     * Builds this recipe into an {@link IFinishedRecipe}.
     */
    @Override
    public void save(final Consumer<IFinishedRecipe> consumer) {
        save(consumer, RegistryUtil.getRequiredRegistryName(result.getItem()));
    }

    /**
     * Builds this recipe into an {@link IFinishedRecipe}. Use {@link #save(Consumer)} if save is the same as the ID for
     * the result.
     */
    @Override
    public void save(final Consumer<IFinishedRecipe> consumer, final String save) {
        final ResourceLocation registryName = result.getItem().getRegistryName();
        if (new ResourceLocation(save).equals(registryName)) {
            throw new IllegalStateException("Shaped Recipe " + save + " should remove its 'save' argument");
        } else {
            save(consumer, new ResourceLocation(save));
        }
    }

    /**
     * Validates that the recipe result has NBT or a custom group has been specified.
     *
     * @param id The recipe ID
     */
    protected void ensureValid(final ResourceLocation id) {
        if (itemGroup == null && result.getItem().getItemCategory() == null) {
            throw new IllegalStateException("Enhanced Shaped Recipe " + id + " has result " + result + " with no item group - use EnhancedShapedRecipeBuilder.itemGroup to specify one");
        }
    }

    /**
     * Builds this recipe into an {@link IFinishedRecipe}.
     */
    @Override
    public void save(final Consumer<IFinishedRecipe> consumer, final ResourceLocation id) {
        try {
            // Perform the super class's validation
            ENSURE_VALID.invoke(this, id);

            // Perform our validation
            ensureValid(id);

            // We can't call the super method directly because it throws an exception when the result is an item that
            // doesn't belong to an item group (e.g. Mob Spawners).

            final Advancement.Builder advancementBuilder = ((Advancement.Builder) ADVANCEMENT.get(this))
                    .parent(new ResourceLocation("minecraft", "recipes/root"))
                    .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                    .rewards(AdvancementRewards.Builder.recipe(id))
                    .requirements(IRequirementsStrategy.OR);

            String group = (String) GROUP.get(this);
            if (group == null) {
                group = "";
            }

            final List<String> rows = (List<String>) ROWS.get(this);

            final Map<Character, Ingredient> key = (Map<Character, Ingredient>) KEY.get(this);

            String itemGroupName = itemGroup;
            if (itemGroupName == null) {
                final ItemGroup itemGroup = Preconditions.checkNotNull(result.getItem().getItemCategory());
                itemGroupName = itemGroup.getRecipeFolderName();
            }

            final ResourceLocation advancementID = new ResourceLocation(id.getNamespace(), "recipes/" + itemGroupName + "/" + id.getPath());

            final Result baseRecipe = new Result(id, result.getItem(), result.getCount(), group, rows, key, advancementBuilder, advancementID);

            consumer.accept(new SimpleFinishedRecipe(baseRecipe, result, serializer));
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to build Enhanced Shaped Recipe " + id, e);
        }
    }

    public static class Vanilla extends EnhancedShapedRecipeBuilder<ShapedRecipe, Vanilla> {
        private Vanilla(final ItemStack result) {
            super(result, IRecipeSerializer.SHAPED_RECIPE);
        }

        /**
         * Creates a new builder for a Vanilla shaped recipe with NBT and/or a custom item group.
         *
         * @param result The recipe result
         * @return The builder
         */
        public static Vanilla shapedRecipe(final ItemStack result) {
            return new Vanilla(result);
        }

        @Override
        protected void ensureValid(final ResourceLocation id) {
            super.ensureValid(id);

            if (!result.hasTag() && itemGroup == null) {
                throw new IllegalStateException("Vanilla Shaped Recipe " + id + " has no NBT and no custom item group - use ShapedRecipeBuilder instead");
            }
        }
    }
}
