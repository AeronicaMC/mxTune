package aeronicamc.mods.mxtune.gui.mml;

import aeronicamc.mods.mxtune.caches.DirectoryWatcher;
import aeronicamc.mods.mxtune.caches.FileHelper;
import aeronicamc.mods.mxtune.gui.MXScreen;
import aeronicamc.mods.mxtune.gui.ModGuiHelper;
import aeronicamc.mods.mxtune.gui.TextColorFg;
import aeronicamc.mods.mxtune.gui.widget.*;
import aeronicamc.mods.mxtune.gui.widget.list.FileDataList;
import aeronicamc.mods.mxtune.gui.widget.list.PartList;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.mxt.MXTuneFile;
import aeronicamc.mods.mxtune.mxt.MXTuneFileHelper;
import aeronicamc.mods.mxtune.mxt.MXTunePart;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import aeronicamc.mods.mxtune.sound.IAudioStatusCallback;
import aeronicamc.mods.mxtune.util.MusicProperties;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static aeronicamc.mods.mxtune.gui.ModGuiHelper.*;
import static net.minecraftforge.fml.LogicalSide.CLIENT;

public class GuiMusicLibrary extends MXScreen implements IAudioStatusCallback
{
    private static final Logger LOGGER = LogManager.getLogger(GuiMusicLibrary.class);
    private final Object threadSync = new Object();
    private final Screen parent;

    private final FileDataList fileDataListWidget = new FileDataList();
    private FileDataList.Entry selectedEntry;

    private MXLabel titleLabel;
    private MXLabel searchLabel;
    private final MXTextFieldWidget searchText = new MXTextFieldWidget(30);
    private boolean sorted;
    private SortType sortType = SortType.NORMAL;
    private String lastSearch = "";

    private List<Path> libraryPaths = new ArrayList<>();

    private final DirectoryWatcher watcher;
    private boolean watcherStarted;

    // Part List
    private MXTuneFile mxTuneFile;
    private final PartList partListWidget = new PartList();
    private final List<MXTunePart> tuneParts = new ArrayList<>();

    // playing
    private MXButton buttonPlay;
    private boolean isPlaying;
    private int playId;

    private final GuiHelpButton buttonGuiHelp = new GuiHelpButton(p -> helpToggled());

