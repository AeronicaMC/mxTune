package aeronicamc.mods.mxtune.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses the forge ExtendedButton render but extends vanilla Button.
 */
public class MXButton extends Button implements ILayout, IHooverText
{
    protected int padding = 0;
    private int index = Integer.MAX_VALUE;
    private final List<ITextComponent> hooverTexts = new ArrayList<>();
    private boolean hooverTextsOverride;

    public MXButton(IPressable pOnPress)
    {
        super(0, 0, 50, 20, DialogTexts.GUI_DONE, pOnPress);
    }

    public MXButton(ITextComponent pMessage, IPressable pOnPress)
    {
        super(0, 0, 50, 20, pMessage, pOnPress);
    }

    public MXButton(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, Button.IPressable pOnPress)
    {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
    }

    public MXButton(IPressable pOnPress, Button.ITooltip pOnTooltip)
    {
        super(0, 0, 50, 20, DialogTexts.GUI_DONE, pOnPress, pOnTooltip);
    }

    public MXButton(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, Button.IPressable pOnPress, Button.ITooltip pOnTooltip)
    {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
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
        return this.visible && pMouseX >= this.x && pMouseY >= this.y && pMouseX < (this.x + this.width) && pMouseY < (this.y + this.height);
    }

    @Override
    public List<ITextComponent> getHooverTexts()
    {
        return hooverTexts;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void renderButton(MatrixStack mStack, int mouseX, int mouseY, float partial)
    {
        if (this.visible)
        {
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int k = this.getYImage(this.isHovered());
            GuiUtils.drawContinuousTexturedBox(mStack, WIDGETS_LOCATION, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
            this.renderBg(mStack, mc, mouseX, mouseY);

            ITextComponent buttonText = this.getMessage();
            int strWidth = mc.font.width(buttonText);
            int ellipsisWidth = mc.font.width("...");

            if (strWidth > width - 6 && strWidth > ellipsisWidth)
                // srg names make it hard to figure out how to append to an ITextProperties from this trim operation, to wrap this in StringTextComponent is kinda dirty.
                buttonText = new StringTextComponent(mc.font.substrByWidth(buttonText, width - 6 - ellipsisWidth).getString() + "...");

            drawCenteredString(mStack, mc.font, buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, getFGColor());
        }
    }
}
