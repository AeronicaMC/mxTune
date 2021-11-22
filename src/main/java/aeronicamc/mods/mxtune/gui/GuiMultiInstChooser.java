package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;

public class GuiMultiInstChooser extends Screen
{
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/multi_inst_chooser.png");
    private final int imageWidth = 256;
    private final int imageHeight = 164;
    private int guiLeft;
    private int guiTop;
    private final Screen parent;

    protected GuiMultiInstChooser(Screen parent)
    {
        super(new TranslationTextComponent("gui.mxtune.guimultiinstchooser.title"));
        this.parent = parent;
    }

    @Override
    public void init(Minecraft pMinecraft, int pWidth, int pHeight)
    {
        super.init(pMinecraft, pWidth, pHeight);
        this.width = pWidth;
        this.height = pHeight;
        this.guiLeft = (this.width - this.imageWidth) / 2;
        this.guiTop = (this.height - this.imageHeight) / 2;

        this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, new TranslationTextComponent("gui.done"), (done) -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(this.parent);
        }));
    }

    @Override
    public String getNarrationMessage() {
        return super.getNarrationMessage() + ". " + this.title.getString();
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return true;
    }

    @Override
    public void renderBackground(MatrixStack pMatrixStack)
    {
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft).getTextureManager().bind(GUI_TEXTURE);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(pMatrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);

        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}