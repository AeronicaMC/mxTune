/**
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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.aeronica.libs.mml.core.MMLLexer;
import net.aeronica.libs.mml.core.MMLParser;
import net.aeronica.libs.mml.core.MMLToMIDI;
import net.aeronica.mods.mxtune.mml.MMLManager;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.MusicTextMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.GuiScrollingList;

public class GuiMusicPaperParse extends GuiScreen implements MetaEventListener
{
    public static final int GUI_ID = 7;
    private Minecraft mc;
    private String TITLE = "MML Simple Editor";
    private GuiTextField txt_mmlTitle;
    private GuiMMLBox txt_mmlPaste;
    private GuiTextField lbl_status;
    private GuiButton btn_okay, btn_canc, btn_play, btn_stop;
    private GuiParserErrorList lst_mmlError;
    private GuiInstruments lst_inst;
    private int helperTextCounter;
    private short helperTextColor;

    /** MML Parser */
    private static byte[] mmlBuf = null;
    private InputStream is;
    private ParseErrorListener parseErrorListener = null;
    private ArrayList<ParseErrorEntry> parseErrorCache;
    private ParseErrorEntry selectedErrorEntry;
    private int selectedError = -1;

    /** Instruments */
    private Synthesizer synth;
    private Instrument[] inst;
    private ArrayList<String> instrumentCache;
    private int instListWidth;
    private String selectedInstName = "";
    private int selectedInst = -1;
    private boolean isPlaying = false;

    /** Cached State for when the GUI is resized */
    private boolean isStateCached = false;
    private boolean cachedIsPlaying;
    private String cachedMMLTitle;
    private String cachedMMLText;
    private int cachedSelectedInst;

    public GuiMusicPaperParse() {}

    @Override
    public void updateScreen()
    {
        txt_mmlTitle.updateCursorCounter();
        txt_mmlPaste.updateCursorCounter();
        updateHelperTextCounter();
        selectedError = this.lst_mmlError.selectedIndex(parseErrorCache.indexOf(selectedErrorEntry));
        selectedInst = this.lst_inst.selectedIndex(instrumentCache.indexOf(selectedInstName));
    }

    @Override
    public void initGui()
    {
        this.mc = Minecraft.getMinecraft();
        parseErrorListener = new ParseErrorListener();
        new ArrayList<ParseErrorEntry>();
        parseErrorCache = new ArrayList<ParseErrorEntry>();
        instrumentCache = new ArrayList<String>();
        selectedError = selectedInst = -1;
        selectedInst = -1;
        initInstrumentCache();

        Keyboard.enableRepeatEvents(true);

        buttonList.clear();

        for (String in : instrumentCache)
        {
            instListWidth = Math.max(instListWidth, getFontRenderer().getStringWidth(in) + 10);
            instListWidth = Math.max(instListWidth, getFontRenderer().getStringWidth(in) + 5 + this.getFontRenderer().FONT_HEIGHT + 2);
        }
        instListWidth = Math.min(instListWidth, 150);

        // create Instrument selector, and buttons
        lst_inst = new GuiInstruments(this, instrumentCache, instListWidth, this.getFontRenderer().FONT_HEIGHT + 2);
        btn_okay = new GuiButton(0, (this.lst_inst.getRight() + this.width / 2) - 75 + 30, this.height - 32, 75, 20, I18n.format("gui.done"));
        btn_canc = new GuiButton(1, (this.lst_inst.getRight() + this.width / 2) - 150 + 25, this.height - 32, 75, 20, I18n.format("gui.cancel"));
        btn_play = new GuiButton(2, 10, this.height - 49, this.instListWidth, 20, "Play");
        btn_stop = new GuiButton(3, 10, this.height - 27, this.instListWidth, 20, "Stop");
        this.buttonList.add(btn_okay);
        this.buttonList.add(btn_canc);
        this.buttonList.add(btn_play);
        this.buttonList.add(btn_stop);

        /** create MML Title field */
        int posX = this.lst_inst.getRight() + 5;
        int posY = 32;
        txt_mmlTitle = new GuiTextField(0, getFontRenderer(), posX, posY, this.width - posX - 10, 18);
        txt_mmlTitle.setFocused(true);
        txt_mmlTitle.setCanLoseFocus(true);
        txt_mmlTitle.setMaxStringLength(50);

        /** create MML Paste/Edit field */
        posY = 32 + 18 + 5;
        txt_mmlPaste = new GuiMMLBox(1, getFontRenderer(), posX, posY, this.width - posX - 10, 62);
        txt_mmlPaste.setFocused(false);
        txt_mmlPaste.setCanLoseFocus(true);
        txt_mmlPaste.setMaxStringLength(10000);

        /** create Status line */
        posY = 32 + 18 + 5 + 62 + 5;
        lbl_status = new GuiTextField(2, getFontRenderer(), posX, posY, this.width - posX - 10, 18);
        lbl_status.setFocused(false);
        lbl_status.setCanLoseFocus(true);
        lbl_status.setEnabled(false);
        lbl_status.setMaxStringLength(80);

        /** create Parse Error selector */
        posY = 32 + 18 + 5 + 62 + 5 + 18 + 5;
        lst_mmlError = new GuiParserErrorList(this, parseErrorCache, posX, posY, this.width - posX - 10, this.height - posY - 42, this.getFontRenderer().FONT_HEIGHT + 2);

        reloadState();
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        this.txt_mmlTitle.setText(cachedMMLTitle);
        this.txt_mmlPaste.setText(cachedMMLText);
        this.lst_inst.elementClicked(cachedSelectedInst, false);
        this.isPlaying = this.cachedIsPlaying;
        this.parseMML(this.txt_mmlPaste.getText());
        updateButtonState();
    }

    private void updateState()
    {
        this.cachedMMLTitle = this.txt_mmlTitle.getText();
        this.cachedMMLText = this.txt_mmlPaste.getText();
        this.cachedSelectedInst = selectedInst;
        this.cachedIsPlaying = this.isPlaying;
        this.lbl_status.setText(String.format("[%04d]", this.txt_mmlPaste.getCursorPosition()));
        updateButtonState();

        this.isStateCached = true;
    }

    private void updateButtonState()
    {
        /** enable OKAY button when Title Field is greater than 0 chars and passes the MML parsing tests */
        boolean isOK = (txt_mmlPaste.getText().length() > 0) && parseErrorCache.isEmpty() && !txt_mmlTitle.getText().isEmpty();
        ((GuiButton) buttonList.get(btn_okay.id)).enabled = isOK;
        ((GuiButton) buttonList.get(btn_play.id)).enabled = !this.isPlaying && isOK;
        ((GuiButton) buttonList.get(btn_stop.id)).enabled = this.isPlaying && isOK;
    }

    @Override
    public void drawScreen(int mouseX, int mounseY, float partialTicks)
    {
        drawDefaultBackground();

        /** draw "TITLE" at the top/right column middle */
        int posX = (this.lst_inst.getRight() + this.width / 2) - getFontRenderer().getStringWidth(TITLE);
        int posY = 10;
        getFontRenderer().drawStringWithShadow(TITLE, posX, posY, 0xD3D3D3);

        /** draw Field names */
        posX = this.lst_inst.getRight() + 10;
        posY = 20;
        getFontRenderer().drawStringWithShadow("Title / MML@:", posX, posY, 0xD3D3D3);

        /** draw the instrument list */
        posX = 10;
        posY = 20;
        getFontRenderer().drawStringWithShadow("Instruments", posX, posY, 0xD3D3D3);

        lst_inst.drawScreen(mouseX, mounseY, partialTicks);
        lst_mmlError.drawScreen(mouseX, mounseY, partialTicks);

        /** draw the GuiTextField */
        txt_mmlTitle.drawTextBox();
        txt_mmlPaste.drawTextBox();
        lbl_status.drawTextBox();

        /** draw helpers */
        if (txt_mmlTitle.getText().isEmpty())
        {
            String helperText = "Enter a Title!";
            int helperWidth = getFontRenderer().getStringWidth(helperText);
            int fontHeight = getFontRenderer().FONT_HEIGHT + 2;
            getFontRenderer().drawString(helperText, txt_mmlTitle.xPosition + txt_mmlTitle.width / 2 - helperWidth / 2, txt_mmlTitle.yPosition + fontHeight / 2, HelperTextColor());
        }
        if (txt_mmlPaste.getText().isEmpty())
        {
            String helperText = "Paste in MML!";
            int helperWidth = getFontRenderer().getStringWidth(helperText);
            int fontHeight = getFontRenderer().FONT_HEIGHT + 2;
            getFontRenderer().drawString(helperText, txt_mmlPaste.xPosition + txt_mmlPaste.width / 2 - helperWidth / 2, txt_mmlPaste.yPosition + txt_mmlPaste.height / 2 - fontHeight / 2,
                    HelperTextColor());
        }

        super.drawScreen(mouseX, mounseY, partialTicks);
    }

    private void updateHelperTextCounter()
    {
        ++this.helperTextCounter;
    }

    private int HelperTextColor()
    {
        boolean updown = helperTextCounter / 32 % 2 == 0;
        helperTextColor = (short) (updown ? Math.min(0xC0, ++helperTextColor) : Math.max(0x20, --helperTextColor));
        int color = helperTextColor;
        color &= 0xFF;
        int RGB = ((color << 16) + (color << 8) + color);
        return RGB;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        /** if button is disabled ignore click */
        if (!guibutton.enabled) return;

        /** id 0 = okay; 1 = cancel; 2 = play; 3 = stop */
        switch (guibutton.id)
        {
        case 0:
            /** Done / OKAY - Save MML */
            String musictext = txt_mmlPaste.getText().trim();
            String musictitle = txt_mmlTitle.getText().trim();
            mmlStop();
            ModLogger.logInfo("Save new MML to server");
            sendMMLTextToServer(musictitle, musictext);

        case 1:
            /** Cancelled - remove the GUI */
            mmlStop();
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
            break;

        case 2:
            /** Play MML */
            if (this.selectedInst < 0)
            {
                this.selectedInst = 0;
                this.lst_inst.elementClicked(selectedInst, false);
            }
            String mml = this.txt_mmlPaste.getText().replace("MML@", "MML@i" + (this.selectedInst + 1));
            this.isPlaying = mmlPlay(mml);
            break;

        case 3:
            /** Stop playing MML */
            mmlStop();
            break;
        default:
        }
        updateState();
    }

    /**
     * Fired when a key is typed. This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e).
     * 
     * @throws IOException
     */
    @Override
    protected void keyTyped(char c, int i) throws IOException
    {
        /** add char to GuiTextField */
        txt_mmlTitle.textboxKeyTyped(c, i);
        txt_mmlPaste.textboxKeyTyped(c, i);
        if (i == Keyboard.KEY_TAB)
        {
            if (txt_mmlTitle.isFocused())
            {
                txt_mmlPaste.setFocused(true);
                txt_mmlTitle.setFocused(false);
            } else
            {
                txt_mmlPaste.setFocused(false);
                txt_mmlTitle.setFocused(true);
            }
        }
        parseMML(txt_mmlPaste.getText());
        updateState();
        /** perform click event on ok button when Enter is pressed */
        if (c == '\n' || c == '\r')
        {
            /** Better to eat than close and save prematurely */
            // actionPerformed((GuiButton) buttonList.get(btn_okay.id));
        }
        if (i == Keyboard.KEY_ESCAPE)
        {
            actionPerformed((GuiButton) buttonList.get(btn_canc.id));
        }
        super.keyTyped(c, i);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        /**
         * A hack is a hack is a hack - Disabling mouse handling on other
         * controls. In this case to ensure a particular control keeps focus
         * while clicking on the error list.
         **/
        if (!this.lst_mmlError.isHovering()) super.handleMouseInput();

        lst_inst.handleMouseInput(mouseX, mouseY);
        lst_mmlError.handleMouseInput(mouseX, mouseY);
    }

    /**
     * Called when the mouse is clicked.
     * 
     * @throws IOException
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int partialTicks) throws IOException
    {
        txt_mmlTitle.mouseClicked(mouseX, mouseY, partialTicks);
        txt_mmlPaste.mouseClicked(mouseX, mouseY, partialTicks);
        super.mouseClicked(mouseX, mouseY, partialTicks);
        updateState();
    }

    protected void sendMMLTextToServer(String titleIn, String mmlIn)
    {
        PacketDispatcher.sendToServer(new MusicTextMessage(titleIn, mmlIn));
    }

    /** Load Default General MIDI instruments */
    private void initInstrumentCache()
    {
        instrumentCache.clear();
        try
        {
            synth = MidiSystem.getSynthesizer();
            synth.open();
        } catch (MidiUnavailableException e)
        {
            /** TODO: Stuff the error into a status window in this GUI */
            e.printStackTrace();
        }
        inst = synth.getLoadedInstruments();
        if (inst.length > 0)
        {
            int idx = 0;
            for (Instrument e : inst)
            {
                if (idx++ < 128)
                {
                    ModLogger.logInfo("Inst: " + e);
                    instrumentCache.add(e.getName());
                }
            }
        }
        if (synth != null && synth.isOpen()) synth.close();
    }

    /** MML Parsing */
    private void parseMML(String mml)
    {
        try
        {
            mmlBuf = mml.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e)
        {
            System.out.println(e.getLocalizedMessage());
            /** TODO: Stuff the error into a status window in this GUI */
            e.printStackTrace();
        }
        is = new java.io.ByteArrayInputStream(mmlBuf);

        /** ANTLR4 MML Parser BEGIN */
        ANTLRInputStream input = null;

        try
        {
            input = new ANTLRInputStream(is);
        } catch (IOException e1)
        {
            e1.printStackTrace();
        }
        MMLLexer lexer = new MMLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MMLParser parser = new MMLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(parseErrorListener);
        parser.setBuildParseTree(true);
        @SuppressWarnings("unused")
        ParseTree tree = parser.inst();
        parseErrorCache.clear();
        for (ParseErrorEntry e : parseErrorListener.getParseErrorEntries())
        {
            parseErrorCache.add(e);
        }
    }

    public static class ParseErrorListener extends BaseErrorListener implements IParseErrorEntries
    {

        public ArrayList<ParseErrorEntry> parseErrorList = new ArrayList<ParseErrorEntry>();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
        {
            List<String> stack = ((Parser) recognizer).getRuleInvocationStack();
            Collections.reverse(stack);
            parseErrorList.add(new ParseErrorEntry(stack, (Token) offendingSymbol, line, charPositionInLine, msg, e));
        }

        @Override
        public ArrayList<ParseErrorEntry> getParseErrorEntries()
        {
            /** copy the records out then clear the local list */
            ArrayList<ParseErrorEntry> temp = new ArrayList<ParseErrorEntry>();
            for (ParseErrorEntry e : parseErrorList)
            {
                temp.add(e);
            }
            parseErrorList.clear();
            return temp;
        }
    }

    public interface IParseErrorEntries
    {
        public List<ParseErrorEntry> getParseErrorEntries();
    }

    public static class ParseErrorEntry
    {
        private List<String> ruleStack;
        private Token offendingToken;
        private int line;
        private int charPositionInLine;
        private String msg;
        private RecognitionException e;

        public ParseErrorEntry(List<String> ruleStack, Token offendingToken, int line, int charPositionInLine, String msg, RecognitionException e)
        {
            this.ruleStack = ruleStack;
            this.offendingToken = offendingToken;
            this.line = line;
            this.charPositionInLine = charPositionInLine;
            this.msg = msg;
            this.e = e;
        }

        public List<String> getRuleStack() {return ruleStack;}

        public Object getOffendingToken() {return offendingToken;}

        public int getLine() {return line;}

        public int getCharPositionInLine() {return charPositionInLine;}

        public String getMsg() {return msg;}

        public RecognitionException getE() {return e;}
    }

    public static class GuiParserErrorList extends GuiScrollingList
    {
        private GuiMusicPaperParse parent;
        private final ArrayList<ParseErrorEntry> parseErrorCache;

        public GuiParserErrorList(GuiMusicPaperParse parent, ArrayList<ParseErrorEntry> parseErrorCache, int left, int top, int listWidth, int listHeight, int slotHeight)
        {
            super(parent.getMinecraftInstance(), listWidth, listHeight, top, top + listHeight, left, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.parseErrorCache = parseErrorCache;
        }

        public boolean isHovering()
        {
            boolean isHovering = mouseX >= left && mouseX <= left + listWidth && mouseY >= top && mouseY <= bottom && getSize() > 0;
            return isHovering;
        }

        int selectedIndex(int s) {return selectedIndex = s;}

        @Override
        protected int getSize() {return parseErrorCache.size();}

        ArrayList<ParseErrorEntry> getErrors() {return parseErrorCache;}

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
            String s = font.trimStringToWidth(charAt + ": " + pe.msg, listWidth - 10);
            font.drawString(s, this.left + 3, top, 0xFF2222);
        }
    }

    /**
     * element was clicked
     * 
     * @throws InterruptedException
     */
    public void selectErrorIndex(int index)
    {
        this.selectedError = index;
        this.selectedErrorEntry = (index >= 0 && index <= parseErrorCache.size()) ? parseErrorCache.get(selectedError) : null;
        if (this.selectedErrorEntry != null)
        {
            this.txt_mmlPaste.setCursorPosition(this.selectedErrorEntry.charPositionInLine);
            ModLogger.logInfo("selectErrorIndex.Start: " + this.selectedErrorEntry.offendingToken.getStartIndex());
            ModLogger.logInfo("selectErrorIndex.End  : " + this.selectedErrorEntry.offendingToken.getStopIndex());
            ModLogger.logInfo("selectErrorIndex.Hover: " + this.lst_mmlError.isHovering());
            this.txt_mmlPaste.setFocused(true);
        }
        updateState();
    }

    public boolean errorIndexSelected(int index) {return index == selectedError;}

    public static class GuiInstruments extends GuiScrollingList
    {
        private GuiMusicPaperParse parent;
        private ArrayList<String> inst;

        public GuiInstruments(GuiMusicPaperParse parent, ArrayList<String> inst, int listWidth, int slotHeight)
        {
            super(parent.getMinecraftInstance(), listWidth, parent.height - 32 - 60 + 4, 32, parent.height - 60 + 4, 10, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.inst = inst;
        }

        int selectedIndex(int s) {return selectedIndex = s;}

        public int getRight() {return right;}

        @Override
        protected int getSize() {return inst.size();}

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
            String ins = inst.get(slotIdx);

            String s = font.trimStringToWidth(ins, listWidth - 10);
            /** light Blue */
            font.drawStringWithShadow(s, this.left + 3, slotTop, 0xADD8E6);        }
    }

    /** element was clicked */
    public void selectInstIndex(int index)
    {
        if (index == this.selectedError) return;
        this.selectedInst = index;
        this.selectedInstName = (index >= 0 && index <= instrumentCache.size()) ? instrumentCache.get(selectedInst) : null;
        updateState();
    }

    public boolean instIndexSelected(int index) {return index == selectedInst;}

    public Minecraft getMinecraftInstance() {return mc;}

    public FontRenderer getFontRenderer() {return mc.fontRendererObj;}

    /** MML Player */
    private Sequencer sequencer = null;
    private Synthesizer synthesizer = null;

    public boolean mmlPlay(String mmlIn)
    {
        Soundbank defaultSB;
        byte[] mmlBuf = null;
        InputStream is;
        final int masterTempo = 120;

        MMLManager.muteSounds();

        try
        {
            mmlBuf = mmlIn.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e)
        {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
        is = new java.io.ByteArrayInputStream(mmlBuf);

        /** ANTLR4 MML Parser BEGIN */
        MMLToMIDI mmlTrans = new MMLToMIDI();
        ANTLRInputStream input = null;

        try
        {
            input = new ANTLRInputStream(is);
        } catch (IOException e1)
        {
            e1.printStackTrace();
        }
        MMLLexer lexer = new MMLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MMLParser parser = new MMLParser(tokens);
        parser.removeErrorListeners();
        parser.setBuildParseTree(true);
        ParseTree tree = parser.band();

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(mmlTrans, tree);
        /** ANTLR4 MML Parser END */

        try
        {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            defaultSB = synthesizer.getDefaultSoundbank();
            ModLogger.logInfo("defaultSB:   " + defaultSB.getName());

            sequencer = MidiSystem.getSequencer();
            sequencer.addMetaEventListener(this);
            sequencer.setMicrosecondPosition(0l);
            sequencer.setTempoInBPM((float) masterTempo);

            Sequence seq = mmlTrans.getSequence();

            sequencer.open();
            for (Transmitter t : sequencer.getTransmitters())
            {
                t.setReceiver(synthesizer.getReceiver());
            }

            sequencer.setSequence(seq);
            sequencer.start();

            return true;

        } catch (Exception ex)
        {
            if (sequencer != null && sequencer.isOpen()) sequencer.close();
            if (synthesizer != null && synthesizer.isOpen()) synthesizer.close();
            ModLogger.logInfo("mmlPlay failed midi TRY " + ex);

            MMLManager.unMuteSounds();
            ;
            return false;
        }
    }

    @Override
    public void meta(MetaMessage event)
    {
        if (event.getType() == 47)
        { /** end of stream */
            ModLogger.logInfo("MetaMessage EOS event received");
            mmlStop();
            this.updateButtonState();
        }
    }

    public void mmlStop()
    {
        if (sequencer != null && sequencer.isOpen())
        {
            sequencer.stop();
            sequencer.setMicrosecondPosition(0L);
            sequencer.removeMetaEventListener(this);
            try
            {
                Thread.sleep(250);
            } catch (InterruptedException e)
            {
            }
            if (sequencer != null && sequencer.isOpen()) sequencer.close();
            if (synthesizer != null && synthesizer.isOpen()) synthesizer.close();

            MMLManager.unMuteSounds();
            this.isPlaying = false;
        }
    }
}
