package net.aeronica.mods.mxtune.datafixers;

import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class ItemInventoryWalker implements IDataWalker
{
    public ItemInventoryWalker() {/* NOP */}

    @Nonnull
    @Override
    public NBTTagCompound process(@Nonnull IDataFixer fixer, @Nonnull NBTTagCompound compound, int version)
    {
        if ("mxtune:instrument".equalsIgnoreCase(compound.getString("id")))
        {
            if (compound.hasKey("tag", Constants.NBT.TAG_COMPOUND))
            {
                NBTTagCompound itemInventory = compound.getCompoundTag("tag");
                DataFixesManager.processInventory(fixer, itemInventory, version, "ItemInventory");
                ModLogger.info("ItemInventoryWalker Walked inventory %s of ItemInstrument %s, containing %d items", "ItemInventory", compound.getString("id"), itemInventory.getInteger("Size"));
            }
        }
        return compound;
    }
}
