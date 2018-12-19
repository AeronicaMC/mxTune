/*
 * Aeronica's mxTune MOD
 * Copyright {2018} Paul Boese a.k.a. Aeronica
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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.blocks.TileBandAmp;
import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.BandAmpMessage;
import net.aeronica.mods.mxtune.sound.SoundRange;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.aeronica.mods.mxtune.world.LockableHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLockIconButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static net.aeronica.mods.mxtune.gui.GuiRedstoneButton.ArrowFaces;

public class GuiBandAmp extends GuiContainer
{
    static final ResourceLocation BG_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/band_amp.png");
    private InventoryPlayer inventoryPlayer;
    public static final int GUI_ID = 9;
    private TileBandAmp tileBandAmp;
    private ItemStack stackBandAmp;
    private GuiLockIconButton lockButton;
    private GuiRedstoneButton rearInputButton;
    private GuiRedstoneButton leftOutputButton;
    private GuiRedstoneButton rightOutputButton;
    private GuiButton soundRangeButton;
    private boolean prevLockState;
    private boolean prevRearInputButtonState;
    private boolean prevLeftOutputButtonState;
    private boolean prevRightOutputButtonState;
    private SoundRange soundRange;
    private SoundRange prevSoundRange;

    public GuiBandAmp(Container container, InventoryPlayer inventoryPlayer, TileBandAmp tileBandAmp)
    {
        super(container);
        this.inventoryPlayer = inventoryPlayer;
        this.tileBandAmp = tileBandAmp;
        stackBandAmp = new ItemStack(ModItems.ITEM_BAND_AMP);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        boolean isEnabled = LockableHelper.canLock(mc.player, tileBandAmp);
        soundRange = tileBandAmp.getSoundRange();
        prevSoundRange = soundRange;

        lockButton =  new GuiLockIconButton(100, guiLeft + 7, guiTop + 25);
        buttonList.add(lockButton);
        lockButton.setLocked(tileBandAmp.isLocked());
        lockButton.enabled = isEnabled;
        prevLockState = lockButton.isLocked();

        rearInputButton = new GuiRedstoneButton(101, guiLeft + 139, guiTop + 25, ArrowFaces.DOWN);
        buttonList.add(rearInputButton);
        rearInputButton.setSignalEnabled(tileBandAmp.isRearRedstoneInputEnabled());
        rearInputButton.enabled = isEnabled;
        prevRearInputButtonState = rearInputButton.isSignalEnabled();

        leftOutputButton = new GuiRedstoneButton(102, guiLeft + 129, guiTop + 45, ArrowFaces.LEFT);
        buttonList.add(leftOutputButton);
        leftOutputButton.setSignalEnabled(tileBandAmp.isLeftRedstoneOutputEnabled());
        leftOutputButton.enabled = isEnabled;
        prevLeftOutputButtonState = leftOutputButton.isSignalEnabled();

        rightOutputButton = new GuiRedstoneButton(103, guiLeft + 149, guiTop + 45, ArrowFaces.RIGHT);
        buttonList.add(rightOutputButton);
        rightOutputButton.setSignalEnabled(tileBandAmp.isRightRedstoneOutputEnabled());
        rightOutputButton.enabled = isEnabled;
        prevRightOutputButtonState = rightOutputButton.isSignalEnabled();

        soundRangeButton = new GuiButton(104, guiLeft +7 , guiTop + 45, 20, 20, I18n.format(soundRange.getLanguageKey()));
        buttonList.add(soundRangeButton);
        soundRangeButton.enabled = isEnabled;
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        sendButtonChanges();
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        toggleLockButton(lockButton, button);
        toggleRedstoneButton(rearInputButton, button);
        toggleRedstoneButton(leftOutputButton, button);
        toggleRedstoneButton(rightOutputButton, button);
        nextRangeButton(soundRangeButton, button);
    }

    private void toggleLockButton(GuiLockIconButton lockIconButton, GuiButton buttonClicked)
    {
        if (buttonClicked.id == lockIconButton.id)
        {
            boolean invertButton = !lockIconButton.isLocked();
            lockIconButton.setLocked(invertButton);
            sendButtonChanges();
        }
    }

    private void toggleRedstoneButton(GuiRedstoneButton guiRedstoneButton, GuiButton buttonClicked)
    {
        if (buttonClicked.id == guiRedstoneButton.id)
        {
            boolean invertButton = !guiRedstoneButton.isSignalEnabled();
            guiRedstoneButton.setSignalEnabled(invertButton);
            sendButtonChanges();
        }
    }

    private void nextRangeButton(GuiButton guiRangeButton, GuiButton buttonClicked)
    {
        if(buttonClicked.id == guiRangeButton.id)
        {
            soundRange = SoundRange.nextRange(soundRange);
            guiRangeButton.displayString = I18n.format(soundRange.getLanguageKey());
            sendButtonChanges();
        }
    }

    private void updateButtonStatus()
    {
        updateLockButtonStatus();

        boolean state = tileBandAmp.isRearRedstoneInputEnabled();
        prevRearInputButtonState = updateButtonStatus(rearInputButton, state, prevRearInputButtonState);

        state = tileBandAmp.isLeftRedstoneOutputEnabled();
        prevLeftOutputButtonState = updateButtonStatus(leftOutputButton, state, prevLeftOutputButtonState);

        state = tileBandAmp.isRightRedstoneOutputEnabled();
        prevRightOutputButtonState = updateButtonStatus(rightOutputButton, state, prevRightOutputButtonState);

        updateSoundRangeButton();
    }

    private void updateLockButtonStatus()
    {
        boolean lockState = this.tileBandAmp.isLocked();
        if (prevLockState != lockState)
        {
            this.lockButton.setLocked(lockState);
            this.prevLockState = lockState;
        }
    }

    private void updateSoundRangeButton()
    {
        soundRange = tileBandAmp.getSoundRange();
        if(prevSoundRange != soundRange)
        {
            soundRangeButton.displayString = I18n.format(soundRange.getLanguageKey());
            prevSoundRange = soundRange;
        }
    }

    private boolean updateButtonStatus(GuiRedstoneButton button, boolean buttonState, boolean prevState)
    {
        if (prevState != buttonState)
        {
            button.setSignalEnabled(buttonState);
            return buttonState;
        }
        else
            return prevState;
    }

    private void sendButtonChanges()
    {
        PacketDispatcher.sendToServer(new BandAmpMessage(
                tileBandAmp.getPos(), lockButton.isLocked(),
                rearInputButton.isSignalEnabled(),
                leftOutputButton.isSignalEnabled(),
                rightOutputButton.isSignalEnabled(), soundRange));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        //drawBandAmp();
        updateButtonStatus();
        String name = I18n.format(tileBandAmp.getName());
        fontRenderer.drawString(name, 8, 6, 0x404040);
        String duration = SheetMusicUtil.formatDuration(tileBandAmp.getDuration());
        int durationLength = fontRenderer.getStringWidth(duration) + 8;
        fontRenderer.drawString(duration, xSize - durationLength, 6, 0x404040);
        fontRenderer.drawString(inventoryPlayer.getDisplayName().getUnformattedText(), 8, ySize - 94, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(BG_TEXTURE);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @SuppressWarnings("unused")
    private void drawBandAmp()
    {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.scale(2,2,1);
        this.itemRender.renderItemAndEffectIntoGUI(stackBandAmp, 5, 13);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
    }
}
