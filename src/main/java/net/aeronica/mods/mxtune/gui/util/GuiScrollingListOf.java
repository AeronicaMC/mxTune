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
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class GuiScrollingListOf<E> extends GuiScrollingList
{
    private ArrayList<E> arrayList = new ArrayList<>();
    protected GuiScreen gui;
    protected Minecraft mc;
    private int entryHeight;

    public <T extends GuiScreen> GuiScrollingListOf(T gui, int entryHeight, int width, int height, int top, int bottom, int left)
    {
        super(gui.mc, width, height, top, bottom, left, entryHeight, gui.width, gui.height);
        this.gui = gui;
        this.mc = gui.mc;
        this.entryHeight = entryHeight;
    }

    public float getScroll()
    {
        return ObfuscationReflectionHelper.getPrivateValue(GuiScrollingList.class, this, "scrollDistance");
    }

    public void resetScroll() {

        ObfuscationReflectionHelper.setPrivateValue(GuiScrollingList.class, this, applyScrollLimits(), "scrollDistance");
    }

    private float applyScrollLimits()
    {
        int listHeight = this.getContentHeight() - (this.bottom - this.top - 4);
        float scrollDistance = selectedIndex * entryHeight;

        if (listHeight < 0)
        {
            listHeight /= 2;
        }

        if (scrollDistance < 0.0F)
        {
            scrollDistance = 0.0F;
        }

        if (scrollDistance > (float)listHeight)
        {
            scrollDistance = (float)listHeight;
        }
        return scrollDistance;
    }

    public int getRight() {return right;}

    public int getSelectedIndex() { return selectedIndex; }

    public void setSelectedIndex(int index)
    {
        selectedIndex = index;
    }

    public int size()
    {
        return getSize();
    }

    public void clear()
    {
        arrayList.clear();
    }

    public boolean add(E e)
    {
        return this.arrayList.add(e);
    }

    public boolean addAll(Collection<? extends E> c)
    {
        return this.arrayList.addAll(c);
    }

    public E get(int index)
    {
        return this.arrayList.get(index);
    }


    @Override
    protected int getSize()
    {
        return arrayList != null ? arrayList.size() : 0;
    }

    public List<E> getList()
    {
        return arrayList;
    }

    public void elementClicked(int index)
    {
        elementClicked(index, false);
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick)
    {
        if (index == selectedIndex && !doubleClick) return;
        selectedIndex = (index >= 0 && index <= arrayList.size() && arrayList.size() != 0 ? index : -1);

        if (selectedIndex >= 0)
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
        return index == selectedIndex && selectedIndex >= 0 && selectedIndex <= arrayList.size();
    }

    @Override
    protected void drawBackground()
    {
        Gui.drawRect(left - 1, top - 1, left + listWidth + 1, top + listHeight + 1, -6250336);
        Gui.drawRect(left, top, left + listWidth, top + listHeight, -16777216);
    }
}
