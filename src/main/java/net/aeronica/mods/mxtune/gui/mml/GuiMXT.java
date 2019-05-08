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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiMXT extends GuiScreen
{
    private List<IHooverText> hooverTexts = new ArrayList<>();
    private GuiScreen guiScreenParent;
    private boolean isStateCached;
    private GuiLabelMX labelMXTFileName;
    private String cachedMXTFilename;
    private GuiButtonExt buttonImport;
    private GuiButtonExt buttonOpen;
    private GuiButtonExt buttonSave;
    private GuiButtonExt buttonSaveAs;
    private GuiButtonExt buttonDone;
    private GuiLabelMX labelTitle;
    private GuiLabelMX labelAuthor;
    private GuiLabelMX labelSource;
    private GuiTextField textTitle;
    private GuiTextField textAuthor;
    private GuiTextField textSource;
    private GuiButtonExt buttonPlayStop;

    // Common data
    MXTuneFile mxTuneFile;

    // Child tabs
    private static final int MAX_TABS = 12;
    private static final int MIN_TABS = 1;
    private static final int TAB_BTN_IDX = 200;
    private GuiMXTPartTab[] childTabs = new GuiMXTPartTab[MAX_TABS];
    private int activeChildIndex;
    private int cachedActiveChildIndex;
    private GuiButtonExt buttonAddTab;

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
        int titleWidth = width - padding * 2;
        int tabbedAreaTop = (height * 2) / 5;

        labelMXTFileName = new GuiLabelMX(fontRenderer, 0, padding, padding, titleWidth, singleLineHeight, -1);
        setDisplayedFilename(cachedMXTFilename);
        int buttonWidth = Math.max(((width * 2 / 3)- padding * 2)/ 5, 75);
        int buttonY = labelMXTFileName.y + labelMXTFileName.height;

        buttonImport = new GuiButtonExt(1, padding, buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.import"));
        buttonOpen = new GuiButtonExt(2, buttonImport.x + buttonImport.width, buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.open"));
        buttonSave = new GuiButtonExt(3, buttonOpen.x + buttonOpen.width, buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.save"));
        buttonSaveAs = new GuiButtonExt(4, buttonSave.x + buttonSave.width, buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.save_as"));
        buttonDone = new GuiButtonExt(5, buttonSaveAs.x + buttonSaveAs.width, buttonY, buttonWidth, 20, I18n.format("gui.done"));
        buttonList.add(buttonImport);
        buttonList.add(buttonOpen);
        buttonList.add(buttonSave);
        buttonList.add(buttonSaveAs);
        buttonList.add(buttonDone);

        int textY = buttonDone.y + buttonDone.height + padding;
        String labelTitleText = I18n.format("mxtune.gui.label.title");
        int labelTitleWidth = fontRenderer.getStringWidth(labelTitleText);
        String labelAuthorText = I18n.format("mxtune.gui.label.author");
        int labelAuthorWidth = fontRenderer.getStringWidth(labelAuthorText);
        labelTitle = new GuiLabelMX(fontRenderer, 1, padding, textY, labelTitleWidth, fontRenderer.FONT_HEIGHT + 2, -1);
        labelTitle.setLabel(labelTitleText);
        textTitle = new GuiTextField(1, fontRenderer, labelTitle.x + labelTitleWidth + padding, textY, width / 2 - labelTitle.width - padding, fontRenderer.FONT_HEIGHT + 2);
        textTitle.setMaxStringLength(80);
        textTitle.setCanLoseFocus(true);
        labelAuthor = new GuiLabelMX(fontRenderer, 2, textTitle.x + textTitle.width + padding, textY, labelAuthorWidth, fontRenderer.FONT_HEIGHT + 2, -1);
        labelAuthor.setLabel(labelAuthorText);
        textAuthor = new GuiTextField(2, fontRenderer, labelAuthor.x + labelAuthorWidth + padding, textY, width - labelAuthor.x - labelAuthor.width - 1 - padding * 2, fontRenderer.FONT_HEIGHT + 2);
        textAuthor.setMaxStringLength(80);
        textAuthor.setCanLoseFocus(true);
        textY = textTitle.y + textTitle.height + padding;
        String labelSourceText = I18n.format("mxtune.gui.label.source");
        int labelSourceWidth = fontRenderer.getStringWidth(labelSourceText);
        labelSource = new GuiLabelMX(fontRenderer, 3, padding, textY, labelSourceWidth, fontRenderer.FONT_HEIGHT + 2, -1);
        labelSource.setLabel(labelSourceText);
        textSource = new GuiTextField(3, fontRenderer, labelSource.x + labelSource.width + padding, textY, width - labelSource.x - labelSource.width - 1 - padding * 2, fontRenderer.FONT_HEIGHT + 2);
        textSource.setMaxStringLength(320);
        textSource.setCanLoseFocus(true);

        // Button tabs
        for (int i = 0; i< MAX_TABS; i++)
        {
            buttonList.add(new GuiButton(TAB_BTN_IDX + i, 5 + 20 * i, tabbedAreaTop - 25, 20, 20, String.format("%d", i + 1)));
            childTabs[i].setLayout(tabbedAreaTop, height - 5, height - 5 - tabbedAreaTop);
            childTabs[i].initGui();
        }
        buttonAddTab = new GuiButtonExt(250, 5 + MAX_TABS * 20 + 20, tabbedAreaTop - 25, 20, 20, "+");
        buttonList.add(buttonAddTab);

        buttonPlayStop = new GuiButtonExt(6, width - 5 - 100, tabbedAreaTop - 25, 100, 20, "Play/Stop");
        buttonList.add(buttonPlayStop);
        reloadState();
    }

    private void reloadState()
    {
        updateButtons();
        if (!isStateCached) return;
        labelMXTFileName.setLabel(cachedMXTFilename);
        activeChildIndex = cachedActiveChildIndex;
        viewableTabCount = cachedViewableTabCount;
    }

    private void updateState()
    {
        cachedMXTFilename = labelMXTFileName.getLabel();
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
        buttonAddTab.x = 5 + viewableTabCount * 20;
        buttonAddTab.enabled = viewableTabCount < 12;
    }

    @Override
    public void updateScreen()
    {
        textTitle.updateCursorCounter();
        textAuthor.updateCursorCounter();
        textSource.updateCursorCounter();
        childTabs[activeChildIndex].updateScreen();
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id >= TAB_BTN_IDX && button.id < TAB_BTN_IDX + MAX_TABS)
        {
            this.activeChildIndex = button.id - TAB_BTN_IDX;
            this.childTabs[activeChildIndex].onResize(mc, width, height);
        }
        switch (button.id)
        {
            case 250:
                // Add Tab
                viewableTabCount = (viewableTabCount + 1) > MAX_TABS ? viewableTabCount : viewableTabCount + 1;
                break;
            default:
        }
        updateButtons();
        updateState();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        childTabs[activeChildIndex].keyTyped(typedChar, keyCode);
        textTitle.textboxKeyTyped(typedChar, keyCode);
        textAuthor.textboxKeyTyped(typedChar, keyCode);
        textSource.textboxKeyTyped(typedChar, keyCode);
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

        labelMXTFileName.drawLabel(mc, mouseX, mouseY);
        labelTitle.drawLabel(mc, mouseX, mouseY);
        textTitle.drawTextBox();
        labelAuthor.drawLabel(mc, mouseX, mouseY);
        textAuthor.drawTextBox();
        labelSource.drawLabel(mc, mouseX, mouseY);
        textSource.drawTextBox();
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
        textTitle.mouseClicked(mouseX, mouseY, mouseButton);
        textAuthor.mouseClicked(mouseX, mouseY, mouseButton);
        textSource.mouseClicked(mouseX, mouseY, mouseButton);
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
    public void onResize(@Nonnull Minecraft mcIn, int w, int h)
    {
        super.onResize(mcIn, w, h);
        childTabs[activeChildIndex].onResize(mcIn, w, h);
    }

    private void setDisplayedFilename(@Nullable String name)
    {
        if (name == null) name = "";
        labelMXTFileName.setLabel(I18n.format("mxtune.gui.guiMXT.displayedFilename", name));
    }
}
