package net.aeronica.mods.mxtune.gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.inventory.ContainerInstrument;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import scala.Char;

public class GuiInstInvExp extends GuiContainer {
	public static final int GUI_ID = 20;

	private Minecraft mc;
	private FontRenderer fontRenderer = null;
	int theInvItemSlot;

	private static final ResourceLocation inventoryTexture = new ResourceLocation(
			MXTuneMain.prependModID("textures/gui/instrument_inventory.png"));

	/** The inventory to render on screen */
	// private final InventoryInstrument inventory;

	public GuiInstInvExp(ContainerInstrument containerInstrument) {
		super(containerInstrument);
		this.mc = Minecraft.getMinecraft();

		// The slot inventory.currentItem is 0 based
		this.theInvItemSlot = mc.thePlayer.inventory.currentItem;
		this.fontRenderer = mc.fontRendererObj;

		xSize = 184;
		ySize = 166;
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of
	 * the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		if (mc.thePlayer.getHeldItemMainhand() == null)
			return;
		String s = mc.thePlayer.getHeldItemMainhand().getDisplayName();
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
	 * Don't allow the held inventory to be moved from it's slot.
	 * 
	 */
	@Override
	protected void keyTyped(char par1, int par2) throws IOException {
		if (Char.char2int(par1) == (this.theInvItemSlot + 49))
			return;
		super.keyTyped(par1, par2);
	}
}
