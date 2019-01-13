/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.aeronica.mods.mxtune.datafixers;

import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TileEntityInventoryWalker implements IDataWalker
{
    private static final List<String> INVENTORY_KEY_MADNESS = new ArrayList<>();
    private static final String KEY_INVENTORY = "Inventory";
    private static final String KEY_ITEMS = "Items";
    private static final String KEY_ID = "id";

    static
    {
        // vanilla chest, forge item stack handler
        INVENTORY_KEY_MADNESS.add(KEY_ITEMS);
        // Thermal Expansion Strongbox
        INVENTORY_KEY_MADNESS.add(KEY_INVENTORY);
    }

    public TileEntityInventoryWalker() {/* NOP */}

    @Nonnull
    @Override
    public NBTTagCompound process(@Nonnull IDataFixer fixer, @SuppressWarnings("NullableProblems") NBTTagCompound te, int version)
    {
        for (String key : INVENTORY_KEY_MADNESS)
            if (te.hasKey(key))
            {
                DataFixesManager.processInventory(fixer, te, version, key);
                ModLogger.info("TileEntityInventoryWalker Walked inventory of TE %s using tag %s", te.getString(KEY_ID), key);
            }
        return te;
    }
}
