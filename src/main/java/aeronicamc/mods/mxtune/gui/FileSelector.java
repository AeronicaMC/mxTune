package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.caches.DirectoryWatcher;
import aeronicamc.mods.mxtune.caches.FileHelper;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
import aeronicamc.mods.mxtune.gui.widget.label.MXLabel;
import aeronicamc.mods.mxtune.gui.widget.list.PathList;
import aeronicamc.mods.mxtune.items.MXScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.loading.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraftforge.fml.LogicalSide.CLIENT;

public class FileSelector extends MXScreen
{
    private static final Logger LOGGER = LogManager.getLogger(FileSelector.class);
    private enum SortType implements Comparator<PathList.Entry>
    {
        NORMAL,
        A_TO_Z{ @Override protected int compare(String name1, String name2){ return name1.compareTo(name2); }},
        Z_TO_A{ @Override protected int compare(String name1, String name2){ return name2.compareTo(name1); }};

        Button button;
        protected int compare(String name1, String name2){ return 0; }
        @Override
        public int compare(PathList.Entry o1, PathList.Entry o2) {
            String name1 = StringUtils.toLowerCase(o1.getPath().getFileName().toString());
            String name2 = StringUtils.toLowerCase(o2.getPath().getFileName().toString());
            return compare(name1, name2);
        }

        ITextComponent getButtonText() {
            return new TranslationTextComponent("fml.menu.mods." + StringUtils.toLowerCase(name()));
        }
    }
    private final Screen parent;
    private int guiLeft;
    private int guiTop;
    private boolean isStateCached;
    private int cachedSelectedIndex;

    private final PathList pathListWidget =  new PathList();
    private PathList.Entry selectedEntry;

    private MXLabel titleLabel;
    private MXLabel searchLabel;
    private MXTextFieldWidget searchText = new MXTextFieldWidget(30);
    private boolean sorted = false;
    private SortType sortType = SortType.NORMAL;
    private String lastSearch = "";
    private SortType cachedSortType = SortType.NORMAL;

    private MXButton buttonCancel;

    private List<Path> fileList = new ArrayList<>();

    private final DirectoryWatcher watcher;
    private boolean watcherStarted = false;
    private static final int PADDING = 6;

