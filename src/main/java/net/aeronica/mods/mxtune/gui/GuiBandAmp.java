package net.aeronica.mods.mxtune.gui;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.init.ModBlocks;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiBandAmp extends GuiContainer
{
    private static final ResourceLocation BG_TEXTURE = new ResourceLocation(MXTuneMain.MODID, "textures/gui/band_amp.png");
    private InventoryPlayer inventoryPlayer;
    public static final int GUI_ID = 9;

    public GuiBandAmp(Container container, InventoryPlayer inventoryPlayer)
    {
        super(container);
        this.inventoryPlayer = inventoryPlayer;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String name = I18n.format(ModBlocks.BAND_AMP.getTranslationKey() + ".name");
        fontRenderer.drawString(name, xSize / 2 - fontRenderer.getStringWidth(name) / 2, 6, 0x404040);
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
}
