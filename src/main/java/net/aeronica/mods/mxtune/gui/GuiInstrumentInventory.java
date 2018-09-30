package net.aeronica.mods.mxtune.gui;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.inventory.ContainerInstrument;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import scala.Char;

import java.io.IOException;

public class GuiInstrumentInventory extends GuiContainer {
	public static final int GUI_ID = 1;

	private FontRenderer fontRenderer = null;
	int theInvItemSlot;

	private static final ResourceLocation inventoryTexture = new ResourceLocation(
			MXTuneMain.prependModID("textures/gui/instrument_inventory.png"));

	public GuiInstrumentInventory(ContainerInstrument containerInstrument) {
		super(containerInstrument);
		this.mc = Minecraft.getMinecraft();

		// The slot inventory.currentItem is 0 based
		this.theInvItemSlot = mc.player.inventory.currentItem;
		this.fontRenderer = mc.fontRenderer;

		xSize = 184;
		ySize = 166;
	}

    /**
     * Draws the screen and all the components in it including tool tips
     */
	@Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

	/**
	 * Draw the tool tips of items in this inventory
	 */
	@Override
    protected void renderHoveredToolTip(int mouseX, int mouseY)
    {
        if (this.getSlotUnderMouse() != null && this.getSlotUnderMouse().getHasStack())
        {
            this.renderToolTip(this.getSlotUnderMouse().getStack(), mouseX, mouseY);
        }
    }

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of
	 * the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		if (mc.player.getHeldItemMainhand().equals(ItemStack.EMPTY))
			return;
		String s = mc.player.getHeldItemMainhand().getDisplayName();
		this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 12, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 12, this.ySize - 96 + 4, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(inventoryTexture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	/*
	 * Don't allow the held instrument to be moved from it's slot.
	 * 
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (Char.char2int(typedChar) == (this.theInvItemSlot + 49))
			return;
		super.keyTyped(typedChar, keyCode);
	}
}