    protected FileSelector(Screen parent)
    {
        super(new TranslationTextComponent("screen.mxtune.file_selector.title"));
        this.parent = parent;

        // refresh the file list automatically - might be better to not bother the extension filtering but we'll see
        DirectoryStream.Filter<Path> filter = entry ->
                (entry.toString().endsWith(".zip")
                         || entry.toString().endsWith(".mml")
                         || entry.toString().endsWith(".ms2mml"));
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
        this.guiLeft = 0;
        this.guiTop = 0;
        int listWidth = width - 10;
        int entryHeight = Objects.requireNonNull(minecraft).font.lineHeight + 2;
        int left = 5;
        int titleTop = 20;
        int listTop = titleTop + 25;
        int listHeight = height - titleTop - entryHeight - 2 - 10 - 25 - 25;
        int listBottom = listTop + listHeight;
        int statusTop = listBottom + 4;

        titleLabel = new MXLabel(font, (width - font.width(title)) / 2, 5, font.width(title), entryHeight, title, TextColorFg.WHITE);
        pathListWidget.setLayout(left, listTop, listWidth, listHeight);
        this.pathListWidget.setCallBack((entry) -> {
            selectedEntry = entry;
        });

        ITextComponent searchLabelText = new TranslationTextComponent("gui.mxtune.label.search");
        int searchLabelWidth = font.width(searchLabelText) + 4;
        searchLabel = new MXLabel(font, left, statusTop, searchLabelWidth, entryHeight + 2, searchLabelText, TextColorFg.WHITE);
        //searchText = new TextFieldWidget(font, left + searchLabelWidth, statusTop, listWidth - searchLabelWidth, entryHeight, new TranslationTextComponent("gui.mxtune.label.search"));
        searchText.setLayout(left + searchLabelWidth, statusTop, listWidth - searchLabelWidth, entryHeight);
        searchText.setMessage(new TranslationTextComponent("gui.mxtune.label.search"));
        searchText.setFocus(true);
        searchText.setCanLoseFocus(true);

        int buttonMargin = 1;
        int buttonWidth = 75;
        int x = left;

        addButton(SortType.NORMAL.button = new Button(x, titleTop, buttonWidth - buttonMargin, 20 , SortType.NORMAL.getButtonText(), b -> resortFiles(SortType.NORMAL)));
        x += buttonWidth + buttonMargin;
        addButton(SortType.A_TO_Z.button = new Button(x, titleTop, buttonWidth - buttonMargin, 20 , SortType.A_TO_Z.getButtonText(), b -> resortFiles(SortType.A_TO_Z)));
        x += buttonWidth + buttonMargin;
        addButton(SortType.Z_TO_A.button = new Button(x, titleTop, buttonWidth - buttonMargin, 20 , SortType.Z_TO_A.getButtonText(), b -> resortFiles(SortType.Z_TO_A)));

        int buttonTop = height - 25;
        int xOpen = (this.width /2) - 75 * 2;
        int xRefresh = xOpen + 75;
        int xDone = xRefresh + 75;
        int xCancel = xDone + 75;
        MXButton mxbOpenFolder = new MXButton(open->openFolder());
        mxbOpenFolder.setLayout(xOpen, buttonTop, 75, 20);
        mxbOpenFolder.addHooverText(true, new StringTextComponent("TEST Help Title").withStyle(TextFormatting.YELLOW));
        mxbOpenFolder.addHooverText(false, new StringTextComponent("TEST Help Text . . . blah blah").withStyle(TextFormatting.WHITE));
        addButton(mxbOpenFolder);

        addWidget(pathListWidget);
        addWidget(searchText);
        startWatcher();
        resortFiles(sortType);
    }

    @Override
    public void tick()
    {
        searchText.tick();

        if (selectedEntry != null && pathListWidget.children().contains(selectedEntry) && !selectedEntry.equals(pathListWidget.getSelected()))
            pathListWidget.setSelected(selectedEntry);

        if (!sorted)
        {
            reloadFiles();
            pathListWidget.getEntries().sort(sortType);
            if (selectedEntry != null)
            {
                selectedEntry = pathListWidget.children().stream().filter(e -> e.getPath().equals(selectedEntry.getPath())).findFirst().orElse(null);
                if (selectedEntry != null)
                    pathListWidget.centerScrollOn(selectedEntry);
            }
            sorted = true;
        }

        if (!searchText.getValue().equals(lastSearch))
        {
            reloadFiles();
            sorted = false;
        }

    }

    private void reloadFiles()
    {
        synchronized (new Object())
        {
            Path path = FileHelper.getDirectory(FileHelper.CLIENT_MML_FOLDER, CLIENT);
            PathMatcher filter = FileHelper.getMmlMatcher(path);
            try (Stream<Path> paths = Files.list(path))
            {
                fileList = paths
                        .filter(filter::matches)
                        .collect(Collectors.toList());
            } catch (NullPointerException | IOException e)
            {
                LOGGER.error(e);
            }
            List<Path> files = new ArrayList<>();
            for (Path file : fileList)
            {
                if (file.getFileName().toString().toLowerCase(Locale.ROOT).contains(searchText.getValue().toLowerCase(Locale.ROOT)))
                {
                    files.add(file);
                }
            }
            fileList = files;
            pathListWidget.clear();
            pathListWidget.addAll(files);
            lastSearch = searchText.getValue();
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
        FileHelper.openFolder(FileHelper.CLIENT_MML_FOLDER);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
    {
        searchText.mouseClicked(pMouseX, pMouseY, pButton);
        ModGuiHelper.clearOnMouseLeftClicked(searchText, pMouseX, pMouseY, pButton);
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        titleLabel.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        searchLabel.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        pathListWidget.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        searchText.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
