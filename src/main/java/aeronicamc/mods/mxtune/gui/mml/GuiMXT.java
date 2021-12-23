package aeronicamc.mods.mxtune.gui.mml;

import aeronicamc.mods.mxtune.caches.FileHelper;
import aeronicamc.mods.mxtune.config.MXTuneConfig;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXLabel;
import aeronicamc.mods.mxtune.gui.widget.MXLink;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
import aeronicamc.mods.mxtune.items.MXScreen;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.mxt.MXTuneFile;
import aeronicamc.mods.mxtune.mxt.MXTuneFileHelper;
import aeronicamc.mods.mxtune.mxt.MXTunePart;
import aeronicamc.mods.mxtune.mxt.MXTuneStaff;
import aeronicamc.mods.mxtune.network.PacketDispatcher;
import aeronicamc.mods.mxtune.network.messages.CreateSheetMusicMessage;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import aeronicamc.mods.mxtune.sound.IAudioStatusCallback;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import aeronicamc.mods.mxtune.util.ValidDuration;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import static aeronicamc.mods.mxtune.Reference.*;

public class GuiMXT extends MXScreen implements IAudioStatusCallback
{
    private static final Logger LOGGER = LogManager.getLogger(GuiMXT.class);
    public enum Mode
    {
        CLIENT, SERVER, SHEET_MUSIC
    }
    private final Screen parent;

    private boolean isStateCached;
    private MXLabel labelMXTFileName;
    private String cachedMXTFilename = "";
    private MXLabel labelTitle;
    private MXLabel labelAuthor;
    private MXLabel labelSource;
    private MXLabel labelDuration;
    private final MXTextFieldWidget textTitle = new MXTextFieldWidget(MXT_SONG_TITLE_LENGTH);
    private String cachedTitle = "";
    private final MXTextFieldWidget textAuthor = new MXTextFieldWidget(MXT_SONG_AUTHOR_LENGTH);
    private String cachedAuthor = "";
    private final MXTextFieldWidget textSource = new MXTextFieldWidget(MXT_SONG_SOURCE_LENGTH);
    private String cachedSource = "";
    private MXButton buttonPlayStop;
    private MXButton buttonDoneMode;
    private int durationTotal;
    private int ticks;

    // Links
    private MXLink sourcesLink = new MXLink(p->openSourceUrl());
    private MXLink mmlLink = new MXLink(p->openMmlUrl());

    // Common data
    private MXTuneFile mxTuneFile;
    private boolean isPlaying = false;
    private boolean cachedIsPlaying;
    private MXButton buttonSave;
    private static final int PADDING = 4;
    private final Mode mode;

    /* MML Player */
    private int playId = PlayIdSupplier.INVALID;

    // Child tabs
    private static final int MAX_TABS = 16;
    private static final int MIN_TABS = 1;
    private final GuiMXTPartTab[] childTabs = new GuiMXTPartTab[MAX_TABS];
    private int activeChildIndex;
    private int cachedActiveChildIndex;
    private MXButton buttonAddTab;
    private MXButton buttonMinusTab;
    private final MXButton[] buttonNames = new MXButton[MAX_TABS];

    // Tab limits - allow limiting the viewable tabs
    private int viewableTabCount = MIN_TABS;
    private int cachedViewableTabCount;

    public GuiMXT(Screen parent, Mode mode)
    {
        super(new TranslationTextComponent("gui.mxtune.gui_mxt.title"));
        this.parent = parent;
        this.mode = mode;
        for (int i = 0; i < MAX_TABS; i++)
        {
            childTabs[i] = new GuiMXTPartTab(this);
        }
    }

