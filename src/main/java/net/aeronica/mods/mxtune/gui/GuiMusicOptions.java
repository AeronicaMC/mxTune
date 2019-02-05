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
package net.aeronica.mods.mxtune.gui;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.MusicOptionsMessage;
import net.aeronica.mods.mxtune.options.ClassifiedPlayer;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
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
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GuiMusicOptions extends GuiScreen
{
    public static final int GUI_ID = 8;
    private GuiScreen guiScreenOld;
    private static final String TITLE = I18n.format("mxtune.gui.musicOptions.title");
    private static final String LABEL_WHITELIST = I18n.format("mxtune.gui.musicOptions.label.whitelist");
    private static final String LABEL_PLAYERS  = I18n.format("mxtune.gui.musicOptions.label.players");
    private static final String LABEL_BLACKLIST  = I18n.format("mxtune.gui.musicOptions.label.blacklist");
    private static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    private static final String BUTTON_ADJ_HUD = I18n.format("mxtune.gui.musicOptions.adjHud");

    private GuiButtonExt buttonMuteOption;
    private GuiButtonExt buttonCancel;
    private GuiNetPlayerList listBoxPlayers;
    private GuiWhiteList listBoxWhite;
    private GuiBlackList listBoxBlack;

    private EntityPlayer player;
    private int muteOption;
    private boolean midiUnavailable;

    /* PlayerList */
    private List<ClassifiedPlayer> netPlayerList;
    private int selectedPlayerIndex = -1;
    private ClassifiedPlayer selectedNetPlayer = new ClassifiedPlayer();
    private int playerListWidth;

    /* WhiteList */
    private List<ClassifiedPlayer> whiteList;
    private int selectedWhiteIndex = -1;
    private ClassifiedPlayer selectedWhitePlayer = new ClassifiedPlayer();
    private int whiteListWidth;
    
    /* WhiteList */
    private List<ClassifiedPlayer> blackList;
    private int selectedBlackIndex = -1;
    private ClassifiedPlayer selectedBlackPlayer = new ClassifiedPlayer();
    private int blackListWidth;

    /* Cached State for when the GUI is resized */
    private boolean isStateCached = false;
    private int cachedSelectedPlayerIndex = -1;
    private int cachedSelectedWhiteIndex = -1;
    private int cachedSelectedBlackIndex = -1;

    public GuiMusicOptions(@Nullable GuiScreen guiScreenIn)
    {
        this.mc = Minecraft.getMinecraft();
        player = mc.player;
        this.guiScreenOld = guiScreenIn;
        muteOption = MusicOptionsUtil.getMuteOption(player);
        midiUnavailable = MIDISystemUtil.midiUnavailable();
        initPlayerLists();
    }
    
    @Override
    public void updateScreen()
    {
        selectedPlayerIndex = this.listBoxPlayers.selectedIndex(netPlayerList.indexOf(selectedNetPlayer));
        selectedWhiteIndex = this.listBoxWhite.selectedIndex(whiteList.indexOf(selectedWhitePlayer));
        selectedBlackIndex = this.listBoxBlack.selectedIndex(blackList.indexOf(selectedBlackPlayer));
        super.updateScreen();
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();
        playerListWidth = whiteListWidth = blackListWidth = mc.fontRenderer.getStringWidth("MWMWMWMWMWMWMWMW") + 10 + 12;
        
        int y = (height - 100) / 2;
        int x = (width - (playerListWidth + whiteListWidth + blackListWidth + 24 + 24)) / 2;
        listBoxWhite = new GuiWhiteList(this, whiteList, x, whiteListWidth, mc.fontRenderer.FONT_HEIGHT + 2);
        GuiButtonExt buttonWhiteToPlayers = new GuiButtonExt(11, listBoxWhite.getRight() + 2, y, 20, 20, ">");
        GuiButtonExt buttonPlayersToWhite = new GuiButtonExt(12, listBoxWhite.getRight() + 2, y + 25, 20, 20, "<");
        listBoxPlayers = new GuiNetPlayerList(this, netPlayerList, playerListWidth, mc.fontRenderer.FONT_HEIGHT + 2);
        GuiButtonExt buttonPlayersToBlack = new GuiButtonExt(13, listBoxPlayers.getRight() + 2, y, 20, 20, ">");
        GuiButtonExt buttonBlackToPlayers = new GuiButtonExt(14, listBoxPlayers.getRight() + 2, y + 25, 20, 20, "<");
        listBoxBlack = new GuiBlackList(this, blackList, blackListWidth, mc.fontRenderer.FONT_HEIGHT + 2);

        int buttonWidth = 100;
        y = height - 100 + 4 + 5;
        x = listBoxBlack.getRight() - buttonWidth + 2;
        GuiButtonExt buttonDone = new GuiButtonExt(3, x, y, buttonWidth, 20, I18n.format("gui.done"));
        buttonCancel = new GuiButtonExt(2, x, y+22, buttonWidth, 20, I18n.format("gui.cancel"));

        y = height - 100 + 4 + 5;
        x = listBoxWhite.getRight() - whiteListWidth - 2;
        buttonWidth = 225;
        buttonMuteOption = new GuiButtonExt(0, x, y, buttonWidth, 20, (MusicOptionsUtil.EnumMuteOptions.byIndex(muteOption).toString()));
        y += 22;
        GuiButtonExt buttonAdjHud = new GuiButtonExt(4, x, y, buttonWidth, 20, BUTTON_ADJ_HUD);
        
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
    }
    
    private void updateState()
    {      
        this.cachedSelectedPlayerIndex = this.selectedPlayerIndex;
        this.cachedSelectedWhiteIndex = this.selectedWhiteIndex;
        this.cachedSelectedBlackIndex = this.selectedBlackIndex;
        this.isStateCached = true;
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
        int posX = (this.width - mc.fontRenderer.getStringWidth(localTITLE)) / 2 ;
        int posY = 10;
        mc.fontRenderer.drawStringWithShadow(localTITLE, posX, posY, 0xD3D3D3);
        
        /* draw list names - Whitelist */
        posX = (this.listBoxWhite.getRight() - whiteListWidth / 2) - (mc.fontRenderer.getStringWidth(LABEL_WHITELIST) / 2);
        posY = 20;
        mc.fontRenderer.drawStringWithShadow(LABEL_WHITELIST, posX, posY, 0xD3D3D3);

        /* Players list */
        posX = this.listBoxPlayers.getRight() - playerListWidth / 2 - (mc.fontRenderer.getStringWidth(LABEL_PLAYERS) / 2);
        posY = 20;
        mc.fontRenderer.drawStringWithShadow(LABEL_PLAYERS, posX, posY, 0xD3D3D3);

        /* Blacklist */
        posX = this.listBoxBlack.getRight() - blackListWidth / 2 - (mc.fontRenderer.getStringWidth(LABEL_BLACKLIST) / 2);
        posY = 20;
        mc.fontRenderer.drawStringWithShadow(LABEL_BLACKLIST, posX, posY, 0xD3D3D3);

        
        listBoxWhite.drawScreen(mouseX, mouseY, partialTicks);
        listBoxPlayers.drawScreen(mouseX, mouseY, partialTicks);
        listBoxBlack.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        ClassifiedPlayer classifiedPlayerRef;

        switch (guibutton.id)
        {
        case 0:
            /* Increment Mute Option */
            this.muteOption = ((++this.muteOption) % MusicOptionsUtil.EnumMuteOptions.values().length);
            buttonMuteOption.displayString = MusicOptionsUtil.EnumMuteOptions.byIndex(muteOption).toString();
          break;

        case 1:
            /* Volume */
            break;
        case 3:
            /* done */
            sendOptionsToServer(this.muteOption);
            mc.displayGuiScreen(guiScreenOld);
            break;
        case 2:
            /* cancel */
            mc.displayGuiScreen(guiScreenOld);
            break;
        case 4:
            /* Adjust HUD */
            sendOptionsToServer(this.muteOption);
            this.mc.displayGuiScreen(new GuiHudAdjust(this));
            break;
        case 11:
            if (this.selectedWhiteIndex == -1 || this.selectedWhiteIndex > this.whiteList.size()) break;
            classifiedPlayerRef = this.whiteList.get(this.selectedWhiteIndex);
            this.whiteList.remove(this.selectedWhiteIndex);
            this.netPlayerList.add(classifiedPlayerRef);
            break;            
        case 12:
            if (this.selectedPlayerIndex == -1 || this.selectedPlayerIndex > this.netPlayerList.size()) break;
            classifiedPlayerRef = this.netPlayerList.get(this.selectedPlayerIndex);
            this.netPlayerList.remove(this.selectedPlayerIndex);
            this.whiteList.add(classifiedPlayerRef);
            break;
        case 13:
            if (this.selectedPlayerIndex == -1 || this.selectedPlayerIndex > this.netPlayerList.size()) break;
            classifiedPlayerRef = this.netPlayerList.get(this.selectedPlayerIndex);
            this.netPlayerList.remove(this.selectedPlayerIndex);
            this.blackList.add(classifiedPlayerRef);
            break;
        case 14:
            if (this.selectedBlackIndex == -1 || this.selectedBlackIndex > this.blackList.size()) break;
            classifiedPlayerRef = this.blackList.get(this.selectedBlackIndex);
            this.blackList.remove(this.selectedBlackIndex);
            this.netPlayerList.add(classifiedPlayerRef);
            break;            

        default:
        }
        sortLists();
        updateState();
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        /* capture the ESC key do we close cleanly */
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            this.actionPerformed(buttonCancel);
            return;
        }
        updateState();
        super.keyTyped(typedChar, keyCode);
    }
    
    private void sendOptionsToServer(int muteOption)
    {
        PacketDispatcher.sendToServer(new MusicOptionsMessage(muteOption, blackList, whiteList));
    }

    /* Lists - Players, Whitelist, Blacklist */
    
    public static class GuiNetPlayerList extends GuiScrollingList
    {
        GuiMusicOptions parent;
        private final List<ClassifiedPlayer> playerLists;
        
        GuiNetPlayerList(GuiMusicOptions parent, List<ClassifiedPlayer> playerLists, int listWidth, int slotHeight)
        {
            super(parent.mc, listWidth, parent.height - 32 - 100 + 4, 32, parent.height - 100 + 4, parent.listBoxWhite.getRight()+ 24, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.playerLists = playerLists;
        }
        
        int selectedIndex(int s)
        {
            selectedIndex = s;
            return selectedIndex;
        }

        public int getRight() {return right;}
        
        @Override
        protected int getSize() {return this.playerLists.size();}

        @Override
        protected void elementClicked(int index, boolean doubleClick)
        {
            if (index == parent.selectedPlayerIndex) return;
            parent.selectedPlayerIndex = index;
            parent.selectedNetPlayer = (index >= 0 && index <= parent.netPlayerList.size()) ? parent.netPlayerList.get(parent.selectedPlayerIndex) : null;
        }

        @Override
        protected boolean isSelected(int index) {return index == parent.selectedPlayerIndex;}

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
            FontRenderer font = parent.mc.fontRenderer;
            String ins = this.playerLists.get(slotIdx).getPlayerName();

            String s = font.trimStringToWidth(ins, listWidth - 10);
            /* light Blue */
            font.drawStringWithShadow(s, (float)this.left + 3, slotTop, 0xADD8E6);
            drawPing(parent ,this.left + 3, listWidth - 10, slotTop, this.playerLists.get(slotIdx));
        }
    }

    /* The White List */
    public static class GuiWhiteList extends GuiScrollingList
    {
        GuiMusicOptions parent;
        private final List<ClassifiedPlayer> whiteLists;
        
        GuiWhiteList(GuiMusicOptions parent, List<ClassifiedPlayer> whiteLists, int left, int listWidth, int slotHeight)
        {
            super(parent.mc, listWidth, parent.height - 32 - 100 + 4, 32, parent.height - 100 + 4, left, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.whiteLists = whiteLists;
        }
        
        int selectedIndex(int s)
        {
            selectedIndex = s;
            return selectedIndex;
        }

        public int getRight() {return right;}
        
        @Override
        protected int getSize() {return this.whiteLists.size();}

        @Override
        protected void elementClicked(int index, boolean doubleClick)
        {
            if (index == parent.selectedWhiteIndex) return;
            parent.selectedWhiteIndex = index;
            parent.selectedWhitePlayer = (index >= 0 && index <= parent.whiteList.size()) ? parent.whiteList.get(parent.selectedWhiteIndex) : null;
        }

        @Override
        protected boolean isSelected(int index) {return index == parent.selectedWhiteIndex;}

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
            FontRenderer font = parent.mc.fontRenderer;
            String ins = this.whiteLists.get(slotIdx).getPlayerName();

            String s = font.trimStringToWidth(ins, listWidth - 10);
            /* light Blue */
            font.drawStringWithShadow(s, (float)this.left + 3, slotTop, 0xADD8E6);
            drawPing(parent ,this.left + 3, listWidth - 10, slotTop, this.whiteLists.get(slotIdx));
        }
    }

    /* The Black List */
    public static class GuiBlackList extends GuiScrollingList
    {
        GuiMusicOptions parent;
        private final List<ClassifiedPlayer> blackLists;
        
        GuiBlackList(GuiMusicOptions parent, List<ClassifiedPlayer> blackLists, int listWidth, int slotHeight)
        {
            super(parent.mc, listWidth, parent.height - 32 - 100 + 4, 32, parent.height - 100 + 4, parent.listBoxPlayers.getRight() + 24, slotHeight, parent.width, parent.height);
            this.parent = parent;
            this.blackLists = blackLists;
        }
        
        int selectedIndex(int s)
        {
            selectedIndex = s;
            return selectedIndex;
        }

        public int getRight() {return right;}
        
        @Override
        protected int getSize() {return this.blackLists.size();}

        @Override
        protected void elementClicked(int index, boolean doubleClick)
        {
            if (index == parent.selectedBlackIndex) return;
            parent.selectedBlackIndex = index;
            parent.selectedBlackPlayer = (index >= 0 && index <= parent.blackList.size()) ? parent.blackList.get(parent.selectedBlackIndex) : null;
        }

        @Override
        protected boolean isSelected(int index) {return index == parent.selectedBlackIndex;}

        @Override
        protected void drawBackground()
        {
            Gui.drawRect(this.left - 1, this.top - 1, this.left + this.listWidth + 1, this.top + this.listHeight + 1, -6250336);
            Gui.drawRect(this.left, this.top, this.left + this.listWidth, this.top + this.listHeight, -16777216);
        }

        @Override
        protected int getContentHeight() {return getSize() * slotHeight;}

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
        {
            FontRenderer font = parent.mc.fontRenderer;
            String ins = blackLists.get(slotIdx).getPlayerName();

            String s = font.trimStringToWidth(ins, listWidth - 10);
            /* light Blue */
            font.drawStringWithShadow(s, (float)this.left + 3, slotTop, 0xADD8E6);
            drawPing(parent ,left + 3, listWidth - 10, slotTop, blackLists.get(slotIdx));
        }
    }

    private static void drawPing(GuiMusicOptions parent, int x, int sWidth, int y, ClassifiedPlayer playerInfo)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        parent.mc.getTextureManager().bindTexture(ICONS);
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
        public int compare(NetworkPlayerInfo pCompare1, NetworkPlayerInfo pCompare2)
        {
            return ComparisonChain.start().compare(pCompare1.getGameProfile().getName(), pCompare2.getGameProfile().getName()).result();
        }
    }

    private static final Ordering<NetworkPlayerInfo> ENTRY_ORDERING = Ordering.from(new GuiMusicOptions.PlayerComparator());
    
    static class PlayerListsComparator implements Comparator<ClassifiedPlayer>
    {
        private PlayerListsComparator() {}        
        @Override
        public int compare(ClassifiedPlayer o1, ClassifiedPlayer o2)
        {
            return ComparisonChain.start().compareTrueFirst(o1.isOnline(), o2.isOnline()).compare(o1.getPlayerName(), o2.getPlayerName()).result();
        }   
    }
    
    private static final Ordering<ClassifiedPlayer> LIST_ORDERING = Ordering.from(new GuiMusicOptions.PlayerListsComparator());
    
    private void initPlayerLists()
    {
        netPlayerList = new ArrayList<>();
        whiteList = MusicOptionsUtil.getWhiteList(player);
        blackList = MusicOptionsUtil.getBlackList(player);

        if (!(this.mc.isIntegratedServerRunning() && this.mc.player.connection.getPlayerInfoMap().size() <= 1))
        {
            NetHandlerPlayClient nethandlerplayclient = this.mc.player.connection;
            List<NetworkPlayerInfo> list = ENTRY_ORDERING.sortedCopy(nethandlerplayclient.getPlayerInfoMap());
            for (NetworkPlayerInfo networkplayerinfo : list)
            {
                ClassifiedPlayer networkPlayer = new ClassifiedPlayer();
                networkPlayer.setPlayerName(getPlayerName(networkplayerinfo));
                networkPlayer.setOnline(true);
                networkPlayer.setUuid(networkplayerinfo.getGameProfile().getId());
                if (!networkPlayer.getUuid().equals(player.getUniqueID())) netPlayerList.add(networkPlayer);
            }
        }
        initClassifiedPlayerList(netPlayerList, blackList);
        initClassifiedPlayerList(netPlayerList, whiteList);
        sortLists();
    }

    private void initClassifiedPlayerList(List<ClassifiedPlayer> netPlayers, List<ClassifiedPlayer> classifiedPlayers)
    {
        for (ClassifiedPlayer classifiedPlayer: classifiedPlayers)
        {
            for (int i=0; i < netPlayers.size(); i++)
            {
                ClassifiedPlayer nPlayer = netPlayers.get(i);
                if (nPlayer.getUuid().equals(classifiedPlayer.getUuid()))
                {
                    classifiedPlayer.setOnline(true);
                    classifiedPlayer.setPlayerName(nPlayer.getPlayerName());
                    netPlayers.remove(i);
                }
            }
        }
    }

    /* A brute force way to keep the lists sorted and not worry about thread safety */
    private void sortLists()
    {
        List<ClassifiedPlayer> tempWhiteList = LIST_ORDERING.sortedCopy(whiteList);
        whiteList.clear();
        whiteList.addAll(tempWhiteList);
        
        List<ClassifiedPlayer> tempBlackList = LIST_ORDERING.sortedCopy(blackList);
        blackList.clear();
        blackList.addAll(tempBlackList);

        List<ClassifiedPlayer> tempPlayerList = LIST_ORDERING.sortedCopy(netPlayerList);
        netPlayerList.clear();
        netPlayerList.addAll(tempPlayerList);
    }
    
    private String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn)
    {
        return networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
    }
}
