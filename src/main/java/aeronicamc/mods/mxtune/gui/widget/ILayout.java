package aeronicamc.mods.mxtune.gui.widget;

public interface ILayout
{
    void setPosition(int pX, int pY);
//    {
//        this.x = pX;
//        this.y = pY;
//    }

    void setLayout(int pX, int pY, int pWidth, int pHeight);
//    {
//        this.x = pX;
//        this.y = pY;
//        this.width = pWidth;
//        this.height = pHeight;
//    }

    int getLeft();
//    {
//        return this.x;
//    }

    int getTop();
//    {
//        return this.y;
//    }

    int getRight();
//    {
//        return this.x + this.width + padding;
//    }

    int getBottom();
//    {
//        return this.y + this.height + padding;
//    }

    int getPadding();
//    {
//        return padding;
//    }

    void setPadding(int padding);
//    {
//        this.padding = padding;
//    }

}
