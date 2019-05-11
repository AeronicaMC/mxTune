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
import net.aeronica.mods.mxtune.caches.MXTuneFile;
import net.aeronica.mods.mxtune.caches.MXTuneFileHelper;
import net.aeronica.mods.mxtune.caches.MXTunePart;
import net.aeronica.mods.mxtune.gui.util.GuiScrollingListOf;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.managers.PlayIdSupplier;
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

import static net.aeronica.mods.mxtune.gui.mml.SortHelper.SortType;
import static net.aeronica.mods.mxtune.gui.mml.SortHelper.updateSortButtons;
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

    private Path selectedFile;
    private GuiButton buttonCancel;
    private List<GuiButton> safeButtonList;
    private GuiScrollingListOf<Path> guiLibraryList;
    private List<Path> libraryFiles = new ArrayList<>();

    // Sort and Search
    private GuiLabel searchLabel;
    private GuiTextField search;
    private boolean sorted = false;
    private SortType sortType = SortType.NORMAL;
    private String lastSearch = "";

    // Cache across screen resizing
    private int cachedSelectedIndex = -1;
    private SortType cachedSortType;
    private boolean cachedIsPlaying;
    private int cachedPlayId;
    private Path cachedSelectedFile;

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

        guiLibraryList = new GuiScrollingListOf<Path>(this, entryHeight, guiListWidth, listHeight, listTop, listBottom, left){
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                // get the filename and remove the '.mxt' extension
                String name = (get(slotIdx).getFileName().toString()).replaceAll("\\.[mM][xX][tT]$", "");
                String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                int color = isSelected(slotIdx) ? 0xFFFF00 : 0xADD8E6;
                fontRenderer.drawStringWithShadow(trimmedName, (float)left + 3, slotTop, color);
            }

            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                selectedFile = get(selectedIndex);
                updatePartList(selectedFile);
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex)
            {
                selectedFile = get(selectedIndex);
                selectDone();
            }
        };

        guiPartList = new GuiScrollingListOf<MXTunePart>(this, entryHeight, partListWidth, listHeight, listTop, listBottom, guiLibraryList.getRight() + 5)
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

        String searchLabelText = I18n.format("mxtune.gui.label.search");
        int searchLabelWidth =  fontRenderer.getStringWidth(searchLabelText) + 4;
        searchLabel = new GuiLabel(fontRenderer, 0, left, statusTop, searchLabelWidth, entryHeight + 2, 0xFFFFFF );
        searchLabel.addLine(searchLabelText);
        searchLabel.visible = true;
        search = new GuiTextField(0, fontRenderer, left + searchLabelWidth, statusTop, guiListWidth - searchLabelWidth, entryHeight + 2);
        search.setFocused(true);
        search.setCanLoseFocus(true);

        int buttonMargin = 1;
        int buttonWidth = (guiListWidth / 3);
        int x = left;
        GuiButton normalSort = new GuiButton(SortType.NORMAL.getButtonID(), x, titleTop, buttonWidth - buttonMargin, 20, I18n.format("fml.menu.mods.normal"));
        normalSort.enabled = false;
        buttonList.add(normalSort);
        x += buttonWidth + buttonMargin;
        buttonList.add(new GuiButton(SortType.A_TO_Z.getButtonID(), x, titleTop, buttonWidth - buttonMargin, 20, "A-Z"));
        x += buttonWidth + buttonMargin;
        buttonList.add(new GuiButton(SortType.Z_TO_A.getButtonID(), x, titleTop, buttonWidth - buttonMargin, 20, "Z-A"));

        int buttonTop = height - 25;
        int xImport = (this.width /2) - 75 * 2;
        int xPlay = xImport + 75;
        int xSaveDone = xPlay + 75;
        int xCancel = xSaveDone + 75;

        GuiButton buttonImport = new GuiButton(2, xImport, buttonTop, 75, 20, I18n.format("mxtune.gui.button.importMML"));
        buttonPlay = new GuiButton(3, xPlay, buttonTop, 75, 20,
                                   isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play"));
        GuiButton buttonDone = new GuiButton(0, xSaveDone, buttonTop, 75, 20, I18n.format("gui.done"));
        buttonCancel = new GuiButton(1, xCancel, buttonTop, 75, 20, I18n.format("gui.cancel"));

        buttonList.add(buttonImport);
        buttonList.add(buttonPlay);
        buttonList.add(buttonDone);
        buttonList.add(buttonCancel);
        safeButtonList = new CopyOnWriteArrayList<>(buttonList);
        sorted = false;
        initFileList();
        reloadState();
        updateSortButtons(sortType, safeButtonList);
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        sortType = cachedSortType;
        search.setText(lastSearch);
        guiLibraryList.setSelectedIndex(cachedSelectedIndex);
        updatePartList(cachedSelectedFile);
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
        cachedSelectedFile = selectedFile;
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
                        selectDone();
                        break;
                    case 2:
                        // Import
                        mc.displayGuiScreen(new GuiMusicImporter(this));
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
        ActionGet.INSTANCE.select(selectedFile);
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
        for (Path file : libraryFiles)
        {
            if (file.getFileName().toString().toLowerCase(Locale.ROOT).contains(search.getText().toLowerCase(Locale.ROOT)))
            {
                files.add(file);
            }
        }
        libraryFiles = files;
        guiLibraryList.clear();
        guiLibraryList.addAll(files);
        lastSearch = search.getText();
    }

    private void searchAndSort()
    {
        if (!search.getText().equals(lastSearch))
        {
            initFileList();
            sorted = false;
            updatePartList(guiLibraryList.get());
        }
        if (!sorted)
        {
            initFileList();
            guiLibraryList.sort(sortType);
            guiLibraryList.setSelectedIndex(libraryFiles.indexOf(selectedFile));
            cachedSelectedIndex = guiLibraryList.getSelectedIndex();
            sorted = true;
            updatePartList(guiLibraryList.get());
        }
    }

    private void updatePartList(Path selectedFile)
    {
        cachedSelectedFile = selectedFile;
        if (selectedFile != null)
        {
            NBTTagCompound compound = FileHelper.getCompoundFromFile(selectedFile);
            tuneParts.clear();
            if (compound != null)
            {
                mxTuneFile = MXTuneFile.build(compound);
                tuneParts.addAll(mxTuneFile.getParts());
            }
            guiPartList.clear();
            guiPartList.addAll(tuneParts);
            ModLogger.debug("Selected file: %s", selectedFile.toString());
        }
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
}
