/**
 * Copyright {2017} Paul Boese aka Aeronica
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.crafting;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;

import net.aeronica.mods.mxtune.config.ModConfig;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * Disableable Shaped Ore Recipe
 * @author Aeronica
 *
 */
public class DisablableShapedOreRecipeFactory implements IRecipeFactory
{

    @Override
    public IRecipe parse(JsonContext context, JsonObject json)
    {
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);
        ShapedPrimer primer = new ShapedPrimer();
        primer.width = recipe.getRecipeWidth();
        primer.height = recipe.getRecipeHeight();
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = recipe.getIngredients();
        ResourceLocation group = recipe.getGroup().isEmpty() ? null : new ResourceLocation(recipe.getGroup());
        return new DisablableRecipe(group, recipe.getRecipeOutput(), primer); 
    }

    public static class DisablableRecipe extends ShapedOreRecipe {
        
        public DisablableRecipe(ResourceLocation group, ItemStack result, ShapedPrimer primer)
        {
            super(group, result, primer);
        }

        @Override
        @Nonnull
        public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1)
        {
            return ModConfig.isRecipeEnabled(this.output) ? this.output.copy() : ItemStack.EMPTY;
        }

        @Override
        public boolean isDynamic()
        {
            return ModConfig.isRecipeHidden(this.output.copy());
        }                
    }

}
