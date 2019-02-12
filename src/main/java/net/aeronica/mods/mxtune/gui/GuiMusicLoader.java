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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuiMusicLoader extends GuiScreen
{
    private static final String TITLE = I18n.format("mxtune.gui.guiMusicLoader.title");
    private static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    private GuiScreen guiScreenParent;
    private int guiLeft;
    private int guiTop;
    private boolean isStateCached;
    private boolean midiUnavailable;
    private SELECTOR selector = SELECTOR.CANCEL;

    private GuiButton buttonCancel;
    private List<GuiButton> safeButtonList;

    public GuiMusicLoader(GuiScreen guiScreenParent)
    {
        this.guiScreenParent = guiScreenParent;
        mc = Minecraft.getMinecraft();
        fontRenderer = mc.fontRenderer;
        midiUnavailable = MIDISystemUtil.midiUnavailable();
    }

    @Override
    public void initGui()
    {
        buttonList.clear();
        this.guiLeft = 0;
        this.guiTop = 0;

        int buttonTop = height - 25;
        int xFiles = (width /2) - 75 * 2;
        int xPaste = xFiles + 75;
        int xDone = xPaste + 75;
        int xCancel = xDone + 75;
        GuiButton buttonDone = new GuiButton(0, xDone, buttonTop, 75, 20, I18n.format("gui.done"));
        buttonCancel = new GuiButton(1, xCancel, buttonTop, 75, 20, I18n.format("gui.cancel"));
        GuiButton buttonFiles = new GuiButton(2, xFiles, buttonTop, 75, 20, I18n.format("mxtune.gui.button.pickFile"));
        GuiButton buttonPaste = new GuiButton(3, xPaste, buttonTop, 75, 20, I18n.format("mxtune.gui.button.pasteMML"));

        safeButtonList = new CopyOnWriteArrayList<>(buttonList);
        buttonList.add(buttonDone);
        buttonList.add(buttonCancel);
        buttonList.add(buttonPaste);
        buttonList.add(buttonFiles);
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
        selector = SELECTOR.FILE;
        ActionGet.INSTANCE.select(null);
        mc.displayGuiScreen(new GuiFileSelector(this));
    }

    private void getPaste()
    {
        selector = SELECTOR.PASTE;
        ActionGet.INSTANCE.select(null, null);
        mc.displayGuiScreen(new GuiMusicPaperParse(this));
    }

    private void getSelection()
    {
        switch (selector)
        {
            case FILE:
                ModLogger.info("File: %s", ActionGet.INSTANCE.getFileNameString());
                break;
            case PASTE:
                ModLogger.info("Paste: %s", ActionGet.INSTANCE.getTitle());
                break;
            case CANCEL:
                break;
            default:
        }
        selector = SELECTOR.CANCEL;
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
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

        //guiFileList.handleMouseInput(mouseX, mouseY);
        super.handleMouseInput();
    }

    enum SELECTOR
    {
        FILE,
        PASTE,
        CANCEL;
    }
}
