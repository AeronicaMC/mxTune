package aeronicamc.mods.mxtune.gui.mml;

import aeronicamc.libs.mml.parser.MMLAllowedChars;
import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.gui.MXScreen;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXLabel;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
import aeronicamc.mods.mxtune.gui.widget.list.SoundFontList;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.mxt.MXTunePart;
import aeronicamc.mods.mxtune.mxt.MXTuneStaff;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import aeronicamc.mods.mxtune.sound.IAudioStatusCallback;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import aeronicamc.mods.mxtune.util.SoundFontProxy;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import aeronicamc.mods.mxtune.util.ValidDuration;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import java.util.*;
import java.util.stream.IntStream;

public class GuiMXTPartTab extends MXScreen implements IAudioStatusCallback
{
    private final GuiMXT guiMXT;

    // Layout
    private int top;
    private int bottom;
    private final int entryHeight;
    private static final int PADDING = 4;

    // Content
    private MXTunePart mxTunePart = new MXTunePart();
    private final MXTextFieldWidget labelStatus = new MXTextFieldWidget(150);
    private MXButton buttonPlay;
    private final SoundFontList listBoxInstruments = new SoundFontList().init();

    /* MML Staves: Melody, chord 01, chord 02 ... */
    private static final int MAX_MML_LINES = 10;
    private static final int MIN_MML_LINES = 1;
    private final MXTextFieldWidget[] mmlTextLines = new MXTextFieldWidget[MAX_MML_LINES];
    private final MXLabel[] mmlLabelLines = new MXLabel[MAX_MML_LINES];
    private final String[] cachedTextLines = new String[MAX_MML_LINES];
    private final int[] cachedCursorPos = new int[MAX_MML_LINES];
    private MXButton buttonAddChord;
    private MXButton buttonMinusChord;
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
    private boolean isPlaying = false;

    /* Cached State for when the GUI is resized */
    private boolean isStateCached = false;
    private boolean cachedIsPlaying;
    private SoundFontList.Entry cachedSelectedInst;

    public GuiMXTPartTab(GuiMXT guiMXT)
    {
        super(new StringTextComponent("Should not be seen"));
        this.guiMXT = guiMXT;
        this.minecraft = Minecraft.getInstance();
        this.font = minecraft.font;
        this.width = minecraft.getWindow().getWidth();
        this.height = minecraft.getWindow().getHeight();

        entryHeight = font.lineHeight + 2;
        Arrays.fill(cachedTextLines, "");
        initPartNames();
    }

