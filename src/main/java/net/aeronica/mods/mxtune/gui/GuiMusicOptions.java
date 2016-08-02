/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import net.aeronica.mods.mxtune.mml.MMLManager;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.MusicOptionsMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.options.PlayerLists;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiMusicOptions extends GuiScreen
{
    public static final int GUI_ID = 8;
    private Minecraft mc;
    private String TITLE;

    private GuiButtonExt btn_muteOption;
    private GuiSliderMX btn_midiVolume;
    private GuiButtonExt btn_cancel, btn_done, btn_reset;
    private GuiButtonExt btn_white_to_players, btn_players_to_white, btn_black_to_players, btn_players_to_black;
    private GuiLabel lbl_desc;
    private GuiPlayerList lst_players;
    private GuiWhiteList lst_white;
    private GuiBlackList lst_black;
    
    private EntityPlayer player;
    private float midiVolume;
    private int muteOption;
    private int lastMuteOption = -1;

    /** PlayerList */
    private ArrayList<PlayerLists> playerList; 
    private int selectedPlayerIndex = -1;
    private PlayerLists selectedPlayerList = new PlayerLists();
    private int playerListWidth;

    /** WhiteList */
    private ArrayList<PlayerLists> whiteList; 
    private int selectedWhiteIndex = -1;
    private PlayerLists selectedWhiteList = new PlayerLists();
    private int whiteListWidth;
    
    /** WhiteList */
    private ArrayList<PlayerLists> blackList; 
    private int selectedBlackIndex = -1;
    private PlayerLists selectedBlackList = new PlayerLists();
    private int blackListWidth;

    /** Cached State for when the GUI is resized */
    private boolean isStateCached = false;
    private int cachedSelectedPlayerIndex = -1;
    private int cachedSelectedWhiteIndex = -1;
    private int cachedSelectedBlackIndex = -1;

    public GuiMusicOptions(EntityPlayer playerIn)
    {
        this.player = playerIn;
        this.mc = Minecraft.getMinecraft();
        midiVolume = MusicOptionsUtil.getMidiVolume(player);
        muteOption = MusicOptionsUtil.getMuteOption(player);
        midiInit();
        initPlayerList();
    }
    
    @Override
    public void updateScreen()
    {
        updateGuiElments();
        midiUpdate();
        selectedPlayerIndex = this.lst_players.selectedIndex(playerList.indexOf(selectedPlayerList));
        selectedWhiteIndex = this.lst_white.selectedIndex(whiteList.indexOf(selectedWhiteList));
        selectedBlackIndex = this.lst_black.selectedIndex(blackList.indexOf(selectedBlackList));
        super.updateScreen();
    }

    @Override
    public void initGui()
    {
        TITLE = I18n.format("mxtune.gui.musicOptions.title");
        
        this.buttonList.clear();

        for (PlayerLists in : playerList)
        {
            String playerName = in.getPlayerName();
            playerListWidth = Math.max(playerListWidth, getFontRenderer().getStringWidth(playerName) + 10 + 12);
            playerListWidth = Math.max(playerListWidth, getFontRenderer().getStringWidth(playerName) + 5 + this.getFontRenderer().FONT_HEIGHT + 2);
        }
        playerListWidth = whiteListWidth = blackListWidth = Math.min(playerListWidth, 150);

        lst_white = new GuiWhiteList(this, whiteList, whiteListWidth, this.getFontRenderer().FONT_HEIGHT + 2);
        btn_white_to_players = new GuiButtonExt(10, lst_white.getRight() - 20, height - 100 + 4 + 3, 20, 20, ">");
        lst_players = new GuiPlayerList(this, playerList, playerListWidth, this.getFontRenderer().FONT_HEIGHT + 2);
        btn_players_to_white = new GuiButtonExt(11, lst_players.getRight() - playerListWidth, height - 100 + 4 + 3, 20, 20, "<");
        btn_players_to_black = new GuiButtonExt(12, lst_players.getRight() - 20, height - 100 + 4 + 3, 20, 20, ">");
        lst_black = new GuiBlackList(this, blackList, blackListWidth, this.getFontRenderer().FONT_HEIGHT + 2);
        btn_black_to_players = new GuiButtonExt(13, lst_black.getRight() - blackListWidth, height - 100 + 4 + 3, 20, 20, "<");
        
        int y = 30;
        int x = lst_black.getRight() + 10; //(width - 200) / 2;
        int buttonWidth = (width - x - 10);
        btn_muteOption = new GuiButtonExt(0, x, y, buttonWidth, 20, (MusicOptionsUtil.EnumMuteOptions.byMetadata(muteOption).toString()));
        btn_midiVolume = new GuiSliderMX(1, x, y+=25, buttonWidth, 20, I18n.format("mxtune.gui.slider.midiVolume"), midiVolume*100F, 0F, 100F, 1F);
        
        btn_cancel = new GuiButtonExt(2, x, y+=25, buttonWidth, 20, I18n.format("gui.cancel"));
        btn_done = new GuiButtonExt(3, x, y+=25, buttonWidth, 20, I18n.format("gui.done"));
        
        btn_reset = new GuiButtonExt(4, x, y+=25, buttonWidth, 20, I18n.format("mxtune.gui.musicOptions.reset"));
        
//        x = (width - 250) / 2;
//        lbl_desc = new GuiLabel(this.getFontRenderer(), 4, x, y, 250, 100, 0xD3D3D3);
//        lbl_desc.addLine(I18n.format("mxtune.gui.musicOptions.label.description01"));
//        lbl_desc.addLine(I18n.format("mxtune.gui.musicOptions.label.description02"));
//        lbl_desc.addLine(I18n.format("mxtune.gui.musicOptions.label.description03"));
//        lbl_desc.addLine(I18n.format("mxtune.gui.musicOptions.label.description04"));
//        lbl_desc.addLine(I18n.format("mxtune.gui.musicOptions.label.description05"));
        this.buttonList.add(btn_white_to_players);
        this.buttonList.add(btn_players_to_white);
        this.buttonList.add(btn_players_to_black);
        this.buttonList.add(btn_black_to_players);
        
        this.buttonList.add(btn_muteOption);
        this.buttonList.add(btn_midiVolume);
        this.buttonList.add(btn_cancel);
        this.buttonList.add(btn_done);
        this.buttonList.add(btn_reset);
        
        reloadState();
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        
        this.lst_players.elementClicked(this.cachedSelectedPlayerIndex, false);
        this.lst_white.elementClicked(this.cachedSelectedWhiteIndex, false);
        this.lst_black.elementClicked(this.cachedSelectedBlackIndex, false);
        updateButtonState();
    }
    
    private void updateState()
    {      
        this.cachedSelectedPlayerIndex = this.selectedPlayerIndex;
        this.cachedSelectedWhiteIndex = this.selectedWhiteIndex;
        this.cachedSelectedBlackIndex = this.selectedBlackIndex;
        updateButtonState();
        this.isStateCached = true;
    }
    
    private void updateButtonState()
    {
        
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();

        /** draw "TITLE" at the top/right column middle */
        int posX = (this.width - getFontRenderer().getStringWidth(TITLE)) / 2 ;
        int posY = 10;
        getFontRenderer().drawStringWithShadow(TITLE, posX, posY, 0xD3D3D3);
        //lbl_desc.drawLabel(mc, mouseX, mouseY);
        lst_white.drawScreen(mouseX, mouseY, partialTicks);
        lst_players.drawScreen(mouseX, mouseY, partialTicks);
        lst_black.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException
    {
        /** if button is disabled ignore click */
        if (!guibutton.enabled) return;

        /** id 0 = okay; 1 = cancel; 2 = play; 3 = stop */
        switch (guibutton.id)
        {
        case 0:
            /** Increment Mute Option */
            this.muteOption = ((++this.muteOption) % MusicOptionsUtil.EnumMuteOptions.values().length);
            ModLogger.logInfo("muteOption meta: " + muteOption + ", text: " + MusicOptionsUtil.EnumMuteOptions.byMetadata(muteOption).toString() +
                    ", enum: " + MusicOptionsUtil.EnumMuteOptions.byMetadata(muteOption).name());
            btn_muteOption.displayString = MusicOptionsUtil.EnumMuteOptions.byMetadata(muteOption).toString();
          break;

        case 1:
            /** Volume */
            updateGuiElments();
            break;
        case 3:
            /** done */
            this.midiVolume = btn_midiVolume.getValue() / 100;
            sendOptionsToServer(this.midiVolume, this.muteOption);
        case 2:
            /** cancel */
            midiClose();
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        case 4:
            /** reset */
            MMLManager.getInstance().abortAll();
            break;
        default:
        }
        updateState();
    }

    private void updateGuiElments()
    {
        this.midiVolume = btn_midiVolume.getValue() / 100;
        if ((this.midiVolume == 0F) && this.lastMuteOption == -1)
        {
            this.lastMuteOption = this.muteOption;
            this.muteOption = MusicOptionsUtil.EnumMuteOptions.ALL.getMetadata();
            btn_muteOption.displayString = MusicOptionsUtil.EnumMuteOptions.byMetadata(this.muteOption).toString();
            btn_muteOption.enabled = false;
        }
        if ((this.midiVolume != 0F) && this.lastMuteOption != -1)
        {
            btn_muteOption.enabled = true;
            this.muteOption = this.lastMuteOption;
            btn_muteOption.displayString = MusicOptionsUtil.EnumMuteOptions.byMetadata(this.muteOption).toString();
            this.lastMuteOption = -1;
        }   
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        /** capture the ESC key do we close cleanly */
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            actionPerformed((GuiButton) buttonList.get(btn_cancel.id));
        }
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @SuppressWarnings("unused")
    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        
        if (btn_midiVolume.isMouseOver()) {
            updateGuiElments();
        }
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public Minecraft getMinecraftInstance() {return mc;}

    public FontRenderer getFontRenderer() {return mc.fontRendererObj;}
    
    protected void sendOptionsToServer(float midiVolume, int muteOption)
    {
        PacketDispatcher.sendToServer(new MusicOptionsMessage(midiVolume, muteOption));
    }
    
    /** MIDI generator for testing volume level - the John Cage Machine */
    private Synthesizer synth = null;
    MidiChannel[]   channels;
    MidiChannel channel;
    
    private void midiInit()
    {
        try {synth = MidiSystem.getSynthesizer();}
        catch (MidiUnavailableException e) {e.printStackTrace();}
        try {synth.open();}
        catch (MidiUnavailableException e) {e.printStackTrace();}
        channels = synth.getChannels();
        channel = channels[0];
        channel.programChange(2);
        setWait(2);
    }
    
    private void midiUpdate()
    {
        overControl(btn_midiVolume.isMouseOver());
        nextNote();
    }
    
    private void midiClose() {synth.close();}

    private int tickLen = 1;
    private int noteMidi1, noteMidi2, noteMidi3, noteMidi4, noteMidi5, run1, run2;
    private long tick;
    private long tock;
    private boolean waiting;
    private int noteActive = 0;
    private boolean noteOff = true;
    private double cyc = 0D;
    
    public void nextTick()
    {
        if (waiting)
        {
            if ((++tick % tickLen) == 0)
            {
                if (tock-- <= 0)
                {
                    waiting = false;
                }
            }
        }
    }

    public void setWait(int ms)
    {
        if (ms <= 0) return;
        tock = ms;
        tick = 0;
        waiting = true;
    }

    public void overControl(boolean hasHoover) {if (hasHoover) noteActive = 1; else noteActive = 0;}
    
    public void nextNote() {nextCmd(); cyc =  cyc + 0.16D;}

    public void nextCmd()
    {
        nextTick();

        if (waiting) return;   
        
        if (noteOff) {
            noteMidi1 = (int) Math.round(((((Math.sin(cyc)+1.D)/2D) *30.0D)+50.0D));
            noteMidi2 = noteMidi1 + ((int) Math.round((Math.random()*1.D)) ) + 4;
            //ModLogger.logInfo("diff: " + (noteMidi2 - noteMidi1));
            noteMidi3 = noteMidi1 + 9;
            noteMidi4 = ((run1+=3) %24) + 60;
            noteMidi5 = ((run2-=4) %10) + 40;
            int scaledVolume =  scaleVolume(127*noteActive);
            channel.noteOn(noteMidi1, scaledVolume);
            channel.noteOn(noteMidi2, scaledVolume);
            channel.noteOn(noteMidi3, scaledVolume);
            channel.noteOn(noteMidi4, scaledVolume);
            channel.noteOn(noteMidi5, scaledVolume);
            setWait(2);
            noteOff=false;
        } else {
            channel.noteOff(noteMidi1);
            channel.noteOff(noteMidi2);
            channel.noteOff(noteMidi3);
            channel.noteOff(noteMidi4);
            channel.noteOff(noteMidi5);
            setWait(1);
            noteOff = true;
        }
    }
    
    private int scaleVolume(int volumeIn)
    {
        int temp = (int) Math.round((float)volumeIn * (Math.exp(this.midiVolume)-1)/(Math.E-1));
        //ModLogger.logInfo("volumeIn: " + volumeIn + ", midiVolume: " + midiVolume + ", scaled: " + temp);
        return temp;
    }

    /** Lists - Players, Whitelist, Blacklist */
 // Notes: For saving to disk use UUIDs. For client-server communication use getEntityID. Done.
 // UUID does not work on the client.
    
    public static class GuiPlayerList extends GuiScrollingList
    {
        GuiMusicOptions parent;
        private final ArrayList<PlayerLists> playerLists;
        
        public GuiPlayerList(GuiMusicOptions parent, ArrayList<PlayerLists> playerLists,  int listWidth, int slotHeight)
        {
            super(parent.getMinecraftInstance(), listWidth, parent.height - 32 - 100 + 4, 32, parent.height - 100 + 4, parent.lst_white.getRight()+ 5, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.playerLists = playerLists;
        }
        
        int selectedIndex(int s) {return selectedIndex = s;}

        public int getRight() {return right;}
        
        @Override
        protected int getSize() {return this.playerLists.size();}

        @Override
        protected void elementClicked(int index, boolean doubleClick) {this.parent.selectPlayerIndex(index);}

        @Override
        protected boolean isSelected(int index) {return this.parent.playerIndexSelected(index);}

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
            String ins = this.playerLists.get(slotIdx).getPlayerName();

            String s = font.trimStringToWidth(ins, listWidth - 10);
            /** light Blue */
            font.drawStringWithShadow(s, this.left + 3, slotTop, 0xADD8E6);
            drawPing(this.parent ,this.left + 3, listWidth - 10, slotTop, this.playerLists.get(slotIdx));
        }
    }
    
    /** element was clicked */
    public void selectPlayerIndex(int index)
    {
        if (index == this.selectedPlayerIndex) return;
        this.selectedPlayerIndex = index;
        this.selectedPlayerList = (index >= 0 && index <= playerList.size()) ? playerList.get(selectedPlayerIndex) : null;
        updateState();
    }

    public boolean playerIndexSelected(int index) {return index == selectedPlayerIndex;}

    /** The White List */
    public static class GuiWhiteList extends GuiScrollingList
    {
        GuiMusicOptions parent;
        private final ArrayList<PlayerLists> whiteLists;
        
        public GuiWhiteList(GuiMusicOptions parent, ArrayList<PlayerLists> whiteLists,  int listWidth, int slotHeight)
        {
            super(parent.getMinecraftInstance(), listWidth, parent.height - 32 - 100 + 4, 32, parent.height - 100 + 4, 5, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.whiteLists = whiteLists;
        }
        
        int selectedIndex(int s) {return selectedIndex = s;}

        public int getRight() {return right;}
        
        @Override
        protected int getSize() {return this.whiteLists.size();}

        @Override
        protected void elementClicked(int index, boolean doubleClick) {this.parent.selectWhiteIndex(index);}

        @Override
        protected boolean isSelected(int index) {return this.parent.whiteIndexSelected(index);}

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
            String ins = this.whiteLists.get(slotIdx).getPlayerName();

            String s = font.trimStringToWidth(ins, listWidth - 10);
            /** light Blue */
            font.drawStringWithShadow(s, this.left + 3, slotTop, 0xADD8E6);
            drawPing(this.parent ,this.left + 3, listWidth - 10, slotTop, this.whiteLists.get(slotIdx));
        }
    }
    
    /** element was clicked */
    public void selectWhiteIndex(int index)
    {
        if (index == this.selectedWhiteIndex) return;
        this.selectedWhiteIndex = index;
        this.selectedWhiteList = (index >= 0 && index <= whiteList.size()) ? whiteList.get(selectedWhiteIndex) : null;
        updateState();
    }

    public boolean whiteIndexSelected(int index) {return index == selectedWhiteIndex;}

    /** The Black List */
    public static class GuiBlackList extends GuiScrollingList
    {
        GuiMusicOptions parent;
        private final ArrayList<PlayerLists> blackLists;
        
        public GuiBlackList(GuiMusicOptions parent, ArrayList<PlayerLists> blackLists,  int listWidth, int slotHeight)
        {
            super(parent.getMinecraftInstance(), listWidth, parent.height - 32 - 100 + 4, 32, parent.height - 100 + 4, parent.lst_players.getRight() + 5, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.blackLists = blackLists;
        }
        
        int selectedIndex(int s) {return selectedIndex = s;}

        public int getRight() {return right;}
        
        @Override
        protected int getSize() {return this.blackLists.size();}

        @Override
        protected void elementClicked(int index, boolean doubleClick) {this.parent.selectBlackIndex(index);}

        @Override
        protected boolean isSelected(int index) {return this.parent.blackIndexSelected(index);}

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
            String ins = this.blackLists.get(slotIdx).getPlayerName();

            String s = font.trimStringToWidth(ins, listWidth - 10);
            /** light Blue */
            font.drawStringWithShadow(s, this.left + 3, slotTop, 0xADD8E6);
            drawPing(this.parent ,this.left + 3, listWidth - 10, slotTop, this.blackLists.get(slotIdx));
        }
    }
    
    /** element was clicked */
    public void selectBlackIndex(int index)
    {
        if (index == this.selectedBlackIndex) return;
        this.selectedBlackIndex = index;
        this.selectedBlackList = (index >= 0 && index <= blackList.size()) ? blackList.get(selectedBlackIndex) : null;
        updateState();
    }

    public boolean blackIndexSelected(int index) {return index == selectedBlackIndex;}

    
    private static void drawPing(GuiMusicOptions parent, int x, int sWidth, int y, PlayerLists playerInfo)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        parent.getMinecraftInstance().getTextureManager().bindTexture(ICONS);
        int j;

        if (playerInfo.isOnline())
        {
            j = 0;
        }
        else
        {
            j = 5;
        }

        parent.zLevel += 100.0F;
        parent.drawTexturedModalRect(sWidth + x - 11, y, 0, 176 + j * 8, 10, 8);
        parent.zLevel -= 100.0F;
    }
    
    static class PlayerComparator implements Comparator<NetworkPlayerInfo>
        {
            private PlayerComparator() {}

            public int compare(NetworkPlayerInfo p_compare_1_, NetworkPlayerInfo p_compare_2_)
            {
                return ComparisonChain.start().compare(p_compare_1_.getGameProfile().getName(), p_compare_2_.getGameProfile().getName()).result();
            }
        }

    private static final Ordering<NetworkPlayerInfo> ENTRY_ORDERING = Ordering.from(new GuiMusicOptions.PlayerComparator());
    
    private void initPlayerList()
    {
        playerList = new ArrayList<PlayerLists>();
        whiteList = new ArrayList<PlayerLists>();
        blackList = new ArrayList<PlayerLists>();
        
        PlayerLists pList;
        if (this.mc.isIntegratedServerRunning() && this.mc.thePlayer.connection.getPlayerInfoMap().size() <= 1)
            {
                pList = new PlayerLists();
                pList.setPlayerName(this.getMinecraftInstance().thePlayer.getDisplayName().getUnformattedText());
                pList.setOnline(true);
                playerList.add(pList);
                whiteList.add(pList);
                blackList.add(pList);
                return;
            }
        NetHandlerPlayClient nethandlerplayclient = this.getMinecraftInstance().thePlayer.connection;
        List<NetworkPlayerInfo> list = ENTRY_ORDERING.<NetworkPlayerInfo>sortedCopy(nethandlerplayclient.getPlayerInfoMap());
        for (NetworkPlayerInfo networkplayerinfo : list)
        {
            pList = new PlayerLists();
            pList.setPlayerName(getPlayerName(networkplayerinfo));
            pList.setOnline(true);
            playerList.add(pList);
        }
        for (int i = 0; i< 30; i++) 
        {
        pList = new PlayerLists();
        pList.setPlayerName("Melodian" + i);
        pList.setOnline(false);
        playerList.add(pList);
        whiteList.add(pList);

        pList = new PlayerLists();
        pList.setPlayerName("0123456789ABCDEF");
        pList.setOnline(false);
        playerList.add(pList);
        blackList.add(pList);

        pList = new PlayerLists();
        pList.setPlayerName("ABC" + i);
        pList.setOnline(false);
        playerList.add(pList);
        }

    }

    public String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn)
    {
        return networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
    }

}
