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

import net.aeronica.mods.mxtune.caches.DirectoryWatcher;
import net.aeronica.mods.mxtune.caches.FileHelper;
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuiFileSelector extends GuiScreen
{
    private static final String TITLE = I18n.format("mxtune.gui.guiFileSelector.title");
    private static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    private int guiLeft;
    private int guiTop;
    private GuiScreen guiScreenParent;
    private boolean isStateCached;
    private int cachedSelectedIndex;
    private String cachedStatus;
    private boolean midiUnavailable;

    private GuiFileList guiFileList;
    private int entryHeight;

    private GuiTextField textStatus;
    private GuiButton buttonCancel;

    private List<Path> mmlFiles;
    private boolean watcherStarted = false;

    private DirectoryWatcher watcher;

    public GuiFileSelector(@Nullable GuiScreen guiScreenParent)
    {
        this.guiScreenParent = guiScreenParent;
        mc = Minecraft.getMinecraft();
        fontRenderer = mc.fontRenderer;
        midiUnavailable = MIDISystemUtil.midiUnavailable();

        // refresh the file list automatically - might be better to not bother the extension filtering but we'll see
        DirectoryStream.Filter<Path> filter = entry ->
                (entry.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip")
                         || entry.toString().toLowerCase(Locale.ENGLISH).endsWith(".mml")
                         || entry.toString().toLowerCase(Locale.ENGLISH).endsWith(".ms2mml"));
        watcher = new DirectoryWatcher.Builder()
                .addDirectories(FileHelper.getDirectory(FileHelper.CLIENT_MML_FOLDER))
                .setPreExistingAsCreated(true)
                .setFilter(filter::accept)
                .build((event, path) ->
                       {
                           switch (event)
                           {
                               case ENTRY_CREATE:
                               case ENTRY_MODIFY:
                               case ENTRY_DELETE:
                                   initGui();
                           }
                       });
    }

    @Override
    public void onGuiClosed()
    {
        stopWatcher();
    }

    private void startWatcher()
    {
        if (!watcherStarted)
            try
            {
                watcherStarted = true;
                watcher.start();
            }
            catch (Exception e)
            {
                watcherStarted = false;
                ModLogger.error(e);
            }
    }

    private void stopWatcher()
    {
        if (watcherStarted)
            watcher.stop();
    }

    @Override
    public void initGui()
    {
        initFileList();
        buttonList.clear();
        this.guiLeft = 0;
        this.guiTop = 0;
        int guiListWidth = width - 10;
        entryHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int left = 5;
        int listTop = 20;
        int listHeight = height - 10 - 30 - 30;
        int listBottom = listTop + listHeight;
        int statusTop = listBottom + 7;
        int buttonTop = height - 25;

        guiFileList = new GuiFileList(this, mmlFiles, guiListWidth, listHeight, listTop, listBottom, left);

        textStatus = new GuiTextField(0, fontRenderer, left, statusTop, guiListWidth, entryHeight + 2);
        textStatus.setFocused(false);
        textStatus.setCanLoseFocus(true);
        textStatus.setEnabled(false);
        textStatus.setMaxStringLength(80);
        textStatus.setDisabledTextColour(0xFFFF00);

        int xOpen = (width /2) - 75 * 2;
        int xRefresh = xOpen + 75;
        int xDone = xRefresh + 75;
        int xCancel = xDone + 75;
        GuiButtonHooverText buttonOpen = new GuiButtonHooverText(2, xOpen, buttonTop, 75, 20, I18n.format("mxtune.gui.guiFileSelector.openFolder"));
        buttonOpen.addHooverText(TextFormatting.YELLOW + I18n.format("mxtune.gui.guiFileSelector.openFolder.help"));
        GuiButtonHooverText buttonRefresh = new GuiButtonHooverText(3, xRefresh, buttonTop, 75, 20, I18n.format("mxtune.gui.guiFileSelector.refresh"));
        buttonRefresh.addHooverText(TextFormatting.YELLOW + I18n.format("mxtune.gui.guiFileSelector.refresh.help"));
        GuiButton buttonOkay = new GuiButton(0, xDone, buttonTop, 75, 20, I18n.format("gui.done"));
        buttonCancel = new GuiButton(1, xCancel, buttonTop, 75, 20, I18n.format("gui.cancel"));

        buttonList.add(buttonOkay);
        buttonList.add(buttonCancel);
        buttonList.add(buttonOpen);
        buttonList.add(buttonRefresh);
        reloadState();
        startWatcher();
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        guiFileList.elementClicked(cachedSelectedIndex, false);
        cachedSelectedIndex = guiFileList.getSelectedIndex();
        textStatus.setText(cachedStatus);
    }

    private void updateState()
    {
        cachedSelectedIndex = guiFileList.getSelectedIndex();
        cachedStatus = textStatus.getText();
        this.isStateCached = true;
    }

    @Override
    public void updateScreen()
    {
        cachedSelectedIndex = guiFileList.getSelectedIndex();
        guiFileList.elementClicked(cachedSelectedIndex, false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        String title;
        if (midiUnavailable)
            title = TITLE + " - " + TextFormatting.RED + MIDI_NOT_AVAILABLE;
        else
            title = TITLE;
        /* draw "TITLE" at the top middle */
        int posX = (this.width - mc.fontRenderer.getStringWidth(title)) / 2 ;
        int posY = 5;
        mc.fontRenderer.drawStringWithShadow(title, posX, posY, 0xD3D3D3);

        guiFileList.drawScreen(mouseX, mouseY, partialTicks);
        textStatus.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawHooveringButtonHelp(mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 0:
                // Done
                ActionGetPath.INSTANCE.select(selectedFile());
                ModLogger.info("GuiFileSelector::select %s", ActionGetPath.INSTANCE.getFileNameString());
                mc.displayGuiScreen(guiScreenParent);
                break;
            case 1:
                // Cancel
                mc.displayGuiScreen(guiScreenParent);
                break;
            case 2:
                // Open Folder
                openFolder();
                break;
            case 3:
                // Refresh File List
                refresh();
                break;
            default:
        }
        updateState();
        super.actionPerformed(button);
    }

    private Path selectedFile()
    {
        int selectedIndex = guiFileList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex > mmlFiles.size() || mmlFiles.isEmpty())
            return null;
        else
            return mmlFiles.get(selectedIndex);
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

        guiFileList.handleMouseInput(mouseX, mouseY);
        super.handleMouseInput();
    }

    private static class GuiFileList extends GuiScrollingList
    {
        private List<Path> mmlFiles;
        private FontRenderer fontRenderer;

        GuiFileList(GuiFileSelector parent, List<Path> mmlFilesIn, int width, int height, int top, int bottom, int left)
        {
            super(parent.mc, width, height, top, bottom, left, parent.entryHeight, parent.width, parent.height);
            this.mmlFiles = mmlFilesIn;
            this.fontRenderer = parent.mc.fontRenderer;
        }

        int getRight() {return right;}

        int getSelectedIndex() { return selectedIndex; }

        @Override
        protected int getSize()
        {
            return mmlFiles.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick)
        {
            if (index == selectedIndex) return;
            selectedIndex = (index >= 0 && index <= mmlFiles.size() ? index : -1);
        }

        @Override
        protected boolean isSelected(int index)
        {
            return index == selectedIndex && selectedIndex >= 0 && selectedIndex <= mmlFiles.size();
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
            String name = (mmlFiles.get(slotIdx).getFileName().toString());
            String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
            fontRenderer.drawStringWithShadow(trimmedName, (float)left + 3, slotTop, 0xADD8E6);
        }
    }

    private void initFileList()
    {
        Path path = FileHelper.getDirectory(FileHelper.CLIENT_MML_FOLDER);
        PathMatcher filter = FileHelper.getMMLMatcher(path);
        try (Stream<Path> paths = Files.list(path))
        {
            mmlFiles = paths
                    .filter(filter::matches)
                    .collect(Collectors.toList());
        }
        catch (NullPointerException | IOException e)
        {
            textStatus.setText(e.getMessage());
            ModLogger.error(e);
        }
    }

    private void openFolder()
    {
        FileHelper.openFolder(FileHelper.CLIENT_MML_FOLDER);
    }

    private void refresh()
    {
        initGui();
    }

    private void drawHooveringButtonHelp(int mouseX, int mouseY)
    {
        for(GuiButton b : buttonList)
            if (isMouseOverButton(b, mouseX, mouseY))
                this.drawHoveringText(((GuiButtonHooverText) b).getHooverTexts(), mouseX, mouseY);
    }

    private <T extends GuiButton> boolean isMouseOverButton( T button, int mouseX, int mouseY)
    {
        return (button instanceof GuiButtonHooverText) && isPointInRegion(button.x - guiLeft, button.y - guiTop, button.width, button.height, mouseX, mouseY);
    }

    private boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY)
    {
        int i = this.guiLeft;
        int j = this.guiTop;
        pointX = pointX - i;
        pointY = pointY - j;
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }
}
