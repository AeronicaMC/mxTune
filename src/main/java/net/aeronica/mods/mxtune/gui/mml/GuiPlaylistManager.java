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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.gui.util.*;
import net.aeronica.mods.mxtune.managers.ClientFileManager;
import net.aeronica.mods.mxtune.managers.ClientPlayManager;
import net.aeronica.mods.mxtune.managers.PlayIdSupplier;
import net.aeronica.mods.mxtune.managers.records.PlayList;
import net.aeronica.mods.mxtune.managers.records.RecordType;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.managers.records.SongProxy;
import net.aeronica.mods.mxtune.mxt.MXTuneFile;
import net.aeronica.mods.mxtune.mxt.MXTuneFileHelper;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.GetBaseDataListsMessage;
import net.aeronica.mods.mxtune.network.bidirectional.SetServerSerializedDataMessage;
import net.aeronica.mods.mxtune.network.server.PlayerSelectedPlayListMessage;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.aeronica.mods.mxtune.gui.mml.SortHelper.PLAYLIST_ORDERING;
import static net.aeronica.mods.mxtune.gui.mml.SortHelper.SONG_PROXY_ORDERING;
import static net.aeronica.mods.mxtune.gui.toasts.ModToastHelper.postPlayListManagerToast;
import static net.aeronica.mods.mxtune.gui.util.ModGuiUtils.clearOnMouseLeftClicked;

public class GuiPlaylistManager extends GuiScreen
{
    private static final String TITLE = I18n.format("mxtune.gui.guiPlayListManager.title");
    private static final int PADDING = 4;
    // Song Multi Selector
    private GuiLabelMX labelSongList;
    private GuiScrollingMultiListOf<SongProxy> guiSongList;
    private GuiSortButton<SongProxy> buttonSortSongs;
    private int cachedSortMode;

    // Song Search
    private GuiLabelMX labelSearch;
    private GuiTextField textSearch;
    private String cachedSearch = "";

    // Playlist Selector
    private GuiLabel labelPlayListList;
    private GuiScrollingListOf<PlayList> guiPlayList;

    // PlayList Day
    private GuiLabel labelPlaylistDay;
    private GuiScrollingMultiListOf<SongProxy> guiDay;

    // PlayList Night
    private GuiLabel labelPlaylistNight;
    private GuiScrollingMultiListOf<SongProxy> guiNight;

    // Playlist Name Field
    private GuiLabelMX labelPlaylistName;
    private GuiTextField textPlayListName;
    private String cachedPlayListName = "";

