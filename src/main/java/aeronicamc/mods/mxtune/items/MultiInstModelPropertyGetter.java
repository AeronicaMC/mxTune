package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class MultiInstModelPropertyGetter
{
    public static final ResourceLocation NAME = new ResourceLocation(Reference.MOD_ID, "multi_inst_model");

    private static final IItemPropertyGetter GETTER = (pItemStack, pLevel, pEntity) ->
            !pItemStack.isEmpty() ? MathHelper.clamp(((IInstrument)pItemStack.getItem()).getPatch(pItemStack), 0F, SoundFontProxyManager.countInstruments()) : 0F;

    public static void registerToItem(Item pItem)
    {
        ItemModelsProperties.register(pItem, NAME, GETTER);
    }
}
