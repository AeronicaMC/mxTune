package aeronicamc.mods.mxtune.gui.widget;

import net.minecraft.util.text.ITextComponent;

import java.util.List;

public interface IHooverText
{
    void addHooverTexts(ITextComponent hooverText);

    boolean isMouseOverWidget(int guiLeft, int guiTop, double mouseX, double mouseY);

    List<ITextComponent> getHooverTexts();
}
