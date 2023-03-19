package aeronicamc.mods.mxtune.init;

import aeronicamc.mods.mxtune.Reference;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class ModTags
{
    public static class Blocks
    {
        public static final Tags.IOptionalNamedTag<Block> MUSIC_MACHINES = mxtuneBlockTag("music_machines/music_machine");
    }

    private static Tags.IOptionalNamedTag<Block> mxtuneBlockTag(String name)
    {
        return BlockTags.createOptional(new ResourceLocation(Reference.MOD_ID, name));
    }

    public static class Items
    {
        public static final Tags.IOptionalNamedTag<Item> TOOLS_WRENCH = forgeTag("tools/wrench");
        public static final Tags.IOptionalNamedTag<Item> INSTRUMENTS = mxtuneItemTag("instruments/instrument");
        public static final Tags.IOptionalNamedTag<Item> MUSIC_MACHINES = mxtuneItemTag("music_machines/music_machine");
    }

    private static Tags.IOptionalNamedTag<Item> forgeTag(String name) {

        return ItemTags.createOptional(new ResourceLocation(Reference.MOD_ID_FORGE, name));
    }

    private static Tags.IOptionalNamedTag<Item> mxtuneItemTag(String name)
    {
        return ItemTags.createOptional(new ResourceLocation(Reference.MOD_ID, name));
    }
}
