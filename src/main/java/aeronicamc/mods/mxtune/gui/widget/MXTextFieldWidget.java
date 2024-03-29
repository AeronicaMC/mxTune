package aeronicamc.mods.mxtune.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

public class MXTextFieldWidget extends TextFieldWidget implements ILayout
{
    protected int padding = 0;
    public MXTextFieldWidget(int pMaxLength)
    {
        super(Minecraft.getInstance().font, 0, 0, 0, 0, new StringTextComponent(""));
        this.setMaxLength(pMaxLength);
    }

    public MXTextFieldWidget(FontRenderer pFont, int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage)
    {
        super(pFont, pX, pY, pWidth, pHeight, pMessage);
    }

    public MXTextFieldWidget(FontRenderer pFont, int pX, int pY, int pWidth, int pHeight, @Nullable TextFieldWidget p_i232259_6_, ITextComponent pMessage)
    {
        super(pFont, pX, pY, pWidth, pHeight, p_i232259_6_, pMessage);
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
}
