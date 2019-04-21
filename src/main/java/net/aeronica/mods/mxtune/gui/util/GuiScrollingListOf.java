/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class GuiScrollingListOf<E> extends GuiScrollingListMX implements List<E>, IHooverText
{
    private final List<E> arrayList = new ArrayList<>();
    protected GuiScreen gui;
    protected Minecraft mc;
    protected int entryHeight;
    private final List<String> hooverTexts = new ArrayList<>();
    private final List<String> hooverTextsCopy = new ArrayList<>();
    private String hooverStatusText = "";

    public <T extends GuiScreen> GuiScrollingListOf(T gui, int entryHeight, int width, int height, int top, int bottom, int left)
    {
        super(gui.mc, width, height, top, bottom, left, entryHeight, gui.width, gui.height);
        this.gui = gui;
        this.mc = gui.mc;
        this.entryHeight = entryHeight;
    }

    public void resetScroll() {

        this.scrollDistance = keepSelectionInViewableArea();
    }

    private float keepSelectionInViewableArea()
    {
        int listHeight = this.getContentHeight() - (this.bottom - this.top - 4);
        float scrollDistance = (float) selectedIndex * entryHeight;

        if (listHeight < 0)
        {
            listHeight /= 2;
        }

        if (scrollDistance < 0.0F)
        {
            scrollDistance = 0.0F;
        }

        if (scrollDistance > (float) listHeight)
        {
            scrollDistance = (float) listHeight;
        }
        return scrollDistance;
    }

    public void scrollToEnd()
    {
        elementClicked(arrayList.size());
        resetScroll();
    }

    public void scrollToTop()
    {
        elementClicked(0);
        resetScroll();
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (isPointInRegion(this.mouseX, this.mouseY))
        {
            if (keyCode == Keyboard.KEY_HOME)
                scrollToTop();
            else if (keyCode == Keyboard.KEY_END)
                scrollToEnd();
        }
    }

    @Override
    public boolean isMouseOverElement(int guiLeft, int guiTop, int mouseX, int mouseY)
    {
        return this.isPointInRegion(mouseX, mouseY);
    }

    private boolean isPointInRegion(int pointX, int pointY)
    {
        int rectX = this.left;
        int rectY = this.top;
        int rectHeight = this.bottom - this.top;
        return pointX >= rectX - 1 && pointX < rectX + this.listWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }

    @Override
    public List<String> getHooverTexts()
    {
        hooverTextsCopy.clear();
        hooverTextsCopy.addAll(hooverTexts);
        if (!hooverStatusText.equals(""))hooverTextsCopy.add(hooverStatusText);
        return hooverTextsCopy;
    }

    public void setHooverStatusText(String hooverStatusText) {this.hooverStatusText = hooverStatusText;}

    public void addHooverText(String hooverText) {hooverTexts.add(hooverText);}

    private boolean isEnableHighlightSelected()
    {
        return this.highlightSelected;
    }

    public int getSelectedIndex() { return selectedIndex; }

    public void setSelectedIndex(int index)
    {
        selectedIndex = index;
    }

    @Nullable
    public E get()
    {
        if(this.isSelected(selectedIndex))
            return arrayList.get(selectedIndex);
        else
            return null;
    }

    @Override
    protected int getSize()
    {
        return arrayList != null ? arrayList.size() : 0;
    }

    public List<E> getList()
    {
        return arrayList;
    }

    public void elementClicked(int index)
    {
        elementClicked(index, false);
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick)
    {
        if (isEnableHighlightSelected() && index == selectedIndex && !doubleClick) return;
        selectedIndex = (index >= 0 && index <= arrayList.size() ? (index >= arrayList.size() ? arrayList.size() - 1 : index) : -1);

        if (selectedIndex >= 0)
        {
            if (!doubleClick)
                selectedClickedCallback(selectedIndex);
            else
                selectedDoubleClickedCallback(selectedIndex);
        }
    }

    protected abstract void selectedClickedCallback(int selectedIndex);

    protected abstract void selectedDoubleClickedCallback(int selectedIndex);

    @Override
    protected boolean isSelected(int index)
    {
        return index == selectedIndex && selectedIndex >= 0 && selectedIndex <= arrayList.size();
    }

    @Override
    protected void drawBackground()
    {
        Gui.drawRect(left - 1, top - 1, left + listWidth + 1, top + listHeight + 1, -6250336);
        Gui.drawRect(left, top, left + listWidth, top + listHeight, -16777216);
    }

    // Wrap ArrayList<E>
    @Override
    public boolean isEmpty()
    {
        return arrayList.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return arrayList.contains(o);
    }

    @Override
    @Nonnull
    public Iterator<E> iterator()
    {
        return arrayList.iterator();
    }

    @Override
    @Nonnull
    public Object[] toArray()
    {
        return arrayList.toArray();
    }

    @Override
    @Nonnull
    public <T> T[] toArray(@Nonnull T[] a)
    {
        //noinspection SuspiciousToArrayCall
        return arrayList.toArray(a);
    }

    @Override
    public boolean remove(Object o)
    {
        return arrayList.remove(o);
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c)
    {
        return arrayList.containsAll(c);
    }

    @Override
    public boolean addAll(int index, @Nonnull Collection<? extends E> c)
    {
        return arrayList.addAll(index, c);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator)
    {
        arrayList.replaceAll(operator);
    }

    @Override
    public Spliterator<E> spliterator()
    {
        return arrayList.spliterator();
    }

    @Override
    public int size()
    {
        return arrayList.size();
    }

    @Override
    public boolean add(E e)
    {
        return arrayList.add(e);
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends E> c)
    {
        return arrayList.addAll(c);
    }

    @Override
    public void clear()
    {
        arrayList.clear();
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> c)
    {
        return arrayList.removeAll(c);
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> c)
    {
        return arrayList.retainAll(c);
    }

    @Override
    public E get(int index)
    {
        return arrayList.get(index);
    }

    @Override
    public E set(int index, E element)
    {
        return arrayList.set(index, element);
    }

    @Override
    public void add(int index, E element)
    {
        arrayList.add(element);
    }

    @Override
    public E remove(int index)
    {
        return arrayList.remove(index);
    }

    @Override
    public int indexOf(Object o)
    {
        return arrayList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return arrayList.lastIndexOf(o);
    }

    @Override
    @Nonnull
    public ListIterator<E> listIterator()
    {
        return arrayList.listIterator();
    }

    @Override
    @Nonnull
    public ListIterator<E> listIterator(int index)
    {
        return arrayList.listIterator(index);
    }

    @Override
    @Nonnull
    public List<E> subList(int fromIndex, int toIndex)
    {
        return arrayList.subList(fromIndex, toIndex);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter)
    {
        return arrayList.removeIf(filter);
    }

    @Override
    public Stream<E> stream()
    {
        return arrayList.stream();
    }

    @Override
    public Stream<E> parallelStream()
    {
        return arrayList.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super E> action)
    {
        arrayList.forEach(action);
    }

    @Override
    public void sort(Comparator<? super E> c)
    {
        arrayList.sort(c);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof List<?> && arrayList.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return arrayList.hashCode();
    }
}
