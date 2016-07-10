/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
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

import org.lwjgl.input.Keyboard;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.server.MusicTextMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;

public class GuiMusicPaper extends GuiScreen
{
    public static final int GUI_ID = 0;

    private Minecraft mc;
    private FontRenderer fontRenderer = null;

    private static final ResourceLocation score_entryTexture = new ResourceLocation(MXTuneMain.prependModID("textures/gui/score_entry.png"));
    private String TITLE = "MML Clipboard Paste Dialog";

    private GuiTextField txt_mmlTitle;
    private GuiTextField txt_mmlPaste;
    private GuiButton btn_ok, btn_cancel;

    public GuiMusicPaper() {}

    @Override
    public void updateScreen()
    {
        txt_mmlTitle.updateCursorCounter();
        txt_mmlPaste.updateCursorCounter();
    }

    @Override
    public void initGui()
    {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRendererObj;

        Keyboard.enableRepeatEvents(true);
        buttonList.clear();

        /** create button for OK and disable it initially */
        int posX = width / 2 + 100 - 80;
        int posY = height / 2 + 50 - 24 + 25;
        btn_ok = new GuiButton(0, posX, posY, 60, 20, "OK");
        btn_ok.enabled = false;

        posX = width / 2 - 100 + 20;
        posY = height / 2 + 50 - 24 + 25;
        btn_cancel = new GuiButton(1, posX, posY, 60, 20, "Cancel");

        buttonList.add(btn_ok);
        buttonList.add(btn_cancel);

        /** create text field */
        posX = width / 2 - 100;
        posY = height / 2 - 50 + 47;
        txt_mmlTitle = new GuiTextField(0, fontRenderer, posX, posY, 200, 20);
        txt_mmlTitle.setFocused(true);
        txt_mmlTitle.setCanLoseFocus(true);
        txt_mmlTitle.setMaxStringLength(35);

        /** create text field */
        posX = width / 2 - 100;
        posY = height / 2 - 50 + 47 + 25;
        txt_mmlPaste = new GuiTextField(1, fontRenderer, posX, posY, 200, 20);
        txt_mmlPaste.setFocused(false);
        txt_mmlPaste.setCanLoseFocus(true);
        txt_mmlPaste.setMaxStringLength(10000);
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        drawDefaultBackground();
        drawGuiBackground();

        /** draw "TITLE" at the top in the middle */
        int posX = width / 2 - fontRenderer.getStringWidth(TITLE) / 2;
        int posY = height / 2 - 50 + 20;
        fontRenderer.drawString(TITLE, posX, posY, 0x000000);

        /** draw "Field name:" at the left site above the GuiTextField */
        posX = width / 2 - 100;
        posY = height / 2 - 50 + 35;
        fontRenderer.drawString("Title / MML@:", posX, posY, 0x404040);

        /** draw the GuiTextField */
        txt_mmlTitle.drawTextBox();
        txt_mmlPaste.drawTextBox();

        super.drawScreen(i, j, f);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        /** if button is disabled ignore click */
        if (!guibutton.enabled) { return; }

        /** id 0 = ok; id 1 = cancel */
        switch (guibutton.id)
        {
        case 0:
            String musictext = txt_mmlPaste.getText().toUpperCase().trim();
            String musictitle = txt_mmlTitle.getText().trim();

            /** save the mml */
            System.out.println("+++ Gui send packet to server +++");
            sendNewNameToServer(musictitle, musictext);

        case 1:
            /** remove the GUI */
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
            break;
        default:
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e).
     * 
     * @throws IOException
     */
    @Override
    protected void keyTyped(char c, int i) throws IOException
    {
        /** add char to GuiTextField */
        txt_mmlTitle.textboxKeyTyped(c, i);
        txt_mmlPaste.textboxKeyTyped(c, i);
        // if (txt_mmlTitle.isFocused()) txt_mmlTitle.textboxKeyTyped(c, i);
        // if (txt_mmlPaste.isFocused()) txt_mmlPaste.textboxKeyTyped(c, i);
        if (i == Keyboard.KEY_TAB)
        {
            if (txt_mmlTitle.isFocused())
            {
                txt_mmlPaste.setFocused(true);
                txt_mmlTitle.setFocused(false);
            } else
            {
                txt_mmlPaste.setFocused(false);
                txt_mmlTitle.setFocused(true);
            }
        }
        /** enable ok button when GuiTextField content is greater than 0 chars and passes a basic MML test */
        ((GuiButton) buttonList.get(0)).enabled = (txt_mmlPaste.getText().trim().length() > 0) && txt_mmlPaste.getText().contains("MML@") && txt_mmlPaste.getText().contains(";");
        /** perform click event on ok button when Enter is pressed */
        if (c == '\n' || c == '\r')
        {
            actionPerformed((GuiButton) buttonList.get(0));
        }
        /** perform click event on cancel button when Esc is pressed */
        if (Integer.valueOf(c) == 27)
        {
            actionPerformed((GuiButton) buttonList.get(1));
        }
        super.keyTyped(c, i);
    }

    /**
     * Called when the mouse is clicked.
     * 
     * @throws IOException
     */
    @Override
    protected void mouseClicked(int i, int j, int k) throws IOException
    {
        super.mouseClicked(i, j, k);
        /** move cursor to clicked position in GuiTextField */
        txt_mmlTitle.mouseClicked(i, j, k);
        txt_mmlPaste.mouseClicked(i, j, k);
    }

    /**
     * Gets the image for the background and renders it in the middle of the
     * screen.
     */
    protected void drawGuiBackground()
    {
        mc.renderEngine.bindTexture(score_entryTexture);
        int j = (width - 100) / 2;
        int k = (height - 50) / 2;
        drawTexturedModalRect(j - 100 + 30, k - 50 + 30 + 5, 0, 0, 240, 120);
    }

    protected void sendNewNameToServer(String title, String data)
    {
        PacketDispatcher.sendToServer(new MusicTextMessage(title, data));
    }
}
