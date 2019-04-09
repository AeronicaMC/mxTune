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

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * <p>Creates a multi entry selection list.</p>
 * <p>To select items hold the left control key and left click on each item with the mouse</p>
 * @param <E> type of list entries to be presented.
 */
public abstract class GuiScrollingMultiListOf<E> extends GuiScrollingListOf<E>
{
    protected SortedSet<Integer> selectedRowIndexes = new TreeSet<>();

    public <T extends GuiScreen> GuiScrollingMultiListOf(T gui, int entryHeight, int width, int height, int top, int bottom, int left)
    {
        super(gui, entryHeight, width, height, top, bottom, left);
        enableHighlightSelected(false);
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick)
    {
        super.elementClicked(index, doubleClick);

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && !selectedRowIndexes.contains(selectedIndex))
            selectedRowIndexes.add(selectedIndex);
        else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && selectedRowIndexes.contains(selectedIndex))
            selectedRowIndexes.remove(selectedIndex);
        else if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
            selectedRowIndexes.clear();

        if (doubleClick)
            selectedDoubleClickedCallback(selectedIndex);
        else
            selectedClickedCallback(selectedIndex);

        this.selectedIndex = -1;
    }

    /**
     * Gets an immutable copy of the selected indexes.
     * @return
     */
    public Set<Integer> getSelectedRowIndexes()
    {
        return Collections.unmodifiableSet(selectedRowIndexes);
    }

    /**
     * Intended to restore a cached set of indexes. Typically to restore the indexes when the screen is resized.
     * @param set
     */
    public void setSelectedRowIndexes(Set<Integer> set)
    {
        selectedRowIndexes.clear();
        selectedRowIndexes.addAll(set);
    }

    public int getSelectedRowsCount()
    {
        return selectedRowIndexes.size();
    }

    public List<E> getSelectedRows()
    {
        List<E> selectedData = new ArrayList<>();
        for (Integer index : selectedRowIndexes)
        {
            selectedData.add(get(index));
        }
        return selectedData;
    }

    public void deleteSelectedRows()
    {
        synchronized (this)
        {
            // remove starting from last to preserve order and index relevance
            for (int i = this.getSize(); i >= 0; i--)
            {
                if (selectedRowIndexes.contains(i))
                {
                    this.remove(i);
                    selectedRowIndexes.remove(i);
                }
            }
        }
    }

    @Override
    protected void selectedClickedCallback(int selectedIndex) {/* NOP */}

    @Override
    protected void selectedDoubleClickedCallback(int selectedIndex) {/* NOP */}

    private float getScrollDistance()
    {
        return this.scrollDistance;
    }

    private void enableHighlightSelected(boolean state)
    {
        this.highlightSelected = state;
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
    {
        if (selectedRowIndexes.contains(slotIdx)|| isSelected(slotIdx)) drawHighlight(slotIdx , getScrollDistance(), tess);
        drawSlot(slotIdx, entryRight, slotTop, slotBuffer, getScrollDistance(), tess);
    }

    protected abstract void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess);

    // Copied and modified from the GuiScrollingList base class.
    // I know, I know, I should write a new version of the parent class
    private void drawHighlight(int slotIdx, float scrollDistance, Tessellator tess)
    {
        BufferBuilder box = tess.getBuffer();
        int scrollBarWidth = 6;
        int scrollBarRight = this.left + this.listWidth;
        int scrollBarLeft  = scrollBarRight - scrollBarWidth;
        int entryRight     = scrollBarLeft - 1;
        int border         = 4;
        int baseY = this.top + border - (int)scrollDistance;
        int slotTop = baseY + slotIdx * this.slotHeight;
        int slotBuffer = this.slotHeight - border;

        if (slotTop <= this.bottom && slotTop + slotBuffer >= this.top &&
                selectedRowIndexes.contains(slotIdx) || selectedIndex == slotIdx)
        {
            int min = this.left;
            int max = entryRight;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableTexture2D();
            box.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            box.pos(min, slotTop + slotBuffer + (double) 2, 0).tex(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            box.pos(max, slotTop + slotBuffer + (double) 2, 0).tex(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            box.pos(max, slotTop - (double) 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            box.pos(min, slotTop - (double) 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            box.pos(min + (double) 1, slotTop + slotBuffer + (double) 1, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            box.pos(max - (double) 1, slotTop + slotBuffer + (double) 1, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            box.pos(max - (double) 1, slotTop - (double) 1, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            box.pos(min + (double) 1, slotTop - (double) 1, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            tess.draw();
            GlStateManager.enableTexture2D();
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof List<?> && super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }
}
