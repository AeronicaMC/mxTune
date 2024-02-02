package aeronicamc.mods.mxtune.gui.widget;

@SuppressWarnings("unused")
public interface ILayout
{
    void setPosition(int pX, int pY);

    void setLayout(int pX, int pY, int pWidth, int pHeight);

    int getLeft();

    int getTop();

    int getRight();

    int getBottom();

    int getPadding();

    void setPadding(int padding);
}
