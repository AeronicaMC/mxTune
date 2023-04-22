package aeronicamc.mods.mxtune.gui.widget;

import aeronicamc.mods.mxtune.gui.TextColorFg;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class MXLabel extends AbstractGui implements IRenderable, ILayout
{
    protected int padding = 0;
    private int width;
    private int height;
    private int x;
    private int y;
    private ITextComponent labelName = new StringTextComponent("");
    private ITextComponent labelText = new StringTextComponent("");
    private boolean centered;
    private boolean visible = true;
    private boolean background = false;
    private int textColor;
    private int backColor = -1;
    private int ulColor = -1;
    private int brColor = -1;
    private int border = 0;
    private FontRenderer fontRenderer;

    public MXLabel()
    {
        this(Minecraft.getInstance().font, 0, 0, 0, 0, new StringTextComponent(""), TextColorFg.WHITE | MathHelper.ceil(1F * 255.0F) << 24);
    }

    public MXLabel (FontRenderer pFontRenderer, int pX, int pY, int pWidth, int pHeight, ITextComponent pLabelText, int pTextColor)
    {
        fontRenderer = pFontRenderer;
        x = pX;
        y = pY;
        width = pWidth;
        height = pHeight;
        labelName = pLabelText;
        textColor = pTextColor;
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

    public void setFontRenderer(FontRenderer fontRenderer)
    {
        this.fontRenderer = fontRenderer;
    }

    // TODO: Rethink the label. Could I just base this on TextFieldWidget instead
    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public boolean isCentered()
    {
        return centered;
    }

    public void setCentered(boolean centered)
    {
        this.centered = centered;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public boolean isBackground()
    {
        return background;
    }

    public void setBackground(boolean background)
    {
        this.background = background;
    }

    public void setTextColor(int textColor)
    {
        this.textColor = textColor;
    }

    public void setBackColor(int backColor)
    {
        this.backColor = backColor;
    }

    public void setUlColor(int ulColor)
    {
        this.ulColor = ulColor;
    }

    public void setBrColor(int brColor)
    {
        this.brColor = brColor;
    }

    public void setBorder(int border)
    {
        this.border = border;
    }

    public void setLabelName(ITextComponent labelName)
    {
        this.labelName = labelName;
    }

    public ITextComponent getLabelName()
    {
        return labelName;
    }

    public ITextComponent getLabelText()
    {
        return labelText;
    }

    public void setLabelText(ITextComponent labelText)
    {
        this.labelText = labelText;
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        renderBackGround(pMatrixStack);

        ITextComponent combinedText = labelName.copy().append(labelText);

        if (centered)
            drawCenteredString(pMatrixStack, fontRenderer, combinedText, x + this.width / 2, y + height / 4, textColor | MathHelper.ceil(1F * 255.0F) << 24);
        else
            drawString(pMatrixStack, fontRenderer, combinedText, x, y + height / 4, textColor | MathHelper.ceil(1F * 255.0F) << 24);
    }

    protected void renderBackGround(MatrixStack pMatrixStack)
    {
        if (background)
        {
            int wb = width + border * 2;
            int hb = height + border * 2;
            int xb = x - this.border;
            int yb = y - this.border;
            fill(pMatrixStack, xb, yb, xb + wb, yb + hb, backColor | MathHelper.ceil(1F * 255.0F) << 24);
            vLine(pMatrixStack,xb + wb, yb, yb + hb, brColor | MathHelper.ceil(1F * 255.0F) << 24);
            hLine(pMatrixStack, xb, xb + wb, yb, ulColor | MathHelper.ceil(1F * 255.0F) << 24);
            hLine(pMatrixStack, xb, xb + wb, yb + hb, brColor | MathHelper.ceil(1F * 255.0F) << 24);
            vLine(pMatrixStack, xb, yb, yb + hb + 1, ulColor | MathHelper.ceil(1F * 255.0F) << 24);
        }
    }
}
