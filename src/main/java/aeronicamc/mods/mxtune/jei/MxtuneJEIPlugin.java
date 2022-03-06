package aeronicamc.mods.mxtune.jei;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class MxtuneJEIPlugin implements IModPlugin
{

    /**
     * The unique ID for this mod plugin.
     * The namespace should be your mod's modId.
     */
    @Override
    public ResourceLocation getPluginUid()
    {
        return new ResourceLocation(Reference.MOD_ID);
    }

    /**
     * If your item has subtypes that depend on NBT or capabilities, use this to help JEI identify those subtypes correctly.
     *
     * @param registration
     */
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration)
    {
        registration.registerSubtypeInterpreter(ModItems.MULTI_INST.get().getItem(), new MultiInstSubTypeInterpreter());
    }

    public static class MultiInstSubTypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack>
    {
        /**
         * Get the data from an ingredient that is relevant to telling subtypes apart in the given context.
         * This should account for nbt, and anything else that's relevant.
         * <p>
         * {@link UidContext} can be used to give different subtype information depending on the given context.
         * Most cases will return the same value for all contexts and it can usually be ignored.
         * <p>
         * Return {@link #NONE} if there is no data used for subtypes.
         *
         * @param ingredient
         * @param context
         */
        @Override
        public String apply(ItemStack ingredient, UidContext context)
        {
            return Integer.toString(ingredient.getDamageValue());
        }
    }
}
