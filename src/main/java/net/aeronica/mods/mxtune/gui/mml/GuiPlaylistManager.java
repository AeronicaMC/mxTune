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
import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.caches.MXTuneFile;
import net.aeronica.mods.mxtune.caches.MXTuneFileHelper;
import net.aeronica.mods.mxtune.gui.util.*;
import net.aeronica.mods.mxtune.managers.ClientFileManager;
import net.aeronica.mods.mxtune.managers.records.Area;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.managers.records.SongProxy;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.GetAreasMessage;
import net.aeronica.mods.mxtune.network.bidirectional.SetServerSerializedDataMessage;
import net.aeronica.mods.mxtune.network.server.PlayerSelectedAreaMessage;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.aeronica.mods.mxtune.gui.mml.SortHelper.PLAYLIST_ORDERING;
import static net.aeronica.mods.mxtune.gui.mml.SortHelper.SONG_PROXY_ORDERING;

public class GuiPlaylistManager extends GuiScreen
{
    private static final String TITLE = I18n.format("mxtune.gui.guiPlayListManager.title");
    // Song Multi Selector
    private GuiLabelMX labelFileList;
    private GuiScrollingMultiListOf<Path> guiFileList;
    private List<Path> cachedFileList = new ArrayList<>();
    private Set<Integer> cachedSelectedSongs = new HashSet<>();
    private int cachedSelectedFileDummy = -1;

    // Playlist Selector
    private GuiLabel labelPlayListList;
    private GuiScrollingListOf<Area> guiPlayList;
    private List<Area> cachedPlayListGuiList = new ArrayList<>();
    private int cachedSelectedPlayListIndex = -1;

    // PlayList Day
    private GuiLabel labelPlaylistDay;
    private GuiScrollingMultiListOf<SongProxy> guiDay;
    private List<SongProxy> cachedDayList = new ArrayList<>();
    private Set<Integer> cachedSelectedDaySongs = new HashSet<>();
    private int cachedSelectedDaySongDummy = -1;

    // PlayList Night
    private GuiLabel labelPlaylistNight;
    private GuiScrollingMultiListOf<SongProxy> guiNight;
    private List<SongProxy> cachedNightList = new ArrayList<>();
    private Set<Integer> cachedSelectedNightSongs = new HashSet<>();
    private int cachedSelectedNightSongDummy = -1;

    // Playlist Name Field
    private GuiLabel labelPlaylistName;
    private GuiTextField playListName;
    private String cachedPlayListName = "";

    // Logging
    private GuiLabelMX labelLog;
    private GuiScrollingListOf<ITextComponent> guiLogList;
    private List<ITextComponent> cachedGuiLogList = new ArrayList<>();
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
        cacheKeyRepeatState = Keyboard.areRepeatEventsEnabled();
        Keyboard.enableRepeatEvents(false);
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
        int guiPlayListListWidth = Math.min(Math.max((width - 15) / 3, 100), 400);
        int guiFileListWidth = guiPlayListListWidth;
        int singleLineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int entryPlayListHeight = singleLineHeight;
        int padding = 4;
        int titleTop = padding;
        int left = padding;
        int titleWidth = fontRenderer.getStringWidth(I18n.format("mxtune.gui.guiPlayListManager.title"));
        int titleX = (width / 2) - (titleWidth / 2);

        int titleHeight = singleLineHeight + 2;
        int statusHeight = singleLineHeight * 8;
        int entryFileHeight = singleLineHeight;

        int listTop = titleTop + titleHeight;
        int fileListBottom = Math.max(height - statusHeight - listTop - titleHeight - padding, entryPlayListHeight * 9);

        int fileListHeight = Math.max(fileListBottom - listTop, singleLineHeight);
        int logStatusTop = fileListBottom + padding;

        int thirdsHeight = ((logStatusTop - listTop) / 3) - padding;
        int areaListHeight = Math.max(thirdsHeight, entryPlayListHeight);
        int areaBottom = listTop + areaListHeight;

        int dayTop = areaBottom + padding;
        int dayBottom = dayTop + areaListHeight;

