package aeronicamc.mods.mxtune.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.common.ForgeHooks;

public class MXLink extends MXButton
{
    private AlignText alignText;
    private String url;

    public enum AlignText
    {
        LEFT, CENTER, RIGHT
    }

    public MXLink(IPressable pOnPress)
    {
        super(pOnPress);
        this.alignText = AlignText.LEFT;
        this.url = "";
    }

    public AlignText getAlignText()
    {
        return alignText;
    }

    public void setAlignText(AlignText alignText)
    {
        this.alignText = alignText;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    public void renderButton(MatrixStack mStack, int mouseX, int mouseY, float partial)
    {
        if (this.visible)
        {
            Minecraft mc = Minecraft.getInstance();
            FontRenderer font = mc.font;
            ITextComponent formattedLink = ForgeHooks.newChatWithLinks(this.url, false);
            int stringWidth = Math.min(font.width(formattedLink.getVisualOrderText()), width);
            int alignX = this.x;
            switch (this.alignText)
            {
                case RIGHT:
                    alignX = this.x - stringWidth;
                    break;
                case CENTER:
                    alignX = this.x - stringWidth + stringWidth / 2;
                    break;
                case LEFT:
                    break;
                default:
            }
            String displayLink = font.plainSubstrByWidth(formattedLink.getString(), width);
            this.fillGradient(mStack,alignX - 2 , y - 2, alignX + width + 2, y +  height, 0xCC000000, 0xCC000000);
            drawString(mStack, font, displayLink, alignX, this.y, 0xFF8080);
        }
    }

    /**
     * Call this method from the parent class that extends GuiScreen e.g. {@link Screen#handleComponentClicked}
     * @return {@link Style} for the URL
     */
    public Style getLinkComponent()
    {
        return ForgeHooks.newChatWithLinks(this.url, false).getSiblings().get(0).getStyle();
    }
}
