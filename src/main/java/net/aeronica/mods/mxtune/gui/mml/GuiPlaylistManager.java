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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.gui.util.*;
import net.aeronica.mods.mxtune.managers.ClientFileManager;
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
import net.aeronica.mods.mxtune.util.CallBackManager;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.aeronica.mods.mxtune.gui.mml.SortHelper.PLAYLIST_ORDERING;
import static net.aeronica.mods.mxtune.gui.mml.SortHelper.SONG_PROXY_ORDERING;
import static net.aeronica.mods.mxtune.gui.toasts.ModToastHelper.postPlayListManagerToast;

public class GuiPlaylistManager extends GuiScreen
{
    private static final String TITLE = I18n.format("mxtune.gui.guiPlayListManager.title");
    private static final int PADDING = 4;
    // Song Multi Selector
    private GuiLabelMX labelFileList;
    private GuiScrollingMultiListOf<SongProxy> guiFileList;

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
    private GuiLabel labelPlaylistName;
    private GuiTextField playListName;
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
    private GuiButton buttonToServer;
    private Pattern patternPlaylistName = Pattern.compile("^\\s+|\\s+$|^\\[");

    // Uploading
    private boolean uploading = false;

    // Mapping
    private BiMap<Path, SongProxy> pathSongProxyBiMap = HashBiMap.create();
    private BiMap<SongProxy, Path> songProxyPathBiMap;
    private BiMap<Path, GUID> pathSongGuidBiMap = HashBiMap.create();
    private BiMap<GUID, Path> songGuidPathBiMap;

    // TODO: Finnish updating string to use the lang file.
    public GuiPlaylistManager()
    {
        mc = Minecraft.getMinecraft();
        fontRenderer = mc.fontRenderer;
        cacheKeyRepeatState = Keyboard.areRepeatEventsEnabled();
        Keyboard.enableRepeatEvents(true);

        guiFileList = new GuiScrollingMultiListOf<SongProxy>(this)
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
                    int color = selectedRowIndexes.contains(slotIdx) ? 0xFFFF00 : 0xADD8E6;
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
        Keyboard.enableRepeatEvents(cacheKeyRepeatState);
    }

