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

import net.aeronica.mods.mxtune.caches.DirectoryWatcher;
import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.gui.util.GuiButtonMX;
import net.aeronica.mods.mxtune.gui.util.GuiScrollingListOf;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.aeronica.mods.mxtune.gui.mml.SortPathHelper.SortType;
import static net.aeronica.mods.mxtune.gui.mml.SortPathHelper.updateSortButtons;
import static net.aeronica.mods.mxtune.gui.util.ModGuiUtils.clearOnMouseLeftClicked;

public class GuiFileSelector extends GuiScreen
{
    private static final String TITLE = I18n.format("mxtune.gui.guiFileSelector.title");
    private static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    private int guiLeft;
    private int guiTop;
    private final GuiScreen guiScreenParent;
    private boolean isStateCached;
    private int cachedSelectedIndex;
    private boolean midiUnavailable;

    private GuiScrollingListOf<Path> guiFileList;
    private Path selectedFile;

    private GuiLabel searchLabel;
    private GuiTextField search;
    private boolean sorted = false;
    private SortType sortType = SortType.NORMAL;
    private String lastSearch = "";
    private SortType cachedSortType;

    private GuiButton buttonCancel;
    private List<GuiButton> safeButtonList;

    private List<Path> fileList = new ArrayList<>();
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
                (entry.toString().endsWith(".zip")
                         || entry.toString().endsWith(".mml")
                         || entry.toString().endsWith(".ms2mml"));
        watcher = new DirectoryWatcher.Builder()
                .addDirectories(FileHelper.getDirectory(FileHelper.CLIENT_MML_FOLDER, Side.CLIENT))
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
        buttonList.clear();
        this.guiLeft = 0;
        this.guiTop = 0;
        int guiListWidth = width - 10;
        int entryHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int left = 5;
        int titleTop = 20;
        int listTop = titleTop + 25;
        int listHeight = height - titleTop - entryHeight - 2 - 10 - 25 - 25;
        int listBottom = listTop + listHeight;
        int statusTop = listBottom + 4;

