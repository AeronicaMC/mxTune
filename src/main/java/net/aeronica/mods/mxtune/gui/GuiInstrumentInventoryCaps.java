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

import org.lwjgl.opengl.GL11;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.inventory.ContainerSheetMusic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import scala.Char;

public class GuiInstrumentInventoryCaps extends GuiContainer
{
    public static final int GUI_ID = 1;

    private Minecraft mc;
    private EntityPlayer player;
    private FontRenderer fontRenderer = null;
    int theInvItemSlot;

    private static final ResourceLocation inventoryTexture = new ResourceLocation(MXTuneMain.prependModID("textures/gui/instrument_inventory.png"));

    public GuiInstrumentInventoryCaps(EntityPlayer  playerIn, ItemStack stackIn)
    {
        super(new ContainerSheetMusic(playerIn.inventory, stackIn));
        this.player = playerIn;
        this.mc = Minecraft.getMinecraft();

        /** The slot inventory.currentItem is 0 based */
        this.theInvItemSlot = playerIn.inventory.currentItem;
        this.fontRenderer = mc.fontRendererObj;

        xSize = 184;
        ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        if (player.getHeldItemMainhand() == null) return;
        String s = player.getHeldItemMainhand().getDisplayName();
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 12, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 12, this.ySize - 96 + 4, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.renderEngine.bindTexture(inventoryTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    /** Don't allow the held inventory to be moved from it's slot. */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (Char.char2int(typedChar) == (this.theInvItemSlot + 49)) return;
        super.keyTyped(typedChar, keyCode);
    }
}
