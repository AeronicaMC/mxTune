package net.aeronica.mods.mxtune.gui;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.inventory.ContainerInstrument;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import scala.Char;

import java.io.IOException;

public class GuiInstrumentInventory extends GuiContainer {
	public static final int GUI_ID = 1;
    private static final ResourceLocation inventoryTexture = new ResourceLocation(Reference.MOD_ID, "textures/gui/instrument_inventory.png");
    private String BUTTON_MUSIC_OPTIONS = I18n.format("mxtune.key.openMusicOptions");
    private String BUTTON_ADJ_HUD = I18n.format("mxtune.gui.musicOptions.adjHud");

	private int theInvItemSlot;

	public GuiInstrumentInventory(ContainerInstrument containerInstrument) {
		super(containerInstrument);
		this.mc = Minecraft.getMinecraft();

		// The slot inventory.currentItem is 0 based
		this.theInvItemSlot = mc.player.inventory.currentItem;

		xSize = 184;
		ySize = 166;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		int xPos = guiLeft + xSize - 100 - 12;
		int yPos = guiTop + 11 + 20;
		GuiButton buttonMusicOptions = new GuiButton(0, xPos, yPos, 100,20,BUTTON_MUSIC_OPTIONS );
		addButton(buttonMusicOptions);
		yPos += 20;
		GuiButton buttonAdjustHUD = new GuiButton(1, xPos, yPos, 100, 20,BUTTON_ADJ_HUD);
		addButton(buttonAdjustHUD);
	}

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 0:
                mc.displayGuiScreen(new GuiMusicOptions(this));
                break;
            case 1:
                mc.displayGuiScreen(new GuiHudAdjust(this));
                break;
            default:
        }
        super.actionPerformed(button);
    }

	@Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

	@Override
    protected void renderHoveredToolTip(int mouseX, int mouseY)
    {
        if (this.getSlotUnderMouse() != null && this.getSlotUnderMouse().getHasStack())
        {
            this.renderToolTip(this.getSlotUnderMouse().getStack(), mouseX, mouseY);
        }
    }

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		if (mc.player.getHeldItemMainhand().equals(ItemStack.EMPTY))
			return;
		String s = mc.player.getHeldItemMainhand().getDisplayName();
		this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 12, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 12, this.ySize - 96 + 4, 4210752);
		super.drawGuiContainerForegroundLayer(par1, par2);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(inventoryTexture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

	    // Don't allow the held instrument to be moved from it's slot.
		if (Char.char2int(typedChar) == (this.theInvItemSlot + 49))
			return;

		super.keyTyped(typedChar, keyCode);
	}
}
