package net.aeronica.mods.mxtune.datafixers;

import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;

import javax.annotation.Nonnull;

public class CapInventoryWalker implements IDataWalker
{
    public CapInventoryWalker() {/* NOP */}

    @Nonnull
    @Override
    public NBTTagCompound process(@Nonnull IDataFixer fixer, @Nonnull NBTTagCompound te, int version)
    {
        if (te.hasKey("Items") || te.hasKey("items") || te.hasKey("Inventory"))
        {
            DataFixesManager.processInventory(fixer, te, version, "Items");
            DataFixesManager.processInventory(fixer, te, version, "items");
            DataFixesManager.processInventory(fixer, te, version, "Inventory");
            ModLogger.info("CapInventoryWalker Walked inventory of TE %s", te.getString("id"));
        }

        return te;
    }
}
