package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.inventory.MultiInstContainer;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;

public class MultiInstScreen extends ContainerScreen<MultiInstContainer>
{
    private static final ResourceLocation GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/instrument_inventory.png");
    private final MXButton buttonChangeInstrument = new MXButton(this::openSelector);

    public MultiInstScreen(MultiInstContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 166;
        this.imageWidth = 184;
    }

    private void openSelector(Button button)
    {
        Objects.requireNonNull(minecraft).setScreen(new GuiMultiInstChooser(this));
    }

    @Override
    protected void init()
    {
        super.init();

        int yPos = topPos + 6;
        int xPos = leftPos + 12;

        buttonChangeInstrument.setLayout(xPos, yPos, imageWidth - 24, 20);
        this.addButton(buttonChangeInstrument);
        updateButton(((IInstrument)inventory.getSelected().getItem()).getPatch(inventory.getSelected()));
    }

    int getInstrument()
    {
        return ((IInstrument)inventory.getSelected().getItem()).getPatch(inventory.getSelected());
    }

    void updateButton(int selected)
    {
        buttonChangeInstrument.setMessage(new TranslationTextComponent(SoundFontProxyManager.getLangKeyName(selected)));
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y)
    {
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft).getTextureManager().bind(GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(MatrixStack matrixStack , int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        ModGuiHelper.RenderGuiItemScaled(this.itemRenderer, inventory.getSelected(),
                getGuiLeft() + 51, getGuiTop() + 50, 2, true);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack , int mouseX, int mouseY) {
//        ITextComponent title = inventory.getSelected().getHoverName();
//        this.font.draw(matrixStack, title, (float)(imageWidth - font.width(title))/2, 10, TextColorFg.DARK_GRAY);
        this.font.draw(matrixStack, new TranslationTextComponent("container.inventory"), 10, 72, TextColorFg.DARK_GRAY);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        // Prevent the Instrument from being swapped
        if (Objects.requireNonNull(this.minecraft).options.keyHotbarSlots[inventory.selected].isActiveAndMatches(InputMappings.getKey(pKeyCode, pScanCode)) || (hoveredSlot != null && hoveredSlot.index == inventory.selected))
            return true;
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}
