package aeronicamc.mods.mxtune.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * {@link <A ref="https://gist.github.com/XFactHD/ea8882b5ccc5d5f299d4d38a36ce3505">XFactHD/SelectionWidget.java</A>}
 */
public class SelectionWidget<T extends SelectionWidget.SelectionEntry> extends Widget
{
    private static final ResourceLocation ICONS = new ResourceLocation("minecraft", "textures/gui/resource_packs.png");
    private static final int ENTRY_HEIGHT = 20;
    private final ITextComponent title;
    private final Consumer<T> selectCallback;
    private final List<T> entries = new ArrayList<>();
    private T selected = null;
    private boolean extended = false;
    private int scrollOffset = 0;

    public SelectionWidget(int x, int y, int width, ITextComponent title, Consumer<T> selectCallback)
    {
        super(x, y, width, ENTRY_HEIGHT, new StringTextComponent(""));
        this.title = title;
        this.selectCallback = selectCallback;
    }

    @Override
    public void renderButton(MatrixStack mstack, int mouseX, int mouseY, float partialTicks)
    {
        super.renderButton(mstack, mouseX, mouseY, partialTicks);

        if (selected != null)
        {
            selected.render(mstack, x, y, width, false, getFGColor(), alpha);
        }
        else
        {
            FontRenderer font = Minecraft.getInstance().font;
            drawString(mstack, font, title, x + 6, y + (height - 8) / 2, getFGColor() | MathHelper.ceil(alpha * 255.0F) << 24);
        }

        if (extended)
        {
            int boxHeight = Math.max(1, ENTRY_HEIGHT * Math.min(entries.size(), 4)) + 2;

            fill(mstack, x,     y + ENTRY_HEIGHT - 1, x + width,     y + ENTRY_HEIGHT + boxHeight - 1, 0xFFFFFFFF);
            fill(mstack, x + 1, y + ENTRY_HEIGHT,     x + width - 1, y + ENTRY_HEIGHT + boxHeight - 2, 0xFF000000);

            Minecraft.getInstance().textureManager.bind(ICONS);
            blit(mstack, x + width - 17, y + 6, 114, 5, 11, 7);

            T hoverEntry = getEntryAtPosition(mouseX, mouseY);

            for (int i = 0; i < 4; i++)
            {
                int idx = i + scrollOffset;
                if (idx < entries.size())
                {
                    int entryY = y + ((i + 1) * ENTRY_HEIGHT);

                    T entry = entries.get(idx);
                    entry.render(mstack, x + 1, entryY, width - 2, entry == hoverEntry, getFGColor(), alpha);
                }
            }

            if (entries.size() > 4)
            {
                int scrollY = y + (ENTRY_HEIGHT * (scrollOffset + 1));
                int barHeight = (ENTRY_HEIGHT * 4) - (ENTRY_HEIGHT * (entries.size() - 4));

                fill(mstack, x + width - 5, scrollY,     x + width - 1, scrollY + barHeight,     0xFF666666);
                fill(mstack, x + width - 4, scrollY + 1, x + width - 2, scrollY + barHeight - 1, 0xFFAAAAAA);
            }
        }
        else
        {
            Minecraft.getInstance().textureManager.bind(ICONS);
            blit(mstack, x + width - 17, y + 6, 82, 20, 11, 7);
        }
    }

    @Override
    public int getHeight()
    {
        if (extended)
        {
            return ENTRY_HEIGHT * (Math.min(entries.size(), 4) + 1) + 1;
        }
        return ENTRY_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (active && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + getHeight())
        {
            int maxX = x + width - (entries.size() > 4 ? 5 : 0);
            int maxY = y + ENTRY_HEIGHT * Math.min(entries.size() + 1, 5);
            if (extended && mouseX < maxX && mouseY > (y + ENTRY_HEIGHT) && mouseY < maxY)
            {
                setSelected(getEntryAtPosition(mouseX, mouseY), true);
            }

            if (mouseX < maxX)
            {
                extended = !extended;
                scrollOffset = 0;
            }

            playDownSound(Minecraft.getInstance().getSoundManager());

            return true;
        }

        extended = false;
        scrollOffset = 0;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        int maxY = y + ENTRY_HEIGHT * Math.min(entries.size() + 1, 5);
        if (extended && mouseX >= x && mouseX <= x + width && mouseY > y + ENTRY_HEIGHT && mouseY < maxY)
        {
            if (delta < 0 && scrollOffset < entries.size() - 4)
            {
                scrollOffset++;
            }
            else if (delta > 0 && scrollOffset > 0)
            {
                scrollOffset--;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY)
    {
        if (!active || !visible) { return false; }
        return pMouseX >= x && pMouseY >= y && pMouseX < (x + width) && pMouseY < (y + getHeight());
    }

    private T getEntryAtPosition(double mouseX, double mouseY)
    {
        if (mouseX < x || mouseX > x + width || mouseY < (y + ENTRY_HEIGHT) || mouseY > (y + (ENTRY_HEIGHT * 5)))
        {
            return null;
        }

        double posY = mouseY - (y + ENTRY_HEIGHT);
        int idx = (int) (posY / ENTRY_HEIGHT) + scrollOffset;

        return idx < entries.size() ? entries.get(idx) : null;
    }

    public void addEntry(T entry) { entries.add(entry); }

    public void setSelected(T selected, boolean notify)
    {
        this.selected = selected;
        if (notify && selectCallback != null)
        {
            selectCallback.accept(selected);
        }
    }

    public T getSelected() { return selected; }

    public Stream<T> stream() { return entries.stream(); }

    public static class SelectionEntry implements IGuiEventListener
    {
        private final ITextComponent message;

        public SelectionEntry(ITextComponent message) { this.message = message; }

        public void render(MatrixStack mstack, int x, int y, int width, boolean hovered, int fgColor, float alpha)
        {
            if (hovered)
            {
                fill(mstack, x, y, x + width, y + ENTRY_HEIGHT, 0xFFA0A0A0);
            }

            FontRenderer font = Minecraft.getInstance().font;
            IReorderingProcessor text = LanguageMap.getInstance().getVisualOrder(ITextProperties.composite(font.substrByWidth(message, width - 12)));
            font.drawShadow(mstack, text, x + 6, y + 6, fgColor | MathHelper.ceil(alpha * 255.0F) << 24);
        }
    }
}