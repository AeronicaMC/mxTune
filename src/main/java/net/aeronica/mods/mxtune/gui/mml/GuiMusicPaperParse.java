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
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.groups.PlayIdSupplier;
import net.aeronica.mods.mxtune.gui.util.GuiLink;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.MusicTextMessage;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.ClientAudio.Status;
import net.aeronica.mods.mxtune.sound.IAudioStatusCallback;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiMusicPaperParse extends GuiScreen implements IAudioStatusCallback
{
    // Localization Keys
    private static final String TITLE = I18n.format("mxtune.gui.musicPaperParse.title");
    private static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    private static final String HELPER_ENTER_TITLE = I18n.format("mxtune.gui.musicPaperParse.enterTitle");
    private static final String HELPER_ENTER_MML = I18n.format("mxtune.gui.musicPaperParse.enterMML");
    private static final String LABEL_INSTRUMENTS = I18n.format("mxtune.gui.musicPaperParse.labelInstruments");
    private static final String LABEL_TITLE_MML = I18n.format("mxtune.gui.musicPaperParse.labelTitleMML");

    private GuiScreen guiScreenParent;
    private GuiTextField textMMLTitle;
    private GuiMMLBox textMMLPaste;
    private GuiTextField labelStatus;
    private GuiButton buttonOkay;
    private GuiButton buttonCancel;
    private GuiButton buttonPlay;
    private GuiButton buttonStop;
    private GuiLink mmlLink;
    private GuiParserErrorList listBoxMMLError;
    private GuiInstruments listBoxInstruments;
    private GuiCheckBox checkBoxIgnoreParseErrors;
    private int helperTextCounter;
    private int helperTextColor;
    private boolean helperState;
    private boolean ignoreParseErrors = false;

    /* MML Parser */
    private ParseErrorListener parseErrorListener = null;
    private List<ParseErrorEntry> parseErrorCache;
    private ParseErrorEntry selectedErrorEntry;
    private int selectedError;

    /* MML Player */
    private int playId = PlayIdSupplier.PlayType.INVALID;
    
    /* Instruments */
    private List<Instrument> instrumentCache;
    private int instListWidth;
    private Instrument selectedInstID = null;
    private int selectedInst;
    private boolean isPlaying = false;
    private boolean midiUnavailable;

    /* Cached State for when the GUI is resized */
    private boolean isStateCached = false;
    private boolean cachedIsPlaying;
    private String cachedMMLTitle;
    private String cachedMMLText;
    private int cachedSelectedInst;
    private boolean cachedIgnoreParseErrors;

    public GuiMusicPaperParse(GuiScreen guiScreenParent)
    {
        this.guiScreenParent = guiScreenParent;
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        midiUnavailable = MIDISystemUtil.midiUnavailable();
        instrumentCache = MIDISystemUtil.getInstrumentCacheCopy();
    }
    
    @Override
    public void updateScreen()
    {
        textMMLTitle.updateCursorCounter();
        textMMLPaste.updateCursorCounter();
        updateHelperTextCounter();
        selectedError = listBoxMMLError.selectedIndex(parseErrorCache.indexOf(selectedErrorEntry));
        selectedInst = listBoxInstruments.selectedIndex(instrumentCache.indexOf(selectedInstID));
    }

    @Override
    public void initGui()
    {
        int entryHeight = fontRenderer.FONT_HEIGHT + 2;
        parseErrorListener = new ParseErrorListener();
        parseErrorCache = new ArrayList<>();
        selectedError = -1;
        selectedInst = -1;
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();

        for (Instrument in : instrumentCache)
        {
            int stringWidth = fontRenderer.getStringWidth(I18n.format(in.getName()));
            instListWidth = Math.max(instListWidth, stringWidth + 10);
            instListWidth = Math.max(instListWidth, stringWidth + 5 + entryHeight);
        }
        instListWidth = Math.min(instListWidth, 150);

        // create Instrument selector, and buttons
        listBoxInstruments = new GuiInstruments(this, instrumentCache, instListWidth, entryHeight);
        buttonOkay = new GuiButton(0, (listBoxInstruments.getRight() + width / 2) - 75 + 30, height - 27, 75, 20, I18n.format("gui.done"));
        buttonCancel = new GuiButton(1, (listBoxInstruments.getRight() + width / 2) - 150 + 25, height - 27, 75, 20, I18n.format("gui.cancel"));
        buttonPlay = new GuiButton(2, 10, height - 49, instListWidth, 20, I18n.format("mxtune.gui.button.play"));
        buttonStop = new GuiButton(3, 10, height - 27, instListWidth, 20, I18n.format("mxtune.gui.button.stop"));
        mmlLink = new GuiLink(4, width - 10 , 20, ModConfig.getMmlLink(), GuiLink.AlignText.RIGHT);
        checkBoxIgnoreParseErrors = new GuiCheckBox(5, listBoxInstruments.getRight() + 5, height - 40, I18n.format("mxtune.gui.musicPaperParse.checkBox.ignoreParseErrors"), ignoreParseErrors);
        buttonPlay.enabled = false;
        buttonStop.enabled = false;
        buttonOkay.enabled = false;
        buttonList.add(buttonOkay);
        buttonList.add(buttonCancel);
        buttonList.add(buttonPlay);
        buttonList.add(buttonStop);
        buttonList.add(mmlLink);
        buttonList.add(checkBoxIgnoreParseErrors);

        /* create MML Title field */
        int posX = listBoxInstruments.getRight() + 5;
        int posY = 32;
        textMMLTitle = new GuiTextField(0, fontRenderer, posX, posY, width - posX - 10, 18);
        textMMLTitle.setFocused(true);
        textMMLTitle.setCanLoseFocus(true);
        textMMLTitle.setMaxStringLength(50);

        /* create MML Paste/Edit field */
        posY = 32 + 18 + 5;
        textMMLPaste = new GuiMMLBox(1, fontRenderer, posX, posY, width - posX - 10, 77);
        textMMLPaste.setFocused(false);
        textMMLPaste.setCanLoseFocus(true);
        textMMLPaste.setMaxStringLength(10000);

        /* create Status line */
        posY = 32 + 18 + 5 + 77 + 5;
        labelStatus = new GuiTextField(2, fontRenderer, posX, posY, width - posX - 10, 18);
        labelStatus.setFocused(false);
        labelStatus.setCanLoseFocus(true);
        labelStatus.setEnabled(false);
        labelStatus.setMaxStringLength(80);

        /* create Parse Error selector */
        posY = 32 + 18 + 5 + 77 + 5 + 18 + 5;
        listBoxMMLError = new GuiParserErrorList(this, parseErrorCache, posX, posY, width - posX - 10, height - posY - 42, entryHeight);

        reloadState();
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        textMMLTitle.setText(cachedMMLTitle);
        textMMLPaste.setText(cachedMMLText);
        listBoxInstruments.elementClicked(cachedSelectedInst, false);
        isPlaying = cachedIsPlaying;
        ignoreParseErrors = cachedIgnoreParseErrors;
        parseMML(textMMLPaste.getText());
        updateButtonState();
    }

    private void updateState()
    {
        cachedMMLTitle = textMMLTitle.getText();
        cachedMMLText = textMMLPaste.getText();
        cachedSelectedInst = selectedInst;
        cachedIsPlaying = isPlaying;
        cachedIgnoreParseErrors = ignoreParseErrors;
        labelStatus.setText(String.format("[%04d]", textMMLPaste.getCursorPosition()));
        updateButtonState();
        isStateCached = true;
    }

    private void updateButtonState()
    {
        /* enable OKAY button when Title Field is greater than 0 chars and passes the MML parsing tests */
        ignoreParseErrors = checkBoxIgnoreParseErrors.isChecked();
        textMMLPaste.setBlockCursor(!ignoreParseErrors);
        boolean isOK = (!textMMLPaste.isEmpty()) && (parseErrorCache.isEmpty() || ignoreParseErrors) && !textMMLTitle.getText().isEmpty();
        buttonList.get(buttonOkay.id).enabled = isOK;
        buttonList.get(buttonPlay.id).enabled = !isPlaying && isOK;
        buttonList.get(buttonStop.id).enabled = isPlaying && isOK;
    }

    @Override
    public void onGuiClosed()
    {
        mmlStop();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        String localTITLE;
        if (midiUnavailable)
            localTITLE = TITLE + " - " + TextFormatting.RED + MIDI_NOT_AVAILABLE;
        else
            localTITLE = TITLE;
        /* draw "TITLE" at the top/right column middle */
        int posX = (width - fontRenderer.getStringWidth(localTITLE)) / 2;
        int posY = 10;
        fontRenderer.drawStringWithShadow(localTITLE, posX, posY, 0xD3D3D3);

        /* draw Field names */
        posX = listBoxInstruments.getRight() + 10;
        posY = 20;
        fontRenderer.drawStringWithShadow(LABEL_TITLE_MML, posX, posY, 0xD3D3D3);

        /* draw the instrument list */
        posX = 10;
        posY = 20;
        fontRenderer.drawStringWithShadow(LABEL_INSTRUMENTS, posX, posY, 0xD3D3D3);

        listBoxInstruments.drawScreen(mouseX, mouseY, partialTicks);
        listBoxMMLError.drawScreen(mouseX, mouseY, partialTicks);

        /* draw the GuiTextField */
        textMMLTitle.drawTextBox();
        textMMLPaste.drawTextBox();
        labelStatus.drawTextBox();

        /* draw helpers */
        if (textMMLTitle.getText().isEmpty())
        {
            int helperWidth = fontRenderer.getStringWidth(HELPER_ENTER_TITLE);
            int fontHeight = fontRenderer.FONT_HEIGHT + 2;
            fontRenderer.drawString(HELPER_ENTER_TITLE, textMMLTitle.x + textMMLTitle.width / 2 - helperWidth / 2, textMMLTitle.y + fontHeight / 2, getHelperTextColor());
        }
        if (textMMLPaste.isEmpty())
        {
            int helperWidth = fontRenderer.getStringWidth(HELPER_ENTER_MML);
            int fontHeight = fontRenderer.FONT_HEIGHT + 2;
            fontRenderer.drawString(HELPER_ENTER_MML, textMMLPaste.xPosition + textMMLPaste.width / 2 - helperWidth / 2, textMMLPaste.yPosition + textMMLPaste.height / 2 - fontHeight / 2,
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
                String musicText = textMMLPaste.getTextToParse().trim();
                String musicTitle = textMMLTitle.getText().trim();
                mmlStop();
                if (guiScreenParent == null)
                    sendMMLTextToServer(musicTitle, musicText);
                else
                {
                    if (selectedInst < 0 || selectedInst > MIDISystemUtil.getInstrumentCacheCopy().size()) selectedInst = 0;
                    ActionGet.INSTANCE.select(musicTitle, null, null, musicText, MIDISystemUtil.getInstrumentCacheCopy().get(selectedInst).getName());
                }
                mc.displayGuiScreen(guiScreenParent);
                break;

            case 1:
                /* Cancelled - remove the GUI */
                if (guiScreenParent != null)
                    ActionGet.INSTANCE.cancel();
                mmlStop();
                mc.displayGuiScreen(guiScreenParent);
                break;

            case 2:
                /* Play MML */
                if (selectedInst < 0)
                {
                    selectedInst = 0;
                    listBoxInstruments.elementClicked(selectedInst, false);
                }
                String mml = textMMLPaste.getTextToParse();
                isPlaying = mmlPlay(mml);
                break;

            case 3:
                /* Stop playing MML */
                mmlStop();
                break;

            case 4:
                handleComponentClick(mmlLink.getLinkComponent());
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
            this.actionPerformed(buttonCancel);
            return;
        }
        /* add char to GuiTextField */
        textMMLTitle.textboxKeyTyped(typedChar, keyCode);
        textMMLPaste.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_TAB)
        {
            if (textMMLTitle.isFocused())
            {
                textMMLPaste.setFocused(true);
                textMMLTitle.setFocused(false);
            } else
            {
                textMMLPaste.setFocused(false);
                textMMLTitle.setFocused(true);
            }
        }
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

    /*
     * Called when the mouse is clicked.
     * 
     * @throws IOException
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int partialTicks) throws IOException
    {
        textMMLTitle.mouseClicked(mouseX, mouseY, partialTicks);
        textMMLPaste.mouseClicked(mouseX, mouseY, partialTicks);
        super.mouseClicked(mouseX, mouseY, partialTicks);
        updateState();
    }

    private void sendMMLTextToServer(String titleIn, String mmlIn)
    {
        PacketDispatcher.sendToServer(new MusicTextMessage(titleIn, mmlIn));
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
            ModLogger.debug("MMLParserFactory.getMMLParser() IOException in %s, Error: %s", GuiMusicPaperParse.class.getSimpleName(), e);
            parseErrorCache.clear();
            parseErrorCache.add(new ParseErrorEntry(0,0, "MMLParserFactory.getMMLParser(mml) is null", null));
            return;
        }
        parser.removeErrorListeners();
        parser.addErrorListener(parseErrorListener);
        parser.setBuildParseTree(true);
        parseErrorCache.clear();
        parser.test();
        parseErrorCache.addAll(parseErrorListener.getParseErrorEntries());
    }

    public static class GuiParserErrorList extends GuiScrollingList
    {
        private GuiMusicPaperParse parent;
        private final List<ParseErrorEntry> parseErrorCache;
        private FontRenderer fontRenderer;

        GuiParserErrorList(GuiMusicPaperParse parent, List<ParseErrorEntry> parseErrorCache, int left, int top, int listWidth, int listHeight, int slotHeight)
        {
            super(parent.mc, listWidth, listHeight, top, top + listHeight, left, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.parseErrorCache = parseErrorCache;
            this.fontRenderer = parent.mc.fontRenderer;
        }

        boolean isHovering()
        {
            return mouseX >= left && mouseX <= left + listWidth && mouseY >= top && mouseY <= bottom && getSize() > 0;
        }

        int selectedIndex(int s)
        {
            selectedIndex = s;
            return s;
        }

        @Override
        protected int getSize() {return parseErrorCache.size();}

        @Override
        protected void elementClicked(int index, boolean doubleClick)
        {
            parent.selectedError = index;
            parent.selectedErrorEntry = (index >= 0 && index <= parseErrorCache.size()) ? parseErrorCache.get(parent.selectedError) : null;
            if (parent.selectedErrorEntry != null)
            {
                parent.textMMLPaste.setCursorPosition(parent.selectedErrorEntry.getCharPositionInLine());
                parent.textMMLPaste.setFocused(true);
            }
            parent.updateState();
        }

        @Override
        protected boolean isSelected(int index) { return index == parent.selectedError; }

        @Override
        protected void drawBackground()
        {
            Gui.drawRect(left - 1, top - 1, left + listWidth + 1, top + listHeight + 1, -6250336);
            Gui.drawRect(left, top, left + listWidth, top + listHeight, -16777216);
        }

        @Override
        protected int getContentHeight() {return (getSize()) * slotHeight;}

        @Override
        protected void drawSlot(int idx, int right, int top, int height, Tessellator tess)
        {
            ParseErrorEntry errorEntry = parseErrorCache.get(idx);
            String charAt = String.format("%04d", errorEntry.getCharPositionInLine());
            String formattedErrorEntry = fontRenderer.trimStringToWidth(charAt + ": " + errorEntry.getMsg(), listWidth - 10);
            fontRenderer.drawString(formattedErrorEntry, left + 3, top, 0xFF2222);
        }
    }

    public static class GuiInstruments extends GuiScrollingList
    {
        private GuiMusicPaperParse parent;
        private List<Instrument> instruments;
        private FontRenderer fontRenderer;

        GuiInstruments(GuiMusicPaperParse parent, List<Instrument> instruments, int listWidth, int slotHeight)
        {
            super(parent.mc, listWidth, parent.height - 32 - 60 + 4, 32, parent.height - 60 + 4, 10, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.instruments = instruments;
            this.fontRenderer = parent.mc.fontRenderer;
        }

        int selectedIndex(int s)
        {
            selectedIndex = s;
            return s;
        }

        public int getRight() {return right;}

        @Override
        protected int getSize() { return instruments.size(); }

        @Override
        protected void elementClicked(int index, boolean doubleClick)
        {
            if (index == parent.selectedInst) return;
            parent.selectedInst = index;
            parent.selectedInstID = (index >= 0 && index <= parent.instrumentCache.size() && !parent.instrumentCache.isEmpty()) ? parent.instrumentCache.get(parent.selectedInst) : null;
            parent.updateState();
        }

        @Override
        protected boolean isSelected(int index)
        {
            return index == parent.selectedInst;
        }

        @Override
        protected void drawBackground()
        {
            Gui.drawRect(left - 1, top - 1, left + listWidth + 1, top + listHeight + 1, -6250336);
            Gui.drawRect(left, top, left + listWidth, top + listHeight, -16777216);
        }

        @Override
        protected int getContentHeight() { return (getSize()) * slotHeight; }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
        {
            Instrument instrument = instruments.get(slotIdx);
            String s = fontRenderer.trimStringToWidth(I18n.format(instrument.getName()), listWidth - 10);
            /* light Blue */
            fontRenderer.drawStringWithShadow(s, (float)left + 3, slotTop, 0xADD8E6);
        }
    }

    /** Table Flip!
     * Because of the apparent different interpretations of MIDI and
     * SoundFont specifications and the way Sun implemented
     * {@link javax.sound.midi.Instrument}, soundfont loading, etc.:
     * <br/><br/>
     * A soundfont preset bank:0, program:0 for a piano AND
     * a soundfont preset bank:128, program:0 for a standard percussion set
     * produce identical {@link javax.sound.midi.Patch} objects using
     * {@link Patch javax.sound.midi.Instrument.getPatch()}
     * <br/><br/>
     * While a synthesizer can load the Instrument directly, if you want
     * to manipulate or test values for bank or program settings you are
     * out of luck.
     */
    @SuppressWarnings("restriction")
    private boolean mmlPlay(String mmlIn)
    {
        String mml = mmlIn;
        Instrument inst = instrumentCache.get(selectedInst);
        
        /* Table Flip! */
        boolean isPercussionSet = inst.toString().contains("Drumkit:");
        /* A SoundFont 2.04 preset allows 128 banks 0-127) plus the percussion
         * set for 129 sets! OwO However when you get a patch from an
         * Instrument from a loaded soundfont you will find the bank value
         * for the preset is left shifted 7 bits. However what's worse is that
         * for preset bank:128 the value returned by getBank() is ZERO!
         * So as a workaround I check the name of instrument to see if it's a percussion set.
         */
        int bank = inst.getPatch().getBank() >>> 7;
        int program = inst.getPatch().getProgram();
        int packedPreset = isPercussionSet ? MMLUtil.preset2PackedPreset(128, program) : MMLUtil.preset2PackedPreset(bank, program);
        
        mml = mml.replace("MML@", "MML@i" + packedPreset);
        ModLogger.debug("GuiMusicPaperParse.mmlPlay() name: %s, bank %05d, program %03d, packed %08d, perc: %s", inst.getName(), bank, program, packedPreset, isPercussionSet);
        ModLogger.debug("GuiMusicPaperParse.mmlPlay(): %s", mml.substring(0, mml.length() >= 25 ? 25 : mml.length()));

        playId = PlayIdSupplier.PlayType.PERSONAL.getAsInt();
        ClientAudio.playLocal(playId, mml, this);
        return true;
    }


    @Override
    public void statusCallBack(Status status, int playId)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (this.playId == playId && (status == Status.ERROR || status == Status.DONE))
            {
                ModLogger.debug("AudioStatus event received: %s, playId: %s", status, playId);
                mmlStop();
                updateButtonState();
            }
        });
    }

    private void mmlStop()
    {
        ClientAudio.queueAudioDataRemoval(playId);
        isPlaying = false;
        playId = PlayIdSupplier.PlayType.INVALID;
    }
}
