/*
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.gui;

import net.aeronica.libs.mml.core.*;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.MusicTextMessage;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.sound.midi.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiMusicPaperParse extends GuiScreen implements MetaEventListener
{
    public static final int GUI_ID = 7;
    // Localization Keys
    private String TITLE = I18n.format("mxtune.gui.musicPaperParse.title");
    private String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    private String HELPER_ENTER_TITLE = I18n.format("mxtune.gui.musicPaperParse.enterTitle");
    private String HELPER_ENTER_MML = I18n.format("mxtune.gui.musicPaperParse.enterMML");
    private String LABEL_INSTRUMENTS = I18n.format("mxtune.gui.musicPaperParse.labelInstruments");
    private String LABEL_TITLE_MML = I18n.format("mxtune.gui.musicPaperParse.labelTitleMML");

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
    private int helperTextCounter;
    private int helperTextColor;
    private boolean helperState;

    /* MML Parser */
    private ParseErrorListener parseErrorListener = null;
    private ArrayList<ParseErrorEntry> parseErrorCache;
    private ParseErrorEntry selectedErrorEntry;
    private int selectedError;

    /* MML Player */
    private Sequencer sequencer = null;
    @SuppressWarnings("restriction")
    private com.sun.media.sound.AudioSynthesizer synthesizer = null;
    
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

    public GuiMusicPaperParse()
    {
        midiUnavailable = MIDISystemUtil.midiUnavailable();
        instrumentCache = MIDISystemUtil.getInstrumentCacheCopy();
    }
    
    @Override
    public void updateScreen()
    {
        textMMLTitle.updateCursorCounter();
        textMMLPaste.updateCursorCounter();
        updateHelperTextCounter();
        selectedError = this.listBoxMMLError.selectedIndex(parseErrorCache.indexOf(selectedErrorEntry));
        selectedInst = this.listBoxInstruments.selectedIndex(instrumentCache.indexOf(selectedInstID));
    }

    @Override
    public void initGui()
    {        
        this.mc = Minecraft.getMinecraft();
        parseErrorListener = new ParseErrorListener();
        parseErrorCache = new ArrayList<>();
        selectedError = -1;
        selectedInst = -1;
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();

        for (Instrument in : instrumentCache)
        {
            int stringWidth = getFontRenderer().getStringWidth(I18n.format(in.getName()));
            instListWidth = Math.max(instListWidth, stringWidth + 10);
            instListWidth = Math.max(instListWidth, stringWidth + 5 + this.getFontRenderer().FONT_HEIGHT + 2);
        }
        instListWidth = Math.min(instListWidth, 150);

        // create Instrument selector, and buttons
        listBoxInstruments = new GuiInstruments(this, instrumentCache, instListWidth, this.getFontRenderer().FONT_HEIGHT + 2);
        buttonOkay = new GuiButton(0, (this.listBoxInstruments.getRight() + this.width / 2) - 75 + 30, this.height - 32, 75, 20, I18n.format("gui.done"));
        buttonCancel = new GuiButton(1, (this.listBoxInstruments.getRight() + this.width / 2) - 150 + 25, this.height - 32, 75, 20, I18n.format("gui.cancel"));
        buttonPlay = new GuiButton(2, 10, this.height - 49, this.instListWidth, 20, I18n.format("mxtune.gui.button.play"));
        buttonStop = new GuiButton(3, 10, this.height - 27, this.instListWidth, 20, I18n.format("mxtune.gui.button.stop"));
        mmlLink = new GuiLink(4, this.width - 10 , 20, ModConfig.getMmlLink(), GuiLink.AlignText.Right); 
        buttonPlay.enabled = false;
        buttonStop.enabled = false;
        buttonOkay.enabled = false;
        this.buttonList.add(buttonOkay);
        this.buttonList.add(buttonCancel);
        this.buttonList.add(buttonPlay);
        this.buttonList.add(buttonStop);
        this.buttonList.add(mmlLink);

        /* create MML Title field */
        int posX = this.listBoxInstruments.getRight() + 5;
        int posY = 32;
        textMMLTitle = new GuiTextField(0, getFontRenderer(), posX, posY, this.width - posX - 10, 18);
        textMMLTitle.setFocused(true);
        textMMLTitle.setCanLoseFocus(true);
        textMMLTitle.setMaxStringLength(50);

        /* create MML Paste/Edit field */
        posY = 32 + 18 + 5;
        textMMLPaste = new GuiMMLBox(1, getFontRenderer(), posX, posY, this.width - posX - 10, 62);
        textMMLPaste.setFocused(false);
        textMMLPaste.setCanLoseFocus(true);
        textMMLPaste.setMaxStringLength(10000);

        /* create Status line */
        posY = 32 + 18 + 5 + 62 + 5;
        labelStatus = new GuiTextField(2, getFontRenderer(), posX, posY, this.width - posX - 10, 18);
        labelStatus.setFocused(false);
        labelStatus.setCanLoseFocus(true);
        labelStatus.setEnabled(false);
        labelStatus.setMaxStringLength(80);

        /* create Parse Error selector */
        posY = 32 + 18 + 5 + 62 + 5 + 18 + 5;
        listBoxMMLError = new GuiParserErrorList(this, parseErrorCache, posX, posY, this.width - posX - 10, this.height - posY - 42, this.getFontRenderer().FONT_HEIGHT + 2);

        reloadState();
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        this.textMMLTitle.setText(cachedMMLTitle);
        this.textMMLPaste.setText(cachedMMLText);
        this.listBoxInstruments.elementClicked(cachedSelectedInst, false);
        this.isPlaying = this.cachedIsPlaying;
        this.parseMML(this.textMMLPaste.getText());
        updateButtonState();
    }

    private void updateState()
    {
        this.cachedMMLTitle = this.textMMLTitle.getText();
        this.cachedMMLText = this.textMMLPaste.getText();
        this.cachedSelectedInst = selectedInst;
        this.cachedIsPlaying = this.isPlaying;
        this.labelStatus.setText(String.format("[%04d]", this.textMMLPaste.getCursorPosition()));
        updateButtonState();

        this.isStateCached = true;
    }

    private void updateButtonState()
    {
        /* enable OKAY button when Title Field is greater than 0 chars and passes the MML parsing tests */
        boolean isOK = (!textMMLPaste.isEmpty()) && parseErrorCache.isEmpty() && !textMMLTitle.getText().isEmpty();
        buttonList.get(buttonOkay.id).enabled = isOK;
        buttonList.get(buttonPlay.id).enabled = !this.isPlaying && isOK;
        buttonList.get(buttonStop.id).enabled = this.isPlaying && isOK;
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
        int posX = (this.width - getFontRenderer().getStringWidth(localTITLE)) / 2;
        int posY = 10;
        getFontRenderer().drawStringWithShadow(localTITLE, posX, posY, 0xD3D3D3);

        /* draw Field names */
        posX = this.listBoxInstruments.getRight() + 10;
        posY = 20;
        getFontRenderer().drawStringWithShadow(LABEL_TITLE_MML, posX, posY, 0xD3D3D3);

        /* draw the instrument list */
        posX = 10;
        posY = 20;
        getFontRenderer().drawStringWithShadow(LABEL_INSTRUMENTS, posX, posY, 0xD3D3D3);

        listBoxInstruments.drawScreen(mouseX, mouseY, partialTicks);
        listBoxMMLError.drawScreen(mouseX, mouseY, partialTicks);

        /* draw the GuiTextField */
        textMMLTitle.drawTextBox();
        textMMLPaste.drawTextBox();
        labelStatus.drawTextBox();

        /* draw helpers */
        if (textMMLTitle.getText().isEmpty())
        {
            int helperWidth = getFontRenderer().getStringWidth(HELPER_ENTER_TITLE);
            int fontHeight = getFontRenderer().FONT_HEIGHT + 2;
            getFontRenderer().drawString(HELPER_ENTER_TITLE, textMMLTitle.x + textMMLTitle.width / 2 - helperWidth / 2, textMMLTitle.y + fontHeight / 2, HelperTextColor());
        }
        if (textMMLPaste.isEmpty())
        {
            int helperWidth = getFontRenderer().getStringWidth(HELPER_ENTER_MML);
            int fontHeight = getFontRenderer().FONT_HEIGHT + 2;
            getFontRenderer().drawString(HELPER_ENTER_MML, textMMLPaste.xPosition + textMMLPaste.width / 2 - helperWidth / 2, textMMLPaste.yPosition + textMMLPaste.height / 2 - fontHeight / 2,
                    HelperTextColor());
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void updateHelperTextCounter()
    {
        ++this.helperTextCounter;
    }

    private int HelperTextColor()
    {
        final int LO = 0x30;
        final int HI = 0xD0;
        
        if (this.helperTextCounter % 20 == 0) 
        {
//            helperState = helperTextColor <= LO && !helperState ? !helperState : helperTextColor >= HI && helperState ? !helperState : helperState;
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
            sendMMLTextToServer(musicTitle, musicText);
            closeGui();
            break;

        case 1:
            /* Cancelled - remove the GUI */
            mmlStop();
            closeGui();
            break;

        case 2:
            /* Play MML */
            if (this.selectedInst < 0)
            {
                this.selectedInst = 0;
                this.listBoxInstruments.elementClicked(selectedInst, false);
            }
            String mml = this.textMMLPaste.getTextToParse();
            this.isPlaying = mmlPlay(mml);
            break;

        case 3:
            /* Stop playing MML */
            mmlStop();
            break;
            
        case 4:
            this.handleComponentClick(mmlLink.getLinkComponent());
            break;
        default:
        }
        updateState();
    }

    private void closeGui()
    {
        mc.displayGuiScreen(null);
        mc.setIngameFocus();
    }
    
    /*
     * Fired when a key is typed. This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e).
     * 
     * @throws IOException
     */
    @Override
    protected void keyTyped(char c, int i) throws IOException
    {
        /* add char to GuiTextField */
        textMMLTitle.textboxKeyTyped(c, i);
        textMMLPaste.textboxKeyTyped(c, i);
        if (i == Keyboard.KEY_TAB)
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
        if (i == Keyboard.KEY_ESCAPE)
        {
            actionPerformed(buttonList.get(buttonCancel.id));
        }
        super.keyTyped(c, i);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        /*
         * A hack is a hack is a hack - Disabling mouse handling on other
         * controls. In this case to ensure a particular control keeps focus
         * while clicking on the error list.
         **/
        if (!this.listBoxMMLError.isHovering()) super.handleMouseInput();

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
        MMLParser parser = MMLParserFactory.getMMLParser(mml);
        if (parser == null)
        {
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
        private final ArrayList<ParseErrorEntry> parseErrorCache;

        GuiParserErrorList(GuiMusicPaperParse parent, ArrayList<ParseErrorEntry> parseErrorCache, int left, int top, int listWidth, int listHeight, int slotHeight)
        {
            super(parent.getMinecraftInstance(), listWidth, listHeight, top, top + listHeight, left, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.parseErrorCache = parseErrorCache;
        }

        boolean isHovering()
        {
            return mouseX >= left && mouseX <= left + listWidth && mouseY >= top && mouseY <= bottom && getSize() > 0;
        }

        int selectedIndex(int s) {return selectedIndex = s;}

        @Override
        protected int getSize() {return parseErrorCache.size();}

        @Override
        protected void elementClicked(int index, boolean doubleClick) {this.parent.selectErrorIndex(index);}

        @Override
        protected boolean isSelected(int index) {return this.parent.errorIndexSelected(index);}

        @Override
        protected void drawBackground()
        {
            Gui.drawRect(this.left - 1, this.top - 1, this.left + this.listWidth + 1, this.top + this.listHeight + 1, -6250336);
            Gui.drawRect(this.left, this.top, this.left + this.listWidth, this.top + this.listHeight, -16777216);
        }

        @Override
        protected int getContentHeight() {return (this.getSize()) * slotHeight;}

        @Override
        protected void drawSlot(int idx, int right, int top, int height, Tessellator tess)
        {
            FontRenderer font = this.parent.getFontRenderer();
            ParseErrorEntry pe = parseErrorCache.get(idx);
            String charAt = String.format("%04d", pe.getCharPositionInLine());
            String s = font.trimStringToWidth(charAt + ": " + pe.getMsg(), listWidth - 10);
            font.drawString(s, this.left + 3, top, 0xFF2222);
        }
    }

    /*
     * element was clicked
     * 
     * @throws InterruptedException
     */
    private void selectErrorIndex(int index)
    {
        this.selectedError = index;
        this.selectedErrorEntry = (index >= 0 && index <= parseErrorCache.size()) ? parseErrorCache.get(selectedError) : null;
        if (this.selectedErrorEntry != null)
        {
            this.textMMLPaste.setCursorPosition(this.selectedErrorEntry.getCharPositionInLine());
            this.textMMLPaste.setFocused(true);
        }
        updateState();
    }

    private boolean errorIndexSelected(int index) {return index == selectedError;}

    public static class GuiInstruments extends GuiScrollingList
    {
        private GuiMusicPaperParse parent;
        private List<Instrument> instruments;

        GuiInstruments(GuiMusicPaperParse parent, List<Instrument> instruments, int listWidth, int slotHeight)
        {
            super(parent.getMinecraftInstance(), listWidth, parent.height - 32 - 60 + 4, 32, parent.height - 60 + 4, 10, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.instruments = instruments;
        }

        int selectedIndex(int s) {return selectedIndex = s;}

        public int getRight() {return right;}

        @Override
        protected int getSize() {return instruments.size();}

        @Override
        protected void elementClicked(int index, boolean doubleClick) {this.parent.selectInstIndex(index);}

        @Override
        protected boolean isSelected(int index) {return this.parent.instIndexSelected(index);}

        @Override
        protected void drawBackground()
        {
            Gui.drawRect(this.left - 1, this.top - 1, this.left + this.listWidth + 1, this.top + this.listHeight + 1, -6250336);
            Gui.drawRect(this.left, this.top, this.left + this.listWidth, this.top + this.listHeight, -16777216);
        }

        @Override
        protected int getContentHeight() {return (this.getSize()) * slotHeight;}

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
        {
            FontRenderer font = this.parent.getFontRenderer();
            Instrument instrument = instruments.get(slotIdx);

            String s = font.trimStringToWidth(I18n.format(instrument.getName()), listWidth - 10);
            /* light Blue */
            font.drawStringWithShadow(s, (float)this.left + 3, slotTop, 0xADD8E6);
        }
    }

    /* element was clicked */
    private void selectInstIndex(int index)
    {
        if (index == this.selectedInst)
            return;
        this.selectedInst = index;
        this.selectedInstID = (index >= 0 && index <= instrumentCache.size()) ? instrumentCache.get(selectedInst) : null;
        updateState();
    }

    private boolean instIndexSelected(int index) {return index == selectedInst;}

    private Minecraft getMinecraftInstance() {return mc;}

    private FontRenderer getFontRenderer() {return mc.fontRenderer;}

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
        
        if (midiUnavailable) return false;
        MMLParser parser = MMLParserFactory.getMMLParser(mml);
        if (parser == null)
        {
            ModLogger.debug("MMLParserFactory.getMMLParser(mml) is null in %s", this.getClass().getSimpleName());
            return true;
        }
        parser.removeErrorListeners();
        parser.setBuildParseTree(true);
        ParseTree tree = parser.band();

        ParseTreeWalker walker = new ParseTreeWalker();
        MMLToMIDI mmlTrans = new MMLToMIDI();
        walker.walk(mmlTrans, tree);
        /* ANTLR4 MML Parser END */

        boolean midiException;
        midiException = false;
        try
        {
            /* Using the Gervill synthesizer and sequencer */
            synthesizer = (com.sun.media.sound.AudioSynthesizer) MidiSystem.getSynthesizer();
            if (synthesizer != null && !synthesizer.isOpen())
            {
                synthesizer.open();

                if (!instrumentCache.isEmpty())
                    synthesizer.loadInstrument(inst);        

                sequencer = MidiSystem.getSequencer(false);
                sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
                sequencer.open();
                sequencer.addMetaEventListener(this);
                sequencer.setSequence(mmlTrans.getSequence());
                sequencer.setTickPosition(0L);
                sequencer.start();
            } else
            {
                midiException = true;
            }
            return !midiException;

        } catch (Exception e)
        {
            cleanupMIDI();
            ModLogger.error(e);
            midiException = true;
        }
        finally
        {
            if (midiException)
            {
                if (sequencer != null) 
                    sequencer.removeMetaEventListener(this);
            }
        }
        return false;
    }

    @Override
    public void meta(MetaMessage event)
    {
        if (event.getType() == 47)
        { /* end of stream */
            ModLogger.debug("MetaMessage EOS event received");
            mmlStop();
            this.updateButtonState();
        }
    }

    private void mmlStop()
    {
        if (sequencer != null && sequencer.isOpen())
        {
            sequencer.stop();
            sequencer.setTickPosition(0L);
            sequencer.removeMetaEventListener(this);
            try
            {
                Thread.sleep(250);
            } catch (InterruptedException e)
            {
                ModLogger.error(e);
                cleanupMIDI();
                Thread.currentThread().interrupt();
            }
            cleanupMIDI();            
        }
    }
    
    private void cleanupMIDI()
    {
        if (sequencer != null && sequencer.isOpen()) sequencer.close();
        if (synthesizer != null && synthesizer.isOpen()) synthesizer.close();
        this.isPlaying = false;
    }
}
