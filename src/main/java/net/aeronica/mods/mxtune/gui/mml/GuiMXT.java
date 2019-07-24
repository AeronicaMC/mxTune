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

import net.aeronica.mods.mxtune.caches.FileHelper;
import net.aeronica.mods.mxtune.config.MXTuneConfig;
import net.aeronica.mods.mxtune.gui.util.GuiLabelMX;
import net.aeronica.mods.mxtune.gui.util.GuiLink;
import net.aeronica.mods.mxtune.gui.util.IHooverText;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.managers.PlayIdSupplier;
import net.aeronica.mods.mxtune.managers.records.RecordType;
import net.aeronica.mods.mxtune.mxt.MXTuneFile;
import net.aeronica.mods.mxtune.mxt.MXTuneFileHelper;
import net.aeronica.mods.mxtune.mxt.MXTunePart;
import net.aeronica.mods.mxtune.mxt.MXTuneStaff;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.bidirectional.SetServerSerializedDataMessage;
import net.aeronica.mods.mxtune.network.server.MusicTextMessage;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.IAudioStatusCallback;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.aeronica.mods.mxtune.util.ValidDuration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class GuiMXT extends Screen implements IAudioStatusCallback
{
    private List<IHooverText> hooverTexts = new ArrayList<>();
    private Screen guiScreenParent;
    private boolean isStateCached;
    private GuiLabelMX labelMXTFileName;
    private String cachedMXTFilename;
    private GuiLabelMX labelTitle;
    private GuiLabelMX labelAuthor;
    private GuiLabelMX labelSource;
    private GuiLabelMX labelDuration;
    private TextFieldWidget textTitle;
    private String cachedTitle = "";
    private TextFieldWidget textAuthor;
    private String cachedAuthor = "";
    private TextFieldWidget textSource;
    private String cachedSource = "";
    private GuiButtonExt buttonPlayStop;
    private GuiButtonExt buttonDoneMode;
    private int durationTotal;
    private int ticks;

    // Links
    private GuiLink sourcesLink;
    private GuiLink mmlLink;

    // Common data
    private MXTuneFile mxTuneFile;
    private boolean isPlaying = false;
    private boolean cachedIsPlaying;
    private GuiButtonExt buttonSave;
    private static final int PADDING = 4;
    private Mode mode;

    /* MML Player */
    private int playId = PlayIdSupplier.PlayType.INVALID;

    // Child tabs
    private static final int MAX_TABS = 10;
    private static final int MIN_TABS = 1;
    private static final int TAB_BTN_IDX = 200;
    private GuiMXTPartTab[] childTabs = new GuiMXTPartTab[MAX_TABS];
    private int activeChildIndex;
    private int cachedActiveChildIndex;
    private GuiButtonExt buttonAddTab;
    private GuiButtonExt buttonMinusTab;
    private GuiButtonExt[] buttonNames = new GuiButtonExt[MAX_TABS];

    // Tab limits - allow limiting the viewable tabs
    private int viewableTabCount = MIN_TABS;
    private int cachedViewableTabCount;

    public GuiMXT(Screen guiScreenParent, Mode mode)
    {
        this.guiScreenParent = guiScreenParent;
        this.minecraft = Minecraft.getInstance();
        this.font = minecraft.fontRenderer;
        this.mode = mode;
        for (int i = 0; i < MAX_TABS; i++)
        {
            childTabs[i] = new GuiMXTPartTab(this);
        }
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void init()
    {
        hooverTexts.clear();
        int singleLineHeight = minecraft.fontRenderer.FONT_HEIGHT + 2;
        int titleWidth = width - PADDING * 2;

        labelMXTFileName = new GuiLabelMX(font, 0, PADDING, PADDING, titleWidth, singleLineHeight, -1);
        setDisplayedFilename(cachedMXTFilename, TextFormatting.AQUA);
        int buttonWidth = Math.max(((width * 2 / 3) - PADDING * 2) / 6, 65);
        int buttonY = labelMXTFileName.getY() + labelMXTFileName.getHeight();

        GuiButtonExt buttonNew = new GuiButtonExt(0, PADDING, buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.new"));
        GuiButtonExt buttonImport = new GuiButtonExt(1, buttonNew.x + buttonNew.getWidth(), buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.import"));
        GuiButtonExt buttonOpen = new GuiButtonExt(2, buttonImport.x + buttonImport.getWidth(), buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.open"));
        buttonSave = new GuiButtonExt(3, buttonOpen.x + buttonOpen.getWidth(), buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.save"));
        buttonDoneMode = new GuiButtonExt(4, buttonSave.x + buttonSave.getWidth(), buttonY, buttonWidth, 20, getDoneButtonNameByMode());
        GuiButtonExt buttonCancel = new GuiButtonExt(5, buttonDoneMode.x + buttonDoneMode.getWidth(), buttonY, buttonWidth, 20, I18n.format("gui.cancel"));
        if (mode == Mode.CLIENT)
            buttonCancel.visible = false;

        children.add(buttonNew);
        children.add(buttonImport);
        children.add(buttonOpen);
        children.add(buttonSave);
        children.add(buttonDoneMode);
        children.add(buttonCancel);

        // Links
        int textY = buttonDoneMode.y + buttonDoneMode.getHeight() + PADDING;
        int urlWidth = width / 2 - PADDING;
        sourcesLink = new GuiLink(10, PADDING, textY, cachedSource, GuiLink.AlignText.LEFT);
        sourcesLink.setWidth(urlWidth);
        sourcesLink.setHeight(singleLineHeight);
        mmlLink = new GuiLink(11, width - PADDING , textY, MXTuneConfig.getMmlLink(), GuiLink.AlignText.RIGHT);
        mmlLink.setWidth(urlWidth);
        mmlLink.setHeight(singleLineHeight);
        children.add(sourcesLink);
        children.add(mmlLink);

        // Text fields
        textY = sourcesLink.y + sourcesLink.getHeight() + PADDING;
        String labelTitleText = I18n.format("mxtune.gui.label.title");
        int labelTitleWidth = font.getStringWidth(labelTitleText);
        String labelAuthorText = I18n.format("mxtune.gui.label.author");
        int labelAuthorWidth = font.getStringWidth(labelAuthorText);
        labelTitle = new GuiLabelMX(font, 1, PADDING, textY, labelTitleWidth, font.FONT_HEIGHT + 2, -1);
        labelTitle.setLabelName(labelTitleText);
        textTitle = new TextFieldWidget(1, font, labelTitle.getX() + labelTitleWidth + PADDING, textY, width / 2 - labelTitle.getWidth() - PADDING, font.FONT_HEIGHT + 2);
        textTitle.setMaxStringLength(80);
        textTitle.setCanLoseFocus(true);
        labelAuthor = new GuiLabelMX(font, 2, textTitle.x + textTitle.getWidth() + PADDING, textY, labelAuthorWidth, font.FONT_HEIGHT + 2, -1);
        labelAuthor.setLabelName(labelAuthorText);
        textAuthor = new TextFieldWidget(2, font, labelAuthor.getX() + labelAuthorWidth + PADDING, textY, width - labelAuthor.getX() - labelAuthor.getWidth() - PADDING * 2, font.FONT_HEIGHT + 2);
        textAuthor.setMaxStringLength(80);
        textAuthor.setCanLoseFocus(true);
        textY = textTitle.y + textTitle.getHeight() + PADDING;
        String labelSourceText = I18n.format("mxtune.gui.label.source");
        int labelSourceWidth = font.getStringWidth(labelSourceText);
        labelSource = new GuiLabelMX(font, 3, PADDING, textY, labelSourceWidth, font.FONT_HEIGHT + 2, -1);
        labelSource.setLabelName(labelSourceText);
        textSource = new TextFieldWidget(3, font, labelSource.getX() + labelSource.getWidth() + PADDING, textY, width - labelSource.getX() - labelSource.getWidth() - PADDING * 2, font.FONT_HEIGHT + 2);
        textSource.setMaxStringLength(320);
        textSource.setCanLoseFocus(true);

        // Button tabs
        int tabButtonTop = textSource.y +  textSource.getHeight() + PADDING * 2;
        buttonAddTab = new GuiButtonExt(250, PADDING, tabButtonTop, 40, 20, I18n.format("mxtune.gui.button.plus"));
        buttonMinusTab = new GuiButtonExt(251, buttonAddTab.x + buttonAddTab.getWidth(), tabButtonTop, 40, 20, I18n.format("mxtune.gui.button.minus"));
        children.add(buttonAddTab);
        children.add(buttonMinusTab);

        // Total Duration
        String durationLabelName = I18n.format("mxtune.gui.label.total_duration");
        int durationWidth = font.getStringWidth(durationLabelName);
        durationWidth += font.getStringWidth(SheetMusicUtil.formatDuration(0));
        durationWidth += font.getStringWidth(" ");
        labelDuration = new GuiLabelMX(font,5, width - durationWidth - PADDING, tabButtonTop + 20 + PADDING + 2, durationWidth, singleLineHeight, -1);
        labelDuration.setLabelName(I18n.format(durationLabelName));
        labelDuration.setLabelText(SheetMusicUtil.formatDuration(durationTotal));

        int tabbedAreaTop = tabButtonTop + 20 + PADDING;
        for (int i = 0; i < MAX_TABS; i++)
        {
            buttonNames[i] = new GuiButtonExt(TAB_BTN_IDX + i, buttonMinusTab.x + buttonMinusTab.getWidth() + PADDING + 20 * i, tabButtonTop, 20, 20, String.format("%d", i + 1));
            children.add(buttonNames[i]);
            childTabs[i].setLayout(tabbedAreaTop, height - PADDING);
            childTabs[i].initGui();
        }

        // Play/Stop
        buttonPlayStop = new GuiButtonExt(6, width - PADDING - 100, tabbedAreaTop - 24, 100, 20, isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play_all"));
        buttonPlayStop.active = false;
        children.add(buttonPlayStop);
        reloadState();
        getSelection();
    }

    private void reloadState()
    {
        updateButtons();
        if (!isStateCached) return;
        labelMXTFileName.setLabelText(cachedMXTFilename);
        activeChildIndex = cachedActiveChildIndex;
        viewableTabCount = cachedViewableTabCount;
        textTitle.setText(cachedTitle);
        textAuthor.setText(cachedAuthor);
        textSource.setText(cachedSource);
        isPlaying = cachedIsPlaying;
    }

    private void updateState()
    {
        cachedMXTFilename = labelMXTFileName.getLabelText();
        cachedActiveChildIndex = activeChildIndex;
        cachedViewableTabCount = viewableTabCount;
        cachedTitle = textTitle.getText();
        cachedAuthor = textAuthor.getText();
        cachedSource = textSource.getText();
        cachedIsPlaying = isPlaying;
        sourcesLink.setMessage(cachedSource);
        isStateCached = true;
    }

    private void updateButtons()
    {
        for (IGuiEventListener guiListener : children)
        {
            if (guiListener instanceof Button)
            {
                Button button = (Button) guiListener;
                if ((button.id >= TAB_BTN_IDX && button.id < (MAX_TABS + TAB_BTN_IDX))
                {
                    button.active = (activeChildIndex + TAB_BTN_IDX) != button.id;
                    button.visible = (button.id) < (viewableTabCount + TAB_BTN_IDX);
                    if (activeChildIndex >= viewableTabCount)
                        activeChildIndex = viewableTabCount - 1;
                }
            }
        }
        buttonAddTab.active = viewableTabCount < MAX_TABS;
        buttonMinusTab.active = viewableTabCount > MIN_TABS;

        // Play/Stop button state and duration total
        int countOK = 0;
        int duration = 0;
        for (int i=0; i < viewableTabCount; i++)
        {
            countOK = childTabs[i].canPlay() ? countOK + 1 : countOK;
            int partDuration = childTabs[i].getDuration();
            duration = partDuration > duration ? partDuration : duration;
        }
        durationTotal = duration;
        labelDuration.setLabelText(SheetMusicUtil.formatDuration(durationTotal));
        boolean isOK = countOK == viewableTabCount;
        buttonPlayStop.active = isPlaying || isOK;
        buttonDoneMode.active = Mode.CLIENT == mode || (!textTitle.getText().isEmpty() && isOK);
        buttonPlayStop.setMessage(isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play_all"));
        buttonSave.active = !textTitle.getText().isEmpty() && isOK;

        sourcesLink.visible = sourcesLink.getMessage().matches("^(http(s)?:\\/\\/[a-zA-Z0-9\\-_]+\\.[a-zA-Z]+(.)+)+");
    }

    private void updateTabbedButtonNames()
    {
        int prevWidth = buttonMinusTab.x + buttonMinusTab.getWidth();
        int staticButtonWidth = buttonMinusTab.getWidth() + buttonAddTab.getWidth() + buttonPlayStop.getWidth() +  PADDING * 3;
        for (int i = 0; i < viewableTabCount; i++)
        {
            GuiButtonExt gb = buttonNames[i];
            String name = getButtonName(i);
            int nameWidth = font.getStringWidth(name) + PADDING * 4;
            gb.x = prevWidth + PADDING;
            gb.setMessage(name);
            int limitedWidth = Math.min((width - staticButtonWidth) / viewableTabCount, Math.max(20, nameWidth));
            gb.setWidth(limitedWidth);
            prevWidth += limitedWidth;
        }
    }

    private String getButtonName(int index)
    {
        String number = String.format("%d", index + 1);
        MXTunePart part = childTabs[index].getPart();
        return (part != null) && (!part.getInstrumentName().equals("")) ? number + ": " + childTabs[index].getPart().getInstrumentName() : number;
    }

    private void drawMarkers()
    {
        for (int i = 0; i < viewableTabCount; i++)
        {
            GuiButtonExt gb = buttonNames[i];
            if (!gb.active)
                drawBox(gb, -1);
           if (!childTabs[i].canPlay())
                drawLine(gb, 0xFFFF2222);
        }
    }

    private void drawBox(GuiButtonExt gb, int color)
    {
        hLine(gb.x, gb.x + gb.getWidth() - 1, gb.y, color);
        hLine(gb.x, gb.x + gb.getWidth() - 1, gb.y + gb.getHeight() - 1, color);
        vLine(gb.x, gb.y, gb.y + gb.getHeight() - 1, color);
        vLine(gb.x + gb.getWidth() - 1, gb.y, gb.y + gb.getHeight() - 1, color);
    }

    private void drawLine(GuiButtonExt gb, int color)
    {
        hLine(gb.x + 1, gb.y + gb.getHeight() + 1, gb.x + 1 + gb.getHeight() - 2, gb.y + gb.getHeight() + 3);
    }

    @Override
    public void tick()
    {
        textTitle.tick();
        textAuthor.tick();
        textSource.tick();
        childTabs[activeChildIndex].updateScreen();
        if (ticks++ % 5 == 0) updateButtons();
    }

    private String getDoneButtonNameByMode()
    {
        switch (mode)
        {
            case CLIENT:
                return I18n.format("gui.done");
            case SERVER:
                return I18n.format("mxtune.gui.button.upload");
            case SHEET_MUSIC:
                return I18n.format("mxtune.gui.button.create");
                default:
        }
        return I18n.format("gui.none");
    }

    @Override
    protected void actionPerformed(Button button)
    {
        if (button.id >= TAB_BTN_IDX && button.id < TAB_BTN_IDX + MAX_TABS)
        {
            this.activeChildIndex = button.id - TAB_BTN_IDX;
            this.childTabs[activeChildIndex].onResize(minecraft, width, height);
        }
        switch (button.id)
        {
            case 0:
                // New
                newAction();
                break;
            case 1:
                // Import
                importAction();
                break;
            case 2:
                // Open
                openAction();
                break;
            case 3:
                // Save
                saveAction();
                break;
            case 4:
                // DoneMode. Depends on mode.
                doneAction();
                break;
            case 5:
                // Cancel
                cancelAction();
                break;
            case 6:
                // Play/Stop
                play();
                break;
            case 10:
                // Source Link
                handleComponentClick(sourcesLink.getLinkComponent());
                break;
            case 11:
                // MML Link
                handleComponentClick(mmlLink.getLinkComponent());
                break;
            case 250:
                // Add Tab
                addTab();
                break;
            case 251:
                // Minus Tab
                minusTab();
                break;
            default:
        }
        updateButtons();
        updateState();
    }

    private void addTab()
    {
        viewableTabCount = (viewableTabCount + 1) > MAX_TABS ? viewableTabCount : viewableTabCount + 1;
        activeChildIndex = viewableTabCount - 1;
    }

    private void minusTab()
    {
        viewableTabCount = (viewableTabCount - 1) >= MIN_TABS ? viewableTabCount - 1 : viewableTabCount;
    }

    private void newAction()
    {
        stop();
        updateState();
        minecraft.displayGuiScreen(new ConfirmScreen(this, "New File! This will clear all fields and parts!", "Confirm?", 0));
    }

    private void importAction()
    {
        stop();
        ActionGet.INSTANCE.clear();
        ActionGet.INSTANCE.setFileImport();
        viewableTabCount = MIN_TABS;
        minecraft.displayGuiScreen(new GuiFileSelector(this));
    }

    private void openAction()
    {
        stop();
        ActionGet.INSTANCE.clear();
        ActionGet.INSTANCE.setFileOpen();
        viewableTabCount = MIN_TABS;
        minecraft.displayGuiScreen(new GuiMusicLibrary(this));
    }

    private void saveAction()
    {
        // TODO: clean this up and toss warnings as needed.
        String fileName = FileHelper.removeExtension(labelMXTFileName.getLabelText());
        if (fileName.length() < 1)
        {
            fileName = FileHelper.normalizeFilename(textTitle.getText().trim());
            labelMXTFileName.setLabelText(fileName);
        }
        if (!fileName.equals("") && !textTitle.getText().trim().equals(""))
        {
            createMxt();
            CompoundNBT compound = new CompoundNBT();
            mxTuneFile.writeToNBT(compound);
            try
            {
                FileHelper.sendCompoundToFile(FileHelper.getCacheFile(FileHelper.CLIENT_LIB_FOLDER, fileName + FileHelper.EXTENSION_MXT, LogicalSide.CLIENT), compound);
            }
            catch (IOException e)
            {
                ModLogger.error(e);
            }
        }
        updateState();
    }

    private boolean uploadMxt()
    {
        if (!textTitle.getText().trim().equals("") && buttonPlayStop.active)
        {
            createMxt();
            PacketDispatcher.sendToServer(new SetServerSerializedDataMessage(mxTuneFile.getGUID(), RecordType.MXT, mxTuneFile));
            return true;
        }
        else
            Miscellus.audiblePingPlayer(minecraft.player, net.minecraft.util.SoundEvents.BLOCK_ANVIL_PLACE);
        return false;
    }

    private void createMxt()
    {
        if (mxTuneFile == null)
            mxTuneFile = new MXTuneFile();
        ValidDuration validDuration = SheetMusicUtil.validateMML(getMML());
        mxTuneFile.setDuration(validDuration.getDuration());
        mxTuneFile.setTitle(textTitle.getText().trim());
        mxTuneFile.setAuthor(textAuthor.getText().trim());
        mxTuneFile.setSource(textSource.getText().trim());
        List<MXTunePart> parts = new ArrayList<>();
        for (int i = 0; i < viewableTabCount; i++)
        {
            childTabs[i].updatePart();
            MXTunePart part = childTabs[i].getPart();
            if (!part.getStaves().isEmpty())
                parts.add(part);
        }
        mxTuneFile.setParts(parts);
    }

    private boolean makeSheetMusic()
    {
        if (!textTitle.getText().trim().equals("") && buttonPlayStop.active)
        {
            String title = this.textTitle.getText();
            for (int i = 0; i < viewableTabCount; i++)
            {
                childTabs[i].updatePart();
                String titleAndInstrument = formatTitle(title, i, viewableTabCount, childTabs[i].getPart().getInstrumentName());
                String mml = childTabs[i].getMMLClipBoardFormat();
                PacketDispatcher.sendToServer(new MusicTextMessage(titleAndInstrument, mml));
            }
            return true;
        }
        else
            Miscellus.audiblePingPlayer(minecraft.player, net.minecraft.util.SoundEvents.BLOCK_ANVIL_PLACE);
        return false;
    }

    private void doneAction()
    {
        // Todo: Warning if un-saved! Quit Yes/No dialog
        stop();
        updateState();
        switch (mode)
        {
            case CLIENT:
                minecraft.displayGuiScreen(new ConfirmScreen(this, "Have you saved changes?", "Do you still want to exit?", 4));
                break;
            case SERVER:
                if (uploadMxt())
                    minecraft.displayGuiScreen(guiScreenParent);
                break;
            case SHEET_MUSIC:
                if (makeSheetMusic())
                    minecraft.displayGuiScreen(guiScreenParent);
                break;
                default:
        }
    }

    private void cancelAction()
    {
        stop();
        minecraft.displayGuiScreen(guiScreenParent);
    }

    private void getSelection()
    {
        switch (ActionGet.INSTANCE.getSelector())
        {
            case DONE:
                break;
            case FILE_NEW:
                fileNew();
                break;
            case FILE_IMPORT:
                fileImport();
                break;
            case FILE_OPEN:
                fileOpen();
                break;
            case FILE_SAVE:
                break;
            default:
        }
        ActionGet.INSTANCE.setCancel();
    }

    private void fileNew()
    {
        mxTuneFile = null;
        setDisplayedFilename("", TextFormatting.AQUA);
        textTitle.setText("");
        textAuthor.setText("");
        textSource.setText("");
        IntStream.range(0, MAX_TABS).forEach(i -> childTabs[i].clearPart());
        viewableTabCount = MIN_TABS;
        activeChildIndex = 0;
        updateState();
    }

    private void fileImport()
    {
        mxTuneFile = ActionGet.INSTANCE.getMxTuneFile();
        getMXTFileData();
    }

    private void fileOpen()
    {
        mxTuneFile = MXTuneFileHelper.getMXTuneFile(ActionGet.INSTANCE.getPath());
        getMXTFileData();
    }

    private void getMXTFileData()
    {
        if (mxTuneFile != null)
        {
            IntStream.range(0, MAX_TABS).forEach(i -> childTabs[i].clearPart());
            viewableTabCount = MIN_TABS;
            activeChildIndex = 0;
            textTitle.setText(mxTuneFile.getTitle());
            textAuthor.setText(mxTuneFile.getAuthor());
            textSource.setText(mxTuneFile.getSource());
            int tab = 0;
            setDisplayedFilename(ActionGet.INSTANCE.getFileNameString(), TextFormatting.AQUA);
            Iterator<MXTunePart> iterator = mxTuneFile.getParts().iterator();
            while (iterator.hasNext() && tab < MAX_TABS)
            {
                childTabs[tab++].setPart(iterator.next());
                if (iterator.hasNext())
                    addTab();
            }
        }
        else
        {
            Miscellus.audiblePingPlayer(minecraft.player, SoundEvents.BLOCK_GLASS_BREAK);
        }
    }

    @Override
    public void confirmClicked(boolean result, int id)
    {
        switch (id)
        {
            case 0:
                // NewAction
                if (result)
                    fileNew();
                minecraft.displayGuiScreen(this);
                break;
            case 4:
                // Done! Unsaved Changes! Go back and save?
                if (result)
                    minecraft.displayGuiScreen(guiScreenParent);
                else
                    minecraft.displayGuiScreen(this);
                break;
            default:
        }
        super.confirmClicked(result, id);
    }

    @Override
    protected boolean charTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            // Unsaved warning! Exit Yes or No
            return this.getFocused() != null && this.getFocused().charTyped(typedChar, keyCode);
        }
        childTabs[activeChildIndex].keyTyped(typedChar, keyCode);
        textTitle.charTyped(typedChar, keyCode);
        textAuthor.charTyped(typedChar, keyCode);
        textSource.charTyped(typedChar, keyCode);
        updateState();
        super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground();
        // update tabbed button names here!
        updateTabbedButtonNames();
        labelMXTFileName.drawLabel(minecraft, mouseX, mouseY);
        labelTitle.drawLabel(minecraft, mouseX, mouseY);
        textTitle.render(mouseX, mouseX, partialTicks);
        labelAuthor.drawLabel(minecraft, mouseX, mouseY);
        textAuthor.render(mouseX, mouseX, partialTicks);
        labelSource.drawLabel(minecraft, mouseX, mouseY);
        textSource.render(mouseX, mouseX, partialTicks);
        labelDuration.drawLabel(minecraft, mouseX, mouseY);
        super.render(mouseX, mouseY, partialTicks);
        drawMarkers();
        childTabs[activeChildIndex].drawScreen(mouseX, mouseY, partialTicks);
        ModGuiUtils.INSTANCE.drawHooveringHelp(this, hooverTexts, 0, 0, mouseX, mouseY);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        updateState();
        childTabs[activeChildIndex].handleMouseInput();
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        textTitle.mouseClicked(mouseX, mouseY, mouseButton);
        textAuthor.mouseClicked(mouseX, mouseY, mouseButton);
        textSource.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void init(@Nonnull Minecraft mcIn, int w, int h)
    {
        super.init(mcIn, w, h);
        childTabs[activeChildIndex].init(mcIn, w, h);
    }

    private void setDisplayedFilename(@Nullable String name, TextFormatting textFormatting)
    {
        if (name == null) name = "";
        labelMXTFileName.setLabelName(I18n.format("mxtune.gui.guiMXT.displayedFilename"));
        labelMXTFileName.setLabelText(name);
        labelMXTFileName.setTextFormatting(textFormatting);
    }

    private boolean mmlPlay(String mmlIn)
    {
        playId = PlayIdSupplier.PlayType.PERSONAL.getAsInt();
        ClientAudio.playLocal(playId, mmlIn, this);
        return true;
    }

    private void play()
    {
        if (isPlaying)
        {
            stop();
        }
        else
        {
            isPlaying = mmlPlay(getMML());
        }
    }

    public String getMML()
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < viewableTabCount; i++)
        {
            childTabs[i].updatePart();
            MXTunePart part = childTabs[i].getPart();
            builder.append("MML@I=").append(part.getPackedPatch());
            Iterator<MXTuneStaff> iterator = part.getStaves().iterator();
            while (iterator.hasNext())
            {
                builder.append(iterator.next().getMml());
                if (iterator.hasNext())
                    builder.append(",");
            }
            builder.append(";");
        }
        return builder.toString();
    }

    @Override
    public void statusCallBack(ClientAudio.Status status, int playId)
    {
        Minecraft.getInstance().execute(() -> {
            if (this.playId == playId && (status == ClientAudio.Status.ERROR || status == ClientAudio.Status.DONE))
            {
                ModLogger.debug("AudioStatus event received: %s, playId: %s", status, playId);
                stop();
            }
        });
    }

    private void stop()
    {
        ClientAudio.fadeOut(playId, 1);
        isPlaying = false;
        playId = PlayIdSupplier.PlayType.INVALID;

        // stop child tabs
        Arrays.stream(childTabs).forEach(GuiMXTPartTab::onGuiClosed);
        updateState();
    }

    public enum Mode
    {
        CLIENT, SERVER, SHEET_MUSIC
    }

    private String formatTitle(String title, int part, int parts, String instrumentName)
    {
        if (parts == 1)
            return String.format("%s (%s)", title, instrumentName);
        else
            return String.format("%s (%d-%d : %s)", title, part + 1, parts, instrumentName);
    }
}
