package aeronicamc.mods.mxtune.gui.widget.list;

import aeronicamc.mods.mxtune.gui.widget.IHooverText;
import aeronicamc.mods.mxtune.gui.widget.ILayout;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class MXCheckBoxButton extends AbstractButton implements ILayout, IHooverText
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
    private boolean selected;
    private boolean showLabel;

    protected int padding = 0;
    private int index = Integer.MAX_VALUE;
    @SuppressWarnings("FieldMayBeFinal")
    private List<ITextComponent> hooverTexts = new ArrayList<>();
    private boolean hooverTextsOverride;


    public MXCheckBoxButton()
    {
        this(0, 0, 150, 20, StringTextComponent.EMPTY, true, true);
    }

    public MXCheckBoxButton(ITextComponent pMessage)
    {
        this(0, 0, 150, 20, pMessage, true, true);
    }

    public MXCheckBoxButton(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, boolean pSelected) {
        this(pX, pY, pWidth, pHeight, pMessage, pSelected, true);
    }

    public MXCheckBoxButton(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, boolean pSelected, boolean pShowLabel)
    {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.selected = pSelected;
        this.showLabel = pShowLabel;
    }

    public void onPress() {
        this.selected = !this.selected;
    }

    public boolean selected() {
        return this.selected;
    }

    public void setShowLabel(boolean showLabel)
    {
        this.showLabel = showLabel;
    }

    @Override
    public void setPosition(int pX, int pY)
    {
        this.x = pX;
        this.y = pY;
    }

    @Override
    public void setLayout(int pX, int pY, int pWidth, int pHeight)
    {
        this.x = pX;
        this.y = pY;
        this.width = pWidth;
        this.height = pHeight;
    }

    @Override
    public int getLeft()
    {
        return this.x;
    }

    @Override
    public int getTop()
    {
        return this.y;
    }

    @Override
    public int getRight()
    {
        return this.x + this.width + padding;
    }

    @Override
    public int getBottom()
    {
        return this.y + this.height + padding;
    }

    @Override
    public int getPadding()
    {
        return padding;
    }

    @Override
    public void setPadding(int padding)
    {
        this.padding = padding;
    }

    @Override
    public void addHooverText(boolean clearAll, ITextComponent hooverText)
    {
        if (clearAll) hooverTexts.clear();
        hooverTexts.add(hooverText);
    }

    @Override
    public boolean isHooverTextOverride()
    {
        return hooverTextsOverride;
    }

    @Override
    public void setHooverTextOverride(boolean override)
    {
        hooverTextsOverride = override;
    }

    @Override
    public boolean isMouseOverWidget(double mouseX, double mouseY)
    {
        return this.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return this.visible && pMouseX >= (double)this.x && pMouseY >= (double)this.y && pMouseX < (double)(this.x + this.width) && pMouseY < (double)(this.y + this.height);
    }

    @Override
    public List<ITextComponent> getHooverTexts()
    {
        return hooverTexts;
    }

    @Override
    public void setMessage(ITextComponent pMessage)
    {
        super.setMessage(pMessage);
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        this.isHovered = pMouseX >= this.x && pMouseY >= this.y && pMouseX < this.x + this.width && pMouseY < this.y + this.height;
        int k = this.getYImage(this.isHovered());
        GuiUtils.drawContinuousTexturedBox(pMatrixStack, WIDGETS_LOCATION, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());

        minecraft.getTextureManager().bind(TEXTURE);
        RenderSystem.enableDepthTest();
        FontRenderer fontrenderer = minecraft.font;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        blit(pMatrixStack, this.x, this.y, this.isFocused() ? 20.0F : 0.0F, this.selected ? 20.0F : 0.0F, 20, this.height, 64, 64);
        this.renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
        if (this.showLabel) {
            drawString(pMatrixStack, fontrenderer, this.getMessage(), this.x + 24, this.y + (this.height - 8) / 2, 14737632 | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }
    }
}