        int nightTop = dayBottom + padding;
        int nightBottom = nightTop + areaListHeight;

        labelTitle = new GuiLabel(fontRenderer, 0, titleX, titleTop, titleWidth, singleLineHeight, 0xFFFFFF );
        labelTitle.addLine(TITLE);
        labelFileList = new GuiLabelMX(fontRenderer,1, left, titleTop, guiFileListWidth, singleLineHeight, -1);
        labelFileList.setLabel(I18n.format("mxtune.gui.guiPlayListManager.label.music_file_selector", cachedSelectedSongs.size()));

        guiFileList = new GuiScrollingMultiListOf<Path>(this, entryFileHeight, guiFileListWidth, fileListHeight,listTop, fileListBottom, left)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess)
            {
                // get the filename and remove the '.mxt' extension
                Path entry = get(slotIdx);
                if (entry != null)
                {
                    String name = entry.getFileName().toString().replaceAll("\\.[mM][xX][tT]$", "");
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

        guiPlayList = new GuiScrollingListOf<Area>(this, entryPlayListHeight, guiPlayListListWidth, areaListHeight, listTop, areaBottom, width - guiPlayListListWidth - padding)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                Area area = get(slotIdx);
                if (area != null)
                {
                    String playlistName = ModGuiUtils.getPlaylistName(area);
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
                updatePlayersSelectedAreaGuid(guiPlayList.get(selectedIndex));
            }
        };

        guiDay = new GuiScrollingMultiListOf<SongProxy>(this, singleLineHeight, guiPlayListListWidth, areaListHeight ,dayTop, dayBottom, width - guiPlayListListWidth - padding)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess)
            {
                drawSlotCommon(this, slotIdx, slotTop, listWidth, left);
            }
        };

        guiNight = new GuiScrollingMultiListOf<SongProxy>(this, singleLineHeight, guiPlayListListWidth, areaListHeight, nightTop, nightBottom, width - guiPlayListListWidth - padding)
        {
            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, float scrollDistance, Tessellator tess)
            {
                drawSlotCommon(this, slotIdx, slotTop, listWidth, left);
            }
        };

        guiLogList = new GuiScrollingListOf<ITextComponent>(this, singleLineHeight, width - padding *2, statusHeight, logStatusTop, height - 22 - padding * 2, left) {
            @Override
            protected void selectedClickedCallback(int selectedIndex) { /* NOP */ }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex){ /* NOP */ }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                ITextComponent component = get(slotIdx);
                if (component != null)
                {
                    String statusEntry = component.getFormattedText();
                    String trimmedName = fontRenderer.trimStringToWidth(statusEntry, listWidth - 10);
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, -1);
                }
            }
        };

        labelLog = new GuiLabelMX(fontRenderer, 2, guiFileList.getRight(), logStatusTop - singleLineHeight -2, guiPlayList.getLeft()-guiFileList.getRight(), singleLineHeight,-1);
        labelLog.setCentered();
        labelLog.setLabel(I18n.format("mxtune.gui.guiPlayListManager.label.status_log"));

        labelPlayListList = new GuiLabel(fontRenderer,3, guiPlayList.getRight() - guiPlayListListWidth, titleTop, guiPlayListListWidth, singleLineHeight, 0xFFFFFF);
        labelPlayListList.addLine("mxtune.gui.guiPlayListManager.label.playlist_selector");

        int buttonWidth = 80;
        int buttonTop = height - 22;
        int xLibrary = (this.width /2) - buttonWidth;
        int xDone = xLibrary + buttonWidth;

        GuiButton buttonImport = new GuiButton(0, xLibrary, buttonTop, buttonWidth, 20, I18n.format("mxtune.gui.button.musicLibrary"));
        GuiButton buttonDone = new GuiButton(1, xDone, buttonTop, buttonWidth, 20, I18n.format("gui.done"));

        int selectButtonWidth = guiFileListWidth - 10;
        int selectButtonLeft = guiFileList.getRight() + 8;

        labelPlaylistName = new GuiLabel(fontRenderer,4, selectButtonLeft, listTop, guiPlayListListWidth, singleLineHeight, 0xFFFFFF);
        labelPlaylistName.addLine("mxtune.gui.guiPlayListManager.label.playlist_name");
        playListName = new GuiTextField(1, fontRenderer, selectButtonLeft, labelPlaylistName.y + singleLineHeight + 2, selectButtonWidth, singleLineHeight + 2);
        buttonToServer = new GuiButton(6, selectButtonLeft, playListName.y + playListName.height + padding, selectButtonWidth, 20, "Send to Server");

        labelPlaylistDay = new GuiLabel(fontRenderer,5, selectButtonLeft, dayTop + padding, guiPlayListListWidth, singleLineHeight, 0xFFFFFF);
        labelPlaylistDay.addLine("mxtune.gui.guiPlayListManager.label.list_day");

        labelPlaylistNight = new GuiLabel(fontRenderer,6, selectButtonLeft, nightTop + padding, guiPlayListListWidth, singleLineHeight, 0xFFFFFF);
        labelPlaylistNight.addLine("mxtune.gui.guiPlayListManager.label.list_night");

        GuiButton buttonToDay = new GuiButton(2, selectButtonLeft, labelPlaylistDay.y + singleLineHeight + 2, selectButtonWidth, 20, I18n.format("mxtune.gui.guiPlayListManager.button.to_day_list"));
        GuiButton buttonToNight = new GuiButton(3, selectButtonLeft, labelPlaylistNight.y + singleLineHeight + 2, selectButtonWidth, 20, I18n.format("mxtune.gui.guiPlayListManager.button.to_night_list"));
        GuiButton buttonDDeleteDay = new GuiButton(4, selectButtonLeft, buttonToDay.y + buttonToDay.height, selectButtonWidth, 20, I18n.format("mxtune.gui.guiPlayListManager.button.delete"));
        GuiButton buttonDDeleteNight = new GuiButton(5, selectButtonLeft, buttonToNight.y + buttonToNight.height, selectButtonWidth, 20, I18n.format("mxtune.gui.guiPlayListManager.button.delete"));

        buttonList.add(buttonImport);
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
        initHooverHelp();
        initAreas();
        initFileList();
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
        guiPlayList.addAll(cachedPlayListGuiList);
        guiPlayList.setSelectedIndex(cachedSelectedPlayListIndex);

        guiFileList.addAll(cachedFileList);
        guiFileList.setSelectedRowIndexes(cachedSelectedSongs);
        guiFileList.setSelectedIndex(cachedSelectedFileDummy);

        guiDay.addAll(cachedDayList);
        guiDay.setSelectedRowIndexes(cachedSelectedDaySongs);
        guiDay.setSelectedIndex(cachedSelectedDaySongDummy);

        guiNight.addAll(cachedNightList);
        guiNight.setSelectedRowIndexes(cachedSelectedNightSongs);
        guiNight.setSelectedIndex(cachedSelectedNightSongDummy);

        playListName.setText(cachedPlayListName);

        guiLogList.addAll(cachedGuiLogList);
        guiLogList.scrollToEnd();

        updateSongCountStatus();
        guiPlayList.resetScroll();
        guiFileList.resetScroll();
    }

    private void updateState()
    {
        cachedPlayListGuiList.clear();
        cachedPlayListGuiList.addAll(guiPlayList.getList());
        cachedSelectedPlayListIndex = guiPlayList.getSelectedIndex();

        cachedSelectedSongs.clear();
        cachedSelectedSongs.addAll(guiFileList.getSelectedRowIndexes());
        cachedSelectedFileDummy = guiFileList.getSelectedIndex();

        cachedSelectedDaySongs.clear();
        cachedSelectedDaySongs.addAll(guiDay.getSelectedRowIndexes());
        cachedDayList.clear();
        cachedDayList.addAll(guiDay.getList());
        cachedSelectedDaySongDummy = guiDay.getSelectedIndex();

        cachedSelectedNightSongs.clear();
        cachedSelectedNightSongs.addAll(guiNight.getSelectedRowIndexes());
        cachedNightList.clear();
        cachedNightList.addAll(guiNight.getList());
        cachedSelectedNightSongDummy = guiNight.getSelectedIndex();

        cachedPlayListName = playListName.getText();

        cachedGuiLogList.clear();
        cachedGuiLogList.addAll(guiLogList.getList());

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
        labelFileList.setLabel(I18n.format("mxtune.gui.guiPlayListManager.label.music_file_selector", guiFileList.getSelectedRowsCount()));
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
        Keyboard.enableRepeatEvents(playListName.isFocused());
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
                mc.displayGuiScreen(new GuiMusicLibrary(this));
                break;
            case 1:
                // Done
                for (SongProxy songProxy : guiDay)
                    if (songProxy != null)
                        ModLogger.debug("Day Song   guid: %s, Title: %s", songProxy.getGUID().toString(), songProxy.getTitle());
                for (SongProxy songProxy : guiNight)
                    if (songProxy != null)
                        ModLogger.debug("Night Song guid: %s, Title: %s", songProxy.getGUID().toString(), songProxy.getTitle());
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

    // This is experimental and inefficient
    private void initFileList()
    {
        new Thread(
                () ->
                {
                    pathSongProxyBiMap.clear();
                    pathSongGuidBiMap.clear();
                    Path pathDir = FileHelper.getDirectory(FileHelper.CLIENT_LIB_FOLDER, Side.CLIENT);
                    PathMatcher filter = FileHelper.getMxtMatcher(pathDir);
                    try (Stream<Path> paths = Files.list(pathDir))
                    {
                        cachedFileList = paths
                                .filter(filter::matches)
                                .collect(Collectors.toList());
                    } catch (NullPointerException | IOException e)
                    {
                        ModLogger.error(e);
                    }

                    List<Path> files = new ArrayList<>();

                    for (Path file : cachedFileList)
                    {
                        files.add(file);
                        SongProxy songProxy = pathToSongProxy(file);
                        if (songProxy != null)
                        {
                            pathSongProxyBiMap.forcePut(file, songProxy);
                            pathSongGuidBiMap.forcePut(file, songProxy.getGUID());
                        }
                        else
                            updateStatus(TextFormatting.DARK_RED + I18n.format("mxtune.gui.guiPlayListManager.log.corrupted_file", (String.format("%s", TextFormatting.RESET + file.getFileName().toString()))));
                    }
                    cachedFileList = files;
                    guiFileList.clear();
                    guiFileList.addAll(files);
                    songProxyPathBiMap = pathSongProxyBiMap.inverse();
                    songGuidPathBiMap = pathSongGuidBiMap.inverse();
                }).start();
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
    private List<SongProxy> pathsToSongProxies(List<Path> paths, List<SongProxy> current)
    {
        List<SongProxy> songList = new ArrayList<>(current);
        for (Path path : paths)
        {
            SongProxy songProxyPath = pathSongProxyBiMap.get(path);
            if (!songList.contains(songProxyPath))
                songList.add(songProxyPath);
        }
        current.clear();
        return SONG_PROXY_ORDERING.sortedCopy(songList);
    }

    private void shipIt()
    {
        uploading = true;
        playListName.setText(playListName.getText().trim());
        Area area = new Area(playListName.getText(), guiDay.getList(), guiNight.getList());
        updateStatus(TextFormatting.GOLD + I18n.format("mxtune.gui.guiPlayListManager.log.upload_playlist", TextFormatting.RESET + playListName.getText().trim()));
        PacketDispatcher.sendToServer(new SetServerSerializedDataMessage(area.getGUID(), SetServerSerializedDataMessage.SetType.AREA, area));

        // Build one list of songs to send from both lists to remove duplicates!
        List<SongProxy> proxyMap = new ArrayList<>(guiDay.getList());
        for (SongProxy songProxy : guiNight.getList())
        {
            if (!proxyMap.contains(songProxy))
                proxyMap.add(songProxy);
        }

        new Thread ( () ->
                     {
                         int count = 0;
                         for (SongProxy songProxy : proxyMap)
                         {
                             count++;
                             if (songProxy != null)
                             {
                                 Song song = pathToSong(songGuidPathBiMap.get(songProxy.getGUID()));
                                 if (song != null)
                                 {
                                     PacketDispatcher.sendToServer(new SetServerSerializedDataMessage(songProxy.getGUID(), SetServerSerializedDataMessage.SetType.MUSIC, song));
                                     updateStatus(TextFormatting.DARK_GREEN + I18n.format("mxtune.gui.guiPlayListManager.log.uploading_music", String.format("%03d", count), String.format("%03d", proxyMap.size()), TextFormatting.RESET + song.getTitle()));
                                 } else
                                 {
                                     updateStatus(TextFormatting.DARK_RED + I18n.format("mxtune.gui.guiPlayListManager.log.not_found_in_library", String.format("%03d", count), String.format("%03d", proxyMap.size()), TextFormatting.RESET + songProxy.getTitle()));
                                 }
                                 try
                                 {
                                     // Don't spam the server. Upload once per second.
                                     Thread.sleep(1000);
                                 } catch (InterruptedException e)
                                 {
                                     Thread.currentThread().interrupt();
                                 }
                             }
                         }
                         uploading = false;
                         updateState();
                     }).start();
        // done
        initAreas();
        updateState();
    }
    
    private void initAreas()
    {
        PacketDispatcher.sendToServer(new GetAreasMessage(CallBackManager.register(ClientFileManager.INSTANCE)));
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
                       guiPlayList.addAll(PLAYLIST_ORDERING.sortedCopy(ClientFileManager.getAreas()));
                       updateState();
                   }).start();
    }

    private void showSelectedPlaylistsPlaylists(Area selectedArea)
    {
        if (selectedArea != null)
        {
            if (!Reference.NO_MUSIC_GUID.equals(selectedArea.getGUID()) && !Reference.EMPTY_GUID.equals(selectedArea.getGUID()))
                playListName.setText(ModGuiUtils.getPlaylistName(selectedArea));
            else
                playListName.setText("");
            guiDay.clear();
            guiNight.clear();
            guiDay.addAll(SONG_PROXY_ORDERING.sortedCopy(selectedArea.getPlayListDay()));
            guiNight.addAll(SONG_PROXY_ORDERING.sortedCopy(selectedArea.getPlayListNight()));
            cachedSelectedDaySongs.clear();
            cachedSelectedNightSongs.clear();
            updateState();
        }
    }

    private void  updatePlayersSelectedAreaGuid(Area selectedArea)
    {
        ModLogger.debug("GuiTest: guidSelectedArea: %s", selectedArea != null ? selectedArea.getGUID().toString() : "[null]");
        ModLogger.debug("GuiTest: selected Name   : %s", ModGuiUtils.getPlaylistName(selectedArea));
        if (selectedArea != null)
        {
            PacketDispatcher.sendToServer(new PlayerSelectedAreaMessage(selectedArea.getGUID()));
            String areaNameOrUndefined = ModGuiUtils.getPlaylistName(selectedArea);
            updateStatus(TextFormatting.GOLD + I18n.format("mxtune.gui.guiPlayListManager.log.set_staff_of_music_playlist", TextFormatting.RESET + areaNameOrUndefined));
        }
        else
        {
            updateStatus(TextFormatting.DARK_RED + I18n.format("mxtune.gui.guiPlayListManager.log.failed_set_staff_of_music_playlist"));
        }
    }

    private void initHooverHelp()
    {
        guiPlayList.addHooverText(TextFormatting.YELLOW + I18n.format("mxtune.gui.guiPlayListManager.label.playlist_selector"));
        guiPlayList.addHooverText(TextFormatting.RESET + I18n.format("mxtune.gui.guiPlayListManager.help.playlist_selector_01"));
        guiPlayList.addHooverText(TextFormatting.RESET + I18n.format("mxtune.gui.guiPlayListManager.help.playlist_selector_02"));
    }
}