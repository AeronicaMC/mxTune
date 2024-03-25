package aeronicamc.mods.mxtune.gui.widget;

import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class MXSlider extends AbstractSlider implements ILayout, IHooverText {
    protected int padding = 0;
    private final List<ITextComponent> hooverTexts = new ArrayList<>();
    private boolean hooverTextsOverride;
    private final IPressable onPress;
    private ITextComponent sliderText;

    public MXSlider(IPressable onPress) {
        super(0,0,0,50, StringTextComponent.EMPTY, 0F);
        this.onPress = onPress;
        sliderText = StringTextComponent.EMPTY;
    }

    @Override
    public void setMessage(ITextComponent pMessage) {
        sliderText = pMessage;
        super.setMessage(pMessage);
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        ITextComponent itextcomponent = new StringTextComponent((int)(this.value * 100.0D) + "%");
        super.setMessage((new StringTextComponent(sliderText.getString()).append(": ").append(itextcomponent)));
    }

    public void forceValue(double pValue) {
        this.value = MathHelper.clamp(pValue, 0.0D, 1.0D);
    }

    @Override
    protected void applyValue() {
        this.onPress.onPress(this, this.value);
    }

    @Override
    public void addHooverText(boolean clearAll, ITextComponent hooverText) {
        if (clearAll) hooverTexts.clear();
        hooverTexts.add(hooverText);
    }

    @Override
    public boolean isMouseOverWidget(double mouseX, double mouseY) {
        return this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < (this.x + this.width) && mouseY < (this.y + this.height);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (this.isMouseOverWidget(pMouseX, pMouseY) && this.active) {
            this.clampValue(this.value + (pDelta / 100));
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    private void clampValue(double pValue) {
        double d0 = this.value;
        this.value = MathHelper.clamp(pValue, 0.0D, 1.0D);
        if (d0 != this.value) {
            this.applyValue();
        }
        this.updateMessage();
    }

    @Override
    public List<ITextComponent> getHooverTexts() {
        return this.hooverTexts;
    }

    @Override
    public boolean isHooverTextOverride() {
        return this.hooverTextsOverride;
    }

    @Override
    public void setHooverTextOverride(boolean override) {
        this.hooverTextsOverride = override;
    }

    @Override
    public void setPosition(int pX, int pY) {
        this.x = pX;
        this.y = pY;
    }

    @Override
    public void setLayout(int pX, int pY, int pWidth, int pHeight) {
        this.x = pX;
        this.y = pY;
        this.width = pWidth;
        this.height = pHeight;
    }

    @Override
    public int getLeft() {
        return this.x;
    }

    @Override
    public int getTop() {
        return this.y;
    }

    @Override
    public int getRight() {
        return this.x + this.width + padding;
    }

    @Override
    public int getBottom() {
        return this.y + this.height + padding;
    }

    @Override
    public int getPadding() {
        return padding;
    }

    @Override
    public void setPadding(int padding) {
        this.padding = padding;
    }

    public interface IPressable {
        void onPress(MXSlider onPress, double value);
    }
}
