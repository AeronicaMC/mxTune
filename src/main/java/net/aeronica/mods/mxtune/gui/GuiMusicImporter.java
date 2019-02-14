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

import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuiMusicImporter extends GuiScreen
{
    private static final String TITLE = I18n.format("mxtune.gui.guiMusicImporter.title");
    private static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    private GuiScreen guiScreenParent;
    private int guiLeft;
    private int guiTop;
    private boolean isStateCached;
    private boolean midiUnavailable;
    private ActionGet.SELECTOR selector = ActionGet.SELECTOR.CANCEL;

    private List<String> musicParts;
    private int entryHeight;
    private GuiImportList guiImportList;
    private GuiTextField musicTitle;
    private GuiTextField musicAuthor;
    private GuiTextField musicSource;
    private GuiTextField statusText;
    private GuiButton buttonCancel;
    private List<GuiButton> safeButtonList;

    public GuiMusicImporter(GuiScreen guiScreenParent)
    {
        this.guiScreenParent = guiScreenParent;
        mc = Minecraft.getMinecraft();
        fontRenderer = mc.fontRenderer;
        midiUnavailable = MIDISystemUtil.midiUnavailable();
    }

    @Override
    public void initGui()
    {
        initImportList();
        buttonList.clear();
        this.guiLeft = 0;
        this.guiTop = 0;
        int guiListWidth = width - 10;
        entryHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int left = 5;
        int titleTop = 20;
        int authorTop = titleTop + entryHeight;
        int sourceTop = authorTop + entryHeight;
        int listTop = sourceTop + entryHeight;
        int listHeight = height - (entryHeight * 3) - 10 - 30 - 30;
        int listBottom = listTop + listHeight;
        int statusTop = listBottom;

        musicTitle = new GuiTextField(0,fontRenderer, left, titleTop, guiListWidth, entryHeight);
        musicAuthor = new GuiTextField(1, fontRenderer, left, authorTop, guiListWidth, entryHeight);
        musicSource = new GuiTextField(2, fontRenderer, left, sourceTop, guiListWidth, entryHeight);
        guiImportList = new GuiImportList(this, musicParts, guiListWidth, listHeight, listTop, listBottom, left);
        statusText = new GuiTextField(3, fontRenderer, left, statusTop, guiListWidth, entryHeight);
        statusText.setFocused(false);
        statusText.setEnabled(false);

        int buttonTop = height - 25;
        int xFiles = (width /2) - 75 * 2;
        int xPaste = xFiles + 75;
        int xDone = xPaste + 75;
        int xCancel = xDone + 75;
        GuiButton buttonDone = new GuiButton(0, xDone, buttonTop, 75, 20, I18n.format("gui.done"));
        buttonCancel = new GuiButton(1, xCancel, buttonTop, 75, 20, I18n.format("gui.cancel"));
        GuiButton buttonFiles = new GuiButton(2, xFiles, buttonTop, 75, 20, I18n.format("mxtune.gui.button.pickFile"));
        GuiButton buttonPaste = new GuiButton(3, xPaste, buttonTop, 75, 20, I18n.format("mxtune.gui.button.pasteMML"));

        buttonList.add(buttonDone);
        buttonList.add(buttonCancel);
        buttonList.add(buttonPaste);
        buttonList.add(buttonFiles);
        safeButtonList = new CopyOnWriteArrayList<>(buttonList);
        reloadState();
        getSelection();
    }

    private void reloadState()
    {
        if (!isStateCached) return;

    }

    private void updateState()
    {

        this.isStateCached = true;
    }

    @Override
    public void updateScreen()
    {
        musicTitle.updateCursorCounter();
        musicAuthor.updateCursorCounter();
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        String guiTitle;
        if (midiUnavailable)
            guiTitle = TITLE + " - " + TextFormatting.RED + MIDI_NOT_AVAILABLE;
        else
            guiTitle = TITLE;
        /* draw "TITLE" at the top middle */
        int posX = (this.width - mc.fontRenderer.getStringWidth(guiTitle)) / 2 ;
        int posY = 5;
        mc.fontRenderer.drawStringWithShadow(guiTitle, posX, posY, 0xD3D3D3);

        musicTitle.drawTextBox();
        musicAuthor.drawTextBox();
        musicSource.drawTextBox();
        guiImportList.drawScreen(mouseX, mouseY, partialTicks);
        statusText.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
        HooverHelper.INSTANCE.drawHooveringButtonHelp(this, safeButtonList, guiLeft, guiTop, mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 0:
                // Done
                mc.displayGuiScreen(guiScreenParent);
                break;
            case 1:
                // Cancel
                mc.displayGuiScreen(guiScreenParent);
                break;
            case 2:
                // Get File
                getFile();
                break;
            case 3:
                // Get Paste
                getPaste();
                break;
            default:
        }
        updateState();
        super.actionPerformed(button);
    }

    private void getFile()
    {
        selector = ActionGet.SELECTOR.FILE;
        ActionGet.INSTANCE.clear();
        mc.displayGuiScreen(new GuiFileSelector(this));
    }

    private void getPaste()
    {
        selector = ActionGet.SELECTOR.PASTE;
        ActionGet.INSTANCE.clear();
        mc.displayGuiScreen(new GuiMusicPaperParse(this));
    }

    private void getSelection()
    {
        switch (selector)
        {
            case FILE:
                ModLogger.debug("File: %s", ActionGet.INSTANCE.getFileNameString());
                musicTitle.setText(ActionGet.INSTANCE.getFileNameString());
                musicAuthor.setText(ActionGet.INSTANCE.getAuthor());
                musicSource.setText(ActionGet.INSTANCE.getSource());
                break;
            case PASTE:
                ModLogger.debug("Paste: %s", ActionGet.INSTANCE.getTitle());
                musicTitle.setText(ActionGet.INSTANCE.getTitle());
                musicAuthor.setText(ActionGet.INSTANCE.getAuthor());
                musicSource.setText(ActionGet.INSTANCE.getSource());
                musicParts.clear();

                musicParts.addAll(Arrays.asList(ActionGet.INSTANCE.getMml().replaceFirst("MML@|;", "").split(",")));
                break;
            case CANCEL:
                break;
            default:
        }
        selector = ActionGet.SELECTOR.CANCEL;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        // capture the ESC key to close cleanly
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            this.actionPerformed(buttonCancel);
            return;
        }
        /* add char to GuiTextField */
        musicTitle.textboxKeyTyped(typedChar, keyCode);
        musicAuthor.textboxKeyTyped(typedChar, keyCode);
        musicSource.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_TAB)
        {
            if (musicTitle.isFocused())
            {
                musicAuthor.setFocused(true);
                musicSource.setFocused(false);
                musicTitle.setFocused(false);
            }
            else if (musicAuthor.isFocused())
            {
                musicAuthor.setFocused(false);
                musicSource.setFocused(true);
                musicTitle.setFocused(false);
            }
            else if (musicSource.isFocused())
            {
                musicAuthor.setFocused(false);
                musicSource.setFocused(false);
                musicTitle.setFocused(true);
            }
        }
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

        guiImportList.handleMouseInput(mouseX, mouseY);
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        musicTitle.mouseClicked(mouseX, mouseY, mouseButton);
        musicAuthor.mouseClicked(mouseX, mouseY, mouseButton);
        musicSource.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        updateState();
    }

    private static class GuiImportList extends GuiScrollingList
    {
        private List<String> musicParts;
        private FontRenderer fontRenderer;

        GuiImportList(GuiMusicImporter parent, List<String> musicParts, int width, int height, int top, int bottom, int left)
        {
            super(parent.mc, width, height, top, bottom, left, parent.entryHeight, parent.width, parent.height);
            this.musicParts = musicParts;
            this.fontRenderer = parent.mc.fontRenderer;
        }

        int getRight() {return right;}

        int getSelectedIndex() { return selectedIndex; }

        @Override
        protected int getSize()
        {
            return musicParts.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick)
        {
            if (index == selectedIndex) return;
            selectedIndex = (index >= 0 && index <= musicParts.size() ? index : -1);
        }

        @Override
        protected boolean isSelected(int index)
        {
            return index == selectedIndex && selectedIndex >= 0 && selectedIndex <= musicParts.size();
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
            String name = (musicParts.get(slotIdx));
            String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
            fontRenderer.drawStringWithShadow(trimmedName, (float)left + 3, slotTop, 0xADD8E6);
        }
    }

    private void initImportList()
    {
        musicParts = new ArrayList();
        musicParts.add("Part 01");
        musicParts.add("Part 02");
        musicParts.add("Part 03");
        musicParts.add("Part 04");
        musicParts.add("Part 05");
        musicParts.add("Part 06");
        musicParts.add("Part 07");
        musicParts.add("Part 08");
        musicParts.add("Part 09");
        musicParts.add("Part 10");
        musicParts.add("Part 11");
    }
}
