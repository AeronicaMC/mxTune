package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.inventory.InstrumentContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.Objects;

public class InstrumentScreen extends ContainerScreen<InstrumentContainer>
{
    private final ResourceLocation GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/instrument_inventory.png");

    public InstrumentScreen(InstrumentContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y)
    {
        //RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack , int mouseX, int mouseY) {
        this.font.draw(matrixStack, menu.getName().getContents(), 10, 8, TextColor.DARK_GRAY);
        this.font.draw(matrixStack, this.inventory.getDisplayName().getContents() + "Test", 10, 72, TextColor.DARK_GRAY);
    }
}