        guiFileList = new GuiScrollingListOf<Path>(this, entryHeight, guiListWidth, listHeight, listTop, listBottom, left)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                String name = (get(slotIdx).getFileName().toString());
                String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                fontRenderer.drawStringWithShadow(trimmedName, (float)left + 3, slotTop, 0xADD8E6);
            }

            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                selectedFile = get(selectedIndex);
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex)
            {
                selectDone();
            }
        };

        String searchLabelText = I18n.format("mxtune.gui.label.search");
        int searchLabelWidth =  fontRenderer.getStringWidth(searchLabelText) + 4;
        searchLabel = new GuiLabel(fontRenderer, 0, left, statusTop, searchLabelWidth, entryHeight + 2, 0xFFFFFF );
        searchLabel.addLine(searchLabelText);
        searchLabel.visible = true;
        search = new GuiTextField(0, fontRenderer, left + searchLabelWidth, statusTop, guiListWidth - searchLabelWidth, entryHeight + 2);
        search.setFocused(true);
        search.setCanLoseFocus(true);

        int buttonMargin = 1;
        int buttonWidth = 75;
        int x = left;
        GuiButton normalSort = new GuiButton(SortType.NORMAL.getButtonID(), x, titleTop, buttonWidth - buttonMargin, 20, I18n.format("fml.menu.mods.normal"));
        normalSort.enabled = false;
        buttonList.add(normalSort);
        x += buttonWidth + buttonMargin;
        buttonList.add(new GuiButton(SortType.A_TO_Z.getButtonID(), x, titleTop, buttonWidth - buttonMargin, 20, "A-Z"));
        x += buttonWidth + buttonMargin;
        buttonList.add(new GuiButton(SortType.Z_TO_A.getButtonID(), x, titleTop, buttonWidth - buttonMargin, 20, "Z-A"));

        int buttonTop = height - 25;
        int xOpen = (this.width /2) - 75 * 2;
        int xRefresh = xOpen + 75;
        int xDone = xRefresh + 75;
        int xCancel = xDone + 75;
        GuiButtonMX buttonOpen = new GuiButtonMX(2, xOpen, buttonTop, 75, 20, I18n.format("mxtune.gui.button.openFolder"));
        buttonOpen.addHooverTexts(TextFormatting.YELLOW + I18n.format("mxtune.gui.guiFileSelector.openFolder.help"));
        GuiButtonMX buttonRefresh = new GuiButtonMX(3, xRefresh, buttonTop, 75, 20, I18n.format("mxtune.gui.button.refresh"));
        buttonRefresh.addHooverTexts(TextFormatting.YELLOW + I18n.format("mxtune.gui.guiFileSelector.refresh.help"));
        GuiButton buttonDone = new GuiButton(0, xDone, buttonTop, 75, 20, I18n.format("mxtune.gui.button.select"));
        buttonCancel = new GuiButton(1, xCancel, buttonTop, 75, 20, I18n.format("gui.cancel"));

        buttonList.add(buttonDone);
        buttonList.add(buttonCancel);
        buttonList.add(buttonOpen);
        buttonList.add(buttonRefresh);
        safeButtonList = new CopyOnWriteArrayList<>(buttonList);
        sorted = false;
        startWatcher();
        initFileList();
        reloadState();
        updateSortButtons(sortType, safeButtonList);
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        sortType = cachedSortType;
        guiFileList.elementClicked(cachedSelectedIndex);
        guiFileList.resetScroll();
        cachedSelectedIndex = guiFileList.getSelectedIndex();
        search.setText(lastSearch);
    }

    private void updateState()
    {
        cachedSelectedIndex = guiFileList.getSelectedIndex();
        cachedSortType = sortType;
        searchAndSort();
        this.isStateCached = true;
    }

    @Override
    public void updateScreen()
    {
        cachedSelectedIndex = guiFileList.getSelectedIndex();
        guiFileList.elementClicked(cachedSelectedIndex);
        search.updateCursorCounter();
        super.updateScreen();
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
        searchLabel.drawLabel(mc, mouseX, mouseY);
        search.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
        ModGuiUtils.INSTANCE.drawHooveringHelp(this, safeButtonList, guiLeft, guiTop, mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            SortType type = SortType.getTypeForButton(button);
            if (type != null)
            {
                updateSortButtons(type, buttonList);
                sorted = false;
                sortType = type;
                initFileList();
            }
            else
                switch (button.id)
                {
                    case 0:
                        // Done
                        selectDone();
                        break;
                    case 1:
                        // Cancel
                        if (guiScreenParent != null)
                            ActionGet.INSTANCE.setCancel();
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
        }
        updateState();
        super.actionPerformed(button);
    }

    private void selectDone()
    {
        ActionGet.INSTANCE.select(ImportHelper.importToMXTFile(guiFileList.get()));
        mc.displayGuiScreen(guiScreenParent);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        // capture the ESC key to close cleanly
        search.textboxKeyTyped(typedChar, keyCode);
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

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        search.mouseClicked(mouseX, mouseY, mouseButton);
        clearOnMouseLeftClicked(search, mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        updateState();
    }

    private void initFileList()
    {
        Path path = FileHelper.getDirectory(FileHelper.CLIENT_MML_FOLDER, Side.CLIENT);
        PathMatcher filter = FileHelper.getMmlMatcher(path);
        try (Stream<Path> paths = Files.list(path))
        {
            fileList = paths
                    .filter(filter::matches)
                    .collect(Collectors.toList());
        }
        catch (NullPointerException | IOException e)
        {
            ModLogger.error(e);
        }
        List<Path> files = new ArrayList<>();
        for (Path file : fileList)
        {
            if (file.getFileName().toString().toLowerCase(Locale.ROOT).contains(search.getText().toLowerCase(Locale.ROOT)))
            {
                files.add(file);
            }
        }
        fileList = files;
        guiFileList.clear();
        guiFileList.addAll(files);
        lastSearch = search.getText();
    }

    private void openFolder()
    {
        FileHelper.openFolder(FileHelper.CLIENT_MML_FOLDER);
    }

    private void refresh()
    {
        initGui();
    }

    private void searchAndSort()
    {
        if (!search.getText().equals(lastSearch))
        {
            initFileList();
            sorted = false;
        }
        if (!sorted)
        {
            initFileList();
            guiFileList.sort(sortType);
            guiFileList.setSelectedIndex(fileList.indexOf(selectedFile));
            cachedSelectedIndex = guiFileList.getSelectedIndex();
            sorted = true;
        }
    }
}