    @Override
    protected void init()
    {
        super.init();
        children.clear();
        buttons.clear();
        /* Instruments */
        int instListWidth = Math.min(listBoxInstruments.getSuggestedWidth(), 150);

        // create Instrument selector, and buttons
        buttonPlay = new MXButton(PADDING, bottom - 20, instListWidth, 20, isPlaying ? new TranslationTextComponent("gui.mxtune.button.stop") : new TranslationTextComponent("gui.mxtune.button.play_part"), p -> play());
        buttonPlay.active = false;
        addButton(buttonPlay);

        int posY = top + 15;
        listBoxInstruments.setLayout(PADDING, posY, instListWidth, Math.max(buttonPlay.y - PADDING - posY, entryHeight));
        listBoxInstruments.setCallBack(this::selectInstrument);
        int posX = listBoxInstruments.getRight() + PADDING;

        /* create Status line */
        int rightSideWidth = Math.max(width - posX - PADDING, 100);
        labelStatus.setLayout(posX, posY , rightSideWidth, entryHeight);
        labelStatus.active = false;
        labelStatus.setFocus(false);
        labelStatus.setCanLoseFocus(true);

        // Create add/minus line buttons
        posY = labelStatus.y + labelStatus.getHeight() + PADDING;
        buttonAddChord = new MXButton(posX, posY, 30, 20, new TranslationTextComponent("gui.mxtune.button.plus_chord"), p -> addLine());
        buttonAddChord.addHooverText(true, new TranslationTextComponent("gui.mxtune.button.plus_chord").withStyle(TextFormatting.RESET));
        buttonAddChord.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.plus_chord.help01").withStyle(TextFormatting.YELLOW));
        addButton(buttonAddChord);
        buttonMinusChord = new MXButton(buttonAddChord.x + buttonAddChord.getWidth(), posY, 30, 20, new TranslationTextComponent("gui.mxtune.button.minus_chord"), p -> minusLine());
        buttonMinusChord.addHooverText(true, new TranslationTextComponent("gui.mxtune.button.minus_chord").withStyle(TextFormatting.RESET));
        buttonMinusChord.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.minus_chord.help01").withStyle(TextFormatting.YELLOW));
        buttonMinusChord.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.minus_chord.help02").withStyle(TextFormatting.GREEN));
        addButton(buttonMinusChord);

        // Create Clipboard Paste and Copy buttons
        MXButton buttonPasteFromClipBoard = new MXButton(posX, buttonMinusChord.y + buttonMinusChord.getHeight() + PADDING, 60, 20, new TranslationTextComponent("gui.mxtune.button.clipboard_paste_from"), p -> pasteFromClipboard());
        buttonPasteFromClipBoard.addHooverText(true, new TranslationTextComponent("gui.mxtune.button.clipboard_paste_from").withStyle(TextFormatting.RESET));
        buttonPasteFromClipBoard.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.clipboard_paste_from.help01").withStyle(TextFormatting.YELLOW));
        addButton(buttonPasteFromClipBoard);
        MXButton buttonCopyToClipBoard = new MXButton(posX, buttonPasteFromClipBoard.y + buttonPasteFromClipBoard.getHeight(), 60, 20, new TranslationTextComponent("gui.mxtune.button.clipboard_copy_to"), p -> copyToClipboard());
        buttonCopyToClipBoard.addHooverText(true, new TranslationTextComponent("gui.mxtune.button.clipboard_copy_to").withStyle(TextFormatting.RESET));
        buttonCopyToClipBoard.addHooverText(false, new TranslationTextComponent("gui.mxtune.button.clipboard_copy_to.help01").withStyle(TextFormatting.YELLOW));
        addButton(buttonCopyToClipBoard);

        int labelWidth = Math.max(font.width(lineNames[0]), font.width(lineNames[1])) + PADDING;
        posX = buttonCopyToClipBoard.x + buttonCopyToClipBoard.getWidth() + PADDING;
        posY = labelStatus.y + labelStatus.getHeight() + PADDING;
        rightSideWidth = Math.max(width - posX - PADDING, 100);
        int textX = posX + labelWidth + PADDING;
        int linesRightSideWidth = rightSideWidth - labelWidth - PADDING;
        for(int i = 0; i < MAX_MML_LINES; i++)
        {
            mmlLabelLines[i] = new MXLabel();
            mmlLabelLines[i].setLabelText(new StringTextComponent(lineNames[i]));
            mmlLabelLines[i].setLayout(posX, posY, linesRightSideWidth, font.lineHeight + 2);
            mmlTextLines[i] = new MXTextFieldWidget(Reference.MAX_MML_PART_LENGTH);
            mmlTextLines[i].setLayout(textX, posY, linesRightSideWidth, font.lineHeight + 2);
            mmlTextLines[i].setFocus(false);
            mmlTextLines[i].setCanLoseFocus(true);
            posY += entryHeight + PADDING;
        }

        reloadState();
        parseTest(!firstParse);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public void onClose()
    {
        stop();
    }

    void refresh()
    {
        init();
    }

    @Override
    public void tick()
    {
        labelStatus.tick();
        IntStream.range(0, viewableLineCount).forEach(i -> mmlTextLines[i].tick());
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers)
    {
        if (MMLAllowedChars.isAllowedChar(pCodePoint, false))
            IntStream.range(0, viewableLineCount).forEach(i -> mmlTextLines[i].charTyped(pCodePoint, pModifiers));
        return super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        listBoxInstruments.keyPressed(pKeyCode, pScanCode, pModifiers);
        IntStream.range(0, viewableLineCount).forEach(i -> mmlTextLines[i].keyPressed(pKeyCode, pScanCode, pModifiers));
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers)
    {
        listBoxInstruments.keyReleased(pKeyCode, pScanCode, pModifiers);
        IntStream.range(0, viewableLineCount).forEach(i -> mmlTextLines[i].keyReleased(pKeyCode, pScanCode, pModifiers));
        updateState();
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY)
    {
        listBoxInstruments.isMouseOver(pMouseX, pMouseY);
        IntStream.range(0, viewableLineCount).forEach(i -> mmlTextLines[i].isMouseOver(pMouseX, pMouseY));
        return super.isMouseOver(pMouseX, pMouseY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
    {
        listBoxInstruments.mouseClicked(pMouseX, pMouseY, pButton);
        labelStatus.mouseClicked(pMouseX, pMouseY, pButton);
        IntStream.range(0, viewableLineCount).forEach(i -> mmlTextLines[i].mouseClicked(pMouseX, pMouseY, pButton));
        super.mouseClicked(pMouseX, pMouseY, pButton);
        return false;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton)
    {
        listBoxInstruments.mouseReleased(pMouseX, pMouseY, pButton);
        IntStream.range(0, viewableLineCount).forEach(i -> mmlTextLines[i].mouseReleased(pMouseX, pMouseY, pButton));
        super.mouseReleased(pMouseX, pMouseY, pButton);
        return false;
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY)
    {
        listBoxInstruments.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        IntStream.range(0, viewableLineCount).forEach(i -> mmlTextLines[i].mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY));
        super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        return false;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta)
    {
        listBoxInstruments.mouseScrolled(pMouseX, pMouseY, pDelta);
        IntStream.range(0, viewableLineCount).forEach(i -> mmlTextLines[i].mouseScrolled(pMouseX, pMouseY, pDelta));
        super.mouseScrolled(pMouseX, pMouseY, pDelta);
        return false;
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY)
    {
        listBoxInstruments.mouseMoved(pMouseX, pMouseY);
        IntStream.range(0, viewableLineCount).forEach(i -> mmlTextLines[i].mouseMoved(pMouseX, pMouseY));
        super.mouseMoved(pMouseX, pMouseY);
    }


    private static void initPartNames()
    {
        lineNames[0] = new TranslationTextComponent("gui.mxtune.label.melody").getString();
        IntStream.range(1, MAX_MML_LINES).forEach(i -> lineNames[i] = new TranslationTextComponent("gui.mxtune.label.chord", String.format("%02d", i)).getString());
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        /* draw Field names */
        int posX = listBoxInstruments.getRight() + 4;
        int posY = top + 2;
        font.drawShadow(pMatrixStack, new TranslationTextComponent("gui.mxtune.label.status"), posX, posY, 0xD3D3D3);

        /* draw the instrument list */
        posX = 5;
        font.drawShadow(pMatrixStack, new TranslationTextComponent("gui.mxtune.label.instruments"), posX, posY, 0xD3D3D3);

        listBoxInstruments.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        labelStatus.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

        /* draw the MML text lines */
        IntStream.range(0, viewableLineCount).forEach(i -> {
            mmlLabelLines[i].render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            mmlTextLines[i].render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        });
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
        this.listBoxInstruments.children().stream().filter(e -> e.getId().equals(mxTunePart.getInstrumentId())).findFirst().ifPresent(listBoxInstruments::setSelected);
        Iterator<MXTuneStaff> iterator = mxTunePart.getStaves().iterator();
        int i = 0;
        while (iterator.hasNext())
        {
            mmlTextLines[i].setValue(iterator.next().getMml());
            mmlTextLines[i++].keyPressed(268,0,0);
            if (i == MAX_MML_LINES) break;
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
            staves.add(new MXTuneStaff(i, MMLAllowedChars.filter(mmlTextLines[i].getValue(), false)));
        }
        mxTunePart.setStaves(staves);
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

    private void selectInstrument(@Nullable SoundFontList.Entry entry, Boolean doubleClicked)
    {
        SoundFontProxy soundFontProxy = SoundFontProxyManager.getSoundFontProxyDefault();
        mxTunePart.setPackedPatch(entry != null ? entry.getPackedPreset() : soundFontProxy.packed_preset);
        mxTunePart.setInstrumentId(entry != null ? entry.getId() : soundFontProxy.id);
        listBoxInstruments.centerScrollOn(entry != null ? entry : listBoxInstruments.children().get(soundFontProxy.index));
        updateState();
    }

    private void addLine()
    {
        viewableLineCount = (viewableLineCount + 1) > MAX_MML_LINES ? viewableLineCount : viewableLineCount + 1;
        updateState();
    }

    private void minusLine()
    {
        viewableLineCount = (viewableLineCount - 1) >= MIN_MML_LINES ? viewableLineCount - 1 : viewableLineCount;
        updateState();
    }

    private void pasteFromClipboard()
    {
        clearPart();
        int i = 0;

        String clip = MMLAllowedChars.filter(Objects.requireNonNull(minecraft).keyboardHandler.getClipboard(), true);
        if (clip.isEmpty())
            return;
        List<String> lines = new ArrayList<>(Arrays.asList(clip.replaceAll("MML@|;", "").split(",")));
        Iterator<String> iterator = lines.iterator();
        do
        {
            if (i < MAX_MML_LINES)
            {
                mmlTextLines[i].setValue(iterator.next());
                mmlTextLines[i++].keyPressed(268,0,0);
                if (iterator.hasNext())addLine();
            } else
                break;
        } while (iterator.hasNext());
        updateState();
    }

    private void copyToClipboard()
    {
        // Setting the clipboard to the empty string does nothing. If there are no lines use an 'empty' MML formatted string instead.
        String mml = getMMLClipBoardFormat();
        Objects.requireNonNull(minecraft).keyboardHandler.setClipboard(mml.isEmpty() ? "MML@;" : mml);
        updateState();
    }

    public String getMMLClipBoardFormat()
    {
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < viewableLineCount; i++)
        {
            lines.append(mmlTextLines[i].getValue().replace(",", ""));
            if (i < (viewableLineCount - 1)) lines.append(",");
        }

        updateState();
        return getTextToParse(lines.toString());
    }


    private void updateState()
    {
        IntStream.range(0, MAX_MML_LINES).forEach(i -> cachedTextLines[i] = mmlTextLines[i].getValue());
        IntStream.range(0, MAX_MML_LINES).forEach(i -> cachedCursorPos[i] = mmlTextLines[i].getCursorPosition());
        cachedViewableLineCount = viewableLineCount;
        cachedSelectedInst = listBoxInstruments.getSelected();
        cachedIsPlaying = isPlaying;
        updateStatusText();
        updateButtonState();
        isStateCached = true;
    }

    private void updateStatusText()
    {
        labelStatus.setValue(new TranslationTextComponent("gui.mxtune.label.metadata", String.format("%06d", totalCharacters), SheetMusicHelper.formatDuration(duration) , mxTunePart != null ? mxTunePart.getMeta() : "").getString());
    }

    private void updateButtonState()
    {
        // enable Play button when MML Parsing Field has greater than 0 characters and passes the MML parsing tests
        buttonPlay.active = isPlaying || duration > 4 || totalCharacters > 0;
        buttonPlay.setMessage(isPlaying ? new TranslationTextComponent("gui.mxtune.button.stop") : new TranslationTextComponent("gui.mxtune.button.play_part"));

        buttonAddChord.visible = viewableLineCount < MAX_MML_LINES;
        buttonMinusChord.visible = viewableLineCount > MIN_MML_LINES;
        parseTest(false);
    }

    boolean canPlay()
    {
        return buttonPlay.active;
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        listBoxInstruments.setSelected(cachedSelectedInst);
        listBoxInstruments.ensureVisible(cachedSelectedInst);
        isPlaying = cachedIsPlaying;
        updateStatusText();
        IntStream.range(0, MAX_MML_LINES).forEach(i -> mmlTextLines[i].setValue(cachedTextLines[i]));
        IntStream.range(0, MAX_MML_LINES).forEach(i -> mmlTextLines[i].setCursorPosition(cachedCursorPos[i]));
        viewableLineCount = cachedViewableLineCount;
        updateButtonState();
    }

    /* MML Parsing */
    private void parseMML(String mml)
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
            IntStream.range(0, viewableLineCount).forEach(i -> parseMML(mmlTextLines[i].getValue()));
        }
    }