    @Override
    protected void init()
    {
        super.init();
        children.clear();
        buttons.clear();
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(true);
        int singleLineHeight = font.lineHeight + 2;
        int titleWidth = width - PADDING * 2;

        labelMXTFileName = new MXLabel(font, PADDING, PADDING, titleWidth, singleLineHeight, new TranslationTextComponent("gui.mxtune.label.filename"), -1);
        setDisplayedFilename(cachedMXTFilename, TextFormatting.AQUA);
        int buttonWidth = Math.max(((width * 2 / 3) - PADDING * 2) / 6, 65);
        int buttonY = labelMXTFileName.getY() + labelMXTFileName.getHeight();

        MXButton buttonNew = new MXButton(PADDING, buttonY, buttonWidth, 20, new TranslationTextComponent("gui.mxtune.button.new"), pNew->newAction());
        MXButton buttonImport = new MXButton(buttonNew.getLeft() + buttonNew.getWidth(), buttonY, buttonWidth, 20, new TranslationTextComponent("gui.mxtune.button.import"), pImport->importAction());
        MXButton buttonOpen = new MXButton(buttonImport.getLeft() + buttonImport.getWidth(), buttonY, buttonWidth, 20, new TranslationTextComponent("gui.mxtune.button.library"), pOpen->openAction());
        buttonSave = new MXButton(buttonOpen.getLeft() + buttonOpen.getWidth(), buttonY, buttonWidth, 20, new TranslationTextComponent("gui.mxtune.button.save"), pSave->saveAction());
        buttonDoneMode = new MXButton(buttonSave.getLeft() + buttonSave.getWidth(), buttonY, buttonWidth, 20, getDoneButtonNameByMode(), pDone->doneAction());
        MXButton buttonCancel = new MXButton(buttonDoneMode.getLeft() + buttonDoneMode.getWidth(), buttonY, buttonWidth, 20, new TranslationTextComponent("gui.cancel"), pCancel->cancelAction(true));
        if (mode == Mode.CLIENT)
            buttonCancel.visible = false;
        addButton(buttonNew);
        addButton(buttonImport);
        addButton(buttonOpen);
        addButton(buttonSave);
        addButton(buttonDoneMode);
        addButton(buttonCancel);

        // Links
        int textY = buttonDoneMode.y + buttonDoneMode.getHeight() + PADDING;
        int urlWidth = width / 2 - PADDING;
        sourcesLink.setAlignText(MXLink.AlignText.LEFT);
        sourcesLink.setLayout(PADDING, textY, urlWidth, singleLineHeight);
        sourcesLink.setUrl(textSource.getValue());
        mmlLink.setAlignText(MXLink.AlignText.RIGHT);
        mmlLink.setLayout(width - urlWidth - PADDING , textY, urlWidth, singleLineHeight);
        mmlLink.setUrl(MXTuneConfig.getMmlLink());
        addButton(sourcesLink);
        addButton(mmlLink);

        // Text fields
        textY = sourcesLink.y + sourcesLink.getHeight() + PADDING;
        ITextComponent labelTitleText = new TranslationTextComponent("gui.mxtune.label.title");
        int labelTitleWidth = font.width(labelTitleText);
        ITextComponent labelAuthorText = new TranslationTextComponent("gui.mxtune.label.author");
        int labelAuthorWidth = font.width(labelAuthorText);
        labelTitle = new MXLabel(font, PADDING, textY, labelTitleWidth, font.lineHeight + 2, labelTitleText, -1);
        textTitle.setLayout(labelTitle.getX() + labelTitleWidth + PADDING, textY, width / 2 - labelTitle.getWidth() - PADDING, font.lineHeight + 2);
        textTitle.setCanLoseFocus(true);
        labelAuthor = new MXLabel(font, textTitle.x + textTitle.getWidth() + PADDING, textY, labelAuthorWidth, font.lineHeight + 2, labelAuthorText, -1);
        textAuthor.setLayout(labelAuthor.getX() + labelAuthorWidth + PADDING, textY, width - labelAuthor.getX() - labelAuthor.getWidth() - PADDING * 2, font.lineHeight + 2);
        textAuthor.setCanLoseFocus(true);
        textY = textTitle.y + textTitle.getHeight() + PADDING;
        ITextComponent labelSourceText = new TranslationTextComponent("gui.mxtune.label.source");
        int labelSourceWidth = font.width(labelSourceText);
        labelSource = new MXLabel(font, PADDING, textY, labelSourceWidth, font.lineHeight + 2, labelSourceText,-1);
        textSource.setLayout( labelSource.getX() + labelSource.getWidth() + PADDING, textY, width - labelSource.getX() - labelSource.getWidth() - PADDING * 2, font.lineHeight + 2);
        textSource.setCanLoseFocus(true);
        addWidget(textTitle);
        addWidget(textAuthor);
        addWidget(textSource);

        // Button tabs
        int tabButtonTop = textSource.getTop() +  textSource.getHeight() + PADDING * 2;
        // TODO: NOTE SPECIAL BUTTON IDS 250, 251
        buttonAddTab = new MXButton(PADDING, tabButtonTop, 20, 20, new TranslationTextComponent("gui.mxtune.button.plus"), p->addTab());
        buttonMinusTab = new MXButton(buttonAddTab.x + buttonAddTab.getWidth(), tabButtonTop, 20, 20, new TranslationTextComponent("gui.mxtune.button.minus"), p->minusTab());
        addButton(buttonAddTab);
        addButton(buttonMinusTab);

        // Total Duration
        ITextComponent durationLabelName = new TranslationTextComponent("gui.mxtune.label.duration_value_total");
        int durationWidth = font.width(durationLabelName);
        durationWidth += font.width(SheetMusicHelper.formatDuration(0));
        durationWidth += font.width(" ");
        labelDuration = new MXLabel(font, width - durationWidth - PADDING, tabButtonTop + 20 + PADDING + 2, durationWidth, singleLineHeight, durationLabelName, -1);
        labelDuration.setLabelText(new StringTextComponent(SheetMusicHelper.formatDuration(durationTotal)));

        // Part Buttons
        int tabbedAreaTop = tabButtonTop + 20 + PADDING;
        for (int i = 0; i < MAX_TABS; i++)
        {
            buttonNames[i] = new MXButton(buttonMinusTab.x + buttonMinusTab.getWidth() + PADDING + 20 * i, tabButtonTop, 20, 20, new StringTextComponent(String.format("%d", i + 1)), p->setTab());
            buttonNames[i].setIndex(i);
            addButton(buttonNames[i]);
            buttonNames[i].active = false;
            childTabs[i].setLayout(tabbedAreaTop, height - PADDING);
            childTabs[i].refresh();
        }

        // Play/Stop
        buttonPlayStop = new MXButton(width - PADDING - 100, tabbedAreaTop - 24, 100, 20, isPlaying ? new TranslationTextComponent("gui.mxtune.button.stop") : new TranslationTextComponent("gui.mxtune.button.play_all"), p->play());
        buttonPlayStop.active = false;
        addButton(buttonPlayStop);
        reloadState();
        getSelection();
    }

