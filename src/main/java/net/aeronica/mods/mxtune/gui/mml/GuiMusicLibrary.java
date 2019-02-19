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

import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.gui.util.HooverHelper;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuiMusicLibrary extends GuiScreen
{
    enum SortType implements Comparator<Path>
    {
        NORMAL(24),
        A_TO_Z(25){ @Override protected int compare(String name1, String name2){ return name1.compareTo(name2); }},
        Z_TO_A(26){ @Override protected int compare(String name1, String name2){ return name2.compareTo(name1); }};

        private int buttonID;

        private SortType(int buttonID)
        {
            this.buttonID = buttonID;
        }

        @Nullable
        public static GuiMusicLibrary.SortType getTypeForButton(GuiButton button)
        {
            for (GuiMusicLibrary.SortType t : values())
            {
                if (t.buttonID == button.id)
                {
                    return t;
                }
            }
            return null;
        }

        protected int compare(String name1, String name2){ return 0; }

        @Override
        public int compare(Path o1, Path o2)
        {
            String name1 = StringUtils.stripControlCodes(o1.getFileName().toString()).toLowerCase(Locale.ROOT);
            String name2 = StringUtils.stripControlCodes(o2.getFileName().toString()).toLowerCase(Locale.ROOT);
            return compare(name1, name2);
        }
    }

    private static final String TITLE = I18n.format("mxtune.gui.guiMusicLibrary.title");
    private static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    private GuiScreen guiScreenParent;
    private int guiLeft;
    private int guiTop;
    private boolean isStateCached;
    private boolean midiUnavailable;

    protected int entryHeight;
    private Path selectedFile;
    private GuiButton buttonCancel;
    private List<GuiButton> safeButtonList;
    private GuiFileList guiFileList;
    private List<Path> mmlFiles = new ArrayList<>();

    // Sort and Search
    private GuiTextField search;
    private boolean sorted = false;
    private SortType sortType = SortType.NORMAL;
    private String lastSearch = "";

    // Cache across screen resizing
    private int cachedSelectedIndex = -1;
    private SortType cachedSortType;

    public GuiMusicLibrary(GuiScreen guiScreenParent)
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
        int guiListWidth = (width - 15) * 3 / 4 ;
        int guiTextWidth = width - 10;
        entryHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int left = 5;
        int titleTop = 20;
        int listTop = titleTop + 25;
        int listHeight = height - titleTop - entryHeight - 2 - 10 - 25 - 25;
        int listBottom = listTop + listHeight;
        int statusTop = listBottom + 4;

        guiFileList = new GuiFileList(this, guiListWidth, listHeight, listTop, listBottom, left);

        search = new GuiTextField(0, fontRenderer, left, statusTop, guiListWidth, entryHeight + 2);
        search.setFocused(true);
        search.setCanLoseFocus(true);

        int buttonMargin = 1;
        int width = (guiListWidth / 3);
        int x = left, y = titleTop;
        GuiButton normalSort = new GuiButton(SortType.NORMAL.buttonID, x, y, width - buttonMargin, 20, I18n.format("fml.menu.mods.normal"));
        normalSort.enabled = false;
        buttonList.add(normalSort);
        x += width + buttonMargin;
        buttonList.add(new GuiButton(SortType.A_TO_Z.buttonID, x, y, width - buttonMargin, 20, "A-Z"));
        x += width + buttonMargin;
        buttonList.add(new GuiButton(SortType.Z_TO_A.buttonID, x, y, width - buttonMargin, 20, "Z-A"));


        int buttonTop = height - 25;
        int xImport = (this.width /2) - 75 * 2;
        int xPlay = xImport + 75;
        int xSaveDone = xPlay + 75;
        int xCancel = xSaveDone + 75;

        GuiButton buttonImport = new GuiButton(2, xImport, buttonTop, 75, 20, I18n.format("mxtune.gui.button.importMML"));
        GuiButton buttonPlay = new GuiButton(3, xPlay, buttonTop, 75, 20, I18n.format("mxtune.gui.button.play"));
        GuiButton buttonDone = new GuiButton(0, xSaveDone, buttonTop, 75, 20, I18n.format("gui.done"));
        buttonCancel = new GuiButton(1, xCancel, buttonTop, 75, 20, I18n.format("gui.cancel"));

        buttonList.add(buttonImport);
        buttonList.add(buttonPlay);
        buttonList.add(buttonDone);
        buttonList.add(buttonCancel);
        safeButtonList = new CopyOnWriteArrayList<>(buttonList);
        reloadState();
        sorted = false;
        initFileList();
        updateSortButtons(sortType);
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        sortType = cachedSortType;
        search.setText(lastSearch);
        guiFileList.elementClicked(cachedSelectedIndex, false);
    }

    private void updateState()
    {
        cachedSortType = sortType;
        cachedSelectedIndex = guiFileList.getSelectedIndex();
        isStateCached = true;
    }

    private void updateSortButtons(SortType sortType)
    {
        for (GuiButton button : safeButtonList)
        {
            SortType type = SortType.getTypeForButton(button);
            if (type != null && type == sortType)
                button.enabled = false;
            else if (type != null && type != sortType)
                button.enabled = true;
        }
    }

    @Override
    public void updateScreen()
    {
        cachedSelectedIndex = guiFileList.getSelectedIndex();
        guiFileList.elementClicked(cachedSelectedIndex, false);

        if (!search.getText().equals(lastSearch))
        {
            initFileList();
            sorted = false;
        }

        if (!sorted)
        {
            initFileList();
            mmlFiles.sort(sortType);
            guiFileList.elementClicked(mmlFiles.indexOf(selectedFile), false);
            cachedSelectedIndex = guiFileList.getSelectedIndex();
            sorted = true;
        }
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        String guiMusicLibTitle;
        if (midiUnavailable)
            guiMusicLibTitle = TITLE + " - " + TextFormatting.RED + MIDI_NOT_AVAILABLE;
        else
            guiMusicLibTitle = TITLE;
        /* draw "TITLE" at the top middle */
        int posX = (this.width - mc.fontRenderer.getStringWidth(guiMusicLibTitle)) / 2 ;
        int posY = 5;
        mc.fontRenderer.drawStringWithShadow(guiMusicLibTitle, posX, posY, 0xD3D3D3);

        guiFileList.drawScreen(mouseX, mouseY, partialTicks);
        search.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
        HooverHelper.INSTANCE.drawHooveringButtonHelp(this, safeButtonList, guiLeft, guiTop, mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            SortType type = SortType.getTypeForButton(button);
            if (type != null)
            {
                for (GuiButton b : buttonList)
                {
                    if (SortType.getTypeForButton(b) != null)
                    {
                        b.enabled = true;
                    }
                }
                button.enabled = false;
                sorted = false;
                sortType = type;
                initFileList();
            }
            else
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
                        // Import
                        mc.displayGuiScreen(new GuiMusicImporter(this));
                        break;
                    case 3:
                        // Play
                        break;
                    default:
                }
        }
        updateState();
        super.actionPerformed(button);
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
    private static class GuiFileList extends GuiScrollingList
    {
        private FontRenderer fontRenderer;
        GuiMusicLibrary parent;

        GuiFileList(GuiMusicLibrary parent, int width, int height, int top, int bottom, int left)
        {
            super(parent.mc, width, height, top, bottom, left, parent.entryHeight, parent.width, parent.height);
            this.parent = parent;
            this.fontRenderer = parent.mc.fontRenderer;
        }

        int getRight() {return right;}

        int getSelectedIndex() { return selectedIndex; }

        @Override
        protected int getSize()
        {
            return parent.mmlFiles != null ? parent.mmlFiles.size() : 0;
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick)
        {
            if (index == selectedIndex && !doubleClick) return;
            selectedIndex = (index >= 0 && index <= parent.mmlFiles.size() ? index : -1);

            if (selectedIndex >= 0 && selectedIndex <= parent.mmlFiles.size())
                parent.selectedFile = parent.mmlFiles.get(selectedIndex);

            if (doubleClick && parent.guiScreenParent != null)
                try
                {
                    parent.actionPerformed(parent.buttonList.get(0));
                } catch (IOException e)
                {
                    ModLogger.error(e);
                }
        }

        @Override
        protected boolean isSelected(int index)
        {
            return index == selectedIndex && selectedIndex >= 0 && selectedIndex <= parent.mmlFiles.size();
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
            String name = (parent.mmlFiles.get(slotIdx).getFileName().toString());
            String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
            fontRenderer.drawStringWithShadow(trimmedName, (float)left + 3, slotTop, 0xADD8E6);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        search.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 1 && mouseX >= search.x && mouseX < search.x + search.width && mouseY >= search.y && mouseY < search.y + search.height) {
            search.setText("");
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        updateState();
    }

    private void initFileList()
    {
        Path path = FileHelper.getDirectory(FileHelper.CLIENT_LIB_FOLDER);
        PathMatcher filter = FileHelper.getDatMatcher(path);
        try (Stream<Path> paths = Files.list(path))
        {
            mmlFiles = paths
                    .filter(filter::matches)
                    .collect(Collectors.toList());
        }
        catch (NullPointerException | IOException e)
        {
            ModLogger.error(e);
        }

        List<Path> files = new ArrayList<>();
        for (Path file : mmlFiles)
        {
            if (file.getFileName().toString().toLowerCase(Locale.ROOT).contains(search.getText().toLowerCase(Locale.ROOT)))
            {
                files.add(file);
            }
        }
        mmlFiles = files;
        lastSearch = search.getText();
    }
}
