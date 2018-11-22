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
package net.aeronica.mods.mxtune.handler;

import net.aeronica.mods.mxtune.blocks.TileBandAmp;
import net.aeronica.mods.mxtune.gui.*;
import net.aeronica.mods.mxtune.inventory.ContainerBandAmp;
import net.aeronica.mods.mxtune.inventory.ContainerInstrument;
import net.aeronica.mods.mxtune.world.LockableHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GUIHandler implements IGuiHandler
{

    private GUIHandler() {}
    private static class GUIHandlerHolder {private static final GUIHandler INSTANCE = new GUIHandler();}
    public static GUIHandler getInstance() {return GUIHandlerHolder.INSTANCE;}

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer playerIn, World worldIn, int x, int y, int z)
    {
        switch (ID)
        {
            case GuiInstrumentInventory.GUI_ID:
                // Use the player's held item to create the inventory
                return new ContainerInstrument(playerIn);

            case GuiBandAmp.GUI_ID:
                if (!LockableHelper.isLocked(playerIn, worldIn, x, y, z))
                    return new ContainerBandAmp(playerIn.inventory, worldIn, x, y, z);
                else
                    return null;

            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer playerIn, World worldIn, int x, int y, int z)
    {
        switch (ID)
        {
            case GuiMusicPaperParse.GUI_ID:
                return new GuiMusicPaperParse();

            case GuiInstrumentInventory.GUI_ID:
                return new GuiInstrumentInventory(new ContainerInstrument(playerIn));

            case GuiGroup.GUI_ID:
                return new GuiGroup();

            case GuiGroupJoin.GUI_ID:
                return new GuiGroupJoin();

            case GuiMusicOptions.GUI_ID:
                return new GuiMusicOptions(playerIn);

            case GuiBandAmp.GUI_ID:
                return new GuiBandAmp((Container) getServerGuiElement(ID, playerIn, worldIn, x, y, z), playerIn.inventory,
                                      (TileBandAmp) worldIn.getTileEntity(new BlockPos(x, y, z)));

        default:
            return null;
        }
    }
}
