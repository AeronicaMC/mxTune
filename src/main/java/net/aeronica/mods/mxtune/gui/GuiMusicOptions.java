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

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.MusicOptionsMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.options.PlayerLists;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiMusicOptions extends GuiScreen
{
    public static final int GUI_ID = 8;
    String TITLE = I18n.format("mxtune.gui.musicOptions.title");
    String LABEL_WHITELIST = I18n.format("mxtune.gui.musicOptions.label.whitelist");
    String LABEL_PLAYERS  = I18n.format("mxtune.gui.musicOptions.label.players");
    String LABEL_BLACKLIST  = I18n.format("mxtune.gui.musicOptions.label.blacklist");
    String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    String BUTTON_ADJ_HUD = I18n.format("mxtune.gui.musicOptions.adjHud");

    GuiButtonExt buttonMuteOption;
    GuiButtonExt buttonCancel;
    GuiButtonExt buttonDone;
    GuiButtonExt buttonAdjHud;
    GuiButtonExt buttonWhiteToPlayers;
    GuiButtonExt buttonPlayersToWhite;
    GuiButtonExt buttonBlackToPlayers;
    GuiButtonExt buttonPlayersToBlack;
    GuiPlayerList listBoxPlayers;
    GuiWhiteList listBoxWhite;
    GuiBlackList listBoxBlack;
    
    EntityPlayer player;
    int muteOption;
    boolean midiUnavailable;

    /** PlayerList */
    List<PlayerLists> playerList; 
    int selectedPlayerIndex = -1;
    PlayerLists selectedPlayerList = new PlayerLists();
    int playerListWidth;

    /** WhiteList */
    List<PlayerLists> whiteList; 
    int selectedWhiteIndex = -1;
    PlayerLists selectedWhiteList = new PlayerLists();
    int whiteListWidth;
    
    /** WhiteList */
    List<PlayerLists> blackList; 
    int selectedBlackIndex = -1;
    PlayerLists selectedBlackList = new PlayerLists();
    int blackListWidth;

    /** Cached State for when the GUI is resized */
    boolean isStateCached = false;
    int cachedSelectedPlayerIndex = -1;
    int cachedSelectedWhiteIndex = -1;
    int cachedSelectedBlackIndex = -1;

    public GuiMusicOptions(EntityPlayer playerIn)
    {
        this.player = playerIn;
        this.mc = Minecraft.getMinecraft();
        muteOption = MusicOptionsUtil.getMuteOption(player);
        midiUnavailable = MIDISystemUtil.midiUnavailable();
        initPlayerList();
    }
    
    @Override
    public void updateScreen()
    {
        updateGuiElments();
        selectedPlayerIndex = this.listBoxPlayers.selectedIndex(playerList.indexOf(selectedPlayerList));
        selectedWhiteIndex = this.listBoxWhite.selectedIndex(whiteList.indexOf(selectedWhiteList));
        selectedBlackIndex = this.listBoxBlack.selectedIndex(blackList.indexOf(selectedBlackList));
        super.updateScreen();
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();
        playerListWidth = whiteListWidth = blackListWidth = getFontRenderer().getStringWidth("MWMWMWMWMWMWMWMW") + 10 + 12;
        Keyboard.enableRepeatEvents(true);
        
        int y = (height - 100) / 2;
        int x = (width - (playerListWidth + whiteListWidth + blackListWidth + 24 + 24)) / 2;
        listBoxWhite = new GuiWhiteList(this, whiteList, x, whiteListWidth, this.getFontRenderer().FONT_HEIGHT + 2);
        buttonWhiteToPlayers = new GuiButtonExt(11, listBoxWhite.getRight()+2, y, 20, 20, ">");
        buttonPlayersToWhite = new GuiButtonExt(12, listBoxWhite.getRight()+2, y + 25, 20, 20, "<");
        listBoxPlayers = new GuiPlayerList(this, playerList, playerListWidth, this.getFontRenderer().FONT_HEIGHT + 2);
        buttonPlayersToBlack = new GuiButtonExt(13, listBoxPlayers.getRight()+2, y, 20, 20, ">");
        buttonBlackToPlayers = new GuiButtonExt(14, listBoxPlayers.getRight()+2, y + 25, 20, 20, "<");
        listBoxBlack = new GuiBlackList(this, blackList, blackListWidth, this.getFontRenderer().FONT_HEIGHT + 2);

        int buttonWidth = 100;
        y = height - 100 + 4 + 5;
        x = listBoxBlack.getRight() - buttonWidth + 2;
        buttonDone = new GuiButtonExt(3, x, y, buttonWidth, 20, I18n.format("gui.done"));
        buttonCancel = new GuiButtonExt(2, x, y+22, buttonWidth, 20, I18n.format("gui.cancel"));

        y = height - 100 + 4 + 5;
        x = listBoxWhite.getRight() - whiteListWidth - 2;
        buttonWidth = 225;
        buttonMuteOption = new GuiButtonExt(0, x, y, buttonWidth, 20, (MusicOptionsUtil.EnumMuteOptions.byMetadata(muteOption).toString()));
        buttonAdjHud = new GuiButtonExt(4, x, y+=22, buttonWidth, 20, BUTTON_ADJ_HUD);
        
        this.buttonList.add(buttonWhiteToPlayers);
        this.buttonList.add(buttonPlayersToWhite);
        this.buttonList.add(buttonPlayersToBlack);
        this.buttonList.add(buttonBlackToPlayers);
        
        this.buttonList.add(buttonMuteOption);
        this.buttonList.add(buttonCancel);
        this.buttonList.add(buttonDone);
        this.buttonList.add(buttonAdjHud);
        
        reloadState();
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        
        this.listBoxPlayers.elementClicked(this.cachedSelectedPlayerIndex, false);
        this.listBoxWhite.elementClicked(this.cachedSelectedWhiteIndex, false);
        this.listBoxBlack.elementClicked(this.cachedSelectedBlackIndex, false);
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
       /* unused */ 
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
        /** draw "TITLE" at the top/right column middle */
        int posX = (this.width - getFontRenderer().getStringWidth(localTITLE)) / 2 ;
        int posY = 10;
        getFontRenderer().drawStringWithShadow(localTITLE, posX, posY, 0xD3D3D3);
        
        /** draw list names - Whitelist */
        posX = (this.listBoxWhite.getRight() - whiteListWidth / 2) - (getFontRenderer().getStringWidth(LABEL_WHITELIST) / 2);
        posY = 20;
        getFontRenderer().drawStringWithShadow(LABEL_WHITELIST, posX, posY, 0xD3D3D3);

        /** Players list */
        posX = this.listBoxPlayers.getRight() - playerListWidth / 2 - (getFontRenderer().getStringWidth(LABEL_PLAYERS) / 2);
        posY = 20;
        getFontRenderer().drawStringWithShadow(LABEL_PLAYERS, posX, posY, 0xD3D3D3);

        /** Blacklist */
        posX = this.listBoxBlack.getRight() - blackListWidth / 2 - (getFontRenderer().getStringWidth(LABEL_BLACKLIST) / 2);
        posY = 20;
        getFontRenderer().drawStringWithShadow(LABEL_BLACKLIST, posX, posY, 0xD3D3D3);

        
        listBoxWhite.drawScreen(mouseX, mouseY, partialTicks);
        listBoxPlayers.drawScreen(mouseX, mouseY, partialTicks);
        listBoxBlack.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException
    {
        PlayerLists t;
        /** if button is disabled ignore click */
        if (!guibutton.enabled) return;

        /** id 0 = okay; 1 = cancel; 2 = play; 3 = stop */
        switch (guibutton.id)
        {
        case 0:
            /** Increment Mute Option */
            this.muteOption = ((++this.muteOption) % MusicOptionsUtil.EnumMuteOptions.values().length);
            buttonMuteOption.displayString = MusicOptionsUtil.EnumMuteOptions.byMetadata(muteOption).toString();
          break;

        case 1:
            /** Volume */
            updateGuiElments();
            break;
        case 3:
            /** done */
            sendOptionsToServer(this.muteOption);
            cleanup();
            break;
        case 2:
            /** cancel */
            cleanup();
            break;
        case 4:
            /** Adjust HUD */
            sendOptionsToServer(this.muteOption);
            this.mc.displayGuiScreen(new GuiHudAdjust());
            break;
        case 11:
            if (this.selectedWhiteIndex == -1 || this.selectedWhiteIndex > this.whiteList.size()) break;
            t = this.whiteList.get(this.selectedWhiteIndex);
            this.whiteList.remove(this.selectedWhiteIndex);
            this.playerList.add(t);
            break;            
        case 12:
            if (this.selectedPlayerIndex == -1 || this.selectedPlayerIndex > this.playerList.size()) break;
            t = this.playerList.get(this.selectedPlayerIndex);
            this.playerList.remove(this.selectedPlayerIndex);
            this.whiteList.add(t);
            break;
        case 13:
            if (this.selectedPlayerIndex == -1 || this.selectedPlayerIndex > this.playerList.size()) break;
            t = this.playerList.get(this.selectedPlayerIndex);
            this.playerList.remove(this.selectedPlayerIndex);
            this.blackList.add(t);
            break;
        case 14:
            if (this.selectedBlackIndex == -1 || this.selectedBlackIndex > this.blackList.size()) break;
            t = this.blackList.get(this.selectedBlackIndex);
            this.blackList.remove(this.selectedBlackIndex);
            this.playerList.add(t);
            break;            

        default:
        }
        sortLists();
        updateState();
    }

    private void cleanup()
    {
        mc.displayGuiScreen(null);
        mc.setIngameFocus();
    }
    
    private void updateGuiElments()
    {
        /* unused */
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        /** capture the ESC key do we close cleanly */
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            this.actionPerformed((GuiButton) buttonList.get(buttonCancel.id));
        }
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    public Minecraft getMinecraftInstance() {return mc;}

    public FontRenderer getFontRenderer() {return mc.fontRendererObj;}
    
    protected void sendOptionsToServer(int muteOption)
    {
        PacketDispatcher.sendToServer(new MusicOptionsMessage(muteOption, blackList, whiteList));
    }
    

    /** Lists - Players, Whitelist, Blacklist */
    // Notes: For saving to disk use UUIDs. For client-server communication use getEntityID. Done.
    // UUID does not work on the client.
    
    public static class GuiPlayerList extends GuiScrollingList
    {
        GuiMusicOptions parent;
        private final List<PlayerLists> playerLists;
        
        public GuiPlayerList(GuiMusicOptions parent, List<PlayerLists> playerLists,  int listWidth, int slotHeight)
        {
            super(parent.getMinecraftInstance(), listWidth, parent.height - 32 - 100 + 4, 32, parent.height - 100 + 4, parent.listBoxWhite.getRight()+ 24, slotHeight, parent.width, parent.height);
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
            font.drawStringWithShadow(s, (float)this.left + 3, slotTop, 0xADD8E6);
            drawPing(this.parent ,this.left + 3, listWidth - 10, slotTop, this.playerLists.get(slotIdx));
        }
    }
    
    /** element was clicked */
    public void selectPlayerIndex(int index)
    {
        if (index == this.selectedPlayerIndex) return;
        this.selectedPlayerIndex = index;
        this.selectedPlayerList = (index >= 0 && index <= playerList.size()) ? playerList.get(selectedPlayerIndex) : null;
    }

    public boolean playerIndexSelected(int index) {return index == selectedPlayerIndex;}

    /** The White List */
    public static class GuiWhiteList extends GuiScrollingList
    {
        GuiMusicOptions parent;
        private final List<PlayerLists> whiteLists;
        
        public GuiWhiteList(GuiMusicOptions parent, List<PlayerLists> whiteLists, int left, int listWidth, int slotHeight)
        {
            super(parent.getMinecraftInstance(), listWidth, parent.height - 32 - 100 + 4, 32, parent.height - 100 + 4, left, slotHeight, parent.width, parent.height);
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
            font.drawStringWithShadow(s, (float)this.left + 3, slotTop, 0xADD8E6);
            drawPing(this.parent ,this.left + 3, listWidth - 10, slotTop, this.whiteLists.get(slotIdx));
        }
    }
    
    /** element was clicked */
    public void selectWhiteIndex(int index)
    {
        if (index == this.selectedWhiteIndex) return;
        this.selectedWhiteIndex = index;
        this.selectedWhiteList = (index >= 0 && index <= whiteList.size()) ? whiteList.get(selectedWhiteIndex) : null;
    }

    public boolean whiteIndexSelected(int index) {return index == selectedWhiteIndex;}

    /** The Black List */
    public static class GuiBlackList extends GuiScrollingList
    {
        GuiMusicOptions parent;
        private final List<PlayerLists> blackLists;
        
        public GuiBlackList(GuiMusicOptions parent, List<PlayerLists> blackLists,  int listWidth, int slotHeight)
        {
            super(parent.getMinecraftInstance(), listWidth, parent.height - 32 - 100 + 4, 32, parent.height - 100 + 4, parent.listBoxPlayers.getRight() + 24, slotHeight, parent.width, parent.height);
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
            font.drawStringWithShadow(s, (float)this.left + 3, slotTop, 0xADD8E6);
            drawPing(this.parent ,this.left + 3, listWidth - 10, slotTop, this.blackLists.get(slotIdx));
        }
    }
    
    /** element was clicked */
    public void selectBlackIndex(int index)
    {
        if (index == this.selectedBlackIndex) return;
        this.selectedBlackIndex = index;
        this.selectedBlackList = (index >= 0 && index <= blackList.size()) ? blackList.get(selectedBlackIndex) : null;
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
            @Override
            public int compare(NetworkPlayerInfo p_compare_1_, NetworkPlayerInfo p_compare_2_)
            {
                return ComparisonChain.start().compare(p_compare_1_.getGameProfile().getName(), p_compare_2_.getGameProfile().getName()).result();
            }
        }

    private static final Ordering<NetworkPlayerInfo> ENTRY_ORDERING = Ordering.from(new GuiMusicOptions.PlayerComparator());
    
    static class PlayerListsComparator implements Comparator<PlayerLists>
    {
        private PlayerListsComparator() {}        
        @Override
        public int compare(PlayerLists o1, PlayerLists o2)
        {
            return ComparisonChain.start().compareTrueFirst(o1.isOnline(), o2.isOnline()).compare(o1.getPlayerName(), o2.getPlayerName()).result();
        }   
    }
    
    private static final Ordering<PlayerLists> LIST_ORDERING = Ordering.from(new GuiMusicOptions.PlayerListsComparator());
    
    private void initPlayerList()
    {
        playerList = new ArrayList<PlayerLists>();
        whiteList = MusicOptionsUtil.getWhiteList(player);
        blackList = MusicOptionsUtil.getBlackList(player);
        
        PlayerLists pList;
        if (!(this.mc.isIntegratedServerRunning() && this.mc.player.connection.getPlayerInfoMap().size() <= 1))
        {
            NetHandlerPlayClient nethandlerplayclient = this.getMinecraftInstance().player.connection;
            List<NetworkPlayerInfo> list = ENTRY_ORDERING.<NetworkPlayerInfo> sortedCopy(nethandlerplayclient.getPlayerInfoMap());
            for (NetworkPlayerInfo networkplayerinfo : list)
            {
                pList = new PlayerLists();
                pList.setPlayerName(getPlayerName(networkplayerinfo));
                pList.setOnline(true);
                pList.setUuid(networkplayerinfo.getGameProfile().getId());
                if (!pList.getUuid().equals(player.getUniqueID())) playerList.add(pList);
            }
        }
        for (PlayerLists bList : blackList)
        {
            for (int i=0; i < playerList.size(); i++)
            {
                PlayerLists p = playerList.get(i);
                if (p.getUuid().equals(bList.getUuid()))
                {
                    bList.setOnline(true);
                    bList.setPlayerName(p.getPlayerName());
                    playerList.remove(i);                    
                }
            }
        }
        for (PlayerLists wList: whiteList)
        {
            for (int i=0; i < playerList.size(); i++)
            {
                PlayerLists p = playerList.get(i);
                if (p.getUuid().equals(wList.getUuid()))
                {
                    wList.setOnline(true);
                    wList.setPlayerName(p.getPlayerName());
                    playerList.remove(i);                    
                }
            }
        }
        /** Reorder the lists */
        sortLists();
    }

    /** A brute force way to keep the lists sorted and not worry about thread safety */
    private void sortLists()
    {
        List<PlayerLists> tempWhiteList = (List<PlayerLists>) LIST_ORDERING.<PlayerLists> sortedCopy(whiteList);
        whiteList.clear();
        for (PlayerLists w: tempWhiteList) {whiteList.add(w);}
        
        List<PlayerLists> tempBlackList = (List<PlayerLists>) LIST_ORDERING.<PlayerLists> sortedCopy(blackList);
        blackList.clear();
        for (PlayerLists w: tempBlackList) {blackList.add(w);}

        List<PlayerLists> tempPlayerList = (List<PlayerLists>) LIST_ORDERING.<PlayerLists> sortedCopy(playerList);
        playerList.clear();
        for (PlayerLists w: tempPlayerList) {playerList.add(w);}
    }
    
    public String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn)
    {
        return networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
    }
}
