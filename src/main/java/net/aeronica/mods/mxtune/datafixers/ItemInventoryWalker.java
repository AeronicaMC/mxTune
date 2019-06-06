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
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

import static net.aeronica.mods.mxtune.util.SheetMusicUtil.ITEM_INVENTORY;

public class ItemInventoryWalker implements IDataWalker
{
    public ItemInventoryWalker() {/* NOP */}

    @Nonnull
    @Override
    public NBTTagCompound process(@Nonnull IDataFixer fixer, @Nonnull NBTTagCompound compound, int version)
    {
        if ("mxtune:instrument".equalsIgnoreCase(compound.getString("id")) && (compound.hasKey("tag", Constants.NBT.TAG_COMPOUND)))
        {
            NBTTagCompound itemInventory = compound.getCompoundTag("tag");
            DataFixesManager.processInventory(fixer, itemInventory, version, ITEM_INVENTORY);
            ModLogger.debug("ItemInventoryWalker Walked inventory %s of ItemInstrument %s, containing %d items", "ItemInventory", compound.getString("id"), itemInventory.getInteger("Size"));
        }
        return compound;
    }
}
