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

import net.aeronica.mods.mxtune.gui.util.GuiLabelMX;
import net.aeronica.mods.mxtune.gui.util.GuiScrollingListOf;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.managers.PlayIdSupplier;
import net.aeronica.mods.mxtune.mxt.MXTunePart;
import net.aeronica.mods.mxtune.mxt.MXTuneStaff;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.ClientAudio.Status;
import net.aeronica.mods.mxtune.sound.IAudioStatusCallback;
import net.aeronica.mods.mxtune.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import static net.aeronica.mods.mxtune.Reference.MAX_MML_PART_LENGTH;

public class GuiMXTPartTab extends GuiScreen implements IAudioStatusCallback
{
    // Localization Keys
    private static final String LABEL_INSTRUMENTS = I18n.format("mxtune.gui.musicPaperParse.labelInstruments");
    private static final String LABEL_STATUS = I18n.format("mxtune.gui.guiMXT.labelStatus");

    // Layout
    private final GuiMXT guiMXT;
    private int top;
    private int bottom;
    private int entryHeight;
    private static final int PADDING = 4;

    // Content
    private MXTunePart mxTunePart = new MXTunePart();
    private GuiTextField labelStatus;
    private GuiButtonExt buttonPlay;
    private final GuiScrollingListOf<SoundFontProxy> listBoxInstruments;

    // MIDI Channel Settings
//    private GuiCheckBox enableVolume;
//    private boolean cachedEnableVolume;
//    private GuiSlider sliderVolume;
//    private double cachedVolume = 100D;
//
//    private GuiCheckBox enablePan;
//    private boolean cachedEnabledPan;
//    private GuiSlider sliderPan;
//    private double cachedPan = 0D;
//
//    private GuiCheckBox enableReverb;
//    private boolean cachedEnabledReverb;
//    private GuiSlider sliderReverb;
//    private double cachedReverb = 0D;
//
//    private GuiCheckBox enableChorus;
//    private boolean cachedEnabledChorus;
//    private GuiSlider sliderChorus;
//    private double cachedChorus = 0D;

    /* MML Staves: Melody, chord 01, chord 02 ... */
    private static final int MAX_MML_LINES = 10;
    private static final int MIN_MML_LINES = 1;
    private static final int MML_LINE_IDX = 200;
    private final GuiTextField[] mmlTextLines = new GuiTextField[MAX_MML_LINES];
    private final GuiLabelMX[] mmlLabelLines = new GuiLabelMX[MAX_MML_LINES];
    private final String[] cachedTextLines = new String[MAX_MML_LINES];
    private final int[] cachedCursorPos = new int[MAX_MML_LINES];
    private GuiButtonExt buttonAddLine;
    private GuiButtonExt buttonMinusLine;
    private GuiButtonExt buttonPasteFromClipBoard;
    private GuiButtonExt buttonCopyToClipBoard;
    private static final String[] lineNames = new String[MAX_MML_LINES];

    /* MML Line limits - allow limiting the viewable lines */
    private int viewableLineCount = MIN_MML_LINES;
    private int cachedViewableLineCount = MIN_MML_LINES;
    private int totalCharacters;

    /* MML Parser */
    private boolean firstParse = false;
    private int duration;

    /* MML Player */
    private int playId = PlayIdSupplier.PlayType.INVALID;

    /* Instruments */
    private int instListWidth;
    private boolean isPlaying = false;

    /* Cached State for when the GUI is resized */
    private boolean isStateCached = false;
    private boolean cachedIsPlaying;
    private int cachedSelectedInst;