    // Logging
    private GuiLabelMX labelLog;
    private GuiScrollingListOf<ITextComponent> guiLogList;
    private final DateFormat timeInstance = SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM);

    // Misc
    private List<IHooverText> hooverTexts = new ArrayList<>();
    private GuiLabel labelTitle;
    private boolean cacheKeyRepeatState;
    private boolean isStateCached;
    private GuiButtonMX buttonToServer;
    private Pattern patternPlaylistName = Pattern.compile("^\\s+|\\s+$|^\\[");

    /* MML Player */
    private GuiButtonMX buttonPlayStop;
    private int playId = PlayIdSupplier.PlayType.INVALID;
    private boolean isPlaying = false;
    private int counter;

    // Uploading
    private boolean uploading = false;

    // TODO: Finnish updating string to use the lang file.
    public GuiPlaylistManager()
    {
        mc = Minecraft.getMinecraft();
        fontRenderer = mc.fontRenderer;
        cacheKeyRepeatState = Keyboard.areRepeatEventsEnabled();
        Keyboard.enableRepeatEvents(true);

        guiSongList = new GuiScrollingMultiListOf<SongProxy>(this)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess)
            {
                // get the filename and remove the '.mxt' extension
                SongProxy entry = !isEmpty() && slotIdx < getSize() && slotIdx >= 0 ? get(slotIdx) : null;
                if (entry != null)
                {
                    String name = entry.getTitle();
                    String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                    int color = selectedRowIndexes.contains(slotIdx) ? 0xFFFF00 : isSelected(slotIdx) ? 0x0088FF : 0xADD8E6;
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                } else
                {
                    String name = "---NULL---";
                    String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                    int color = 0xFF0000;
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                }
            }

            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                super.selectedClickedCallback(selectedIndex);
                updateSongCountStatus();
            }
        };

        guiPlayList = new GuiScrollingListOf<PlayList>(this)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                PlayList playList = !isEmpty() && slotIdx < getSize() && slotIdx >= 0 ? get(slotIdx) : null;
                if (playList != null)
                {
                    String playlistName = ModGuiUtils.getPlaylistName(playList);
                    String trimmedName = fontRenderer.trimStringToWidth(playlistName, listWidth - 10);
                    int color = isSelected(slotIdx) ? 0xFFFF00 : 0xAADDEE;
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                } else
                {
                    String name = "---GUID Conflict---";
                    String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                    int color = 0xFF0000;
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                }
            }

            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                showSelectedPlaylistsPlaylists(guiPlayList.get(selectedIndex));
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex)
            {
                updatePlayersSelectedPlayListGuid(guiPlayList.get(selectedIndex));
            }
        };

        guiDay = new GuiScrollingMultiListOf<SongProxy>(this)
        {
            @Override
            protected void deleteAction(int index)
            {
                this.deleteSelectedRows();
            }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess)
            {
                drawSlotCommon(this, slotIdx, slotTop, listWidth, left);
            }
        };

        guiNight = new GuiScrollingMultiListOf<SongProxy>(this)
        {
            @Override
            protected void deleteAction(int index)
            {
                this.deleteSelectedRows();
            }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess)
            {
                drawSlotCommon(this, slotIdx, slotTop, listWidth, left);
            }
        };

        guiLogList = new GuiScrollingListOf<ITextComponent>(this) {
            @Override
            protected void selectedClickedCallback(int selectedIndex) { /* NOP */ }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex){ /* NOP */ }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                ITextComponent component = !isEmpty() && slotIdx < getSize() && slotIdx >= 0 ? get(slotIdx) : null;
                if (component != null)
                {
                    String statusEntry = component.getFormattedText();
                    String trimmedName = fontRenderer.trimStringToWidth(statusEntry, listWidth - 10);
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, -1);
                }
            }
        };
        initGuiHooverHelp();
    }

    @Override
    public void onGuiClosed()
    {
        stop();
        Keyboard.enableRepeatEvents(cacheKeyRepeatState);
    }

    @Override
    public void initGui()
    {
        hooverTexts.clear();
        int buttonHeight = 20;
        int guiBottom = height - PADDING;
        int guiListWidth = Math.min(Math.max((width - PADDING * 3) / 2, 720), (width - PADDING * 3) / 2);
        int guiSongListWidth = guiListWidth;
        int singleLineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int entryPlayListHeight = singleLineHeight;
        int textEntryHeight = singleLineHeight + 2;
        int leftPlayLists = width - guiListWidth - PADDING;
        int titleTop = PADDING;
        int left = PADDING;
        int titleWidth = fontRenderer.getStringWidth(I18n.format("mxtune.gui.guiPlayListManager.title"));
        int titleX = width - titleWidth - PADDING;

        int titleHeight = singleLineHeight + 2;
        int entryFileHeight = singleLineHeight;

        int buttonWidthMain = 80;
        int buttonTopMain = titleTop;
        int columnLabelsTop = buttonTopMain + buttonHeight + PADDING;
        int listTop = columnLabelsTop + textEntryHeight;

        // Left side
        int buttonSortSongsTop = listTop;
        int buttonSortRight = left + guiSongListWidth;

        int logStatusHeight = singleLineHeight * 5;
        int logStatusTop = guiBottom - logStatusHeight;
        int logStatusBottom = guiBottom;

        int songListTop = listTop + buttonHeight + PADDING;
        int songListBottom = logStatusTop - singleLineHeight - PADDING;
        int songListHeight = songListBottom - songListTop;

        // Right side
        int playListNameTop = listTop;

        int playListTop = playListNameTop + buttonHeight + PADDING;
        int thirdsHeight = (guiBottom - playListNameTop - (buttonHeight * 3) - (PADDING * 5)) / 3;
        int playListListHeight = Math.max(thirdsHeight, entryPlayListHeight);
        int playListBottom = playListTop + thirdsHeight;

        int dayLabelButtonTop = playListBottom + PADDING;
        int dayTop = dayLabelButtonTop + buttonHeight + PADDING;
        int dayBottom = dayTop + playListListHeight;

        int nightLabelButtonTop = dayBottom + PADDING;
        int nightTop = nightLabelButtonTop + buttonHeight + PADDING;
        int nightBottom = guiBottom;

        labelTitle = new GuiLabel(fontRenderer, 0, titleX, titleTop, titleWidth, singleLineHeight, 0xFFFFFF );
        labelTitle.addLine(TITLE);

        // Play/Stop
        buttonPlayStop = new GuiButtonMX(7, left, buttonTopMain, buttonWidthMain, buttonHeight, isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play"));
        buttonPlayStop.enabled = true;

        GuiButton buttonManageMusic = new GuiButton(0, buttonPlayStop.x + buttonWidthMain, buttonTopMain, buttonWidthMain, buttonHeight, I18n.format("mxtune.gui.button.manageMusic"));
        GuiButton buttonDone = new GuiButton(1, buttonManageMusic.x + buttonWidthMain, buttonTopMain, buttonWidthMain, buttonHeight, I18n.format("gui.done"));

        // Left and Right
        buttonSortSongs = new GuiSortButton<SongProxy>(24, buttonSortRight - 50, buttonSortSongsTop, 50, 20) {
            @Override
            public String getString(SongProxy o) { return o.getTitle(); }
        };
        buttonSortSongs.setDisplayText(I18n.format("mxtune.gui.button.order_a-z"), I18n.format("mxtune.gui.button.order_z-a"),"");
        buttonList.add(buttonSortSongs);

        labelSearch = new GuiLabelMX(fontRenderer, 2, left, buttonSortSongsTop + PADDING, left, singleLineHeight, -1);
        labelSearch.setLabelName(I18n.format("mxtune.gui.label.search"));
        int labelSearchWidth = fontRenderer.getStringWidth(labelSearch.getLabelName()) + PADDING;
        textSearch = new GuiTextField(2, fontRenderer, left + labelSearchWidth, buttonSortSongsTop, guiSongListWidth - labelSearchWidth - buttonSortSongs.width - PADDING, 20);

        guiSongList.setLayout(entryFileHeight, guiSongListWidth, songListHeight, songListTop, songListBottom, left);
        labelSongList = new GuiLabelMX(fontRenderer, 1, left, columnLabelsTop, guiSongListWidth, singleLineHeight, -1);
        labelSongList.setLabelName(I18n.format("mxtune.gui.guiPlayListManager.label.music_file_selector", guiSongList.size()));

        guiPlayList.setLayout(entryPlayListHeight, guiListWidth, playListListHeight, playListTop, playListBottom, leftPlayLists);
        guiDay.setLayout(singleLineHeight, guiListWidth, playListListHeight ,dayTop, dayBottom, leftPlayLists);
        guiNight.setLayout(singleLineHeight, guiListWidth, nightBottom - nightTop, nightTop, nightBottom, leftPlayLists);

        guiLogList.setLayout(singleLineHeight, guiListWidth, logStatusHeight, logStatusTop, logStatusBottom, left);

        labelLog = new GuiLabelMX(fontRenderer, 2, left, logStatusTop - singleLineHeight, guiListWidth, singleLineHeight,-1);
        labelLog.setLabelName(I18n.format("mxtune.gui.guiPlayListManager.label.status_log"));

        labelPlayListList = new GuiLabel(fontRenderer,3, guiPlayList.getRight() - guiListWidth, columnLabelsTop, guiListWidth, singleLineHeight, 0xFFFFFF);
        labelPlayListList.addLine("mxtune.gui.guiPlayListManager.label.playlist_selector");

        int selectButtonLeft = guiSongList.getRight() + PADDING;

        labelPlaylistName = new GuiLabelMX(fontRenderer,4, selectButtonLeft, listTop + PADDING, guiListWidth, singleLineHeight, -1);
        labelPlaylistName.setLabelName(I18n.format("mxtune.gui.guiPlayListManager.label.playlist_name"));
        int labelPlayListNameWidth = fontRenderer.getStringWidth(labelPlaylistName.getLabelName()) + PADDING;

        textPlayListName = new GuiTextField(1, fontRenderer, leftPlayLists + labelPlayListNameWidth, playListNameTop, guiListWidth - labelPlayListNameWidth - 75 - PADDING, buttonHeight);
        buttonToServer = new GuiButtonMX(6, width - 75 - PADDING, playListNameTop, 75, buttonHeight, I18n.format("mxtune.gui.button.send"));

        labelPlaylistDay = new GuiLabel(fontRenderer,5, leftPlayLists, dayLabelButtonTop + PADDING, guiListWidth, singleLineHeight, 0xFFFFFF);
        labelPlaylistDay.addLine("mxtune.gui.guiPlayListManager.label.list_day");

        labelPlaylistNight = new GuiLabel(fontRenderer,6, leftPlayLists, nightLabelButtonTop + PADDING, guiListWidth, singleLineHeight, 0xFFFFFF);
        labelPlaylistNight.addLine("mxtune.gui.guiPlayListManager.label.list_night");

        GuiButton buttonToDay = new GuiButton(2, width - 150 - PADDING, dayLabelButtonTop,  75, buttonHeight, I18n.format("mxtune.gui.guiPlayListManager.button.to_day_list"));
        GuiButton buttonToNight = new GuiButton(3, width - 150 - PADDING, nightLabelButtonTop, 75, buttonHeight, I18n.format("mxtune.gui.guiPlayListManager.button.to_night_list"));
        GuiButton buttonDDeleteDay = new GuiButton(4, width - 75 - PADDING, dayLabelButtonTop, 75, buttonHeight, I18n.format("mxtune.gui.guiPlayListManager.button.delete"));
        GuiButton buttonDDeleteNight = new GuiButton(5, width - 75 - PADDING, nightLabelButtonTop, 75, buttonHeight, I18n.format("mxtune.gui.guiPlayListManager.button.delete"));

        buttonList.add(buttonPlayStop);
        buttonList.add(buttonManageMusic);
        buttonList.add(buttonDone);
        buttonList.add(buttonToDay);
        buttonList.add(buttonToNight);
        buttonList.add(buttonDDeleteDay);
        buttonList.add(buttonDDeleteNight);
        buttonList.add(buttonToServer);

        hooverTexts.add(guiSongList);
        hooverTexts.add(guiPlayList);
        hooverTexts.add(guiDay);
        hooverTexts.add(guiNight);
        initPlayLists();
        initSongList();
        reloadState();
    }

    private <T extends GuiScrollingMultiListOf<SongProxy>> void drawSlotCommon(T parent, int slotIdx, int slotTop, int listWidth, int left)
    {
        SongProxy entry = parent.get(slotIdx);
        if (entry != null)
        {
            String name = entry.getTitle();
            String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
            int color = parent.getSelectedRowIndexes().contains(slotIdx) ? 0xFFFF00 : 0xADD8E6;
            fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
        } else
        {
            String name = "---GUID Conflict---";
            String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
            int color = 0xFF0000;
            fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
        }
    }

    private void reloadState()
    {
        if (!isStateCached) return;
        textSearch.setText(cachedSearch);
        textPlayListName.setText(cachedPlayListName);
        guiLogList.scrollToEnd();
        updateSongCountStatus();
        guiPlayList.resetScroll();
        buttonSortSongs.setSortMode(cachedSortMode);
        guiSongList.resetScroll();
    }

    private void updateState()
    {
        cachedPlayListName = textPlayListName.getText();

        buttonToServer.enabled = !(textPlayListName.getText().equals("") ||
                                           !globalMatcher(patternPlaylistName, textPlayListName.getText()) ||
                                           (guiDay.isEmpty() && guiNight.isEmpty() || uploading));
        cachedSortMode = buttonSortSongs.getSortMode();

        buttonPlayStop.displayString = isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play");

        searchAndSort();
        updateSongCountStatus();
        isStateCached = true;
    }

    private void searchAndSort()
    {
        if (!textSearch.getText().equals(cachedSearch))
        {
            List<SongProxy> songProxies = new ArrayList<>();
            for (SongProxy songProxy : ClientFileManager.getCachedServerSongList())
            {
                if (songProxy.getTitle().toLowerCase(Locale.ROOT).contains(textSearch.getText().toLowerCase(Locale.ROOT)))
                {
                    songProxies.add(songProxy);
                }
            }
            guiSongList.unSelectAll();
            guiSongList.clear();
            guiSongList.addAll(songProxies);
            guiSongList.sort(buttonSortSongs);
            cachedSearch = textSearch.getText();
        }
    }

    private boolean globalMatcher(Pattern pattern, String string)
    {
        int result = 0;
        Matcher matcher = pattern.matcher(string);
        while(matcher.find())
            result++;
        return result == 0;
    }

    private void updateSongCountStatus()
    {
        labelSongList.setLabelName(I18n.format("mxtune.gui.guiPlayListManager.label.music_file_selector", guiSongList.getSelectedRowsCount()));
    }

    private void updateStatus(String message)
    {
        Date dateNow = new Date();
        String now = timeInstance.format(dateNow);
        guiLogList.add(new TextComponentString(TextFormatting.GRAY + now + ": " + TextFormatting.RESET + message));
        guiLogList.scrollToEnd();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        labelTitle.drawLabel(mc, mouseX, mouseY);
        labelSongList.drawLabel(mc, mouseX, mouseY);
        labelSearch.drawLabel(mc, mouseX, mouseY);
        labelPlaylistName.drawLabel(mc, mouseX, mouseY);
        labelPlayListList.drawLabel(mc, mouseX, mouseY);
        labelPlaylistDay.drawLabel(mc, mouseX, mouseY);
        labelPlaylistNight.drawLabel(mc, mouseX, mouseY);
        labelLog.drawLabel(mc, mouseX, mouseY);
        textPlayListName.drawTextBox();
        textSearch.drawTextBox();

        guiSongList.drawScreen(mouseX, mouseY, partialTicks);
        guiPlayList.drawScreen(mouseX, mouseY, partialTicks);
        guiDay.drawScreen(mouseX, mouseY, partialTicks);
        guiNight.drawScreen(mouseX, mouseY, partialTicks);
        guiLogList.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        ModGuiUtils.INSTANCE.drawHooveringHelp(this, hooverTexts,0, 0, mouseX, mouseY);
    }

    @Override
    public void updateScreen()
    {
        if (counter++ % 10 == 0 )
        {
            synchronized (ClientAudio.INSTANCE)
            {
                isPlaying = ClientAudio.getActivePlayIDs().contains(playId);
                buttonPlayStop.displayString = isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play");
            }
        }
        // Keyboard.enableRepeatEvents(textPlayListName.isFocused());
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (!button.enabled) return;
        switch (button.id)
        {
            case 0:
                // Open Music Library
                stop();
                textSearch.setText("");
                cachedSearch = "";
                guiSongList.unSelectAll();
                mc.displayGuiScreen(new GuiMXT(this, GuiMXT.Mode.SERVER));
                break;
            case 1:
                // Done
                stop();
                mc.displayGuiScreen(null);
                break;
            case 2:
                // to Day
                guiDay.addAll(addSelectedSongsToSpecifiedPlayList(guiSongList.getSelectedRows(), guiDay.getList()));
                break;
            case 3:
                // to Night
                guiNight.addAll(addSelectedSongsToSpecifiedPlayList(guiSongList.getSelectedRows(), guiNight.getList()));
                break;
            case 4:
                // delete Day
                guiDay.deleteSelectedRows();
                break;
            case 5:
                // delete Night
                guiNight.deleteSelectedRows();
                break;
            case 6:
                // send to Server
                shipIt();
                break;
            case 7:
                // Play-Stop
                play();
                break;
            case 24:
                // Sort Song List
                guiSongList.sort(buttonSortSongs);
                guiSongList.unSelectAll();
                break;
            default:
        }
        updateState();
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode != Keyboard.KEY_TAB)
        {
            textPlayListName.textboxKeyTyped(typedChar, keyCode);
            textSearch.textboxKeyTyped(typedChar, keyCode);
        }

        guiSongList.keyTyped(typedChar, keyCode);
        guiPlayList.keyTyped(typedChar, keyCode);
        guiDay.keyTyped(typedChar, keyCode);
        guiNight.keyTyped(typedChar, keyCode);
        guiLogList.keyTyped(typedChar, keyCode);
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onResize(@Nonnull Minecraft mcIn, int w, int h)
    {
        updateState();
        super.onResize(mcIn, w, h);
        updateState();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        textPlayListName.mouseClicked(mouseX, mouseY, mouseButton);
        textSearch.mouseClicked(mouseX, mouseY, mouseButton);
        clearOnMouseLeftClicked(textSearch, mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        updateState();
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        guiPlayList.handleMouseInput(mouseX, mouseY);
        guiSongList.handleMouseInput(mouseX, mouseY);
        guiLogList.handleMouseInput(mouseX, mouseY);
        guiDay.handleMouseInput(mouseX, mouseY);
        guiNight.handleMouseInput(mouseX, mouseY);
        super.handleMouseInput();
    }

    @Nullable
    private SongProxy pathToSongProxy(Path path)
    {
        MXTuneFile mxTuneFile = MXTuneFileHelper.getMXTuneFile(path);
        if (mxTuneFile != null)
            return MXTuneFileHelper.getSongProxy(mxTuneFile);
        else
            ModLogger.warn("mxt file is missing or corrupt");

        return null;
    }

    @Nullable
    private Song pathToSong(Path path)
    {
        MXTuneFile mxTuneFile = MXTuneFileHelper.getMXTuneFile(path);
        if (mxTuneFile != null)
            return MXTuneFileHelper.getSong(mxTuneFile);
        else
            ModLogger.warn("mxt file is missing or corrupt");

        return null;
    }

    // So crude and ugly: add unique songs only
    private List<SongProxy> addSelectedSongsToSpecifiedPlayList(List<SongProxy> serverSongs, List<SongProxy> current)
    {
        List<SongProxy> songList = new ArrayList<>(current);
        for (SongProxy songProxyServer : serverSongs)
        {
            if (!songList.contains(songProxyServer))
                songList.add(songProxyServer);
        }
        current.clear();
        return SONG_PROXY_ORDERING.sortedCopy(songList);
    }

    private void shipIt()
    {
        // TODO: Sync MXT Files!

        String playListTitle = StringUtils.stripControlCodes(textPlayListName.getText()).trim();
        if (playListTitle.isEmpty()) return;
        uploading = true;
        textPlayListName.setText(playListTitle);
        PlayList playList = new PlayList(textPlayListName.getText(), guiDay.getList(), guiNight.getList());
        updateStatus(TextFormatting.GOLD + I18n.format("mxtune.gui.guiPlayListManager.log.upload_playlist", TextFormatting.RESET + textPlayListName.getText().trim()));
        PacketDispatcher.sendToServer(new SetServerSerializedDataMessage(playList.getGUID(), RecordType.PLAY_LIST, playList));

        new Thread ( () ->
                     {
                         try
                         {
                             Thread.sleep(1000);
                         } catch (InterruptedException e)
                         {
                             Thread.currentThread().interrupt();
                         }
                         postPlayListManagerToast();
                         uploading = false;
                         initPlayLists();
                         updateState();
                     }).start();
    }

    private void initSongList()
    {
        PacketDispatcher.sendToServer(new GetBaseDataListsMessage(CallBackManager.register(ClientFileManager.INSTANCE), RecordType.SONG_PROXY));
        new Thread(() ->
                   {
                       try
                       {
                           Thread.sleep(1000);
                       } catch (InterruptedException e)
                       {
                           Thread.currentThread().interrupt();
                       }
                       guiSongList.clear();
                       guiSongList.addAll(ClientFileManager.getCachedServerSongList());
                       guiSongList.sort(buttonSortSongs);
                       updateState();
                   }).start();
    }

    private void initPlayLists()
    {
        PacketDispatcher.sendToServer(new GetBaseDataListsMessage(CallBackManager.register(ClientFileManager.INSTANCE), RecordType.PLAY_LIST));
        new Thread(() ->
                   {
                       try
                       {
                           Thread.sleep(1000);
                       } catch (InterruptedException e)
                       {
                           Thread.currentThread().interrupt();
                       }
                       guiPlayList.clear();
                       guiPlayList.addAll(PLAYLIST_ORDERING.sortedCopy(ClientFileManager.getPlayLists()));

                       if (uploading)
                           uploading = false;

                       updateState();
                   }).start();
    }

    private void showSelectedPlaylistsPlaylists(PlayList selectedPlayList)
    {
        if (selectedPlayList != null)
        {
            if (!Reference.NO_MUSIC_GUID.equals(selectedPlayList.getGUID()) && !Reference.EMPTY_GUID.equals(selectedPlayList.getGUID()))
                textPlayListName.setText(ModGuiUtils.getPlaylistName(selectedPlayList));
            else
                textPlayListName.setText("");
            guiDay.clear();
            guiNight.clear();
            guiDay.addAll(SONG_PROXY_ORDERING.sortedCopy(selectedPlayList.getPlayListDay()));
            guiNight.addAll(SONG_PROXY_ORDERING.sortedCopy(selectedPlayList.getPlayListNight()));
            updateState();
        }
    }

    private void updatePlayersSelectedPlayListGuid(PlayList selectedPlayList)
    {
        if (selectedPlayList != null)
        {
            PacketDispatcher.sendToServer(new PlayerSelectedPlayListMessage(selectedPlayList.getGUID()));
            String playListNameOrUndefined = ModGuiUtils.getPlaylistName(selectedPlayList);
            updateStatus(TextFormatting.GOLD + I18n.format("mxtune.gui.guiPlayListManager.log.set_staff_of_music_playlist", TextFormatting.RESET + playListNameOrUndefined));
        }
        else
        {
            updateStatus(TextFormatting.DARK_RED + I18n.format("mxtune.gui.guiPlayListManager.log.failed_set_staff_of_music_playlist"));
        }
    }

    private boolean mmlPlay(GUID guidSong)
    {
        if (guidSong != null && !guidSong.equals(Reference.EMPTY_GUID) && !guidSong.equals(Reference.NO_MUSIC_GUID))
        {
            playId = PlayIdSupplier.PlayType.PERSONAL.getAsInt();
            return ClientPlayManager.playFromCacheElseServer(guidSong, playId);
        }
        return false;
    }

    private void play()
    {
        if (isPlaying)
        {
            stop();
        }
        else
        {
            SongProxy song = !guiSongList.isEmpty() && guiSongList.isSelected(guiSongList.getSelectedIndex()) ? guiSongList.get(guiSongList.getSelectedIndex()) : null;
            if (song != null && !isPlaying)
            {
                isPlaying = mmlPlay(song.getGUID());
                updateStatus(TextFormatting.GOLD + I18n.format("mxtune.gui.guiPlayListManager.log.play", SheetMusicUtil.formatDuration(song.getDuration()), TextFormatting.RESET + song.getTitle()));
            }
            else
                isPlaying = false;

        }
    }

    private void stop()
    {
        ClientAudio.fadeOut(playId, 1);
        isPlaying = false;
        playId = PlayIdSupplier.PlayType.INVALID;
    }

    private void initGuiHooverHelp()
    {
        guiSongList.addHooverTexts(TextFormatting.YELLOW + I18n.format("mxtune.gui.guiPlayListManager.label.music_file_selector", "n"));
        guiSongList.addHooverTexts(
                TextFormatting.RESET +
                        new TextComponentTranslation( "mxtune.gui.guiPlayListManager.help.multi_selector_01",
                                                      TextFormatting.GOLD + new TextComponentTranslation("mxtune.gui.guiPlayListManager.help.ctrl_plus_mouse_left_click").getFormattedText()).getFormattedText());
        guiSongList.addHooverTexts(
                TextFormatting.RESET +
                        new TextComponentTranslation( "mxtune.gui.guiPlayListManager.help.multi_selector_01",
                                                      TextFormatting.GOLD + new TextComponentTranslation("mxtune.gui.guiPlayListManager.help.ctrl_plus_spacebar_up_down_arrow_keys").getFormattedText()).getFormattedText());


        guiPlayList.addHooverTexts(TextFormatting.YELLOW + I18n.format("mxtune.gui.guiPlayListManager.label.playlist_selector"));
        guiPlayList.addHooverTexts(TextFormatting.RESET + I18n.format("mxtune.gui.guiPlayListManager.help.playlist_selector_01"));
        guiPlayList.addHooverTexts(TextFormatting.RESET + I18n.format("mxtune.gui.guiPlayListManager.help.playlist_selector_02"));
        guiPlayList.addHooverTexts(TextFormatting.RESET + "");
        guiPlayList.addHooverTexts(
                TextFormatting.AQUA +
                        new TextComponentTranslation("mxtune.gui.guiPlayListManager.help.playlist_selector_03").getFormattedText());
        guiPlayList.addHooverTexts(
                TextFormatting.RESET +
                        new TextComponentTranslation( "mxtune.gui.guiPlayListManager.help.playlist_selector_04",
                                                      TextFormatting.GOLD + new TextComponentTranslation("mxtune.info.playlist.null_playlist").getFormattedText()).getFormattedText());
        guiPlayList.addHooverTexts(
                TextFormatting.RESET +
                        new TextComponentTranslation("mxtune.gui.guiPlayListManager.help.playlist_selector_05",
                                                     TextFormatting.GOLD + new TextComponentTranslation("mxtune.info.playlist.empty_playlist").getFormattedText()).getFormattedText());

        guiDay.addHooverTexts(TextFormatting.YELLOW + I18n.format("mxtune.gui.guiPlayListManager.label.list_day"));
        guiDay.addHooverTexts(
                TextFormatting.RESET +
                        new TextComponentTranslation( "mxtune.gui.guiPlayListManager.help.multi_selector_01",
                                                      TextFormatting.GOLD + new TextComponentTranslation("mxtune.gui.guiPlayListManager.help.ctrl_plus_mouse_left_click").getFormattedText()).getFormattedText());
        guiDay.addHooverTexts(
                TextFormatting.RESET +
                        new TextComponentTranslation( "mxtune.gui.guiPlayListManager.help.multi_selector_01",
                                                      TextFormatting.GOLD + new TextComponentTranslation("mxtune.gui.guiPlayListManager.help.ctrl_plus_spacebar_up_down_arrow_keys").getFormattedText()).getFormattedText());

        guiNight.addHooverTexts(TextFormatting.YELLOW + I18n.format("mxtune.gui.guiPlayListManager.label.list_night"));
        guiNight.addHooverTexts(
                TextFormatting.RESET +
                        new TextComponentTranslation( "mxtune.gui.guiPlayListManager.help.multi_selector_01",
                                                      TextFormatting.GOLD + new TextComponentTranslation("mxtune.gui.guiPlayListManager.help.ctrl_plus_mouse_left_click").getFormattedText()).getFormattedText());
        guiNight.addHooverTexts(
                TextFormatting.RESET +
                        new TextComponentTranslation( "mxtune.gui.guiPlayListManager.help.multi_selector_01",
                                                      TextFormatting.GOLD + new TextComponentTranslation("mxtune.gui.guiPlayListManager.help.ctrl_plus_spacebar_up_down_arrow_keys").getFormattedText()).getFormattedText());

    }
}