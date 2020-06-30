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
import net.aeronica.mods.mxtune.gui.util.GuiButtonMX;
import net.aeronica.mods.mxtune.gui.util.GuiScrollingListOf;
import net.aeronica.mods.mxtune.gui.util.ModGuiUtils;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.SetMultiInstMessage;
import net.aeronica.mods.mxtune.util.SoundFontProxy;
import net.aeronica.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;

public class GuiGuiMultiInstChooser extends GuiScreen
{
    private static final ResourceLocation guiTexture = new ResourceLocation(Reference.MOD_ID, "textures/gui/multi_inst_chooser.png");
    private final int xSize = 256;
    private final int ySize = 164;
    private int guiLeft;
    private int guiTop;
    private GuiScreen guiScreenOld;

    private GuiScrollingListOf<SoundFontProxy> guiInstruments;

    private GuiButtonMX buttonDone;

    public GuiGuiMultiInstChooser(@Nullable GuiScreen guiScreenIn)
    {
        guiScreenOld = guiScreenIn;
        mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;

        guiInstruments = new GuiScrollingListOf<SoundFontProxy>(this) {
            @Override
            protected void selectedClickedCallback(int selectedIndex)
            {
                SoundFontProxy soundFontProxy = get();
                if (soundFontProxy != null)
                {
                    PacketDispatcher.sendToServer(new SetMultiInstMessage(soundFontProxy.index));
                }
            }

            @Override
            protected void selectedDoubleClickedCallback(int selectedIndex)
            {
                SoundFontProxy soundFontProxy = get();
                if (soundFontProxy != null)
                {
                    PacketDispatcher.sendToServer(new SetMultiInstMessage(soundFontProxy.index));
                }
                mc.displayGuiScreen(null);
            }

            @Override
            protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
            {
                SoundFontProxy soundFontProxy = !isEmpty() && slotIdx < getSize() && slotIdx >= 0 ? get(slotIdx) : null;
                if (soundFontProxy != null)
                {
                    String playlistName = ModGuiUtils.getLocalizedInstrumentName(soundFontProxy.id);
                    String trimmedName = fontRenderer.trimStringToWidth(playlistName, listWidth - 10);
                    int color = isSelected(slotIdx) ? 0xFFFF00 : 0xAADDEE;
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                } else
                {
                    String name = "---ERROR---";
                    String trimmedName = fontRenderer.trimStringToWidth(name, listWidth - 10);
                    int color = 0xFF0000;
                    fontRenderer.drawStringWithShadow(trimmedName, (float) left + 3, slotTop, color);
                }
            }
        };
    }

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(false);
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        int instListWidth = 95;
        for (SoundFontProxy in : SoundFontProxyManager.soundFontProxyMapByIndex.values())
        {
            int stringWidth = fontRenderer.getStringWidth(ModGuiUtils.getLocalizedInstrumentName(in.id));
            instListWidth = Math.max(instListWidth, stringWidth + 10);
        }
        instListWidth = Math.min(instListWidth, 128);
        guiInstruments.setLayout(fontRenderer.FONT_HEIGHT + 2, instListWidth, 145,guiTop + 10, guiTop + 12 + 141, guiLeft + 10);

        /* create button for leave and disable it initially */
        int widthButtons = 50;
        int posX = guiLeft + xSize - widthButtons - 10;
        int posY = guiTop + ySize - 20 - 10;
        buttonDone = new GuiButtonMX(0, posX, posY, widthButtons, 20, I18n.format("gui.done"));

        buttonList.add(buttonDone);
        guiInstruments.addAll(SoundFontProxyManager.soundFontProxyMapByIndex.values());
        reloadState();
    }

    private void reloadState()
    {
        guiInstruments.setSelectedIndex(mc.player.getHeldItemMainhand().getItemDamage());
        guiInstruments.resetScroll();
        updateButtons();
    }

    private void updateState()
    {
        updateSelected();
        updateButtons();
    }

    private void updateButtons()
    {
    }

    private void updateSelected()
    {
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        // capture the ESC key so we close cleanly
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            mc.displayGuiScreen(guiScreenOld);
            return;
        }
        updateState();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {return false;}

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawGuiBackground();

        /* draw "TITLE" at the top right */
        String title = I18n.format("mxtune.gui.GuiGuiMultiInstChooser.title");
        int posX = (xSize - guiInstruments.getRight())/2 + (xSize - this.fontRenderer.getStringWidth(title)/2);
        int posY = guiTop + 10;
        this.fontRenderer.getStringWidth(title);
        this.fontRenderer.drawString(title, posX, posY, 0x000000);

        guiInstruments.drawScreen(mouseX, mouseY, partialTicks);

        /* draw the things in the controlList (buttons) */
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        // if button is disabled ignore click
        if (!guibutton.enabled) { return; }

        switch (guibutton.id)
        {
            case 0:
                // Starting Chunk
                break;
            default:
        }
        mc.displayGuiScreen(guiScreenOld);
//        mc.setIngameFocus();
//        updateState();
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        guiInstruments.handleMouseInput(mouseX, mouseY);
        super.handleMouseInput();
    }

    private void drawGuiBackground()
    {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.renderEngine.bindTexture(guiTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
