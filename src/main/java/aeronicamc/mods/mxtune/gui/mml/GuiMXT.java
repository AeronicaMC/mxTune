package aeronicamc.mods.mxtune.gui.mml;

import aeronicamc.mods.mxtune.caches.FileHelper;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXLabel;
import aeronicamc.mods.mxtune.gui.widget.MXLink;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
import aeronicamc.mods.mxtune.items.MXScreen;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.mxt.MXTuneFile;
import aeronicamc.mods.mxtune.mxt.MXTunePart;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import aeronicamc.mods.mxtune.sound.IAudioStatusCallback;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import aeronicamc.mods.mxtune.util.ValidDuration;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private MXTextFieldWidget textTitle = new MXTextFieldWidget(MXT_SONG_TITLE_LENGTH);
    private String cachedTitle = "";
    private MXTextFieldWidget textAuthor = new MXTextFieldWidget(MXT_SONG_AUTHOR_LENGTH);
    private String cachedAuthor = "";
    private MXTextFieldWidget textSource = new MXTextFieldWidget(MXT_SONG_SOURCE_LENGTH);
    private String cachedSource = "";
    private MXButton buttonPlayStop;
    private MXButton buttonDoneMode;
    private int durationTotal;
    private int ticks;

    // Links
    private MXLink sourcesLink;
    private MXLink mmlLink;

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
    private static final int TAB_BTN_IDX = 200;
//    private final GuiMXTPartTab[] childTabs = new GuiMXTPartTab[MAX_TABS];
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
    }

    @Override
    protected void init()
    {
        super.init();
        Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(true);
        int singleLineHeight = font.lineHeight + 2;
        int titleWidth = width - PADDING * 2;

        labelMXTFileName = new MXLabel(font, PADDING, PADDING, titleWidth, singleLineHeight, new StringTextComponent(""), -1);
        setDisplayedFilename(cachedMXTFilename, TextFormatting.AQUA);
        int buttonWidth = Math.max(((width * 2 / 3) - PADDING * 2) / 6, 65);
        int buttonY = labelMXTFileName.getY() + labelMXTFileName.getHeight();

        MXButton buttonNew = new MXButton(PADDING, buttonY, buttonWidth, 20, new TranslationTextComponent("gui.mxtune.button.new"), pNew->newAction());
        MXButton buttonImport = new MXButton(buttonNew.getLeft() + buttonNew.getWidth(), buttonY, buttonWidth, 20, new TranslationTextComponent("mxtune.gui.button.import"), pImport->importAction());
        MXButton buttonOpen = new MXButton(buttonImport.getLeft() + buttonImport.getWidth(), buttonY, buttonWidth, 20, new TranslationTextComponent("mxtune.gui.button.open"), pOpen->openAction());
        buttonSave = new MXButton(buttonOpen.getLeft() + buttonOpen.getWidth(), buttonY, buttonWidth, 20, new TranslationTextComponent("mxtune.gui.button.save"), pSave->saveAction());
        buttonDoneMode = new MXButton(buttonSave.getLeft() + buttonSave.getWidth(), buttonY, buttonWidth, 20, getDoneButtonNameByMode(), pDone->updateState());
        MXButton buttonCancel = new MXButton(buttonDoneMode.getLeft() + buttonDoneMode.getWidth(), buttonY, buttonWidth, 20, new TranslationTextComponent("gui.cancel"), pCancel->cancelAction());
        if (mode == Mode.CLIENT)
            buttonCancel.visible = false;

        addButton(buttonNew);
        addButton(buttonImport);
        addButton(buttonOpen);
        addButton(buttonSave);
        addButton(buttonDoneMode);
        addButton(buttonCancel);
    }

    private void updateState()
    {

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
                return new TranslationTextComponent("gui.mxtune.button.create");
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
        Objects.requireNonNull(minecraft).setScreen(new ConfirmScreen(this::newCallback, new TranslationTextComponent(""), new TranslationTextComponent("")));
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
//            IntStream.range(0, MAX_TABS).forEach(i -> childTabs[i].clearPart());
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
        Objects.requireNonNull(minecraft).setScreen(new GuiFileSelector(this));
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
//            childTabs[i].updatePart();
//            MXTunePart part = childTabs[i].getPart();
//            if (!part.getStaves().isEmpty())
//                parts.add(part);
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
//                minecraft.setScreen(new GuiYesNo(this, "Have you saved changes?","Do you still want to exit?",4));
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

    private boolean makeSheetMusic()
    {
        if (!textTitle.getValue().trim().equals("") && buttonPlayStop.active)
        {
            String title = this.textTitle.getValue();
            for (int i = 0; i < viewableTabCount; i++)
            {
//                childTabs[i].updatePart();
//                String instrumentName = I18n.format("item.mxtune:multi_inst." + childTabs[i].getPart().getInstrumentName() + ".name");
//                String titleAndInstrument = formatTitle(title, i, viewableTabCount, instrumentName);
//                String mml = childTabs[i].getMMLClipBoardFormat();
//                PacketDispatcher.sendToServer(new MusicTextMessage(titleAndInstrument, mml));
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

    private void cancelAction()
    {
        stop();
        Objects.requireNonNull(minecraft).setScreen(parent);
    }

    private void addTab()
    {
        viewableTabCount = (viewableTabCount + 1) > MAX_TABS ? viewableTabCount : viewableTabCount + 1;
        activeChildIndex = viewableTabCount - 1;
    }

    private void minusTab()
    {
        viewableTabCount = (viewableTabCount - 1) >= MIN_TABS ? viewableTabCount - 1 : viewableTabCount;
    }

    private void firstTab()
    {
        activeChildIndex = 0;
//        childTabs[activeChildIndex].updateScreen();
        updateState();
    }

    private void setDisplayedFilename(@Nullable String name, TextFormatting textFormatting)
    {
        if (name == null) name = "";
        labelMXTFileName.setLabel(new TranslationTextComponent("gui.mxtune.gui_mxt.filename")
                                          .withStyle(TextFormatting.WHITE)
                                          .append(name).withStyle(textFormatting));
    }

    public String getMML()
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < viewableTabCount; i++)
        {
//            childTabs[i].updatePart();
//            MXTunePart part = childTabs[i].getPart();
//            builder.append("MML@I=").append(SoundFontProxyManager.getIndexById(part.getInstrumentName()));
//            Iterator<MXTuneStaff> iterator = part.getStaves().iterator();
//            while (iterator.hasNext())
//            {
//                builder.append(iterator.next().getMml());
//                if (iterator.hasNext())
//                    builder.append(",");
//            }
            builder.append(";");
        }
        return builder.toString();
    }

    private void stop()
    {
        Objects.requireNonNull(minecraft).submitAsync(()->ClientAudio.stop(playId));
        isPlaying = false;
        playId = PlayIdSupplier.INVALID;

        // stop child tabs
//        Arrays.stream(childTabs).forEach(GuiMXTPartTab::onGuiClosed);
        updateState();
    }

    private boolean mmlPlay(String mmlIn)
    {
        playId = PlayIdSupplier.PlayType.PERSONAL.getAsInt();
        Objects.requireNonNull(minecraft).submitAsync(()->ClientAudio.playLocal(playId, mmlIn, this));
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
