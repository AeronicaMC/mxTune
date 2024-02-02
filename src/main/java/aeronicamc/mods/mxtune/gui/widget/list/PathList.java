package aeronicamc.mods.mxtune.gui.widget.list;

import aeronicamc.mods.mxtune.gui.TextColorFg;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public class PathList extends MXExtendedList<PathList.Entry>
{

    public PathList()
    {
        super();
    }

    public PathList(Minecraft minecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight, int pLeft, BiConsumer<Entry, Boolean> selectCallback)
    {
        super(minecraft, pWidth, pHeight, pY0, pY1, pItemHeight, pLeft, selectCallback);
    }

    public int add(Path path)
    {
        return addEntry(new Entry(path));
    }

    public void addAll(@Nullable List<Path> paths)
    {
        if (paths != null)
            paths.forEach(path -> addEntry(new Entry(path)));
        super.setScrollAmount(1);
    }

    public void clear()
    {
        clearEntries();
    }

    public List<PathList.Entry> getEntries()
    {
        return super.children();
    }

    @Override
    public void setCallBack(BiConsumer<Entry, Boolean> selectCallback)
    {
        this.selectCallback = selectCallback;
    }

    public class Entry extends MXExtendedList.AbstractListEntry<Entry>
    {
        protected Path path;

        public Entry(Path path)
        {
            this.path = path;
        }

        public Path getPath()
        {
            return path;
        }

        @Override
        public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks)
        {
            if (pIsMouseOver && isActive())
            {
                fill(pMatrixStack, pLeft - 2, pTop - 2, pLeft - 5 + width, pTop + itemHeight - 1, 0xA0A0A0A0);
            }

            ITextComponent translated = new StringTextComponent(this.path.getFileName().toString());
            ITextProperties trimmed = minecraft.font.substrByWidth(translated, pWidth - 6);
            minecraft.font.drawShadow(pMatrixStack, trimmed.getString(), pLeft, pTop + 1F, getSelected() == this ? TextColorFg.WHITE : TextColorFg.GRAY, true);
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
        {
            if (isMouseOver(pMouseX, pMouseY) && isActive() && selectCallback != null){
                changeFocus(true);
                setFocused(this);
                PathList.this.setSelected(this);
                selectCallback.accept(this, doubleClicked());
                minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            return false;
        }
    }
}


