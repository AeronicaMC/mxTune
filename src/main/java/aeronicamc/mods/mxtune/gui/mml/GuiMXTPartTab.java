package aeronicamc.mods.mxtune.gui.mml;

import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXLabel;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
import aeronicamc.mods.mxtune.gui.widget.list.SoundFontList;
import aeronicamc.mods.mxtune.items.MXScreen;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.mxt.MXTunePart;
import aeronicamc.mods.mxtune.mxt.MXTuneStaff;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import aeronicamc.mods.mxtune.sound.IAudioStatusCallback;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import aeronicamc.mods.mxtune.util.ValidDuration;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.IntStream;

public class GuiMXTPartTab extends MXScreen implements IAudioStatusCallback
{
    private static final Logger LOGGER = LogManager.getLogger(GuiMXTPartTab.class);
    private final GuiMXT guiMXT;

    // Layout
    private int top;
    private int bottom;
    private int entryHeight;
    private static final int PADDING = 4;

    // Content
    private MXTunePart mxTunePart = new MXTunePart();
    private MXTextFieldWidget labelStatus;
    private MXButton buttonPlay;
    private final SoundFontList listBoxInstruments = new SoundFontList().init();

    /* MML Staves: Melody, chord 01, chord 02 ... */
    private static final int MAX_MML_LINES = 16;
    private static final int MIN_MML_LINES = 1;
    private static final int MML_LINE_IDX = 200;
    private final MXTextFieldWidget[] mmlTextLines = new MXTextFieldWidget[MAX_MML_LINES];
    private final MXLabel[] mmlLabelLines = new MXLabel[MAX_MML_LINES];
    private final String[] cachedTextLines = new String[MAX_MML_LINES];
    private final int[] cachedCursorPos = new int[MAX_MML_LINES];
    private MXButton buttonAddLine;
    private MXButton buttonMinusLine;
    private MXButton buttonPasteFromClipBoard;
    private MXButton buttonCopyToClipBoard;
    private static final String[] lineNames = new String[MAX_MML_LINES];

    /* MML Line limits - allow limiting the viewable lines */
    private int viewableLineCount = MIN_MML_LINES;
    private int cachedViewableLineCount = MIN_MML_LINES;
    private int totalCharacters;

    /* MML Parser */
    private boolean firstParse = false;
    private int duration;

    /* MML Player */
    private int playId = PlayIdSupplier.INVALID;

    /* Instruments */
    private int instListWidth;
    private boolean isPlaying = false;

    /* Cached State for when the GUI is resized */
    private boolean isStateCached = false;
    private boolean cachedIsPlaying;
    private int cachedSelectedInst;

    public GuiMXTPartTab(GuiMXT guiMXT)
    {
        super(new TranslationTextComponent("gui.mxtune.gui_mxt_part.title"));
        this.guiMXT = guiMXT;
    }

    @Override
    protected void init()
    {
        super.init();
    }

    void refresh()
    {
        init();
    }

    @Override
    public void tick()
    {

    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        renderBackground(pMatrixStack);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    void setLayout(int top, int bottom)
    {
        this.width = guiMXT.width;
        this.height = guiMXT.height;
        this.top = top;
        this.bottom = bottom;
    }

    void setPart(MXTunePart mxTunePart)
    {
        clearPart();
        CompoundNBT compound = new CompoundNBT();
        mxTunePart.writeToNBT(compound);
        this.mxTunePart = new MXTunePart(compound);
        this.listBoxInstruments.children().stream().filter(e -> e.getId().equals(mxTunePart.getInstrumentName())).findFirst().ifPresent(listBoxInstruments::centerScrollOn);
        Iterator<MXTuneStaff> iterator = mxTunePart.getStaves().iterator();
        int i = 0;
        while (iterator.hasNext())
        {
            mmlTextLines[i].setValue(iterator.next().getMml());
            mmlTextLines[i++].setCursorPosition(0);
            if (iterator.hasNext())
                addLine();
        }
        updateState();
        init();
    }

    MXTunePart getPart()
    {
        return mxTunePart;
    }

    int getDuration()
    {
        return duration;
    }

    void updatePart()
    {
        List<MXTuneStaff> staves = new ArrayList<>();
        for (int i = 0; i < viewableLineCount; i++)
        {
            staves.add(new MXTuneStaff(i, mmlTextLines[i].getValue()));
        }
        mxTunePart.setStaves(staves);
        selectInstrument();
    }

    public void clearPart()
    {
        this.mxTunePart = new MXTunePart();
        this.listBoxInstruments.setSelected(null);
        IntStream.range(0, MAX_MML_LINES).forEach(i -> mmlTextLines[i].setValue(""));
        viewableLineCount = MIN_MML_LINES;
        updateState();
        init();
    }

    private void selectInstrument()
    {
        SoundFontList.Entry entry = listBoxInstruments.getSelected();
        mxTunePart.setPackedPatch(entry != null ? entry.getPackedPreset() : SoundFontProxyManager.getSoundFontProxyDefault().packed_preset);
        mxTunePart.setInstrumentName(entry != null ? entry.getId() : SoundFontProxyManager.getSoundFontProxyDefault().id);
    }

    private void addLine()
    {
        viewableLineCount = (viewableLineCount + 1) > MAX_MML_LINES ? viewableLineCount : viewableLineCount + 1;
    }

    private void minusLine()
    {
        viewableLineCount = (viewableLineCount - 1) >= MIN_MML_LINES ? viewableLineCount - 1 : viewableLineCount;
    }

    private void pasteFromClipboard()
    {
        clearPart();
        int i = 0;

        String clip = Objects.requireNonNull(minecraft).keyboardHandler.getClipboard();
        LOGGER.debug("{}", clip);
        if (clip.isEmpty())
            return;
        List<String> lines = new ArrayList<>(Arrays.asList(clip.replaceAll("MML@|;", "").split(",")));
        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext())
        {
            if (viewableLineCount < MAX_MML_LINES)
            {
                mmlTextLines[i].setValue(iterator.next());
                mmlTextLines[i++].setCursorPosition(0);
                if (iterator.hasNext())addLine();
            } else
                break;
        }
    }

