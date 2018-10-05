/*
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.blocks;

import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IMusicPlayer
{
    @SuppressWarnings("unchecked")
    @Nullable
    default public <T extends TileInstrument> T getTE(World worldIn, BlockPos pos) {return (T) worldIn.getTileEntity(pos);}

    default String getMML(World worldIn, BlockPos blockPos)
    {
        StringBuilder buildMML = new StringBuilder();
        TileEntity te = getTE(worldIn, blockPos);

        if (te != null)
        try
        {
            for (int slot = 0; slot < ((TileInstrument) te).getInventory().getSlots(); slot++)
            {
                ItemStack stackInSlot = ((TileInstrument) te).getInventory().getStackInSlot(slot);
                if (!stackInSlot.isEmpty() && stackInSlot.getItem() instanceof ItemInstrument)
                {
                    ItemInstrument ii = (ItemInstrument) stackInSlot.getItem();
                    int patch = ii.getPatch(stackInSlot.getMetadata());
                    ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(stackInSlot);
                    if (!sheetMusic.isEmpty() && sheetMusic.getTagCompound() != null)
                    {
                        NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("SheetMusic");
                        if (contents != null)
                        {
                            String mml = contents.getString("MML");
                            mml = mml.replace("MML@", "MML@I" + patch);
                            buildMML.append(slot).append("=").append(mml).append("|");
                        }
                    }
                }
            }
        } catch (Exception e)
        {
            ModLogger.error(e);
        }
        return buildMML.toString();
    }
}