    public GuiMusicLibrary(Screen parent)
    {
        super(new TranslationTextComponent("gui.mxtune.gui_music_library.title"));
        this.parent = parent;

        // refresh the file list automatically - might be better to not bother with extension filtering, but we'll see
        DirectoryStream.Filter<Path> filter = entry ->
                (entry.toString().toLowerCase(Locale.ROOT).endsWith(".mxt"));
        watcher = new DirectoryWatcher.Builder()
                .addDirectories(FileHelper.getDirectory(FileHelper.CLIENT_MML_FOLDER, CLIENT))
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
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(false);
        stopWatcher();
        stopMusic();
        minecraft.setScreen(parent);
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
    protected void init()
    {
        super.init();
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(true);
        int listWidth = (width - 15) * 3 / 4;
        int entryHeight = Objects.requireNonNull(minecraft).font.lineHeight + 2;
        int left = 5;
        int titleTop = 20;
        int listTop = titleTop + 25;
        int listHeight = height - titleTop - entryHeight - 2 - 10 - 25 - 25;
        int listBottom = listTop + listHeight;
        int statusTop = listBottom + 4;
        int partListWidth = (width - 15) / 4;

        titleLabel = new MXLabel(font, (width - font.width(title)) / 2, 5, font.width(title), entryHeight, title, TextColorFg.WHITE);
        fileDataListWidget.setLayout(left, listTop, listWidth, listHeight);
        fileDataListWidget.setCallBack((entry, doubleClicked) -> {
            selectedEntry = entry;
            if (doubleClicked)
                selectDone(true);
            updatePartList();
        });

        partListWidget.setLayout(fileDataListWidget.getRight() +5 , listTop, partListWidth, listHeight);
        partListWidget.setActive(false);

        ITextComponent searchLabelText = new TranslationTextComponent("gui.mxtune.label.search");
        int searchLabelWidth = font.width(searchLabelText) + 4;
        searchLabel = new MXLabel(font, left, statusTop, searchLabelWidth, entryHeight + 2, searchLabelText, TextColorFg.WHITE);
        searchText.setLayout(left + searchLabelWidth, statusTop, listWidth - searchLabelWidth, entryHeight);
        searchText.setFocus(true);
        searchText.setCanLoseFocus(true);

        int buttonMargin = 1;
        int buttonWidth = 75;
        int x = left;

        SortType.NORMAL.button = new MXButton(x, titleTop, buttonWidth, 20 , SortType.NORMAL.getButtonText(), b -> resortFiles(SortType.NORMAL));
        addButton(SortType.NORMAL.button);
        SortType.NORMAL.button.addHooverText(true, SortType.NORMAL.getButtonText());
        SortType.NORMAL.button.addHooverText(false, new TranslationTextComponent("gui.mxtune.button_order.normal.help01").withStyle(TextFormatting.YELLOW));
        x += buttonWidth + buttonMargin;
        SortType.A_TO_Z.button = new MXButton(x, titleTop, buttonWidth, 20 , SortType.A_TO_Z.getButtonText(), b -> resortFiles(SortType.A_TO_Z));
        SortType.A_TO_Z.button.addHooverText(true, SortType.A_TO_Z.getButtonText());
        SortType.A_TO_Z.button.addHooverText(false, new TranslationTextComponent("gui.mxtune.button_order.a_to_z.help01").withStyle(TextFormatting.YELLOW));
        addButton(SortType.A_TO_Z.button);
        x += buttonWidth + buttonMargin;
        SortType.Z_TO_A.button = new MXButton(x, titleTop, buttonWidth, 20 , SortType.Z_TO_A.getButtonText(), b -> resortFiles(SortType.Z_TO_A));
        SortType.Z_TO_A.button.addHooverText(true, SortType.Z_TO_A.getButtonText());
        SortType.Z_TO_A.button.addHooverText(false, new TranslationTextComponent("gui.mxtune.button_order.z_to_a.help01").withStyle(TextFormatting.YELLOW));
        addButton(SortType.Z_TO_A.button);

        int buttonTop = height - 25;
        int xOpen = (this.width /2) - (((75 * 5)+20)/2) - (buttonMargin *5);
        int xRefresh = xOpen + buttonWidth + buttonMargin;
        int xPlay = xRefresh + buttonWidth + buttonMargin;
        int xDone = xPlay + buttonWidth + buttonMargin;
        int xCancel = xDone + buttonWidth + buttonMargin;
        int xHelp = xCancel + buttonWidth + buttonMargin;
        MXButton mxbOpenFolder = new MXButton(new TranslationTextComponent("gui.mxtune.button.open_folder"), open->openFolder());
        mxbOpenFolder.setLayout(xOpen, buttonTop, 75, 20);
        mxbOpenFolder.addHooverText(true, new TranslationTextComponent("gui.mxtune.button.open_folder").withStyle(TextFormatting.RESET));
        mxbOpenFolder.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.open_folder.help01").withStyle(TextFormatting.YELLOW));
        mxbOpenFolder.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.open_folder.help02").withStyle(TextFormatting.GREEN));
        addButton(mxbOpenFolder);

        MXButton mxbRefreshFiles = new MXButton(new TranslationTextComponent("gui.mxtune.button.refresh"), refresh->refreshFiles());
        mxbRefreshFiles.setLayout(xRefresh, buttonTop, 75, 20);
        mxbRefreshFiles.addHooverText(true, new TranslationTextComponent("gui.mxtune.button.refresh").withStyle(TextFormatting.RESET));
        mxbRefreshFiles.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.refresh.help01").withStyle(TextFormatting.YELLOW));
        addButton(mxbRefreshFiles);

        buttonPlay = new MXButton(new TranslationTextComponent("gui.mxtune.button.play"), p -> playMusic());
        buttonPlay.setLayout(xPlay, buttonTop, 75, 20);
        buttonPlay.addHooverText(true, new TranslationTextComponent("gui.mxtune.button.play").withStyle(TextFormatting.RESET));
        addButton(buttonPlay);

        MXButton mxbSelectDone = new MXButton(new TranslationTextComponent("gui.mxtune.button.select"), select->selectDone(false));
        mxbSelectDone.setLayout(xDone, buttonTop, 75, 20);
        mxbSelectDone.addHooverText(true, new TranslationTextComponent("gui.mxtune.button.select").withStyle(TextFormatting.RESET));
        addButton(mxbSelectDone);

        MXButton mxbCancelDone = new MXButton(new TranslationTextComponent("gui.cancel"), cancel->cancelDone());
        mxbCancelDone.setLayout(xCancel, buttonTop, 75, 20);
        mxbCancelDone.addHooverText(true, new TranslationTextComponent("gui.cancel").withStyle(TextFormatting.RESET));
        addButton(mxbCancelDone);
        buttonGuiHelp.setLayout(xHelp, buttonTop, 20, 20);
        addButton(buttonGuiHelp);

        addWidget(fileDataListWidget);
        addWidget(partListWidget);
        addWidget(searchText);
        startWatcher();
        sorted = false;
        updateState();
        resortFiles(sortType);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    private void updateState()
    {
        buttonPlay.setMessage(new TranslationTextComponent( isPlaying ? "gui.mxtune.button.stop" : "gui.mxtune.button.play" ));
        buttonPlay.addHooverText(true, new TranslationTextComponent( isPlaying ? "gui.mxtune.button.stop" : "gui.mxtune.button.play" ));
        buttonGuiHelp.addHooverText(true, HELP_HELP01);
        buttonGuiHelp.addHooverText(false, buttonGuiHelp.isHelpEnabled() ? HELP_HELP02 : HELP_HELP03);
        updatePartList();
    }

    @Override
    public void tick()
    {
        searchText.tick();

        if (selectedEntry != null && fileDataListWidget.getEntries().contains(selectedEntry) && !selectedEntry.equals(fileDataListWidget.getSelected()))
            fileDataListWidget.setSelected(selectedEntry);

        if (!sorted)
        {
            reloadFiles();
            fileDataListWidget.getEntries().sort(sortType);
            if (selectedEntry != null)
            {
                selectedEntry = fileDataListWidget.getEntries().stream().filter(e -> e.getName().equals(selectedEntry.getName())).findFirst().orElse(null);
                if (selectedEntry != null)
                    fileDataListWidget.centerScrollOn(selectedEntry);
            }
            sorted = true;
        }

        if (!searchText.getValue().equals(lastSearch))
        {
            reloadFiles();
            updatePartList();
            sorted = false;
        }
    }

    private void helpToggled() {
        buttonGuiHelp.setHelpEnabled(!buttonGuiHelp.isHelpEnabled());
        children.stream().filter(IHooverText.class::isInstance)
                .forEach(b -> ((IHooverText) b).setHooverTextOverride(buttonGuiHelp.isHelpEnabled()));
        updateState();
    }

    private void reloadFiles()
    {
        synchronized (threadSync)
        {
            Path path = FileHelper.getDirectory(FileHelper.CLIENT_LIB_FOLDER, CLIENT);
            PathMatcher filter = FileHelper.getMxtMatcher(path);
            try (Stream<Path> paths = Files.list(path))
            {
                libraryPaths = paths
                        .filter(filter::matches)
                        .collect(Collectors.toList());
            } catch (NullPointerException | IOException e)
            {
                LOGGER.error(e);
            }

            List<FileData> fileDataArrayList = new ArrayList<>();
            for (Path libPath : libraryPaths)
            {
                FileData fileData = new FileData(libPath);
                if (fileData.getName().toLowerCase(Locale.ROOT).contains(searchText.getValue().toLowerCase(Locale.ROOT)))
                {
                    fileDataArrayList.add(fileData);
                }
            }
            fileDataListWidget.clear();
            fileDataListWidget.addAll(fileDataArrayList);
            lastSearch = searchText.getValue();
            updatePartList();
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

    private void openFolder()
    {
        FileHelper.openFolder(FileHelper.CLIENT_LIB_FOLDER);
    }

    private void refreshFiles()
    {
        init();
    }

    private void selectDone(boolean doubleClicked)
    {
        // action get file, etc...
        if (fileDataListWidget.getSelected() != null)
            ActionGet.INSTANCE.select(fileDataListWidget.getSelected().getPath());
        if (doubleClicked)
            getMC().mouseHandler.releaseMouse();
        onClose();
    }

    private void cancelDone()
    {
        // Future Actions on dialog close confirmation
        onClose();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        updateState();
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
    {
        searchText.mouseClicked(pMouseX, pMouseY, pButton);
        ModGuiHelper.clearOnMouseLeftClicked(searchText, pMouseX, pMouseY, pButton);
        updateState();
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        titleLabel.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        searchLabel.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        fileDataListWidget.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        partListWidget.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        searchText.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    private void updatePartList()
    {
        if (fileDataListWidget.getSelected() != null && !fileDataListWidget.getEntries().isEmpty())
        {
            Path path = fileDataListWidget.getSelected().getPath();
            CompoundNBT compound = FileHelper.getCompoundFromFile(path);
            tuneParts.clear();
            if (compound != null)
            {
                mxTuneFile = MXTuneFile.build(compound);
                tuneParts.addAll(mxTuneFile.getParts());
            }
            partListWidget.clear();
            partListWidget.addAll(tuneParts);
        } else if (fileDataListWidget.getEntries().isEmpty())
        {
            fileDataListWidget.setSelected(null);
            mxTuneFile = null;
            partListWidget.clear();
        }
    }

    private boolean isPlaying()
    {
        return this.isPlaying;
    }

    private void setPlaying(boolean play)
    {
        isPlaying = play;
    }

    private void playMusic()
    {
        if (isPlaying())
        {
            stopMusic();
        }
        else if (mxTuneFile != null)
        {
            setPlaying(true);
            MusicProperties musicProperties = MXTuneFileHelper.getMusicProperties(mxTuneFile);
            playId = PlayIdSupplier.PlayType.PERSONAL.getAsInt();
            Objects.requireNonNull(minecraft).submitAsync(
                    () -> ClientAudio.playLocal(musicProperties.getDuration(), playId, musicProperties.getMusicText(), this));
        }
        updateState();
    }

    private void stopMusic()
    {
        Objects.requireNonNull(minecraft).submitAsync(
                ()-> ClientAudio.fadeOut(playId, 1));
        setPlaying(false);
    }

    @Override
    public void statusCallBack(ClientAudio.Status status, int playId)
    {
        Objects.requireNonNull(minecraft).submitAsync(() -> {
            if ((this.playId == playId) && ClientAudio.isDoneOrYieldStatus(status))
            {
                ClientAudio.fadeOut(playId, 1);
                setPlaying(false);
                updateState();
            }
        });
    }
}
