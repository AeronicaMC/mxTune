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
    private GuiScreen guiScreenOld;
    private static final String TITLE = I18n.format("mxtune.gui.musicOptions.title");
    private static final String LABEL_WHITELIST = I18n.format("mxtune.gui.musicOptions.label.whitelist");
    private static final String LABEL_PLAYERS  = I18n.format("mxtune.gui.musicOptions.label.players");
    private static final String LABEL_BLACKLIST  = I18n.format("mxtune.gui.musicOptions.label.blacklist");
    private static final String MIDI_NOT_AVAILABLE = I18n.format("mxtune.chat.msu.midiNotAvailable");
    private static final String BUTTON_ADJ_HUD = I18n.format("mxtune.gui.musicOptions.adjHud");

    private GuiButtonExt buttonMuteOption;
    private GuiButtonExt buttonCancel;
    private GuiPlayerList listBoxPlayers;
    private GuiPlayerList listBoxWhite;
    private GuiPlayerList listBoxBlack;

    private EntityPlayer player;
    private int muteOption;
    private boolean midiUnavailable;
    private int guiListWidth;
    private int entryHeight;

    /* Classified player lists*/
    private List<ClassifiedPlayer> networkPlayers;
    private List<ClassifiedPlayer> whiteListedPlayers;
    private List<ClassifiedPlayer> blackListedPlayers;

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
    public void initGui()
    {
        this.buttonList.clear();
        guiListWidth = mc.fontRenderer.getStringWidth("MWMWMWMWMWMWMWMW") + 10 + 12;
        entryHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int y = (height - 100) / 2;
        int left = (width - ((guiListWidth * 3) + 24 + 24)) / 2;
        int listHeight = height - 32 - 100 + 4;
        int listTop = 32;
        int listBottom = height - 100 + 4;

        listBoxWhite = new GuiPlayerList(this, whiteListedPlayers, guiListWidth, listHeight, listTop, listBottom, left);
        GuiButtonExt buttonWhiteToPlayers = new GuiButtonExt(11, listBoxWhite.getRight() + 2, y, 20, 20, ">");
        GuiButtonExt buttonPlayersToWhite = new GuiButtonExt(12, listBoxWhite.getRight() + 2, y + 25, 20, 20, "<");
        listBoxPlayers = new GuiPlayerList(this, networkPlayers, guiListWidth, listHeight, listTop, listBottom, listBoxWhite.getRight() + 24);
        GuiButtonExt buttonPlayersToBlack = new GuiButtonExt(13, listBoxPlayers.getRight() + 2, y, 20, 20, ">");
        GuiButtonExt buttonBlackToPlayers = new GuiButtonExt(14, listBoxPlayers.getRight() + 2, y + 25, 20, 20, "<");
        listBoxBlack = new GuiPlayerList(this, blackListedPlayers, guiListWidth, listHeight, listTop, listBottom, listBoxPlayers.getRight() + 24);

        int buttonWidth = 100;
        y = height - 100 + 4 + 5;
        left = listBoxBlack.getRight() - buttonWidth + 2;
        GuiButtonExt buttonDone = new GuiButtonExt(3, left, y, buttonWidth, 20, I18n.format("gui.done"));
        buttonCancel = new GuiButtonExt(2, left, y+22, buttonWidth, 20, I18n.format("gui.cancel"));

        y = height - 100 + 4 + 5;
        left = listBoxWhite.getRight() - guiListWidth - 2;
        buttonWidth = 225;
        buttonMuteOption = new GuiButtonExt(0, left, y, buttonWidth, 20, (MusicOptionsUtil.EnumMuteOptions.byIndex(muteOption).toString()));
        y += 22;
        GuiButtonExt buttonAdjHud = new GuiButtonExt(4, left, y, buttonWidth, 20, BUTTON_ADJ_HUD);
        
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
        this.cachedSelectedPlayerIndex = listBoxPlayers.getSelectedIndex();
        this.cachedSelectedWhiteIndex = listBoxWhite.getSelectedIndex();
        this.cachedSelectedBlackIndex = listBoxBlack.getSelectedIndex();
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
        posX = (this.listBoxWhite.getRight() - guiListWidth / 2) - (mc.fontRenderer.getStringWidth(LABEL_WHITELIST) / 2);
        posY = 20;
        mc.fontRenderer.drawStringWithShadow(LABEL_WHITELIST, posX, posY, 0xD3D3D3);

        /* Players list */
        posX = this.listBoxPlayers.getRight() - guiListWidth / 2 - (mc.fontRenderer.getStringWidth(LABEL_PLAYERS) / 2);
        posY = 20;
        mc.fontRenderer.drawStringWithShadow(LABEL_PLAYERS, posX, posY, 0xD3D3D3);

        /* Blacklist */
        posX = this.listBoxBlack.getRight() - guiListWidth / 2 - (mc.fontRenderer.getStringWidth(LABEL_BLACKLIST) / 2);
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
                // Whitelist to NetPlayerList
                moveSelectedPlayer(listBoxWhite, whiteListedPlayers, networkPlayers);
                break;
            case 12:
                // NetPlayerList to WhiteList
                moveSelectedPlayer(listBoxPlayers, networkPlayers, whiteListedPlayers);
                break;
            case 13:
                // NetPlayerList to Blacklist
                moveSelectedPlayer(listBoxPlayers, networkPlayers, blackListedPlayers);
                break;
            case 14:
                // Blacklist to NetPlayerList
                moveSelectedPlayer(listBoxBlack, blackListedPlayers, networkPlayers);
                break;

            default:
        }
        sortLists();
        updateState();
    }

    private void moveSelectedPlayer(GuiPlayerList listBoxFrom, List<ClassifiedPlayer> playersFrom, List<ClassifiedPlayer> playersTo)
    {
        ClassifiedPlayer classifiedPlayerRef;
        int selectedIndex = listBoxFrom.getSelectedIndex();

        if (selectedIndex == -1 || selectedIndex > playersFrom.size() || playersFrom.isEmpty()) return;
        classifiedPlayerRef = playersFrom.get(selectedIndex);
        playersFrom.remove(selectedIndex);
        playersTo.add(classifiedPlayerRef);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        // capture the ESC key to close cleanly
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
        PacketDispatcher.sendToServer(new MusicOptionsMessage(muteOption, blackListedPlayers, whiteListedPlayers));
    }

    public static class GuiPlayerList extends GuiScrollingList
    {
        private List<ClassifiedPlayer> classifiedPlayers;
        private FontRenderer fontRenderer;
        private GuiMusicOptions parent;

        GuiPlayerList(GuiMusicOptions parent, List<ClassifiedPlayer> playerListIn, int width, int height, int top, int bottom, int left)
        {
            super(parent.mc, width, height, top, bottom, left, parent.entryHeight, parent.width, parent.height);
            fontRenderer = parent.mc.fontRenderer;
            this.parent = parent;
            this.classifiedPlayers = playerListIn;
        }

        int getRight() {return right;}

        int getSelectedIndex() { return selectedIndex; }

        @Override
        protected int getSize()
        {
            return classifiedPlayers.size();
        }

        @Override
        protected void elementClicked( int index, boolean doubleClick )
        {
            if (index == selectedIndex) return;
            selectedIndex = (index >= 0 && index <= classifiedPlayers.size() ? index : -1);
         }

        @Override
        protected boolean isSelected(int index)
        {
            return index == selectedIndex && selectedIndex >= 0 && selectedIndex <= classifiedPlayers.size();
        }

        @Override
        protected void drawBackground()
        {
            Gui.drawRect(left - 1, top - 1, left + listWidth + 1, top + listHeight + 1, -6250336);
            Gui.drawRect(left, top, left + listWidth, top + listHeight, -16777216);
        }

        @Override
        protected int getContentHeight() { return (this.getSize()) * slotHeight; }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
        {
            String name = classifiedPlayers.get(slotIdx).getPlayerName();
            String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
            /* light Blue */
            fontRenderer.drawStringWithShadow(trimmedName, (float)left + 3, slotTop, 0xADD8E6);
            drawPing(parent ,left + 3, listWidth - 10, slotTop, classifiedPlayers.get(slotIdx));
        }

        private static void drawPing(GuiMusicOptions parent, int x, int sWidth, int y, ClassifiedPlayer playerInfo)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            parent.mc.getTextureManager().bindTexture(ICONS);
            int offset = playerInfo.isOnline() ? 0 : 5;
            parent.zLevel += 100.0F;
            parent.drawTexturedModalRect(sWidth + x - 11, y, 0, 176 + offset * 8, 10, 8);
            parent.zLevel -= 100.0F;
        }
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
        networkPlayers = new ArrayList<>();
        whiteListedPlayers = MusicOptionsUtil.getWhiteList(player);
        blackListedPlayers = MusicOptionsUtil.getBlackList(player);

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
                if (!networkPlayer.getUuid().equals(player.getUniqueID())) networkPlayers.add(networkPlayer);
            }
        }
        initClassifiedPlayerList(networkPlayers, blackListedPlayers);
        initClassifiedPlayerList(networkPlayers, whiteListedPlayers);
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
        List<ClassifiedPlayer> tempWhiteList = LIST_ORDERING.sortedCopy(whiteListedPlayers);
        whiteListedPlayers.clear();
        whiteListedPlayers.addAll(tempWhiteList);
        
        List<ClassifiedPlayer> tempBlackList = LIST_ORDERING.sortedCopy(blackListedPlayers);
        blackListedPlayers.clear();
        blackListedPlayers.addAll(tempBlackList);

        List<ClassifiedPlayer> tempPlayerList = LIST_ORDERING.sortedCopy(networkPlayers);
        networkPlayers.clear();
        networkPlayers.addAll(tempPlayerList);
    }
    
    private String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn)
    {
        return networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
    }
}
