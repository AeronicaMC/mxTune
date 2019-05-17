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

import net.aeronica.mods.mxtune.caches.*;
import net.aeronica.mods.mxtune.gui.util.GuiLabelMX;
import net.aeronica.mods.mxtune.gui.util.IHooverText;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.managers.PlayIdSupplier;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.sound.IAudioStatusCallback;
import net.aeronica.mods.mxtune.util.Miscellus;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GuiMXT extends GuiScreen implements IAudioStatusCallback
{
    private List<IHooverText> hooverTexts = new ArrayList<>();
    private GuiScreen guiScreenParent;
    private boolean isStateCached;
    private GuiLabelMX labelMXTFileName;
    private String cachedMXTFilename;
    private GuiLabelMX labelTitle;
    private GuiLabelMX labelAuthor;
    private GuiLabelMX labelSource;
    private GuiTextField textTitle;
    private String cachedTitle = "";
    private GuiTextField textAuthor;
    private String cachedAuthor = "";
    private GuiTextField textSource;
    private String cachedSource = "";
    private GuiButtonExt buttonPlayStop;
    private int ticks;

    // Common data
    MXTuneFile mxTuneFile;
    private boolean isPlaying = false;
    private boolean cachedIsPlaying;
    /* MML Player */
    private int playId = PlayIdSupplier.PlayType.INVALID;

    // Child tabs
    private static final int MAX_TABS = 12;
    private static final int MIN_TABS = 1;
    private static final int TAB_BTN_IDX = 200;
    private GuiMXTPartTab[] childTabs = new GuiMXTPartTab[MAX_TABS];
    private int activeChildIndex;
    private int cachedActiveChildIndex;
    private GuiButtonExt buttonAddTab;
    private GuiButtonExt buttonMinusTab;

    // Tab limits - allow limiting the viewable tabs
    private int viewableTabCount = MIN_TABS;
    private int cachedViewableTabCount;

    public GuiMXT(GuiScreen guiScreenParent)
    {
        this.guiScreenParent = guiScreenParent;
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        for (int i = 0; i< MAX_TABS; i++)
        {
            childTabs[i] = new GuiMXTPartTab(this);
        }
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void initGui()
    {
        hooverTexts.clear();
        int singleLineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int padding = 4;
        int titleWidth = width - padding * 2;
        int tabbedAreaTop = (height * 2) / 5;

        labelMXTFileName = new GuiLabelMX(fontRenderer, 0, padding, padding, titleWidth, singleLineHeight, -1);
        setDisplayedFilename(cachedMXTFilename, TextFormatting.AQUA);
        int buttonWidth = Math.max(((width * 2 / 3) - padding * 2) / 6, 65);
        int buttonY = labelMXTFileName.getY() + labelMXTFileName.getHeight();

        GuiButtonExt buttonNew = new GuiButtonExt(0, padding, buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.new"));
        GuiButtonExt buttonImport = new GuiButtonExt(1, buttonNew.x + buttonNew.width, buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.import"));
        GuiButtonExt buttonOpen = new GuiButtonExt(2, buttonImport.x + buttonImport.width, buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.open"));
        GuiButtonExt buttonSave = new GuiButtonExt(3, buttonOpen.x + buttonOpen.width, buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.save"));
        GuiButtonExt buttonSaveAs = new GuiButtonExt(4, buttonSave.x + buttonSave.width, buttonY, buttonWidth, 20, I18n.format("mxtune.gui.button.save_as"));
        GuiButtonExt buttonDone = new GuiButtonExt(5, buttonSaveAs.x + buttonSaveAs.width, buttonY, buttonWidth, 20, I18n.format("gui.done"));
        buttonList.add(buttonNew);
        buttonList.add(buttonImport);
        buttonList.add(buttonOpen);
        buttonList.add(buttonSave);
        buttonList.add(buttonSaveAs);
        buttonList.add(buttonDone);

        // Text fields
        int textY = buttonDone.y + buttonDone.height + padding;
        String labelTitleText = I18n.format("mxtune.gui.label.title");
        int labelTitleWidth = fontRenderer.getStringWidth(labelTitleText);
        String labelAuthorText = I18n.format("mxtune.gui.label.author");
        int labelAuthorWidth = fontRenderer.getStringWidth(labelAuthorText);
        labelTitle = new GuiLabelMX(fontRenderer, 1, padding, textY, labelTitleWidth, fontRenderer.FONT_HEIGHT + 2, -1);
        labelTitle.setLabelName(labelTitleText);
        textTitle = new GuiTextField(1, fontRenderer, labelTitle.getX() + labelTitleWidth + padding, textY, width / 2 - labelTitle.getWidth() - padding, fontRenderer.FONT_HEIGHT + 2);
        textTitle.setMaxStringLength(80);
        textTitle.setCanLoseFocus(true);
        labelAuthor = new GuiLabelMX(fontRenderer, 2, textTitle.x + textTitle.width + padding, textY, labelAuthorWidth, fontRenderer.FONT_HEIGHT + 2, -1);
        labelAuthor.setLabelName(labelAuthorText);
        textAuthor = new GuiTextField(2, fontRenderer, labelAuthor.getX() + labelAuthorWidth + padding, textY, width - labelAuthor.getX() - labelAuthor.getWidth() - padding * 2, fontRenderer.FONT_HEIGHT + 2);
        textAuthor.setMaxStringLength(80);
        textAuthor.setCanLoseFocus(true);
        textY = textTitle.y + textTitle.height + padding;
        String labelSourceText = I18n.format("mxtune.gui.label.source");
        int labelSourceWidth = fontRenderer.getStringWidth(labelSourceText);
        labelSource = new GuiLabelMX(fontRenderer, 3, padding, textY, labelSourceWidth, fontRenderer.FONT_HEIGHT + 2, -1);
        labelSource.setLabelName(labelSourceText);
        textSource = new GuiTextField(3, fontRenderer, labelSource.getX() + labelSource.getWidth() + padding, textY, width - labelSource.getX() - labelSource.getWidth() - padding * 2, fontRenderer.FONT_HEIGHT + 2);
        textSource.setMaxStringLength(320);
        textSource.setCanLoseFocus(true);

        // Button tabs
        buttonAddTab = new GuiButtonExt(250, padding, tabbedAreaTop - 25, 20, 20, I18n.format("mxtune.gui.button.plus"));
        buttonMinusTab = new GuiButtonExt(251, buttonAddTab.x + 20, tabbedAreaTop - 25, 20, 20, I18n.format("mxtune.gui.button.minus"));
        buttonList.add(buttonAddTab);
        buttonList.add(buttonMinusTab);
        for (int i = 0; i< MAX_TABS; i++)
        {
            buttonList.add(new GuiButton(TAB_BTN_IDX + i, buttonMinusTab.x + 40 + 20 * i, tabbedAreaTop - 25, 20, 20, String.format("%d", i + 1)));
            childTabs[i].setLayout(tabbedAreaTop, height - padding, height - padding - tabbedAreaTop);
            childTabs[i].initGui();
        }

        // Play/Stop
        buttonPlayStop = new GuiButtonExt(6, width - padding - 100, tabbedAreaTop - 25, 100, 20, isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play_all"));
        buttonPlayStop.enabled = false;
        buttonList.add(buttonPlayStop);
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
        isStateCached = true;
    }

    private void updateButtons()
    {
        for (GuiButton button : buttonList)
            if (button.id >= TAB_BTN_IDX && button.id < (MAX_TABS + TAB_BTN_IDX))
            {
                button.enabled = (activeChildIndex + TAB_BTN_IDX) != button.id;
                button.visible = (button.id) < (viewableTabCount + TAB_BTN_IDX);
                if (activeChildIndex >= viewableTabCount)
                    activeChildIndex = viewableTabCount - 1;
            }
        buttonAddTab.enabled = viewableTabCount < 12;
        buttonMinusTab.enabled = viewableTabCount > 1;

        // Play/Stop button state
        int countOK = 0;
        for (int i=0; i < viewableTabCount; i++)
        {
            countOK = childTabs[i].canPlay() ? countOK + 1 : countOK;
        }
        boolean isOK = countOK == viewableTabCount;
        buttonPlayStop.enabled = isPlaying || isOK;
        buttonPlayStop.displayString = isPlaying ? I18n.format("mxtune.gui.button.stop") : I18n.format("mxtune.gui.button.play_all");
    }

    @Override
    public void updateScreen()
    {
        textTitle.updateCursorCounter();
        textAuthor.updateCursorCounter();
        textSource.updateCursorCounter();
        childTabs[activeChildIndex].updateScreen();
        if (ticks++ % 5 == 0) updateButtons();
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id >= TAB_BTN_IDX && button.id < TAB_BTN_IDX + MAX_TABS)
        {
            this.activeChildIndex = button.id - TAB_BTN_IDX;
            this.childTabs[activeChildIndex].onResize(mc, width, height);
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
                // Save As
                saveAsAction();
                break;
            case 5:
                // Done
                doneAction();
                break;
            case 6:
                // Play/Stop
                play();
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
        activeChildIndex = viewableTabCount;
    }

    private void minusTab()
    {
        viewableTabCount = (viewableTabCount - 1) >= MIN_TABS ? viewableTabCount - 1 : viewableTabCount;
    }

    private boolean canAddTab()
    {
        return (viewableTabCount + 1) < MAX_TABS;
    }

    private void newAction()
    {
        stop();
        updateState();
        mc.displayGuiScreen(new GuiYesNo(this, "New File! This will clear all fields and parts!","Confirm?",0));
    }

    private void importAction()
    {
        // NOP
    }

    private void openAction()
    {
        stop();
        ActionGet.INSTANCE.clear();
        ActionGet.INSTANCE.setFileOpen();
        viewableTabCount = MIN_TABS;
        mc.displayGuiScreen(new GuiMusicLibrary(this));
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
            if (mxTuneFile == null)
                mxTuneFile = new MXTuneFile();
            mxTuneFile.setTitle(textTitle.getText().trim());
            mxTuneFile.setAuthor(textAuthor.getText().trim());
            mxTuneFile.setSource(textSource.getText().trim());
            List<MXTunePart> parts = new ArrayList<>();
            for (int i = 0; i < viewableTabCount; i++)
            {
                childTabs[i].updatePart();
                MXTunePart part = childTabs[i].getPart();
                if (part.getStaves().size() > 0)
                    parts.add(part);
            }
            mxTuneFile.setParts(parts);

            NBTTagCompound compound = new NBTTagCompound();
            mxTuneFile.writeToNBT(compound);
            try
            {
                FileHelper.sendCompoundToFile(FileHelper.getCacheFile(FileHelper.CLIENT_LIB_FOLDER, fileName + FileHelper.EXTENSION_MXT, Side.CLIENT), compound);
            }
            catch (IOException e)
            {
                ModLogger.error(e);
            }
        }
        updateState();
    }

    private void saveAsAction()
    {
        // NOP
    }

    private void doneAction()
    {
        // Todo: Warning if un-saved! Quit Yes/No dialog
        stop();
        updateState();
        mc.displayGuiScreen(new GuiYesNo(this, "Have you saved changes?","Do you still want to exit?",5));
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
                break;
            case FILE_OPEN:
                fileOpen();
            case FILE_SAVE:
                break;
            case FILE_SAVE_AS:
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
        for (int i = 0; i< MAX_TABS; i++)
            childTabs[i].clearPart();
        viewableTabCount = MIN_TABS;
        activeChildIndex = 0;
        updateState();
    }

    private void fileOpen()
    {
        mxTuneFile = MXTuneFileHelper.getMXTuneFile(ActionGet.INSTANCE.getPath());
        if (mxTuneFile != null)
        {
            textTitle.setText(mxTuneFile.getTitle());
            textAuthor.setText(mxTuneFile.getAuthor());
            textSource.setText(mxTuneFile.getSource());
            int tab = 0;
            setDisplayedFilename(ActionGet.INSTANCE.getFileNameString(), TextFormatting.AQUA);
            for (MXTunePart part : mxTuneFile.getParts())
            {
                if (canAddTab())
                {
                    childTabs[tab++].setPart(part);
                    if ((tab < mxTuneFile.getParts().size()))
                        addTab();
                }
            }
        }
        else
        {
            Miscellus.audiblePingPlayer(mc.player, SoundEvents.BLOCK_GLASS_BREAK);
            fileNew();
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
                mc.displayGuiScreen(this);
                break;
            case 5:
                // Done! Unsaved Changes! Go back and save?
                if (result)
                    mc.displayGuiScreen(guiScreenParent);
                else
                    mc.displayGuiScreen(this);
                break;
            default:
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            // Unsaved warning! Exit Yes or No
            return;
        }
        childTabs[activeChildIndex].keyTyped(typedChar, keyCode);
        textTitle.textboxKeyTyped(typedChar, keyCode);
        textAuthor.textboxKeyTyped(typedChar, keyCode);
        textSource.textboxKeyTyped(typedChar, keyCode);
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();

        labelMXTFileName.drawLabel(mc, mouseX, mouseY);
        labelTitle.drawLabel(mc, mouseX, mouseY);
        textTitle.drawTextBox();
        labelAuthor.drawLabel(mc, mouseX, mouseY);
        textAuthor.drawTextBox();
        labelSource.drawLabel(mc, mouseX, mouseY);
        textSource.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
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

    /**
     * Called when a mouse button is released.
     */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onResize(@Nonnull Minecraft mcIn, int w, int h)
    {
        super.onResize(mcIn, w, h);
        childTabs[activeChildIndex].onResize(mcIn, w, h);
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
        Minecraft.getMinecraft().addScheduledTask(() -> {
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
}
