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

package aeronicamc.mods.mxtune.gui.mml;


import aeronicamc.mods.mxtune.caches.DirectoryWatcher;
import aeronicamc.mods.mxtune.caches.FileHelper;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
import aeronicamc.mods.mxtune.gui.widget.label.MXLabel;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.mxt.MXTuneFile;
import aeronicamc.mods.mxtune.mxt.MXTuneFileHelper;
import aeronicamc.mods.mxtune.mxt.MXTunePart;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import aeronicamc.mods.mxtune.sound.IAudioStatusCallback;
import aeronicamc.mods.mxtune.util.MIDISystemUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static aeronicamc.mods.mxtune.gui.ModGuiHelper.clearOnMouseLeftClicked;


public class OldMusicLibrary extends Screen implements IAudioStatusCallback
{
    private static final Logger LOGGER = LogManager.getLogger(OldMusicLibrary.class);
    private static final ITextComponent MIDI_NOT_AVAILABLE = new TranslationTextComponent("mxtune.chat.msu.midiNotAvailable");
    private Screen parent;
    private int guiLeft;
    private int guiTop;
    private boolean isStateCached;
    private boolean midiUnavailable;

    private MXButton buttonCancel;
    private List<MXButton> safeButtonList;
//    private GuiScrollingListOf<FileData> guiLibraryList;
    private List<Path> libraryFiles = new ArrayList<>();

    // Sort and Search
    private MXLabel searchLabel;
    private final MXTextFieldWidget searchText = new MXTextFieldWidget(30);
    private boolean sorted = false;
    private SortType sortType = SortType.NATURAL;
    private String lastSearch = "";

    // Cache across screen resizing
    private int cachedSelectedIndex = -1;
    private SortType cachedSortType;
    private boolean cachedIsPlaying;
    private int cachedPlayId;

    // Part List
    private MXTuneFile mxTuneFile;
//    private GuiScrollingListOf<MXTunePart> guiPartList;
    private List<MXTunePart> tuneParts = new ArrayList<>();

    // playing
    private MXButton buttonPlay;
    private boolean isPlaying = false;
    private int playId = PlayIdSupplier.INVALID;

    // Directory Watcher
    private boolean watcherStarted = false;
    private DirectoryWatcher watcher;

    public enum SortType implements Comparator<FileData>
    {
        NATURAL,
        ASCENDING{ @Override protected int compare(String name1, String name2){ return name1.compareTo(name2); }},
        DESCENDING{ @Override protected int compare(String name1, String name2){ return name2.compareTo(name1); }};

        private Button button;
        protected int compare(String name1, String name2) { return 0; }
        @Override
        public int compare(FileData o1, FileData o2)
        {
            String name1 = o1.name.toLowerCase(Locale.ROOT);
            String name2 = o2.name.toLowerCase(Locale.ROOT);
            return compare(name1, name2);
        }

        ITextComponent getButtonText() {
            return new TranslationTextComponent("fml.menu.mods." + net.minecraftforge.fml.loading.StringUtils.toLowerCase(name()));
        }
    }

    private void resortFiles(SortType newSort)
    {
        this.sortType = newSort;
        for (SortType sort : SortType.values())
        {
            if (sort.button != null)
                sort.button.active = sortType != sort;
        }
        sorted = false;
    }

    public OldMusicLibrary(Screen parent)
    {
        super(new TranslationTextComponent("gui.mxtune.gui_music_library.title"));
        this.parent = parent;
        midiUnavailable = MIDISystemUtil.midiUnavailable();

        // refresh the file list automatically
        DirectoryStream.Filter<Path> filter = entry ->
                (entry.toString().endsWith(".mxt"));
        watcher = new DirectoryWatcher.Builder()
                .addDirectories(FileHelper.getDirectory(FileHelper.CLIENT_LIB_FOLDER, LogicalSide.CLIENT))
                .setPreExistingAsCreated(true)
                .setFilter(filter::accept)
                .build((event, path) ->
                       {
                           switch (event)
                           {
                               case ENTRY_CREATE:
                               case ENTRY_MODIFY:
                               case ENTRY_DELETE:
                                   init();
                           }
                       });
    }

