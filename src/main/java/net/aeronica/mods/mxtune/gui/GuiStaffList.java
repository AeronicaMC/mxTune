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

package net.aeronica.mods.mxtune.gui;

import net.aeronica.mods.mxtune.caches.MXTunePart;
import net.aeronica.mods.mxtune.caches.MXTuneStaff;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.List;

public class GuiStaffList extends GuiScrollingList
{
    protected List<MXTuneStaff> tuneStaves;
    protected FontRenderer fontRenderer;

    public GuiStaffList(GuiMusicImporter parent, MXTunePart tunePart, int width, int height, int top, int bottom, int left)
    {
        super(parent.mc, width, height, top, bottom, left, parent.entryHeightImportList, parent.width, parent.height);
        this.fontRenderer = parent.mc.fontRenderer;
        this.tuneStaves = tunePart.getStaves();
    }

    int getRight() {return right;}

    int getSelectedIndex() { return selectedIndex; }

    @Override
    protected int getSize()
    {
        return tuneStaves.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick)
    {
        if (index == selectedIndex) return;
        selectedIndex = (index >= 0 && index <= tuneStaves.size() ? index : -1);
    }

    @Override
    protected boolean isSelected(int index)
    {
        return index == selectedIndex && selectedIndex >= 0 && selectedIndex <= tuneStaves.size();
    }

    @Override
    protected void drawBackground()
    {
        Gui.drawRect(left - 1, top - 1, left + listWidth + 1, top + listHeight + 1, -6250336);
        Gui.drawRect(left, top, left + listWidth, top + listHeight, -16777216);
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
    {
        MXTuneStaff tuneStaff = (tuneStaves.get(slotIdx));
        String mml = String.format("%02d: %s", tuneStaff.getStaff(), tuneStaff.getMml().substring(0, Math.min(tuneStaff.getMml().length(), 25)));
        String trimmedName = fontRenderer.trimStringToWidth(mml, listWidth - 10);
        fontRenderer.drawStringWithShadow(trimmedName, (float)left + 3, slotTop, 0xADD8E6);
    }
}
