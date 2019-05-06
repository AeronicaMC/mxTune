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

import net.aeronica.libs.mml.core.*;
import net.aeronica.mods.mxtune.caches.MXTunePart;
import net.aeronica.mods.mxtune.caches.MXTuneStaff;
import net.aeronica.mods.mxtune.gui.util.GuiScrollingListOf;
import net.aeronica.mods.mxtune.managers.PlayIdSupplier;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.ClientAudio.Status;
import net.aeronica.mods.mxtune.sound.IAudioStatusCallback;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import java.io.IOException;

public class GuiMXTPartTab extends GuiScreen implements IAudioStatusCallback
{
    // Localization Keys
    private static final String HELPER_ENTER_MML = I18n.format("mxtune.gui.musicPaperParse.enterMML");
    private static final String LABEL_INSTRUMENTS = I18n.format("mxtune.gui.musicPaperParse.labelInstruments");
    private static final String LABEL_TITLE_MML = I18n.format("mxtune.gui.guiMXT.labelBulkPasteMML");

    // Layout
    private GuiMXT guiMXT;
    private int top;
    private int bottom;
    private int childHeight;

    // Content
    MXTunePart mxTunePart = new MXTunePart();
    private GuiMMLBox textMMLPaste;
    private GuiTextField labelStatus;
    private GuiTextField labelMeta;
    private GuiButtonExt buttonPlay;
    private GuiScrollingListOf<ParseErrorEntry> listBoxMMLError;
    private GuiScrollingListOf<Instrument> listBoxInstruments;
    private GuiScrollingListOf<MXTuneStaff> listBoxStaves;

    // MIDI Channel Settings
    GuiCheckBox enableVolume;
    boolean cachedEnableVolume;
    GuiSlider sliderVolume;
    double cachedVolume = 100D;

    GuiCheckBox enablePan;
    boolean cachedEnabledPan;
    GuiSlider sliderPan;
    double cachedPan = 0D;

    GuiCheckBox enableReverb;
    boolean cachedEnabledReverb;
    GuiSlider sliderReverb;
    double cachedReverb = 0D;

    GuiCheckBox enableChorus;
    boolean cachedEnabledChorus;
    GuiSlider sliderChorus;
    double cachedChorus = 0D;

    /* MML Parser */
    private ParseErrorListener parseErrorListener = new ParseErrorListener();
    private ParseErrorEntry selectedErrorEntry;
    private int selectedError;

    /* MML Player */
    private int playId = PlayIdSupplier.PlayType.INVALID;

    /* Instruments */
    private int instListWidth;
    private Instrument selectedInstrument = null;
    private int selectedInstrumentIndex;
    private boolean isPlaying = false;
    private int selectedPackedPreset;

    /* Cached State for when the GUI is resized */
    private boolean isStateCached = false;
    private boolean cachedIsPlaying;
    private String cachedMMLText;
    private int cachedSelectedInst;

    // Colored Text Helper
    private int helperTextCounter;
    private int helperTextColor;
    private boolean helperState;