    public GuiMXTPartTab(GuiMXT guiMXT)
    {
        this.guiMXT = guiMXT;
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        entryHeight = fontRenderer.FONT_HEIGHT + 2;

        Keyboard.enableRepeatEvents(true);

        Arrays.fill(cachedTextLines, "");
        initPartNames();

        listBoxInstruments = new GuiScrollingListOf<SoundFontProxy>(this)
        {
            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                selectInstrument();
                updateState();
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex) { /* NOP */ }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                SoundFontProxy soundFontProxy = !isEmpty() && slotIdx < getSize() && slotIdx >= 0 ? get(slotIdx) : null;
                if (soundFontProxy != null)
                {
                    String s = fontRenderer.trimStringToWidth(ModGuiUtils.getLocalizedInstrumentName(soundFontProxy.id), listWidth - 10);
                    int color = isSelected(slotIdx) ? 0xFFFF00 : 0xAADDEE;
                    fontRenderer.drawString(s, left + 3, slotTop, color);
                }
            }
        };
        listBoxInstruments.addAll(SoundFontProxyManager.soundFontProxyMapByIndex.values());
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
        NBTTagCompound compound = new NBTTagCompound();
        mxTunePart.writeToNBT(compound);
        this.mxTunePart = new MXTunePart(compound);
        this.listBoxInstruments.setSelectedIndex(SoundFontProxyManager.getIndexById(mxTunePart.getInstrumentId()));
        listBoxInstruments.resetScroll();
        Iterator<MXTuneStaff> iterator = mxTunePart.getStaves().iterator();
        int i = 0;
        while (iterator.hasNext())
        {
            mmlTextLines[i].setText(iterator.next().getMml());
            mmlTextLines[i++].setCursorPositionZero();
            if (iterator.hasNext())
                addLine();
        }
        updateState();
        initGui();
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
            staves.add(new MXTuneStaff(i, mmlTextLines[i].getText()));
        }
        mxTunePart.setStaves(staves);
        selectInstrument();
    }

    public void clearPart()
    {
        this.mxTunePart = new MXTunePart();
        this.listBoxInstruments.setSelectedIndex(-1);
        listBoxInstruments.resetScroll();
        IntStream.range(0, MAX_MML_LINES).forEach(i -> mmlTextLines[i].setText(""));
        viewableLineCount = MIN_MML_LINES;
        updateState();
        initGui();
    }

    @Override
    public void updateScreen()
    {
        for (int i = 0; i < viewableLineCount; i++)
            mmlTextLines[i].updateCursorCounter();
    }

    private static void initPartNames()
    {
        lineNames[0] = I18n.format("mxtune.gui.label.melody");
        IntStream.range(1, MAX_MML_LINES).forEach(i -> lineNames[i] = I18n.format("mxtune.gui.label.chord", String.format("%02d", i)));
    }

    @Override
    public void initGui()
    {
        buttonList.clear();

        for (SoundFontProxy in : listBoxInstruments)
        {
            int stringWidth = fontRenderer.getStringWidth(ModGuiUtils.getLocalizedInstrumentName(in.id));
            instListWidth = Math.max(instListWidth, stringWidth + 10);
        }
        instListWidth = Math.min(instListWidth, 150);

        // create Instrument selector, and buttons
        buttonPlay = new GuiButtonExt(0, PADDING, bottom - 20, instListWidth, 20, isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play_part"));
        buttonPlay.enabled = false;
        buttonList.add(buttonPlay);

        int posY = top + 15;
        int statusHeight = entryHeight;
        listBoxInstruments.setLayout(entryHeight, instListWidth, Math.max(buttonPlay.y - PADDING - posY, entryHeight), posY, buttonPlay.y - PADDING, PADDING);
        int posX = listBoxInstruments.getRight() + PADDING;


        // Create Channel Controls
//        int sliderHeight = fontRenderer.FONT_HEIGHT + PADDING;
//        enableVolume = new GuiCheckBox(20, posX, posY, "", cachedEnableVolume);
//        sliderVolume = new GuiSlider(21, posX + enableVolume.width + 2, posY, 150, sliderHeight, I18n.format("mxtune.gui.guiMXT.Volume") + " ", "%", 0, 100, cachedVolume, false, true);
//        buttonList.add(enableVolume);
//        buttonList.add(sliderVolume);
//        enablePan = new GuiCheckBox(22, posX, sliderVolume.y + sliderVolume.height + 2, "", cachedEnabledPan);
//        sliderPan = new GuiSlider(21, posX + enablePan.width + 2, sliderVolume.y + sliderVolume.height + 2, 150, sliderHeight, I18n.format("mxtune.gui.guiMXT.pan.left") + " ", " " + I18n.format("mxtune.gui.guiMXT.pan.Right"), -100, 100, cachedPan, false, true);
//        buttonList.add(enablePan);
//        buttonList.add(sliderPan);
//        enableReverb = new GuiCheckBox(22, posX, sliderPan.y + sliderPan.height + 2, "", cachedEnabledReverb);
//        sliderReverb = new GuiSlider(21, posX + enableReverb.width + 2, sliderPan.y + sliderPan.height + 2, 150, sliderHeight, I18n.format("mxtune.gui.guiMXT.reverb") + " ", "%", 0, 100, cachedReverb, false, true);
//        buttonList.add(enableReverb);
//        buttonList.add(sliderReverb);
//        enableChorus = new GuiCheckBox(22, posX, sliderReverb.y + sliderReverb.height + 2, "", cachedEnabledChorus);
//        sliderChorus = new GuiSlider(21, posX + enableChorus.width + 2, sliderReverb.y + sliderReverb.height + 2, 150, sliderHeight, I18n.format("mxtune.gui.guiMXT.Chorus") + " ", "%", 0, 100, cachedChorus, false, true);
//        buttonList.add(enableChorus);
//        buttonList.add(sliderChorus);

        /* create Status line */
        int rightSideWidth = Math.max(width - posX - PADDING, 100);
        labelStatus = new GuiTextField(2, fontRenderer, posX, posY , rightSideWidth, statusHeight);
        labelStatus.setFocused(false);
        labelStatus.setCanLoseFocus(true);
        labelStatus.setEnabled(true);
        labelStatus.setMaxStringLength(150);


        // Create add/minus line buttons
        posY = labelStatus.y + labelStatus.height + PADDING;
        buttonAddLine = new GuiButtonExt(1,posX, posY, 40, 20, I18n.format("mxtune.gui.button.plus"));
        buttonList.add(buttonAddLine);
        buttonMinusLine = new GuiButtonExt(2, buttonAddLine.x + buttonAddLine.width, posY, 40, 20, I18n.format("mxtune.gui.button.minus"));
        buttonList.add(buttonMinusLine);

        // Create Clipboard Paste and Copy buttons
        buttonPasteFromClipBoard =  new GuiButtonExt(3, posX, buttonMinusLine.y + buttonMinusLine.height + PADDING, 120, 20, I18n.format("mxtune.gui.button.pasteFromClipboard"));
        buttonList.add(buttonPasteFromClipBoard);
        buttonCopyToClipBoard =  new GuiButtonExt(4, posX, buttonPasteFromClipBoard.y + buttonPasteFromClipBoard.height, 120, 20, I18n.format("mxtune.gui.button.copyToClipboard"));
        buttonList.add(buttonCopyToClipBoard);

        int labelWidth = Math.max(fontRenderer.getStringWidth(lineNames[0]), fontRenderer.getStringWidth(lineNames[1])) + PADDING;
        posX = buttonCopyToClipBoard.x + buttonCopyToClipBoard.width + PADDING;
        posY = labelStatus.y + labelStatus.height + PADDING;
        rightSideWidth = Math.max(width - posX - PADDING, 100);
        int textX = posX + labelWidth + PADDING;
        int linesRightSideWidth = rightSideWidth - labelWidth - PADDING;
        for(int i = 0; i < MAX_MML_LINES; i++)
        {
            mmlLabelLines[i] = new GuiLabelMX(fontRenderer, i, posX, posY, labelWidth, entryHeight, 0xFFFFFF);
            mmlLabelLines[i].setLabelText(lineNames[i]);
            mmlTextLines[i] = new GuiTextField( i + MML_LINE_IDX, fontRenderer, textX, posY, linesRightSideWidth, fontRenderer.FONT_HEIGHT + 2);
            mmlTextLines[i].setFocused(false);
            mmlTextLines[i].setCanLoseFocus(true);
            mmlTextLines[i].setMaxStringLength(MAX_MML_PART_LENGTH);
            posY += entryHeight + PADDING;
        }

        setLinesLayout(cachedViewableLineCount);
        reloadState();
        parseTest(!firstParse);
    }

    private void setLinesLayout(int viewableLines)
    {
        int posX = buttonCopyToClipBoard.x + buttonCopyToClipBoard.width + PADDING;
        int rightSideWidth = Math.max(width - posX - PADDING, 100);
        GuiTextField mmlTextField = mmlTextLines[viewableLines - 1];
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        listBoxInstruments.setSelectedIndex(cachedSelectedInst);
        isPlaying = cachedIsPlaying;
        updateStatusText();
        IntStream.range(0, MAX_MML_LINES).forEach(i -> mmlTextLines[i].setText(cachedTextLines[i]));
        IntStream.range(0, MAX_MML_LINES).forEach(i -> mmlTextLines[i].setCursorPosition(cachedCursorPos[i]));
        viewableLineCount = cachedViewableLineCount;

        updateButtonState();
        listBoxInstruments.resetScroll();
    }

    private void updateState()
    {
        IntStream.range(0, MAX_MML_LINES).forEach(i -> cachedTextLines[i] = mmlTextLines[i].getText());
        IntStream.range(0, MAX_MML_LINES).forEach(i -> cachedCursorPos[i] = mmlTextLines[i].getCursorPosition());
        cachedViewableLineCount = viewableLineCount;
        cachedSelectedInst = listBoxInstruments.getSelectedIndex();
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
        labelStatus.setText(I18n.format("mxtune.gui.guiMXT.textStatus", String.format("%06d", totalCharacters), SheetMusicUtil.formatDuration(duration) ,mxTunePart != null ? mxTunePart.getMeta() : ""));
    }

    private void updateButtonState()
    {
        // enable Play button when MML Parsing Field has greater than 0 characters and passes the MML parsing tests
        buttonPlay.enabled = isPlaying || duration > 4 || totalCharacters > 0;
        buttonPlay.displayString = isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play_part");

        buttonAddLine.enabled = viewableLineCount < MAX_MML_LINES;
        buttonMinusLine.enabled = viewableLineCount > MIN_MML_LINES;
        parseTest(false);
        setLinesLayout(viewableLineCount);
    }

    boolean canPlay()
    {
        return buttonPlay.enabled;
    }

    @Override
    public void onGuiClosed()
    {
        stop();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        /* draw Field names */
        int posX = listBoxInstruments.getRight() + 4;
        int posY = top + 2;
        fontRenderer.drawStringWithShadow(LABEL_STATUS, posX, posY, 0xD3D3D3);

        /* draw the instrument list */
        posX = 5;
        fontRenderer.drawStringWithShadow(LABEL_INSTRUMENTS, posX, posY, 0xD3D3D3);

        listBoxInstruments.drawScreen(mouseX, mouseY, partialTicks);
        labelStatus.drawTextBox();

        /* draw the MML text lines */
        IntStream.range(0, viewableLineCount).forEach(i -> {
            mmlLabelLines[i].drawLabel(mc, mouseX, mouseY);
            mmlTextLines[i].drawTextBox();
        });


        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        /* if button is disabled ignore click */
        if (!guibutton.enabled) return;

        /* id 0 = okay; 1 = cancel; 2 = play; 3 = stop */
        switch (guibutton.id)
        {
            case 0:
                /* Play MML */
                play();
                break;
            case 1:
                addLine();
                parseTest(true);
                break;
            case 2:
                minusLine();
                parseTest(true);
                break;
            case 3:
                // Paste from clipboard
                pasteFromClipboard();
                parseTest(true);
                break;
            case 4:
                // copy to clipboard
                copyToClipboard();
                break;
            default:
        }
        updateState();
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
        ModLogger.debug("%s", GuiScreen.getClipboardString());
        String clip = GuiScreen.getClipboardString();
        if (clip.isEmpty())
            return;
        List<String> lines = new ArrayList<>(Arrays.asList(clip.replaceAll("MML@|;", "").split(",")));
        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext())
        {
            if (viewableLineCount < MAX_MML_LINES)
            {
                mmlTextLines[i].setText(iterator.next());
                mmlTextLines[i++].setCursorPositionZero();
                if (iterator.hasNext())addLine();
            } else
                break;
        }
    }

    private void copyToClipboard()
    {
        // Setting the clipboard to the empty string does nothing. If there are no lines use an 'empty' MML formatted string instead.
        String mml = getMMLClipBoardFormat();
        GuiScreen.setClipboardString(mml.isEmpty() ? "MML@;" : mml);
    }

    public String getMMLClipBoardFormat()
    {
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < viewableLineCount; i++)
        {
            lines.append(mmlTextLines[i].getText().replaceAll(",", ""));
            if (i < (viewableLineCount - 1)) lines.append(",");
        }

        return getTextToParse(lines.toString());
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE)
            return; // Let the parent handle the escape kay!

        // add char to GuiTextField
        IntStream.range(0, viewableLineCount).forEach(i -> mmlTextLines[i].textboxKeyTyped(typedChar, keyCode));
        parseTest(false);
        listBoxInstruments.keyTyped(typedChar, keyCode);
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int partialTicks) throws IOException
    {
        for (int i = 0; i < viewableLineCount; i++)
            mmlTextLines[i].mouseClicked(mouseX, mouseY, partialTicks);

        super.mouseClicked(mouseX, mouseY, partialTicks);
        updateState();
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        super.handleMouseInput();
        listBoxInstruments.handleMouseInput(mouseX, mouseY);
        updateState();
    }

    /* MML Parsing */
    private void parseMML(int index, String mml)
    {
        ValidDuration validDuration = SheetMusicUtil.validateMML(getTextToParse(mml));
        if (validDuration.getDuration() > duration)
            duration = validDuration.getDuration();
    }

    private void parseTest(boolean force)
    {
        firstParse = true;
        int count = 0;
        for (int i = 0; i < viewableLineCount; i++)
            count += mmlTextLines[i].getText().length();
        if (totalCharacters != count || force)
        {
            duration = 0;
            totalCharacters = count;
            IntStream.range(0, viewableLineCount).forEach(i -> parseMML(i, mmlTextLines[i].getText()));
        }
    }

    private void selectInstrument()
    {
        int index = Math.max(listBoxInstruments.getSelectedIndex(), 0);
        mxTunePart.setPackedPatch(listBoxInstruments.get(index).packed_preset);
        mxTunePart.setInstrumentId(listBoxInstruments.get(index).id);
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
        SoundFontProxy soundFontProxy = listBoxInstruments.get();
        if (soundFontProxy != null)
            soundFontProxyIndex = soundFontProxy.index;
        else
            return false;

        mml = mml.replace("MML@", "MML@i" + soundFontProxyIndex);
        ModLogger.debug("GuiMusicPaperParse.mmlPlay() name: %s, packed %08d", soundFontProxy.id, soundFontProxy.packed_preset);
        ModLogger.debug("GuiMusicPaperParse.mmlPlay(): %s", mml.substring(0, Math.min(mml.length(), 25)));

        playId = PlayIdSupplier.PlayType.PERSONAL.getAsInt();
        ClientAudio.playLocal(playId, mml, this);
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
            if (listBoxInstruments.getSelectedIndex() < 0)
                listBoxInstruments.setSelectedIndex(0);

            StringBuilder lines = new StringBuilder();
            for (int i = 0; i < viewableLineCount; i++)
            {
                // commas (chord part breaks) should not exist so we will remove them.
                lines.append(mmlTextLines[i].getText().replaceAll(",", ""));
                // append a comma to separate the melody and chord note parts (tracks).
                if (i < (viewableLineCount - 1)) lines.append(",");
            }

            // bind it all up into Mabinogi Past format
            String mml = getTextToParse(lines.toString());
            isPlaying = mmlPlay(mml);
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
    public void statusCallBack(Status status, int playId)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (this.playId == playId && (status == Status.ERROR || status == Status.DONE))
            {
                ModLogger.debug("AudioStatus event received: %s, playId: %s", status, playId);
                stop();
            }
        });
    }

    private void stop()
    {
        ClientAudio.queueAudioDataRemoval(playId);
        isPlaying = false;
        playId = PlayIdSupplier.PlayType.INVALID;
        updateState();
    }
}
