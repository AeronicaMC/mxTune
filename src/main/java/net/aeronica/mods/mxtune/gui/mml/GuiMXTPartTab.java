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

package net.aeronica.mods.mxtune.gui.mml;

import net.aeronica.mods.mxtune.caches.MXTunePart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

public class GuiMXTPartTab extends GuiTabExtended
{
    private final GuiMXT guiMXT;
    private final Minecraft mc;
    private final GuiTabExtended.IGuiPartTab[] partTabs;
    private int maxTabLabelWidth;

    public GuiMXTPartTab(GuiMXT guiMXT, int topIn, int bottomIn, int slotHeightIn)
    {
        super(guiMXT.mc, guiMXT.width, guiMXT.height, topIn, bottomIn, slotHeightIn);
        this.mc = guiMXT.mc;
        this.guiMXT = guiMXT;

        MXTunePart[] mxTuneParts = (MXTunePart[])ArrayUtils.clone(guiMXT.mxTuneFile.getParts().toArray());
        this.partTabs = new GuiMXTPartTab.IGuiPartTab[mxTuneParts.length];
    }

    @Override
    protected int getSize()
    {
        return partTabs.length;
    }

    /**
     * Gets the IGuiPartTab object for the given index
     */
    @Override
    public GuiTabExtended.IGuiPartTab getTabEntry(int index)
    {
        return partTabs[index];
    }

    @Override
    protected int getScrollBarX()
    {
        return super.getScrollBarX() + 35;
    }

    /**
     * Gets the width of the list
     */
    public int getListWidth()
    {
        return super.getListWidth() + 32;
    }

    @SideOnly(Side.CLIENT)
    public class PartNameTabEntry implements GuiTabExtended.IGuiPartTab
    {
        private final String labelText;
        private final int labelWidth;

        public PartNameTabEntry(String name)
        {
            this.labelText = I18n.format(name);
            this.labelWidth = GuiMXTPartTab.this.mc.fontRenderer.getStringWidth(this.labelText);
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
        {
            GuiMXTPartTab.this.mc.fontRenderer.drawString(this.labelText, GuiMXTPartTab.this.mc.currentScreen.width / 2 - this.labelWidth / 2, y + slotHeight - GuiMXTPartTab.this.mc.fontRenderer.FONT_HEIGHT - 1, 16777215);
        }

        /**
         * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
         * clicked and the list should not be dragged.
         */
        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) { return false; }

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) { /* NOP */ }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) { /* NOP */ }
    }

    @SideOnly(Side.CLIENT)
    public class PartEntry implements GuiMXTPartTab.IGuiPartTab
    {
        private final String partDecc;

        public PartEntry(String name)
        {
            partDecc = name;
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
        {
            GuiMXTPartTab.this.mc.fontRenderer.drawString(this.partDecc, x + 90 - GuiMXTPartTab.this.maxTabLabelWidth, y + slotHeight / 2 - GuiMXTPartTab.this.mc.fontRenderer.FONT_HEIGHT / 2, 16777215);
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {

        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) { /*NOP */ }
    }
}
