package aeronicamc.mods.mxtune.jei;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverride;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@JeiPlugin
public class MxtuneJEIPlugin implements IModPlugin
{
    private static final Logger LOGGER = LogManager.getLogger(MxtuneJEIPlugin.class);
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
        ItemModelMesher itemModelMesher = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
        IBakedModel bakedModel= itemModelMesher.getItemModel(ModItems.MULTI_INST.get());
        if (bakedModel != null)
        {
            ItemOverrideList list = bakedModel.getOverrides();
            list.getOverrides().forEach((ItemOverride o) -> LOGGER.info("{}", o.getModel()));
        }
        registration.registerSubtypeInterpreter(ModItems.MULTI_INST.get().getItem(), new MultiInstSubTypeInterpreter());
    }

    public static class MultiInstSubTypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack>
    {
        @Override
        public String apply(ItemStack ingredient, UidContext context)
        {
            return Integer.toString(ingredient.getDamageValue());
        }
    }
}
