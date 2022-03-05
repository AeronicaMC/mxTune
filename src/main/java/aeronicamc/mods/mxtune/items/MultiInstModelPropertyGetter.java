package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MultiInstModelPropertyGetter
{
    public static final ResourceLocation NAME = new ResourceLocation(Reference.MOD_ID, "multi_inst_model");

    private static final IItemPropertyGetter GETTER = (pItemStack, pLevel, pEntity) ->
    {
        World level = pLevel;
        Entity entity = pEntity != null ? pEntity : pItemStack.getEntityRepresentation();
        if (entity == null)
        {
            return Float.MAX_VALUE;
        }
        else
        {
            if (level == null && entity.level instanceof ClientWorld)
            {
                level = entity.level;
            }
            if (level == null)
            {
                return Float.MAX_VALUE;
            }
        }
        float maxIndex = SoundFontProxyManager.soundFontProxyMapByIndex.size();
        return !pItemStack.isEmpty() ? MathHelper.clamp(pItemStack.getDamageValue(), 0F, maxIndex) : maxIndex;
    };

    public static void registerToItem(Item pItem)
    {
        ItemModelsProperties.register(pItem, NAME, GETTER);
    }
}
