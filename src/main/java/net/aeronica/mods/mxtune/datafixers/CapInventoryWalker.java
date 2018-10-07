package net.aeronica.mods.mxtune.datafixers;

import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CapInventoryWalker implements IDataWalker
{
    private static final List<String> INVENTORY_KEY_MADNESS = new ArrayList<>();
    static {
        // vanilla chest, forge capabilities
        INVENTORY_KEY_MADNESS.add("Items");
        // Thermal Expansion Strongbox
        INVENTORY_KEY_MADNESS.add("Inventory");
    }
    public CapInventoryWalker() {/* NOP */}

    @Nonnull
    @Override
    public NBTTagCompound process(@Nonnull IDataFixer fixer, @SuppressWarnings("NullableProblems") NBTTagCompound te, int version)
    {
            if (te.hasKey("items"))
            {
                // primal_chest
                NBTTagCompound primal_chest = te.getCompoundTag("items");
                DataFixesManager.processInventory(fixer, primal_chest, version, "Items");
                ModLogger.info("CapInventoryWalker Walked inventory of TE %s using tag %s", te.getString("id"), "Items");
            } else
            {
                for (String key : INVENTORY_KEY_MADNESS)
                    if (te.hasKey(key))
                    {
                        DataFixesManager.processInventory(fixer, te, version, key);
                        ModLogger.info("CapInventoryWalker Walked inventory of TE %s using tag %s", te.getString("id"), key);
                    }
            }

        return te;
    }
}