    private void setTab()
    {
        Arrays.stream(buttonNames).filter(Widget::isHovered).findFirst().ifPresent(this::buttonAccept);
    }

    private void buttonAccept(Widget widget)
    {
        this.activeChildIndex = ((MXButton)widget).getIndex();
        ((MXButton)widget).active = true;
        this.childTabs[activeChildIndex].init(Objects.requireNonNull(minecraft), width, height);
        updateState();
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        renderBackground(pMatrixStack);
        // update tabbed button names here!
        updateTabbedButtonNames();
        labelMXTFileName.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        labelTitle.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        textTitle.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        labelAuthor.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        textAuthor.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        labelSource.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        textSource.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        labelDuration.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        drawMarkers(pMatrixStack);
        childTabs[activeChildIndex].render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    private void drawMarkers(MatrixStack pMatrixStack)
    {
        for (int i = 0; i < viewableTabCount; i++)
        {
            MXButton gb = buttonNames[i];
            if (!gb.active)
                drawBox(pMatrixStack, gb, -1);
            if (!childTabs[i].canPlay())
                drawLine(pMatrixStack, gb, 0xFFFF2222);
        }
    }

    private void drawBox(MatrixStack pMatrixStack, MXButton gb, int color)
    {
        hLine(pMatrixStack, gb.x, gb.x + gb.getWidth() - 1, gb.y, color);
        hLine(pMatrixStack, gb.x, gb.x + gb.getWidth() - 1, gb.y + gb.getHeight() - 1, color);
        vLine(pMatrixStack, gb.x, gb.y, gb.y + gb.getHeight() - 1, color);
        vLine(pMatrixStack, gb.x + gb.getWidth() - 1, gb.y, gb.y + gb.getHeight() - 1, color);
    }

    private void drawLine(MatrixStack pMatrixStack, MXButton gb, int color)
    {
        AbstractGui.fill(pMatrixStack, gb.x + 1, gb.y + gb.getHeight() + 1, gb.x +1 + gb.getWidth() - 2, gb.y + gb.getHeight() + 3, color);
    }

    @Override
    public void tick()
    {
        textTitle.tick();
        textAuthor.tick();
        textSource.tick();
        childTabs[activeChildIndex].tick();
        if ((ticks++ % 5) == 0) updateButtons();
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers)
    {
        return childTabs[activeChildIndex].charTyped(pCodePoint, pModifiers) || super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        return childTabs[activeChildIndex].keyPressed(pKeyCode, pScanCode, pModifiers) || super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers)
    {
        updateButtons();
        updateState();
        return childTabs[activeChildIndex].keyReleased(pKeyCode, pScanCode, pModifiers) || super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY)
    {
        return childTabs[activeChildIndex].isMouseOver(pMouseX, pMouseY) || super.isMouseOver(pMouseX, pMouseY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
    {
        textTitle.mouseClicked(pMouseX, pMouseY, pButton);
        textAuthor.mouseClicked(pMouseX, pMouseY, pButton);
        textSource.mouseClicked(pMouseX, pMouseY, pButton);
        return childTabs[activeChildIndex].mouseClicked(pMouseX, pMouseY, pButton) || super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton)
    {
        return childTabs[activeChildIndex].mouseReleased(pMouseX, pMouseY,  pButton) || super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY)
    {
        return childTabs[activeChildIndex].mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY) || super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta)
    {
        return childTabs[activeChildIndex].mouseScrolled(pMouseX, pMouseY, pDelta) || super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY)
    {
        childTabs[activeChildIndex].mouseMoved(pMouseX, pMouseY);
        super.mouseMoved(pMouseX, pMouseY);
    }

    private void reloadState()
    {
        if (isStateCached)
        {
            labelMXTFileName.setLabelText(new StringTextComponent(cachedMXTFilename));
            activeChildIndex = cachedActiveChildIndex;
            viewableTabCount = cachedViewableTabCount;
            isPlaying = cachedIsPlaying;
        }
        updateButtons();
    }

    private void updateState()
    {
        cachedMXTFilename = labelMXTFileName.getLabelText().getString();
        cachedActiveChildIndex = activeChildIndex;
        cachedViewableTabCount = viewableTabCount;
        cachedIsPlaying = isPlaying;
        isStateCached = true;
        sourcesLink.setUrl(textSource.getValue());
    }

    private void getSelection()
    {
        switch (ActionGet.INSTANCE.getSelector())
        {
            case DONE:
                break;
            case FILE_NEW:
                fileNew();
                break;
            case FILE_IMPORT:
                fileImport();
                break;
            case FILE_OPEN:
                fileOpen();
                break;
            case FILE_SAVE:
                break;
            default:
        }
        ActionGet.INSTANCE.setCancel();
    }

    private void fileNew()
    {
        mxTuneFile = null;
        setDisplayedFilename("", TextFormatting.AQUA);
        textTitle.setValue("");
        textAuthor.setValue("");
        textSource.setValue("");
        IntStream.range(0, MAX_TABS).forEach(i -> childTabs[i].clearPart());
        viewableTabCount = MIN_TABS;
        firstTab();
    }

    private void fileImport()
    {
        mxTuneFile = ActionGet.INSTANCE.getMxTuneFile();
        getMXTFileData();
        firstTab();
    }

    private void fileOpen()
    {
        mxTuneFile = MXTuneFileHelper.getMXTuneFile(ActionGet.INSTANCE.getPath());
        getMXTFileData();
        firstTab();
    }

    private void getMXTFileData()
    {
        if (mxTuneFile != null)
        {
            IntStream.range(0, MAX_TABS).forEach(i -> childTabs[i].clearPart());
            viewableTabCount = MIN_TABS;
            activeChildIndex = 0;
            textTitle.setValue(mxTuneFile.getTitle());
            textAuthor.setValue(mxTuneFile.getAuthor());
            textSource.setValue(mxTuneFile.getSource());
            sourcesLink.setUrl(mxTuneFile.getSource());
            int tab = 0;
            setDisplayedFilename(ActionGet.INSTANCE.getFileNameString(), TextFormatting.AQUA);
            Iterator<MXTunePart> iterator = mxTuneFile.getParts().iterator();
            while (iterator.hasNext() && tab < MAX_TABS)
            {
                childTabs[tab++].setPart(iterator.next());
                if (iterator.hasNext())
                    addTab();
            }
        }
        else
        {
            LOGGER.debug("Oops!");
//            Miscellus.audiblePingPlayer(mc.player, SoundEvents.BLOCK_GLASS_BREAK);
        }
    }

    private void updateButtons()
    {
        Arrays.stream(buttonNames).forEach(button -> {
            if (button.getIndex() >= 0 && button.getIndex() < MAX_TABS)
            {
                button.active = (activeChildIndex != button.getIndex());
                button.visible = (button.getIndex() < viewableTabCount);
                if (activeChildIndex >= viewableTabCount)
                    activeChildIndex = viewableTabCount - 1;
            }
        });

        buttonAddTab.active = viewableTabCount < MAX_TABS;
        buttonMinusTab.active = viewableTabCount > MIN_TABS;

        // Play/Stop button state and duration total
        int countOK = 0;
        int duration = 0;
        for (int i=0; i < viewableTabCount; i++)
        {
            countOK = childTabs[i].canPlay() ? countOK + 1 : countOK;
            int partDuration = childTabs[i].getDuration();
            duration = Math.max(partDuration, duration);
        }
        durationTotal = duration;
        labelDuration.setLabelText(new StringTextComponent(SheetMusicHelper.formatDuration(durationTotal)));
        boolean isOK = countOK == viewableTabCount;
        buttonPlayStop.active = isPlaying || isOK;
        buttonDoneMode.active = Mode.CLIENT == mode || (!textTitle.getValue().isEmpty() && isOK);
        buttonPlayStop.setMessage(isPlaying ? new TranslationTextComponent("gui.mxtune.button.stop") : new TranslationTextComponent("gui.mxtune.button.play_all"));
        sourcesLink.visible = sourcesLink.getUrl().matches("^(http(s)?:\\/\\/[a-zA-Z0-9\\-_]+\\.[a-zA-Z]+(.)+)+");

        buttonSave.active = !textTitle.getValue().isEmpty() && isOK;
    }

    private void updateTabbedButtonNames()
    {
        int prevWidth = buttonMinusTab.x + buttonMinusTab.getWidth();
        int staticButtonWidth = buttonMinusTab.getWidth() + buttonAddTab.getWidth() + buttonPlayStop.getWidth() +  PADDING * 3;
        for (int i = 0; i < viewableTabCount; i++)
        {
            MXButton gb = buttonNames[i];
            ITextComponent name = getButtonName(i);
            int nameWidth = font.width(name) + PADDING * 4;
            gb.x = prevWidth + PADDING;
            gb.setMessage(name);
            int limitedWidth = Math.min((width - staticButtonWidth) / viewableTabCount, Math.max(20, nameWidth));
            gb.setWidth(limitedWidth);
            prevWidth += limitedWidth;
        }
    }

    private ITextComponent getButtonName(int index)
    {
        String number = String.format("%d", index + 1);
        MXTunePart part = childTabs[index].getPart();
        ITextComponent localizedInstrumentName = new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(childTabs[index].getPart().getInstrumentName()));
        return (!part.getInstrumentName().equals("")) ? new StringTextComponent(String.format("%s: ", number)).append(localizedInstrumentName) : new StringTextComponent(number);
    }

    private ITextComponent getDoneButtonNameByMode()
    {
        switch (mode)
        {
            case CLIENT:
                return new TranslationTextComponent("gui.done");
            case SERVER:
                return new TranslationTextComponent("gui.mxtune.button.upload");
            case SHEET_MUSIC:
                return new TranslationTextComponent("gui.mxtune.button.write");
            default:
        }
        return new TranslationTextComponent("gui.none");
    }

    @Override
    public void onClose()
    {
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(false);
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    private void newAction()
    {
        stop();
        updateState();
        Objects.requireNonNull(minecraft).setScreen(new ConfirmScreen(this::newCallback, new TranslationTextComponent("gui.mxtune.confirm.new.text01"), new TranslationTextComponent("gui.mxtune.new.text02")));
    }

    private void newCallback(boolean result)
    {
        Objects.requireNonNull(minecraft).setScreen(this);
        if (result)
        {
            mxTuneFile = null;
            setDisplayedFilename("", TextFormatting.AQUA);
            textTitle.setValue("");
            textAuthor.setValue("");
            textSource.setValue("");
            IntStream.range(0, MAX_TABS).forEach(i -> childTabs[i].clearPart());
            viewableTabCount = MIN_TABS;
            firstTab();
        }
    }

    private void importAction()
    {
        stop();
        ActionGet.INSTANCE.clear();
        ActionGet.INSTANCE.setFileImport();
        viewableTabCount = MIN_TABS;
        Objects.requireNonNull(minecraft).setScreen(new GuiFileImporter(this));
    }

    private void openAction()
    {
        stop();
        ActionGet.INSTANCE.clear();
        ActionGet.INSTANCE.setFileOpen();
        viewableTabCount = MIN_TABS;
        Objects.requireNonNull(minecraft).setScreen(new GuiMusicLibrary(this));
    }

    private void saveAction()
    {
        // TODO: clean this up and toss warnings as needed.
        String fileName = FileHelper.removeExtension(cachedMXTFilename);
        if (fileName.length() < 1)
        {
            fileName = FileHelper.normalizeFilename(textTitle.getValue().trim());
            setDisplayedFilename(fileName, TextFormatting.AQUA);
        }
        if (!fileName.equals("") && !textTitle.getValue().trim().equals(""))
        {
            createMxt();
            CompoundNBT compound = new CompoundNBT();
            mxTuneFile.writeToNBT(compound);
            try
            {
                FileHelper.sendCompoundToFile(FileHelper.getCacheFile(FileHelper.CLIENT_LIB_FOLDER, fileName + FileHelper.EXTENSION_MXT, LogicalSide.CLIENT), compound);
            }
            catch (IOException e)
            {
                LOGGER.error(e);
            }
        }
        updateState();
    }

    private boolean uploadMxt()
    {
        if (!textTitle.getValue().trim().equals("") && buttonPlayStop.active)
        {
            createMxt();
//            PacketDispatcher.sendToServer(new SetServerSerializedDataMessage(mxTuneFile.getGUID(), RecordType.MXT, mxTuneFile));
            return true;
        }
        else
//            Miscellus.audiblePingPlayer(mc.player, SoundEvents.BLOCK_ANVIL_PLACE);
        return false;
    }

    private void createMxt()
    {
        if (mxTuneFile == null)
            mxTuneFile = new MXTuneFile();
        ValidDuration validDuration = SheetMusicHelper.validateMML(getMML());
        mxTuneFile.setDuration(validDuration.getDuration());
        mxTuneFile.setTitle(textTitle.getValue().trim());
        mxTuneFile.setAuthor(textAuthor.getValue().trim());
        mxTuneFile.setSource(textSource.getValue().trim());
        List<MXTunePart> parts = new ArrayList<>();
        for (int i = 0; i < viewableTabCount; i++)
        {
            childTabs[i].updatePart();
            MXTunePart part = childTabs[i].getPart();
            if (!part.getStaves().isEmpty())
                parts.add(part);
        }
        mxTuneFile.setParts(parts);
    }

    private void doneAction()
    {
        // Todo: Warning if un-saved! Quit Yes/No dialog
        stop();
        updateState();
        switch (mode)
        {
            case CLIENT:
                Objects.requireNonNull(minecraft).setScreen(new ConfirmScreen(this::cancelAction, new TranslationTextComponent("gui.mxtune.confirm.cancel.text01"), new TranslationTextComponent("gui.mxtune.confirm.cancel.text02")));
                break;
            case SERVER:
                if (uploadMxt())
                    Objects.requireNonNull(minecraft).setScreen(parent);
                break;
            case SHEET_MUSIC:
                if (makeSheetMusic())
                    Objects.requireNonNull(minecraft).setScreen(parent);
                break;
            default:
        }
    }

    private void cancelAction(boolean result)
    {
        if (result)
        {
            stop();
            Objects.requireNonNull(minecraft).setScreen(parent);
        }
    }

    private void openMmlUrl()
    {
        handleComponentClicked(mmlLink.getLinkComponent());
    }

    private void openSourceUrl()
    {
        handleComponentClicked(sourcesLink.getLinkComponent());
    }

    private boolean makeSheetMusic()
    {
        if (!textTitle.getValue().trim().equals("") && buttonPlayStop.active)
        {
            String title = this.textTitle.getValue();
            for (int i = 0; i < viewableTabCount; i++)
            {
                childTabs[i].updatePart();
                ITextComponent instrumentName = new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(childTabs[i].getPart().getInstrumentName()));
                String titleAndInstrument = formatTitle(title, i, viewableTabCount, instrumentName.getString());
                String mml = childTabs[i].getMMLClipBoardFormat();
                PacketDispatcher.sendToServer(new CreateSheetMusicMessage(titleAndInstrument, mml));
            }
            return true;
        }
        else
        {
            LOGGER.info("Oops! Invalid MML");
//            Miscellus.audiblePingPlayer(mc.player, SoundEvents.BLOCK_ANVIL_PLACE);
        }

        return false;
    }

    private String formatTitle(String title, int part, int parts, String instrumentName)
    {
        if (parts == 1)
            return String.format("%s (%s)", title, instrumentName);
        else
            return String.format("%s (%d-%d : %s)", title, part + 1, parts, instrumentName);
    }

    private void addTab()
    {
        viewableTabCount = (viewableTabCount + 1) > MAX_TABS ? viewableTabCount : viewableTabCount + 1;
        activeChildIndex = viewableTabCount - 1;
        updateState();
    }

    private void minusTab()
    {
        viewableTabCount = (viewableTabCount - 1) >= MIN_TABS ? viewableTabCount - 1 : viewableTabCount;
        updateState();
    }

    private void firstTab()
    {
        activeChildIndex = 0;
        childTabs[activeChildIndex].tick();
        updateState();
    }

    private void setDisplayedFilename(@Nullable String name, TextFormatting textFormatting)
    {
        if (name == null) name = "";
        labelMXTFileName.setLabelText(new StringTextComponent(name).withStyle(textFormatting));
    }

    public String getMML()
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < viewableTabCount; i++)
        {
            childTabs[i].updatePart();
            MXTunePart part = childTabs[i].getPart();
            builder.append("MML@I=").append(SoundFontProxyManager.getIndexById(part.getInstrumentName()));
            Iterator<MXTuneStaff> iterator = part.getStaves().iterator();
            while (iterator.hasNext())
            {
                builder.append(iterator.next().getMml());
                if (iterator.hasNext())
                    builder.append(",");
            }
            builder.append(";");
        }
        return builder.toString();
    }

    private void stop()
    {
        Objects.requireNonNull(minecraft).submitAsync(()->ClientAudio.queueAudioDataRemoval(playId));
        isPlaying = false;
        playId = PlayIdSupplier.INVALID;

        // stop child tabs
        Arrays.stream(childTabs).forEach(GuiMXTPartTab::onClose);
        updateState();
    }

    private boolean mmlPlay(String mmlIn)
    {
        playId = PlayIdSupplier.PlayType.PERSONAL.getAsInt();
        Objects.requireNonNull(minecraft).submitAsync(()->ClientAudio.playLocal(playId, mmlIn, this));
        updateState();
        return true;
    }

    private void play()
    {
        if (isPlaying)
        {
            stop();
        }
        else
        {
            isPlaying = mmlPlay(getMML());
        }
    }

    @Override
    public void statusCallBack(ClientAudio.Status status, int playId)
    {
        if (this.playId == playId && (status == ClientAudio.Status.ERROR || status == ClientAudio.Status.DONE))
        {
            LOGGER.debug("AudioStatus event received: {}, playId: {}", status, playId);
            stop();
        }
    }
}
