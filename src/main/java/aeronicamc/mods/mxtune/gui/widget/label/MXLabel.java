package aeronicamc.mods.mxtune.gui.widget.label;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class MXLabel extends AbstractGui implements IRenderable
{
    private int width;
    private int height;
    private int x;
    private int y;
    private ITextComponent labelText;
    private boolean centered;
    private boolean visible = true;
    private boolean labelBgEnabled;
    private int textColor;
    private int backColor = -1;
    private int ulColor = -1;
    private int brColor = -1;
    private int border = 0;
    private FontRenderer fontRenderer;


    public MXLabel (FontRenderer pFontRenderer, int pX, int pY, int pWidth, int pHeight, ITextComponent pLabelText, int pTextColor)
    {
        fontRenderer = pFontRenderer;
        x = pX;
        y = pY;
        width = pWidth;
        height = pHeight;
        labelText = pLabelText;
        textColor = pTextColor;
    }

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

    public boolean isLabelBgEnabled()
    {
        return labelBgEnabled;
    }

    public void setLabelBgEnabled(boolean labelBgEnabled)
    {
        this.labelBgEnabled = labelBgEnabled;
    }

    public int getTextColor()
    {
        return textColor;
    }

    public void setTextColor(int textColor)
    {
        this.textColor = textColor;
    }

    public int getBackColor()
    {
        return backColor;
    }

    public void setBackColor(int backColor)
    {
        this.backColor = backColor;
    }

    public int getUlColor()
    {
        return ulColor;
    }

    public void setUlColor(int ulColor)
    {
        this.ulColor = ulColor;
    }

    public int getBrColor()
    {
        return brColor;
    }

    public void setBrColor(int brColor)
    {
        this.brColor = brColor;
    }

    public int getBorder()
    {
        return border;
    }

    public void setBorder(int border)
    {
        this.border = border;
    }

    public void setLabelText(ITextComponent labelText)
    {
        this.labelText = labelText;
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        renderBackGround(pMatrixStack);

        if (centered)
            drawCenteredString(pMatrixStack, fontRenderer, labelText, x + this.width / 2, y + height / 4, textColor | MathHelper.ceil(0.9F * 255.0F) << 24);
        else
            drawString(pMatrixStack, fontRenderer, labelText, x, y + height / 4, textColor | MathHelper.ceil(0.9F * 255.0F) << 24);

    }

    protected void renderBackGround(MatrixStack pMatrixStack)
    {
        if (labelBgEnabled)
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
