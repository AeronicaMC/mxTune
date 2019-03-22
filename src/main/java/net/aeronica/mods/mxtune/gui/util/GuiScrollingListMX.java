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

package net.aeronica.mods.mxtune.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.List;

public abstract class GuiScrollingListMX extends GuiScrollingList
{
    private static final Minecraft mc = Minecraft.getMinecraft();
    private List<?> listRef;
    private GuiScreen gui;

    public <T extends GuiScreen> GuiScrollingListMX(T gui, List<?> listRef, int entryHeight, int width, int height, int top, int bottom, int left)
    {
        super(mc, width, height, top, bottom, left, entryHeight, gui.width, gui.height);
        this.gui = gui;
        this.listRef = listRef;
    }

    public void updateListRef(List<?>  listRef)
    {
        this.listRef = listRef;
    }

    public int getRight() {return right;}

    public int getSelectedIndex() { return selectedIndex; }

    @Override
    protected int getSize()
    {
        return listRef != null ? listRef.size() : 0;
    }


    public void elementClicked(int index)
    {
        elementClicked(index, false);
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick)
    {
        if (index == selectedIndex && !doubleClick) return;
        selectedIndex = (index >= 0 && index <= listRef.size() ? index : -1);

        if (selectedIndex >= 0 && selectedIndex <= listRef.size())
        {
            if (!doubleClick)
                selectedClickedCallback(selectedIndex);
            else
                selectedDoubleClickedCallback(selectedIndex);
        }

    }

    protected abstract void selectedClickedCallback(int selectedIndex);

    protected abstract void selectedDoubleClickedCallback(int selectedIndex);

    @Override
    protected boolean isSelected(int index)
    {
        return index == selectedIndex && selectedIndex >= 0 && selectedIndex <= listRef.size();
    }

    @Override
    protected void drawBackground()
    {
        Gui.drawRect(left - 1, top - 1, left + listWidth + 1, top + listHeight + 1, -6250336);
        Gui.drawRect(left, top, left + listWidth, top + listHeight, -16777216);
    }
}
