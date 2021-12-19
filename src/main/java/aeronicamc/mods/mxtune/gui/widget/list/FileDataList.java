package aeronicamc.mods.mxtune.gui.widget.list;

import aeronicamc.mods.mxtune.gui.TextColorFg;
import aeronicamc.mods.mxtune.gui.mml.FileData;
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
import java.util.function.Consumer;

public class FileDataList extends MXExtendedList<FileDataList.Entry>
{

    public FileDataList()
    {
        super();
    }

    public FileDataList(Minecraft minecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight, int pLeft, Consumer<FileDataList.Entry> selectCallback)
    {
        super(minecraft, pWidth, pHeight, pY0, pY1, pItemHeight, pLeft, selectCallback);
    }

    public int add(FileData fileData)
    {
        return addEntry(new Entry(fileData));
    }

    public void addAll(@Nullable List<FileData> fileData)
    {
        if (fileData != null)
            fileData.forEach(path -> {
                addEntry(new Entry(path));
            });
        super.setScrollAmount(1);
    }

    public void clear()
    {
        clearEntries();
    }

    public List<FileDataList.Entry> getEntries()
    {
        return super.children();
    }

    @Override
    public void setCallBack(Consumer<Entry> selectCallback)
    {
        this.selectCallback = selectCallback;
    }

    public class Entry extends MXExtendedList.AbstractListEntry<FileDataList.Entry>
    {
        protected FileData fileData;

        public Entry(FileData fileData)
        {
            this.fileData = fileData;
        }

        public FileData getFileData()
        {
            return fileData;
        }

        public Path getPath()
        {
            return fileData.getPath();
        }

        public String getName()
        {
            return fileData.getName();
        }

        @Override
        public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks)
        {
            if (pIsMouseOver)
            {
                fill(pMatrixStack, pLeft - 2, pTop - 2, pLeft - 5 + width, pTop + itemHeight - 1, 0xA0A0A0A0);
            }

            ITextComponent translated = new StringTextComponent(this.fileData.getName());
            ITextProperties trimmed = minecraft.font.substrByWidth(translated, pWidth - 6);
            minecraft.font.drawShadow(pMatrixStack, trimmed.getString(), (float) (pLeft), (float) (pTop + 1), getSelected() == this ? TextColorFg.WHITE : TextColorFg.GRAY, true);
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
        {
            if (isMouseOver(pMouseX, pMouseY)){
                changeFocus(true);
                setFocused(this);
                FileDataList.this.setSelected(this);
                selectCallback.accept(this);
                minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            return false;
        }
    }
}


