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
import net.aeronica.mods.mxtune.gui.util.GuiScrollingListOf;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.managers.PlayIdSupplier;
import net.aeronica.mods.mxtune.mxt.MXTuneFile;
import net.aeronica.mods.mxtune.mxt.MXTuneFileHelper;
import net.aeronica.mods.mxtune.mxt.MXTunePart;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.IAudioStatusCallback;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.aeronica.mods.mxtune.gui.util.ModGuiUtils.clearOnMouseLeftClicked;

public class GuiMusicLibrary extends GuiScreen implements IAudioStatusCallback
{
    private static final String TITLE = I18n.format("mxtune.gui.guiMusicLibrary.title");
    private static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    private GuiScreen guiScreenParent;
    private int guiLeft;
    private int guiTop;
    private boolean isStateCached;
    private boolean midiUnavailable;

    private GuiButton buttonCancel;
    private List<GuiButton> safeButtonList;
    private GuiScrollingListOf<FileData> guiLibraryList;
    private List<Path> libraryFiles = new ArrayList<>();

    // Sort and Search
    private GuiLabel searchLabel;
    private GuiTextField search;
    private boolean sorted = false;
    private SortFileDataHelper.SortType sortType = SortFileDataHelper.SortType.NATURAL;
    private String lastSearch = "";

    // Cache across screen resizing
    private int cachedSelectedIndex = -1;
    private SortFileDataHelper.SortType cachedSortType;
    private boolean cachedIsPlaying;
    private int cachedPlayId;

    // Part List
    private MXTuneFile mxTuneFile;
    private GuiScrollingListOf<MXTunePart> guiPartList;
    private List<MXTunePart> tuneParts = new ArrayList<>();

    // playing
    private GuiButton buttonPlay;
    private boolean isPlaying = false;
    private int playId = PlayIdSupplier.PlayType.INVALID;