    @Override
    public void initGui()
    {
        hooverTexts.clear();
        int buttonHeight = 20;
        int guiBottom = height - 22 - PADDING * 2;
        int guiListWidth = Math.min(Math.max((width - PADDING * 3) / 2, 720), (width - PADDING * 3) / 2);
        int guiFileListWidth = guiListWidth;
        int singleLineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int entryPlayListHeight = singleLineHeight;
        int textEntryHeight = singleLineHeight + 2;
        int leftPlayLists = width - guiListWidth - PADDING;
        int titleTop = PADDING;
        int left = PADDING;
        int titleWidth = fontRenderer.getStringWidth(I18n.format("mxtune.gui.guiPlayListManager.title"));
        int titleX = leftPlayLists - titleWidth - PADDING;

        int titleHeight = singleLineHeight + 2;
        int statusHeight = singleLineHeight * 8;
        int entryFileHeight = singleLineHeight;

        int listTop = titleTop + titleHeight;
        int fileListBottom = Math.max(height - statusHeight - listTop - titleHeight - PADDING, entryPlayListHeight * 9);

        int fileListHeight = Math.max(fileListBottom - listTop, singleLineHeight);
        int logStatusTop = fileListBottom + PADDING;

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


        guiFileList.setLayout(entryFileHeight, guiFileListWidth, fileListHeight,listTop, fileListBottom, left);
        labelFileList = new GuiLabelMX(fontRenderer,1, left, titleTop, guiFileListWidth, singleLineHeight, -1);
        labelFileList.setLabelName(I18n.format("mxtune.gui.guiPlayListManager.label.music_file_selector", guiFileList.size()));

        guiPlayList.setLayout(entryPlayListHeight, guiListWidth, playListListHeight, playListTop, playListBottom, leftPlayLists);
        guiDay.setLayout(singleLineHeight, guiListWidth, playListListHeight ,dayTop, dayBottom, leftPlayLists);
        guiNight.setLayout(singleLineHeight, guiListWidth, nightBottom - nightTop, nightTop, nightBottom, leftPlayLists);

        guiLogList.setLayout(singleLineHeight, guiListWidth, statusHeight, logStatusTop, guiBottom, left);

        labelLog = new GuiLabelMX(fontRenderer, 2, guiFileList.getRight(), logStatusTop - singleLineHeight -2, guiPlayList.getLeft()-guiFileList.getRight(), singleLineHeight,-1);
        labelLog.setCentered();
        labelLog.setLabelName(I18n.format("mxtune.gui.guiPlayListManager.label.status_log"));

        labelPlayListList = new GuiLabel(fontRenderer,3, guiPlayList.getRight() - guiListWidth, titleTop, guiListWidth, singleLineHeight, 0xFFFFFF);
        labelPlayListList.addLine("mxtune.gui.guiPlayListManager.label.playlist_selector");

        int buttonWidth = 80;
        int buttonTop = height - 22;
        int xLibrary = (this.width /2) - buttonWidth;
        int xDone = xLibrary + buttonWidth;

        GuiButton buttonManageMusic = new GuiButton(0, xLibrary, buttonTop, buttonWidth, buttonHeight, I18n.format("mxtune.gui.button.manageMusic"));
        GuiButton buttonDone = new GuiButton(1, xDone, buttonTop, buttonWidth, buttonHeight, I18n.format("gui.done"));

        int selectButtonWidth = guiFileListWidth - 10;
        int selectButtonLeft = guiFileList.getRight() + 8;

        labelPlaylistName = new GuiLabel(fontRenderer,4, selectButtonLeft, listTop, guiListWidth, singleLineHeight, 0xFFFFFF);
        labelPlaylistName.addLine("mxtune.gui.guiPlayListManager.label.playlist_name");
        playListName = new GuiTextField(1, fontRenderer, leftPlayLists, playListNameTop, guiListWidth - 75 -PADDING, buttonHeight);
        buttonToServer = new GuiButton(6, width - 75 - PADDING, playListNameTop, 75, buttonHeight, I18n.format("mxtune.gui.button.send"));

        labelPlaylistDay = new GuiLabel(fontRenderer,5, leftPlayLists, dayLabelButtonTop + PADDING, guiListWidth, singleLineHeight, 0xFFFFFF);
        labelPlaylistDay.addLine("mxtune.gui.guiPlayListManager.label.list_day");

        labelPlaylistNight = new GuiLabel(fontRenderer,6, leftPlayLists, nightLabelButtonTop + PADDING, guiListWidth, singleLineHeight, 0xFFFFFF);
        labelPlaylistNight.addLine("mxtune.gui.guiPlayListManager.label.list_night");

        GuiButton buttonToDay = new GuiButton(2, width - 150 - PADDING, dayLabelButtonTop,  75, buttonHeight, I18n.format("mxtune.gui.guiPlayListManager.button.to_day_list"));
        GuiButton buttonToNight = new GuiButton(3, width - 150 - PADDING, nightLabelButtonTop, 75, buttonHeight, I18n.format("mxtune.gui.guiPlayListManager.button.to_night_list"));
        GuiButton buttonDDeleteDay = new GuiButton(4, width - 75 - PADDING, dayLabelButtonTop, 75, buttonHeight, I18n.format("mxtune.gui.guiPlayListManager.button.delete"));
        GuiButton buttonDDeleteNight = new GuiButton(5, width - 75 - PADDING, nightLabelButtonTop, 75, buttonHeight, I18n.format("mxtune.gui.guiPlayListManager.button.delete"));

        buttonList.add(buttonManageMusic);
        buttonList.add(buttonDone);
        buttonList.add(buttonToDay);
        buttonList.add(buttonToNight);
        buttonList.add(buttonDDeleteDay);
        buttonList.add(buttonDDeleteNight);
        buttonList.add(buttonToServer);

        hooverTexts.add(guiFileList);
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
        playListName.setText(cachedPlayListName);

        guiLogList.scrollToEnd();

        updateSongCountStatus();
        guiPlayList.resetScroll();
        guiFileList.resetScroll();
    }

