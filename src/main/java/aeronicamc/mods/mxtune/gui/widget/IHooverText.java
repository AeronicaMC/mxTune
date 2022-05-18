package aeronicamc.mods.mxtune.gui.widget;

import net.minecraft.util.text.ITextComponent;

import java.util.List;

public interface IHooverText
{
    void addHooverText(boolean clearAll, ITextComponent hooverText);

    boolean isMouseOverWidget(double mouseX, double mouseY);

    List<ITextComponent> getHooverTexts();

    boolean isHooverTextOverride();

    void setHooverTextOverride(boolean override);
}