    public GuiMusicLibrary(GuiScreen guiScreenParent)
    {
        this.guiScreenParent = guiScreenParent;
        mc = Minecraft.getMinecraft();
        fontRenderer = mc.fontRenderer;
        midiUnavailable = MIDISystemUtil.midiUnavailable();

        guiLibraryList = new GuiScrollingListOf<FileData>(this){
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                // get the filename and remove the '.mxt' extension
                String name = (get(slotIdx).name);
                String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                int color = isSelected(slotIdx) ? 0xFFFF00 : 0xADD8E6;
                fontRenderer.drawStringWithShadow(trimmedName, (float)left + 3, slotTop, color);
            }

            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                updatePartList();
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex)
            {
                selectDone();
            }
        };

        guiPartList = new GuiScrollingListOf<MXTunePart>(this)
        {
            @Override
            protected void selectedClickedCallback(int selectedIndex) { /* NOP */ }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex) { /* NOP */ }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                if (!isEmpty() && slotIdx < size())
                {
                    MXTunePart tunePart = get(slotIdx);
                    String trimmedName = fontRenderer.trimStringToWidth(tunePart.getInstrumentName(), listWidth - 10);
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, 0xADD8E6);
                }
            }
        };
    }

    @Override
    public void initGui()
    {
        buttonList.clear();
        this.guiLeft = 0;
        this.guiTop = 0;
        int guiListWidth = (width - 15) * 3 / 4;
        // Library List
        int entryHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int left = 5;
        int titleTop = 20;
        int listTop = titleTop + 25;
        int listHeight = height - titleTop - entryHeight - 2 - 10 - 25 - 25;
        int listBottom = listTop + listHeight;
        int statusTop = listBottom + 4;
        int partListWidth = (width - 15) / 4;

        guiLibraryList.setLayout(entryHeight, guiListWidth, listHeight, listTop, listBottom, left);

        guiPartList.setLayout(entryHeight, partListWidth, listHeight, listTop, listBottom, guiLibraryList.getRight() + 5);

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
        GuiButtonExt normalSort = new GuiButtonExt(SortFileDataHelper.SortType.NATURAL.getButtonID(), x, titleTop, buttonWidth - buttonMargin, 20, I18n.format("fml.menu.mods.normal"));
        normalSort.enabled = false;
        buttonList.add(normalSort);
        x += buttonWidth + buttonMargin;
        buttonList.add(new GuiButtonExt(SortFileDataHelper.SortType.ASCENDING.getButtonID(), x, titleTop, buttonWidth - buttonMargin, 20, "A-Z"));
        x += buttonWidth + buttonMargin;
        buttonList.add(new GuiButtonExt(SortFileDataHelper.SortType.DESCENDING.getButtonID(), x, titleTop, buttonWidth - buttonMargin, 20, "Z-A"));

        int buttonTop = height - 25;
        int xOpen = (this.width / 2) - (75 * 4 / 2);
        int xPlay = xOpen + 75;
        int xSelect = xPlay + 75;
        int xCancel = xSelect + 75;

        GuiButton buttonOpen = new GuiButton(2, xOpen, buttonTop, 75, 20, I18n.format("mxtune.gui.button.openFolder"));
        buttonPlay = new GuiButton(3, xPlay, buttonTop, 75, 20, isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play"));
        GuiButton buttonDone = new GuiButton(0, xSelect, buttonTop, 75, 20, I18n.format("mxtune.gui.button.select"));
        buttonCancel = new GuiButton(1, xCancel, buttonTop, 75, 20, I18n.format("gui.cancel"));

        buttonList.add(buttonOpen);
        buttonList.add(buttonPlay);
        buttonList.add(buttonDone);
        buttonList.add(buttonCancel);
        safeButtonList = new CopyOnWriteArrayList<>(buttonList);
        sorted = false;
        initFileList();
        reloadState();
        SortFileDataHelper.updateSortButtons(sortType, safeButtonList);
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        sortType = cachedSortType;
        search.setText(lastSearch);
        guiLibraryList.setSelectedIndex(cachedSelectedIndex);
        updatePartList();
        guiLibraryList.resetScroll();
        isPlaying = cachedIsPlaying;
        playId = cachedPlayId;
    }

    private void updateState()
    {
        cachedSortType = sortType;
        cachedSelectedIndex = guiLibraryList.getSelectedIndex();
        cachedIsPlaying = isPlaying;
        cachedPlayId = playId;
        searchAndSort();
        buttonPlay.displayString = isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play");
        isStateCached = true;
    }

    @Override
    public void updateScreen()
    {
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
        String guiMusicLibTitle;
        if (midiUnavailable)
            guiMusicLibTitle = TITLE + " - " + TextFormatting.RED + MIDI_NOT_AVAILABLE;
        else
            guiMusicLibTitle = TITLE;
        /* draw "TITLE" at the top middle */
        int posX = (this.width - mc.fontRenderer.getStringWidth(guiMusicLibTitle)) / 2 ;
        int posY = 5;
        mc.fontRenderer.drawStringWithShadow(guiMusicLibTitle, posX, posY, 0xD3D3D3);

        guiLibraryList.drawScreen(mouseX, mouseY, partialTicks);
        guiPartList.drawScreen(mouseX, mouseY, partialTicks);
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
            SortFileDataHelper.SortType type = SortFileDataHelper.SortType.getSortTypeForButton(button);
            if (type != null)
            {
                SortFileDataHelper.updateSortButtons(type, buttonList);
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
                    case 2:
                        openFolder();
                        break;
                    case 1:
                        // Cancel
                        selectCancel();
                        break;
                    case 3:
                        // Play
                        play();
                        break;
                    default:
                }
        }
        updateState();
        super.actionPerformed(button);
    }

    private void selectDone()
    {
        // action get file, etc...
        if (guiLibraryList.isSelected(guiLibraryList.getSelectedIndex()))
            ActionGet.INSTANCE.select(guiLibraryList.get(guiLibraryList.getSelectedIndex()).path);
        stop();
        mc.displayGuiScreen(guiScreenParent);
    }

    private void selectCancel()
    {
        stop();
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
        guiLibraryList.keyTyped(typedChar, keyCode);
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onResize(@Nonnull Minecraft mcIn, int w, int h)
    {
        updateState();
        super.onResize(mcIn, w, h);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        guiLibraryList.handleMouseInput(mouseX, mouseY);
        guiPartList.handleMouseInput(mouseX, mouseY);
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
        Path path = FileHelper.getDirectory(FileHelper.CLIENT_LIB_FOLDER, Side.CLIENT);
        PathMatcher filter = FileHelper.getMxtMatcher(path);
        try (Stream<Path> paths = Files.list(path))
        {
            libraryFiles = paths
                    .filter(filter::matches)
                    .collect(Collectors.toList());
        }
        catch (NullPointerException | IOException e)
        {
            ModLogger.error(e);
        }

        List<Path> files = new ArrayList<>();
        List<FileData> fileDatas = new ArrayList<>();
        for (Path file : libraryFiles)
        {
            FileData fileData = new FileData(file);
            if (fileData.name.toLowerCase(Locale.ROOT).contains(search.getText().toLowerCase(Locale.ROOT)))
            {
                fileDatas.add(fileData);
            }
        }
        libraryFiles = files;
        guiLibraryList.clear();
        guiLibraryList.addAll(fileDatas);
        lastSearch = search.getText();
    }

    private void searchAndSort()
    {
        if (!search.getText().equals(lastSearch))
        {
            initFileList();
            sorted = false;
            updatePartList();
        }
        if (!sorted)
        {
            initFileList();
            guiLibraryList.sort(sortType);
            sorted = true;
            updatePartList();
        }
    }

    private void updatePartList()
    {
        if (guiLibraryList.isSelected(guiLibraryList.getSelectedIndex()))
        {
            Path path = guiLibraryList.get(guiLibraryList.getSelectedIndex()).path;
            NBTTagCompound compound = FileHelper.getCompoundFromFile(path);
            tuneParts.clear();
            if (compound != null)
            {
                mxTuneFile = MXTuneFile.build(compound);
                tuneParts.addAll(mxTuneFile.getParts());
            }
            guiPartList.clear();
            guiPartList.addAll(tuneParts);
            ModLogger.debug("Selected file: %s", path.toString());
        } else if (guiLibraryList.isEmpty())
            guiPartList.clear();
    }

    private void play()
    {
        if (isPlaying)
        {
            stop();
        }
        else if (mxTuneFile != null)
        {
            isPlaying = true;
            String mml = MXTuneFileHelper.getMML(mxTuneFile);
            playId = PlayIdSupplier.PlayType.PERSONAL.getAsInt();
            ClientAudio.playLocal(playId, mml, this);
        }

    }

    private void stop()
    {
        ClientAudio.fadeOut(playId, 1);
        isPlaying = false;
        playId = PlayIdSupplier.PlayType.INVALID;
    }

    @Override
    public void statusCallBack(ClientAudio.Status status, int playId)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (this.playId == playId && (status == ClientAudio.Status.ERROR || status == ClientAudio.Status.DONE))
            {
                ModLogger.debug("AudioStatus event received: %s, playId: %s", status, playId);
                stop();
                updateState();
            }
        });
    }

    private void openFolder()
    {
        FileHelper.openFolder(FileHelper.CLIENT_LIB_FOLDER);
    }
}