    private void updateState()
    {
        cachedPlayListName = playListName.getText();

        buttonToServer.enabled = !(playListName.getText().equals("") ||
                                           !globalMatcher(patternPlaylistName, playListName.getText()) ||
                                           (guiDay.isEmpty() && guiNight.isEmpty() || uploading));
        updateSongCountStatus();
        isStateCached = true;
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
        labelFileList.setLabelName(I18n.format("mxtune.gui.guiPlayListManager.label.music_file_selector", guiFileList.getSelectedRowsCount()));
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
        labelFileList.drawLabel(mc, mouseX, mouseY);
        labelPlaylistName.drawLabel(mc, mouseX, mouseY);
        labelPlayListList.drawLabel(mc, mouseX, mouseY);
        labelPlaylistDay.drawLabel(mc, mouseX, mouseY);
        labelPlaylistNight.drawLabel(mc, mouseX, mouseY);
        labelLog.drawLabel(mc, mouseX, mouseY);
        playListName.drawTextBox();

        guiFileList.drawScreen(mouseX, mouseY, partialTicks);
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
        // Keyboard.enableRepeatEvents(playListName.isFocused());
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
                mc.displayGuiScreen(new GuiMXT(this, GuiMXT.Mode.SERVER));
                break;
            case 1:
                // Done
                mc.displayGuiScreen(null);
                break;
            case 2:
                // to Day
                guiDay.addAll(pathsToSongProxies(guiFileList.getSelectedRows(), guiDay.getList()));
                break;
            case 3:
                // to Night
                guiNight.addAll(pathsToSongProxies(guiFileList.getSelectedRows(), guiNight.getList()));
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
            default:
        }
        updateState();
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode != Keyboard.KEY_TAB)
            playListName.textboxKeyTyped(typedChar, keyCode);

        guiFileList.keyTyped(typedChar, keyCode);
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
        playListName.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        guiPlayList.handleMouseInput(mouseX, mouseY);
        guiFileList.handleMouseInput(mouseX, mouseY);
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

    // So crude: add unique songs only
    private List<SongProxy> pathsToSongProxies(List<SongProxy> serverSongs, List<SongProxy> current)
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
        uploading = true;
        playListName.setText(playListName.getText().trim());
        PlayList playList = new PlayList(playListName.getText(), guiDay.getList(), guiNight.getList());
        updateStatus(TextFormatting.GOLD + I18n.format("mxtune.gui.guiPlayListManager.log.upload_playlist", TextFormatting.RESET + playListName.getText().trim()));
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
                       guiFileList.clear();
                       guiFileList.addAll(SONG_PROXY_ORDERING.sortedCopy(ClientFileManager.getCachedServerSongList()));
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
                playListName.setText(ModGuiUtils.getPlaylistName(selectedPlayList));
            else
                playListName.setText("");
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

    private void initGuiHooverHelp()
    {
        guiFileList.addHooverTexts(TextFormatting.YELLOW + I18n.format("mxtune.gui.guiPlayListManager.label.music_file_selector", "0"));
        guiFileList.addHooverTexts(
                TextFormatting.RESET +
                        new TextComponentTranslation( "mxtune.gui.guiPlayListManager.help.multi_selector_01",
                                                      TextFormatting.GOLD + new TextComponentTranslation("mxtune.gui.guiPlayListManager.help.ctrl_plus_mouse_left_click").getFormattedText()).getFormattedText());
        guiFileList.addHooverTexts(
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

    }
}