    private void copyToClipboard()
    {
        // Setting the clipboard to the empty string does nothing. If there are no lines use an 'empty' MML formatted string instead.
        String mml = getMMLClipBoardFormat();
        Objects.requireNonNull(minecraft).keyboardHandler.setClipboard(mml.isEmpty() ? "MML@;" : mml);
    }

    public String getMMLClipBoardFormat()
    {
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < viewableLineCount; i++)
        {
            lines.append(mmlTextLines[i].getValue().replaceAll(",", ""));
            if (i < (viewableLineCount - 1)) lines.append(",");
        }

        return getTextToParse(lines.toString());
    }


    private void updateState()
    {
        IntStream.range(0, MAX_MML_LINES).forEach(i -> cachedTextLines[i] = mmlTextLines[i].getValue());
        IntStream.range(0, MAX_MML_LINES).forEach(i -> cachedCursorPos[i] = mmlTextLines[i].getCursorPosition());
        cachedViewableLineCount = viewableLineCount;
//        cachedSelectedInst = listBoxInstruments.getSelectedIndex();
        cachedIsPlaying = isPlaying;
        updateStatusText();

//        cachedVolume = sliderVolume.getValue();
//        cachedPan = sliderPan.getValue();
//        cachedReverb = sliderReverb.getValue();
//        cachedChorus = sliderChorus.getValue();
//
//        cachedEnableVolume = enableVolume.isChecked();
//        cachedEnabledPan = enablePan.isChecked();
//        cachedEnabledReverb = enableReverb.isChecked();
//        cachedEnabledChorus = enableChorus.isChecked();

        updateButtonState();
        isStateCached = true;
    }

    private void updateStatusText()
    {
        labelStatus.setMessage(new TranslationTextComponent("gui.mxtune.gui_mxt.text_status", String.format("%06d", totalCharacters), SheetMusicHelper.formatDuration(duration) , mxTunePart != null ? mxTunePart.getMeta() : ""));
    }

    private void updateButtonState()
    {
        // enable Play button when MML Parsing Field has greater than 0 characters and passes the MML parsing tests
        buttonPlay.active = isPlaying || duration > 4 || totalCharacters > 0;
        buttonPlay.setMessage(isPlaying ? new TranslationTextComponent("gui.mxtune.button.stop") : new TranslationTextComponent("gui.mxtune.button.play_part"));

        buttonAddLine.visible = viewableLineCount < MAX_MML_LINES;
        buttonMinusLine.visible = viewableLineCount > MIN_MML_LINES;
        parseTest(false);
        setLinesLayout(viewableLineCount);
    }

    boolean canPlay()
    {
        return buttonPlay.active;
    }


    private void setLinesLayout(int viewableLines)
    {
        int posX = buttonCopyToClipBoard.x + buttonCopyToClipBoard.getWidth() + PADDING;
        int rightSideWidth = Math.max(width - posX - PADDING, 100);
        MXTextFieldWidget mmlTextField = mmlTextLines[viewableLines - 1];
    }

    /* MML Parsing */
    private void parseMML(int index, String mml)
    {
        ValidDuration validDuration = SheetMusicHelper.validateMML(getTextToParse(mml));
        if (validDuration.getDuration() > duration)
            duration = validDuration.getDuration();
    }

    private void parseTest(boolean force)
    {
        firstParse = true;
        int count = 0;
        for (int i = 0; i < viewableLineCount; i++)
            count += mmlTextLines[i].getValue().length();
        if (totalCharacters != count || force)
        {
            duration = 0;
            totalCharacters = count;
            IntStream.range(0, viewableLineCount).forEach(i -> parseMML(i, mmlTextLines[i].getValue()));
        }
    }

    private String getTextToParse(String text)
    {
        /* ArcheAge Semi-Compatibility Adjustments and fixes for stupid MML */
        String copy = text;

        // remove any remaining "MML@" and ";" tokens
        copy = copy.replaceAll("(MML\\@)|;", "");
        StringBuilder sb = new StringBuilder(copy);
        // Add the required MML BEGIN and END tokens
        if (!copy.regionMatches(true, 0, "MML@", 0, 4) && copy.length() > 0)
            sb.insert(0, "MML@");
        if (!copy.endsWith(";") && copy.length() > 0)
            sb.append(";");
        return sb.toString();
    }

    @Override
    public void statusCallBack(ClientAudio.Status status, int playId)
    {

    }
}