    public GuiMXTPartTab(GuiMXT guiMXT)
    {
        this.guiMXT = guiMXT;
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        Keyboard.enableRepeatEvents(true);

        listBoxInstruments = new GuiScrollingListOf<Instrument>(this)
        {
            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                selectInstrument(selectedIndex);
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex) { /* NOP */ }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                Instrument instrument = !isEmpty() && slotIdx < getSize() && slotIdx >= 0 ? get(slotIdx) : null;
                String s = fontRenderer.trimStringToWidth(I18n.format(instrument.getName()), listWidth - 10);
                int color = isSelected(slotIdx) ? 0xFFFF00 : 0xAADDEE;
                fontRenderer.drawString(s, left + 3, slotTop, color);
            }
        };
        listBoxInstruments.addAll(MIDISystemUtil.getInstrumentCacheCopy());

        listBoxMMLError = new GuiScrollingListOf<ParseErrorEntry>(this)
        {
            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                selectError(selectedIndex);
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex) { /* NOP */ }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                ParseErrorEntry errorEntry = !isEmpty() && slotIdx < getSize() && slotIdx >= 0 ? get(slotIdx) : null;
                String charAt = String.format("%05d", errorEntry.getCharPositionInLine());
                String formattedErrorEntry = fontRenderer.trimStringToWidth(charAt + ": " + errorEntry.getMsg(), listWidth - 10);
                fontRenderer.drawString(formattedErrorEntry, this.left + 3, slotTop, 0xFF2222);
            }
        };
    }

    void setLayout(int top, int bottom, int childHeight)
    {
        this.width = guiMXT.width;
        this.height = guiMXT.height;
        this.top = top;
        this.bottom = bottom;
        this.childHeight = childHeight;
    }

    @Override
    public void updateScreen()
    {
        textMMLPaste.updateCursorCounter();
        updateHelperTextCounter();
        listBoxMMLError.setSelectedIndex(listBoxMMLError.indexOf(selectedErrorEntry));
        selectedError = listBoxMMLError.getSelectedIndex();
        selectedInstrumentIndex = listBoxInstruments.getSelectedIndex();
    }

    @Override
    public void initGui()
    {
        int entryHeight = fontRenderer.FONT_HEIGHT + 2;
        selectedError = -1;
        selectedInstrumentIndex = -1;
        buttonList.clear();

        for (Instrument in : listBoxInstruments)
        {
            int stringWidth = fontRenderer.getStringWidth(I18n.format(in.getName()));
            instListWidth = Math.max(instListWidth, stringWidth + 10);
            //instListWidth = Math.max(instListWidth, stringWidth + 5 + entryHeight);
        }
        instListWidth = Math.min(instListWidth, 150);

        // create Instrument selector, and buttons
        buttonPlay = new GuiButtonExt(2, 5, bottom - 20, instListWidth, 20, isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play"));
        buttonPlay.enabled = false;
        buttonList.add(buttonPlay);

        int posY = top + 15;
        int coreHeight = Math.max(bottom - posY, 100);
        int statusHeight = entryHeight + 4;
        int pasteErrorHeight = (coreHeight / 2) - statusHeight - 10;
        listBoxInstruments.setLayout(entryHeight, instListWidth, Math.max(buttonPlay.y - 5 - posY, entryHeight), posY,buttonPlay.y - 5, 5);
        int posX = listBoxInstruments.getRight() + 5;

        // Create Channel Controls
        enableVolume = new GuiCheckBox(20, posX, posY, "", cachedEnableVolume);
        sliderVolume = new GuiSlider(21, posX + enableVolume.width + 2, posY, 150, 20, I18n.format("mxtune.gui.guiMXT.Volume") + " ", "%", 0, 100, cachedVolume, false, true);
        enablePan = new GuiCheckBox(22, posX, sliderVolume.y + sliderVolume.height + 2, "", cachedEnabledPan);
        sliderPan = new GuiSlider(21, posX + enablePan.width + 2, sliderVolume.y + sliderVolume.height + 2, 150, 20, I18n.format("mxtune.gui.guiMXT.pan.left") + " ", " " + I18n.format("mxtune.gui.guiMXT.pan.Right"), -100, 100, cachedPan, false, true);
        enableReverb = new GuiCheckBox(22, posX, sliderPan.y + sliderPan.height + 2, "", cachedEnabledReverb);
        sliderReverb = new GuiSlider(21, posX + enableReverb.width + 2, sliderPan.y + sliderPan.height + 2, 150, 20, I18n.format("mxtune.gui.guiMXT.reverb") + " ", "%", 0, 100, cachedReverb, false, true);
        enableChorus = new GuiCheckBox(22, posX, sliderReverb.y + sliderReverb.height + 2, "", cachedEnabledChorus);
        sliderChorus = new GuiSlider(21, posX + enableChorus.width + 2, sliderReverb.y + sliderReverb.height + 2, 150, 20, I18n.format("mxtune.gui.guiMXT.Chorus") + " ", "%", 0, 100, cachedChorus, false, true);

        buttonList.add(enableVolume);
        buttonList.add(sliderVolume);
        buttonList.add(enablePan);
        buttonList.add(sliderPan);
        buttonList.add(enableReverb);
        buttonList.add(sliderReverb);
        buttonList.add(enableChorus);
        buttonList.add(sliderChorus);

        /* create MML Paste/Edit field */
        posX = sliderVolume.x + sliderVolume.width + 5;
        textMMLPaste = new GuiMMLBox(1, fontRenderer, posX, posY, width - posX - 5, pasteErrorHeight);
        textMMLPaste.setFocused(false);
        textMMLPaste.setCanLoseFocus(true);
        textMMLPaste.setMaxStringLength(10000);

        /* create Status line */
        labelStatus = new GuiTextField(2, fontRenderer, posX, textMMLPaste.yPosition + textMMLPaste.height + 5 , width - posX - 5, statusHeight);
        labelStatus.setFocused(false);
        labelStatus.setCanLoseFocus(true);
        labelStatus.setEnabled(false);
        labelStatus.setMaxStringLength(80);

        /* create Parse Error selector */
        listBoxMMLError.setLayout(entryHeight, width - posX - 5, Math.max(bottom - labelStatus.y - labelStatus.height - 5, entryHeight), labelStatus.y + labelStatus.height + 5, bottom, posX);

        reloadState();
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        textMMLPaste.setText(cachedMMLText);
        listBoxInstruments.setSelectedIndex(cachedSelectedInst);
        isPlaying = cachedIsPlaying;
        parseMML(textMMLPaste.getText());
        labelStatus.setText(String.format("[%05d]", textMMLPaste.getCursorPosition()));
        updateButtonState();
        listBoxInstruments.resetScroll();
    }

    private void updateState()
    {
        cachedMMLText = textMMLPaste.getText();
        cachedSelectedInst = listBoxInstruments.getSelectedIndex();
        cachedIsPlaying = isPlaying;
        labelStatus.setText(String.format("[%05d]", textMMLPaste.getCursorPosition()));
        updateButtonState();
        isStateCached = true;
    }

    private void updateButtonState()
    {
        // enable Play button when MML Parsing Field has greater than 0 characters and passes the MML parsing tests
        boolean isOK = (!textMMLPaste.isEmpty()) && (listBoxMMLError.isEmpty());
        buttonPlay.enabled = isPlaying || isOK;
        buttonPlay.displayString = isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play");
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
        int posX = sliderVolume.x + sliderVolume.width + 5;
        int posY = top + 2;
        fontRenderer.drawStringWithShadow(LABEL_TITLE_MML, posX, posY, 0xD3D3D3);

        /* draw the instrument list */
        posX = 5;
        fontRenderer.drawStringWithShadow(LABEL_INSTRUMENTS, posX, posY, 0xD3D3D3);

        listBoxInstruments.drawScreen(mouseX, mouseY, partialTicks);
        listBoxMMLError.drawScreen(mouseX, mouseY, partialTicks);

        /* draw the GuiTextField */
        textMMLPaste.drawTextBox();
        labelStatus.drawTextBox();

        /* draw helpers */
        if (textMMLPaste.isEmpty())
        {
            int helperWidth = fontRenderer.getStringWidth(HELPER_ENTER_MML);
            int pasteWidth = textMMLPaste.width - 4;
            int visibleHelperWidth = Math.min(helperWidth, pasteWidth);
            String helperText = fontRenderer.trimStringToWidth(HELPER_ENTER_MML, visibleHelperWidth);
            int fontHeight = fontRenderer.FONT_HEIGHT + 2;
            fontRenderer.drawString(helperText, textMMLPaste.xPosition + 4, textMMLPaste.yPosition + textMMLPaste.height / 2 - fontHeight / 2,
                                    getHelperTextColor());
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void updateHelperTextCounter()
    {
        ++helperTextCounter;
    }

    private int getHelperTextColor()
    {
        final int LO = 0x30;
        final int HI = 0xD0;

        if (helperTextCounter % 20 == 0)
        {
            helperState = ((helperTextColor <= LO) && !helperState) != helperState;
            helperState = ((helperTextColor >= HI) && helperState) != helperState;
        }
        helperTextColor = (short) (helperState ? Math.min(HI, ++helperTextColor) : Math.max(LO, --helperTextColor));
        int color = helperTextColor;
        color &= 0xFF;
        return (color << 16) + (color << 8) + -color;
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
                /* Done / OKAY - Save MML */
                break;

            case 1:
                /* Cancelled - remove the GUI */
                stop();
                mc.displayGuiScreen(guiMXT);
                break;

            case 2:
                /* Play MML */
                play();
                break;

            case 5:
                // ignoreParseErrors
                break;
            default:
        }
        updateState();
    }

    /*
     * Fired when a key is typed. This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e).
     *
     * @throws IOException
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            onGuiClosed();
            return;
        }
        /* add char to GuiTextField */
        textMMLPaste.textboxKeyTyped(typedChar, keyCode);
        listBoxInstruments.keyTyped(typedChar, keyCode);
        parseMML(textMMLPaste.getText());
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        /*
         * A hack is a hack is a hack - Disabling mouse handling on other
         * controls. In this case to ensure a particular control keeps focus
         * while clicking on the error list.
         **/
        if (!listBoxMMLError.isHovering()) super.handleMouseInput();
        listBoxInstruments.handleMouseInput(mouseX, mouseY);
        listBoxMMLError.handleMouseInput(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int partialTicks) throws IOException
    {
        textMMLPaste.mouseClicked(mouseX, mouseY, partialTicks);
        super.mouseClicked(mouseX, mouseY, partialTicks);
        updateState();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleKeyboardInput() throws IOException
    {
        super.handleKeyboardInput();
    }

    @Override
    public void onResize(@Nonnull Minecraft mcIn, int w, int h)
    {
        super.onResize(mcIn, w, h);
    }

    /* MML Parsing */
    private void parseMML(String mml)
    {
        MMLParser parser;

        try
        {
            parser = MMLParserFactory.getMMLParser(mml);
        }
        catch (IOException e)
        {
            ModLogger.debug("MMLParserFactory.getMMLParser() IOException in %s, Error: %s", GuiMXTPartTab.class.getSimpleName(), e);
            listBoxMMLError.clear();
            listBoxMMLError.add(new ParseErrorEntry(0,0, "MMLParserFactory.getMMLParser(mml) is null", null));
            return;
        }
        parser.removeErrorListeners();
        parser.addErrorListener(parseErrorListener);
        parser.setBuildParseTree(true);
        listBoxMMLError.clear();
        parser.test();
        listBoxMMLError.addAll(parseErrorListener.getParseErrorEntries());
    }

    private void selectError(int index)
    {
        this.selectedError = index;
        this.selectedErrorEntry = (index >= 0 && index <= listBoxMMLError.size()) ? listBoxMMLError.get(this.selectedError) : null;
        if (this.selectedErrorEntry != null)
        {
            textMMLPaste.setCursorPosition(selectedErrorEntry.getCharPositionInLine());
            textMMLPaste.setFocused(true);
        }
        updateState();
    }

    private void selectInstrument(int index)
    {
        selectedInstrumentIndex = index;
        updateState();
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
        Instrument inst = listBoxInstruments.get(selectedInstrumentIndex);
        int packedPreset = MMLUtil.instrument2PackedPreset(inst);
        
        mml = mml.replace("MML@", "MML@i" + packedPreset);
        ModLogger.debug("GuiMusicPaperParse.mmlPlay() name: %s, bank %05d, program %03d, packed %08d", inst.getName(), inst.getPatch().getBank() >> 7, inst.getPatch().getProgram(), packedPreset);
        ModLogger.debug("GuiMusicPaperParse.mmlPlay(): %s", mml.substring(0, mml.length() >= 25 ? 25 : mml.length()));

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
            if (selectedInstrumentIndex < 0)
            {
                selectedInstrumentIndex = 0;
                listBoxInstruments.setSelectedIndex(selectedInstrumentIndex);
            }
            String mml = textMMLPaste.getTextToParse();
            isPlaying = mmlPlay(mml);
        }
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
