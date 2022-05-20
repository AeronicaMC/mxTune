package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class ModTags
{
    public static class Blocks
    {
        /* NOP */
    }

    public static class Items
    {
        public static final Tags.IOptionalNamedTag<Item> TOOLS_WRENCH = forgeTag("tools/wrench");
    }

    private static Tags.IOptionalNamedTag<Item> forgeTag(String name) {

        return ItemTags.createOptional(new ResourceLocation(Reference.MOD_ID_FORGE, name));
    }
}
