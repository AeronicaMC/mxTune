/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
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
package net.aeronica.mods.mxtune.handler;

import net.aeronica.mods.mxtune.gui.GuiGroup;
import net.aeronica.mods.mxtune.gui.GuiGroupJoin;
import net.aeronica.mods.mxtune.gui.GuiInstrumentInventory;
import net.aeronica.mods.mxtune.gui.GuiMusicOptions;
import net.aeronica.mods.mxtune.gui.mml.GuiChunkTool;
import net.aeronica.mods.mxtune.gui.mml.GuiMXT;
import net.aeronica.mods.mxtune.gui.mml.GuiMusicLibrary;
import net.aeronica.mods.mxtune.gui.mml.GuiPlaylistManager;
import net.aeronica.mods.mxtune.inventory.ContainerBandAmp;
import net.aeronica.mods.mxtune.inventory.ContainerInstrument;
import net.aeronica.mods.mxtune.world.LockableHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import static net.aeronica.mods.mxtune.config.MXTuneConfig.isJAMPartyRightClickDisabled;
import static net.aeronica.mods.mxtune.gui.GuiGuid.*;

public class GUIHandler implements IGuiHandler
{

    private GUIHandler() {}
    private static class GUIHandlerHolder {private static final GUIHandler INSTANCE = new GUIHandler();}
    public static GUIHandler getInstance() {return GUIHandlerHolder.INSTANCE;}

    @Override
    public Object getServerGuiElement(int guiID, PlayerEntity playerIn, World worldIn, int x, int y, int z)
    {
        switch (guiID)
        {
            case GUI_INSTRUMENT_INVENTORY:
                // Use the player's held item to create the inventory
                return new ContainerInstrument(playerIn);

            case GUI_BAND_AMP:
                if (!LockableHelper.isLocked(playerIn, worldIn, x, y, z))
                    return new ContainerBandAmp(playerIn.inventory, worldIn, x, y, z);
                else
                    return null;

            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int guiID, PlayerEntity playerIn, World worldIn, int x, int y, int z)
    {
        switch (guiID)
        {
            case GUI_MUSIC_PAPER_PARSE:
                return new GuiMXT(null, GuiMXT.Mode.SHEET_MUSIC);

            case GUI_INSTRUMENT_INVENTORY:
                return new GuiInstrumentInventory(new ContainerInstrument(playerIn));

            case GUI_GROUP:
                return isJAMPartyRightClickDisabled() ? null : new GuiGroup();

            case GUI_GROUP_JOIN:
                return isJAMPartyRightClickDisabled() ? null : new GuiGroupJoin();

            case GUI_MUSIC_OPTIONS:
                return new GuiMusicOptions(null);

//            case GUI_BAND_AMP:
//                return new GuiBandAmp((net.minecraft.inventory.container.Container) getServerGuiElement(guiID, playerIn, worldIn, x, y, z), playerIn.inventory,
//                                      (TileBandAmp) worldIn.getTileEntity(new BlockPos(x, y, z)));

            case GUI_MUSIC_LIBRARY:
                return new GuiMusicLibrary(null);

            case GUI_PLAY_LIST_MANAGER:
                return new GuiPlaylistManager();

            case GUI_MXT_TEST:
                return new GuiMXT(null, GuiMXT.Mode.CLIENT);

            case GUI_CHUNK_TOOL:
                return new GuiChunkTool();

        default:
            return null;
        }
    }
}
