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

import net.aeronica.mods.mxtune.caches.MXTuneFile;
import net.aeronica.mods.mxtune.gui.util.GuiLabelMX;
import net.aeronica.mods.mxtune.gui.util.IHooverText;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiMXT extends GuiScreen
{
    private List<IHooverText> hooverTexts = new ArrayList<>();
    private GuiLabelMX labelTitle;
    private GuiScreen guiScreenParent;
    private boolean isStateCached;

    // Common data
    MXTuneFile mxTuneFile;

    // Child tabs
    private static final int MAX_TABS = 12;
    private static final int MIN_TABS = 1;
    private static final int TAB_BTN_IDX = 200;
    private GuiMXTPartTab[] childTabs = new GuiMXTPartTab[MAX_TABS];
    private int activeChildIndex;
    private int cachedActiveChildIndex;

    // Tab limits - allow limiting the viewable tabs
    private int viewableTabCount = MIN_TABS;
    private int cachedViewableTabCount;

    public GuiMXT(GuiScreen guiScreenParent)
    {
        this.guiScreenParent = guiScreenParent;
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        for (int i = 0; i< MAX_TABS; i++)
        {
            childTabs[i] = new GuiMXTPartTab(this);
        }
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void initGui()
    {
        hooverTexts.clear();
        int singleLineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int padding = 4;
        int titleTop = padding;
        int left = padding;
        int titleWidth = fontRenderer.getStringWidth("GuiMXT");
        int titleX = (width / 2) - (titleWidth / 2);
        int middle = height / 2;

        labelTitle = new GuiLabelMX(fontRenderer, 1, titleX, titleTop, titleWidth, singleLineHeight, -1);
        labelTitle.setLabel("GuiMXT");

        // Button tabs
        for (int i = 0; i< MAX_TABS; i++)
        {
            buttonList.add(new GuiButton(TAB_BTN_IDX + i, 5 + 20 * i, middle - 25, 20, 20, String.format("%d", i + 1)));
            childTabs[i].setLayout(middle, height - 5, height - 5 - middle);
            childTabs[i].initGui();
        }

        reloadState();
    }

    private void reloadState()
    {
        updateButtons();
        if (!isStateCached) return;
        activeChildIndex = cachedActiveChildIndex;
        viewableTabCount = cachedViewableTabCount;
    }

    private void updateState()
    {
        cachedActiveChildIndex = activeChildIndex;
        cachedViewableTabCount = viewableTabCount;
        isStateCached = true;
    }

    private void updateButtons()
    {
        for (GuiButton button : buttonList)
            if (button.id >= TAB_BTN_IDX && button.id < (MAX_TABS + TAB_BTN_IDX))
                button.enabled = (activeChildIndex + TAB_BTN_IDX) != button.id;

        for (GuiButton button : buttonList)
            if (button.id >= TAB_BTN_IDX && button.id < (MAX_TABS + TAB_BTN_IDX))
            {
                button.visible = (button.id) < (viewableTabCount + TAB_BTN_IDX);
                if (activeChildIndex >= viewableTabCount)
                    activeChildIndex = viewableTabCount - 1;
            }
    }

    @Override
    public void updateScreen()
    {
        childTabs[activeChildIndex].updateScreen();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id >= TAB_BTN_IDX && button.id < TAB_BTN_IDX + MAX_TABS)
        {
            this.activeChildIndex = button.id - TAB_BTN_IDX;
            this.childTabs[activeChildIndex].onResize(mc, width, height);
            ModLogger.info("Tab: %d", button.id - TAB_BTN_IDX - 1);
        }
        updateState();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        childTabs[activeChildIndex].keyTyped(typedChar, keyCode);

        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();

        labelTitle.drawLabel(mc, mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
        childTabs[activeChildIndex].drawScreen(mouseX, mouseY, partialTicks);
        ModGuiUtils.INSTANCE.drawHooveringHelp(this, hooverTexts, 0, 0, mouseX, mouseY);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        updateState();
        childTabs[activeChildIndex].handleMouseInput();
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Called when a mouse button is released.
     */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h)
    {
        super.onResize(mcIn, w, h);
        childTabs[activeChildIndex].onResize(mcIn, w, h);
    }
}
