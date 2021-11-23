package aeronicamc.mods.mxtune.gui;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.util.SoundFontProxy;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
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
    private final static ResourceLocation GUI_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/multi_inst_chooser.png");
    private final static ResourceLocation GUI_TEXTURE_CUTOUT = new ResourceLocation(Reference.MOD_ID, "textures/gui/multi_inst_chooser_cutout.png");
    private final static int imageWidth = 256;
    private final static int imageHeight = 165;
    private final Screen parent;
    SoundFontProxyWidget widget;

    protected GuiMultiInstChooser(Screen parent)
    {
        super(new TranslationTextComponent("gui.mxtune.guimultiinstchooser.title"));
        this.parent = parent;
    }

    @Override
    public void init(Minecraft pMinecraft, int pWidth, int pHeight)
    {
        super.init(pMinecraft, pWidth, pHeight);
        assert minecraft != null;
        this.width = pWidth;
        this.height = pHeight;
        int guiLeft = (this.width - imageWidth) / 2;
        int guiTop = (this.height - imageHeight) / 2;

        /* create button for leave and disable it initially */
        int widthButtons = 50;
        int posX = guiLeft + imageWidth - widthButtons - 10;
        int posY = guiTop + imageHeight - 20 - 10;

        this.addButton(new Button(posX, posY, widthButtons, 20, new TranslationTextComponent("gui.done"), (done) -> {
            this.minecraft.setScreen(this.parent);
        }));

        int instListWidth = 95;
        for (SoundFontProxy in : SoundFontProxyManager.soundFontProxyMapByIndex.values())
        {
            int stringWidth = minecraft.font.width(new TranslationTextComponent(String.format("item.mxtune.%s", in.id)).getString());
            instListWidth = Math.max(instListWidth, stringWidth + 10);
        }
        instListWidth = Math.min(instListWidth, 128);
        widget = new SoundFontProxyWidget(minecraft, instListWidth, 0, guiTop + 15, guiTop + 135 + 15, font.lineHeight + 4, guiLeft + 10).init();
        widget.setRowWidth(instListWidth - 1);
        this.children.add(widget);
        this.children.add(widget.getSelected());
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
        super.renderBackground(pMatrixStack);
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft).getTextureManager().bind(GUI_TEXTURE);
        int relX = (this.width - imageWidth) / 2;
        int relY = (this.height - imageHeight) / 2;
        this.blit(pMatrixStack, relX, relY, 0, 0, imageWidth, imageHeight);
    }

    public void renderBackgroundCutout(MatrixStack pMatrixStack)
    {
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        Objects.requireNonNull(this.minecraft).getTextureManager().bind(GUI_TEXTURE_CUTOUT);
        int relX = (this.width - imageWidth) / 2;
        int relY = (this.height - imageHeight) / 2;
        this.blit(pMatrixStack, relX, relY, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        widget.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        this.renderBackgroundCutout(pMatrixStack);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

}
