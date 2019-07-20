/*
 * Modified from reconstituted MC MCP/Forge/Vanilla source
 */

package net.aeronica.mods.mxtune.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLabelMX extends AbstractGui
{
    private int width;
    private int height;
    private int x;
    private int y;
    private String labelName;
    private String labelText;
    private TextFormatting textFormatting = TextFormatting.RESET;
    public final int id;
    private boolean centered;
    private boolean visible = true;
    private boolean labelBgEnabled;
    private final int textColor;
    private int backColor;
    private int ulColor;
    private int brColor;
    private final FontRenderer fontRenderer;
    private int border;

    public GuiLabelMX(FontRenderer fontRendererObj, int labelId, int xIn, int yIn, int widthIn, int heightIn, int colorIn)
    {
        this.fontRenderer = fontRendererObj;
        this.id = labelId;
        this.x = xIn;
        this.y = yIn;
        this.width = widthIn;
        this.height = heightIn;
        this.labelName = "";
        this.labelText = "";
        this.centered = false;
        this.labelBgEnabled = false;
        this.textColor = colorIn;
        this.backColor = -1;
        this.ulColor = -1;
        this.brColor = -1;
        this.border = 0;
    }

    public void setLabelName(String labelName)
    {
        this.labelName = labelName;
    }

    public String getLabelName()
    {
        return labelName;
    }

    public String getLabelText() { return labelText; }

    public void setLabelText(String labelText) { this.labelText = labelText; }

    /**
     * Sets the Label to be centered
     */
    public GuiLabelMX setCentered()
    {
        this.centered = true;
        return this;
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

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public TextFormatting getTextFormatting()
    {
        return textFormatting;
    }

    public void setTextFormatting(TextFormatting textFormatting)
    {
        this.textFormatting = textFormatting;
    }

    public void drawLabel(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            String combinedTexts = labelName + " " + textFormatting + labelText;
            this.drawLabelBackground(mc, mouseX, mouseY);

            if (this.centered)
            {
                this.drawCenteredString(this.fontRenderer, combinedTexts, this.x + this.width / 2, this.y, this.textColor);
            }
            else
            {
                this.drawString(this.fontRenderer, combinedTexts, this.x, this.y, this.textColor);
            }
        }
    }

    protected void drawLabelBackground(Minecraft mcIn, int mouseX, int mouseY)
    {
        if (this.labelBgEnabled)
        {
            int i = this.width + this.border * 2;
            int j = this.height + this.border * 2;
            int k = this.x - this.border;
            int l = this.y - this.border;
            drawRect(k, l, k + i, l + j, this.backColor);
            this.drawHorizontalLine(k, k + i, l, this.ulColor);
            this.drawHorizontalLine(k, k + i, l + j, this.brColor);
            this.drawVerticalLine(k, l, l + j, this.ulColor);
            this.drawVerticalLine(k + i, l, l + j, this.brColor);
        }
    }
}