    @Override
    public void onClose()
    {
        stopWatcher();
        Objects.requireNonNull(minecraft).setScreen(parent);
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
                LOGGER.error(e);
            }
    }

    private void stopWatcher()
    {
        if (watcherStarted)
            watcher.stop();
    }

    @Override
    public void init()
    {
        this.guiLeft = 0;
        this.guiTop = 0;
        int guiListWidth = (width - 15) * 3 / 4;
        // Library List
        int entryHeight = font.lineHeight + 2;
        int left = 5;
        int titleTop = 20;
        int listTop = titleTop + 25;
        int listHeight = height - titleTop - entryHeight - 2 - 10 - 25 - 25;
        int listBottom = listTop + listHeight;
        int statusTop = listBottom + 4;
        int partListWidth = (width - 15) / 4;

//        guiLibraryList.setLayout(entryHeight, guiListWidth, listHeight, listTop, listBottom, left);
//
//        guiPartList.setLayout(entryHeight, partListWidth, listHeight, listTop, listBottom, guiLibraryList.getRight() + 5);

        ITextComponent searchLabelText = new TranslationTextComponent("gui.mxtune.label.search");
        int searchLabelWidth =  font.width(searchLabelText) + 4;
        searchLabel = new MXLabel(font, left, statusTop, searchLabelWidth, entryHeight + 2, searchLabelText, 0xFFFFFF );
        searchText.setLayout(left + searchLabelWidth, statusTop, guiListWidth - searchLabelWidth, entryHeight + 2);
        searchText.setFocus(true);
        searchText.setCanLoseFocus(true);

        int buttonMargin = 1;
        int buttonWidth = 75;
        int x = left;
//        GuiButtonExt normalSort = new GuiButtonExt(SortType.NATURAL.button, x, titleTop, buttonWidth - buttonMargin, 20, I18n.format("fml.menu.mods.normal"));
//        normalSort.enabled = false;
//        buttonList.add(normalSort);
//        x += buttonWidth + buttonMargin;
//        buttonList.add(new GuiButtonExt(SortFileDataHelper.SortType.ASCENDING.getButton(), x, titleTop, buttonWidth - buttonMargin, 20, "A-Z"));
//        x += buttonWidth + buttonMargin;
//        buttonList.add(new GuiButtonExt(SortFileDataHelper.SortType.DESCENDING.getButton(), x, titleTop, buttonWidth - buttonMargin, 20, "Z-A"));

        int buttonTop = height - 25;
        int xOpen = (this.width / 2) - (75 * 5 / 2);
        int xRefresh = xOpen + 75;
        int xPlay = xRefresh + 75;
        int xSelect = xPlay + 75;
        int xCancel = xSelect + 75;

//        GuiButton buttonOpen = new GuiButton(2, xOpen, buttonTop, 75, 20, I18n.format("mxtune.gui.button.openFolder"));
//        GuiButton buttonRefresh = new GuiButton(3, xRefresh, buttonTop, 75, 20, I18n.format("mxtune.gui.button.refresh"));
//        buttonPlay = new GuiButton(3, xPlay, buttonTop, 75, 20, isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play"));
//        GuiButton buttonDone = new GuiButton(0, xSelect, buttonTop, 75, 20, I18n.format("mxtune.gui.button.select"));
//        buttonCancel = new GuiButton(1, xCancel, buttonTop, 75, 20, I18n.format("gui.cancel"));

//        addButton(buttonOpen);
//        addButton(buttonRefresh);
//        addButton(buttonPlay);
//        addButton(buttonDone);
//        addButton(buttonCancel);

        sorted = false;
        startWatcher();
        initFileList();
        reloadState();
        resortFiles(sortType);
    }

    private void reloadState()
    {
//        if (!isStateCached) return;
//        sortType = cachedSortType;
//        searchText.setValue(lastSearch);
//        guiLibraryList.setSelectedIndex(cachedSelectedIndex);
//        updatePartList();
//        guiLibraryList.resetScroll();
//        isPlaying = cachedIsPlaying;
//        playId = cachedPlayId;
    }

    private void updateState()
    {
//        cachedSortType = sortType;
//        cachedSelectedIndex = guiLibraryList.getSelectedIndex();
//        cachedIsPlaying = isPlaying;
//        cachedPlayId = playId;
//        searchAndSort();
//        buttonPlay.displayString = isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play");
//        isStateCached = true;
    }


    @Override
    public void tick()
    {
        searchText.tick();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        renderBackground(pMatrixStack);
        ITextComponent guiMusicLibTitle;
        if (midiUnavailable)
            guiMusicLibTitle = MIDI_NOT_AVAILABLE;
        else
            guiMusicLibTitle = title;
        /* draw "TITLE" at the top middle */
        int posX = (this.width - font.width(guiMusicLibTitle)) / 2 ;
        int posY = 5;
        font.drawShadow(pMatrixStack, guiMusicLibTitle, posX, posY, 0xD3D3D3);

//        guiLibraryList.drawScreen(pMouseX, pMouseY, pPartialTicks);
//        guiPartList.drawScreen(pMouseX, pMouseY, pPartialTicks);
        searchLabel.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        searchText.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    private void selectDone()
    {
        // action get file, etc...
//        if (guiLibraryList.isSelected(guiLibraryList.getSelectedIndex()))
//            ActionGet.INSTANCE.select(guiLibraryList.get(guiLibraryList.getSelectedIndex()).path);
        stop();
        onClose();
    }

    private void selectCancel()
    {
        stop();
        onClose();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        // capture the ESC key to close cleanly
//        searchText.keyPressed(typedChar, keyCode);
//        if (keyCode == pKeyCode)
//        {
//            this.actionPerformed(buttonCancel);
//            return;
//        }
//        guiLibraryList.keyTyped(typedChar, keyCode);
        updateState();
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void resize(@Nonnull Minecraft mcIn, int w, int h)
    {
        updateState();
        super.resize(mcIn, w, h);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY)
    {
//        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
//        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
//        guiLibraryList.handleMouseInput(mouseX, mouseY);
//        guiPartList.handleMouseInput(mouseX, mouseY);
        super.mouseMoved(pMouseX, pMouseY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
    {
        searchText.mouseClicked(pMouseX, pMouseY, pButton);
        clearOnMouseLeftClicked(searchText, pMouseX, pMouseY, pButton);
        updateState();
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private void initFileList()
    {
        Path path = FileHelper.getDirectory(FileHelper.CLIENT_LIB_FOLDER, LogicalSide.CLIENT);
        PathMatcher filter = FileHelper.getMxtMatcher(path);
        try (Stream<Path> paths = Files.list(path))
        {
            libraryFiles = paths
                    .filter(filter::matches)
                    .collect(Collectors.toList());
        }
        catch (NullPointerException | IOException e)
        {
            LOGGER.error(e);
        }

        List<Path> files = new ArrayList<>();
        List<FileData> fileDatas = new ArrayList<>();
        for (Path file : libraryFiles)
        {
            FileData fileData = new FileData(file);
            if (fileData.name.toLowerCase(Locale.ROOT).contains(searchText.getValue().toLowerCase(Locale.ROOT)))
            {
                fileDatas.add(fileData);
            }
        }
        libraryFiles = files;
//        guiLibraryList.clear();
//        guiLibraryList.addAll(fileDatas);
        lastSearch = searchText.getValue();
    }

    private void searchAndSort()
    {
        if (!searchText.getValue().equals(lastSearch))
        {
            initFileList();
            sorted = false;
            updatePartList();
        }
        if (!sorted)
        {
            initFileList();
//            guiLibraryList.sort(sortType);
            sorted = true;
            updatePartList();
        }
    }

    private void updatePartList()
    {
//        if (guiLibraryList.isSelected(guiLibraryList.getSelectedIndex()))
//        {
//            Path path = guiLibraryList.get(guiLibraryList.getSelectedIndex()).path;
//            CompoundNBT compound = FileHelper.getCompoundFromFile(path);
//            tuneParts.clear();
//            if (compound != null)
//            {
//                mxTuneFile = MXTuneFile.build(compound);
//                tuneParts.addAll(mxTuneFile.getParts());
//            }
//            guiPartList.clear();
//            guiPartList.addAll(tuneParts);
//            LOGGER.debug("Selected file: {}", path.toString());
//        } else if (guiLibraryList.isEmpty())
//            guiPartList.clear();
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
       // ClientAudio.fadeOut(playId, 1);
        isPlaying = false;
        playId = PlayIdSupplier.INVALID;
    }

    @Override
    public void statusCallBack(ClientAudio.Status status, int playId)
    {
        Objects.requireNonNull(minecraft).submitAsync(() -> {
            if (this.playId == playId && (status == ClientAudio.Status.ERROR || status == ClientAudio.Status.DONE))
            {
                LOGGER.debug("AudioStatus event received: {}, playId: {}", status, playId);
                stop();
                updateState();
            }
        });
    }

    private void openFolder()
    {
        FileHelper.openFolder(FileHelper.CLIENT_LIB_FOLDER);
    }

    private void refresh()
    {
        init();
    }
}
