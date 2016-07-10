/**
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

import net.aeronica.mods.mxtune.gui.GuiGroup;
import net.aeronica.mods.mxtune.gui.GuiGroupJoin;
import net.aeronica.mods.mxtune.gui.GuiInstrumentInventory;
import net.aeronica.mods.mxtune.gui.GuiInstrumentInventoryCaps;
import net.aeronica.mods.mxtune.gui.GuiMusicPaperParse;
import net.aeronica.mods.mxtune.gui.GuiPlaying;
import net.aeronica.mods.mxtune.gui.GuiPlayingChat;
import net.aeronica.mods.mxtune.inventory.ContainerSheetMusic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GUIHandlerCaps implements IGuiHandler
{

    private GUIHandlerCaps() {}

    private static class GUIHandlerHolder {private static final GUIHandlerCaps INSTANCE = new GUIHandlerCaps();}

    public static GUIHandlerCaps getInstance() {return GUIHandlerHolder.INSTANCE;}

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer playerIn, World worldIn, int x, int y, int z)
    {
        switch (ID)
        {
        case GuiInstrumentInventory.GUI_ID:
            /** Use the player's held item to create the inventory */
            ItemStack stack = playerIn.getHeldItemMainhand();
            if (stack == null) return null;
            return new ContainerSheetMusic(playerIn.inventory, stack);

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
            ItemStack stack = playerIn.getHeldItemMainhand();
            if (stack == null) return null;
            return new GuiInstrumentInventoryCaps(playerIn, stack);
            
        case GuiGroup.GUI_ID:
            return new GuiGroup();

        case GuiPlaying.GUI_ID:
            return new GuiPlaying();

        case GuiPlayingChat.GUI_ID:
            return new GuiPlayingChat();

        case GuiGroupJoin.GUI_ID:
            return new GuiGroupJoin();

        default:
            return null;
        }
    }
}