    /** Table Flip!
     * Because of the apparent different interpretations of MIDI and
     * SoundFont specifications and the way Sun implemented
     * {@link Instrument}, soundfont loading, etc.:
     * <br/><br/>
     * A soundfont preset bank:0, program:0 for a piano AND
     * a soundfont preset bank:128, program:0 for a standard percussion set
     * produce identical {@link Patch} objects using
     * {@link Patch javax.sound.midi.Instrument.getPatch()} However
     * percussion sets use a different internal class. It uses the Drumkit class.
     * <br/><br/>
     * If you want to manipulate or test values for bank or program settings
     * you must also check for the existence of "Drumkit:" in Instrument#toString.
     */
    @SuppressWarnings("restriction")
    private boolean mmlPlay(String mmlIn)
    {
        String mml = mmlIn;
        int soundFontProxyIndex;
        SoundFontList.Entry entry = listBoxInstruments.getSelected();
        if (entry != null)
            soundFontProxyIndex = entry.getIndex();
        else
            return false;

        mml = mml.replace("MML@", "MML@i" + soundFontProxyIndex);

        playId = PlayIdSupplier.PlayType.PERSONAL.getAsInt();
        ClientAudio.playLocal(getDuration(), playId, mml, this);
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
            if (listBoxInstruments.getSelected() == null)
                listBoxInstruments.setSelected(listBoxInstruments.children().get(SoundFontProxyManager.getSoundFontProxyDefault().index));

            StringBuilder lines = new StringBuilder();
            for (int i = 0; i < viewableLineCount; i++)
            {
                // commas (chord part breaks) should not exist, so we will remove them.
                lines.append(mmlTextLines[i].getValue().replace(",", ""));
                // append a comma to separate the melody and chord note parts (tracks).
                if (i < (viewableLineCount - 1)) lines.append(",");
            }

            // bind it all up into Mabinogi Paste format
            String mml = getTextToParse(lines.toString());
            isPlaying = mmlPlay(mml);
        }
        updateState();
    }

    private String getTextToParse(String text)
    {
        /* ArcheAge Semi-Compatibility Adjustments and fixes for stupid MML */
        String copy = text;

        // remove any remaining "MML@" and ";" tokens
        copy = copy.replaceAll("(MML@)|;", "");
        StringBuilder sb = new StringBuilder(copy);
        // Add the required MML BEGIN and END tokens
        if (!copy.regionMatches(true, 0, "MML@", 0, 4) && !copy.isEmpty())
            sb.insert(0, "MML@");
        if (!copy.endsWith(";") && !copy.isEmpty())
            sb.append(";");
        return sb.toString();
    }

    private void stop()
    {
        Objects.requireNonNull(minecraft).submitAsync(()->ClientAudio.fadeOut(playId, 1));
        isPlaying = false;
        playId = PlayIdSupplier.INVALID;
        updateState();
    }

    @Override
    public void statusCallBack(ClientAudio.Status status, int playId)
    {
        if ((this.playId == playId) && ClientAudio.isDoneOrYieldStatus(status))
        {
            stop();
        }
    }
}
