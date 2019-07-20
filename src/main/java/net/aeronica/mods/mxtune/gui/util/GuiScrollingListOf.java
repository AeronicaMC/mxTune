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
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public abstract class GuiScrollingListOf<E> extends GuiScrollingListMX implements List<E>, IHooverText
{
    private final List<E> arrayList = new CopyOnWriteArrayList<>();
    protected Screen gui;
    protected Minecraft mc;
    protected int entryHeight;
    private final List<String> hooverTexts = new ArrayList<>();
    private final List<String> hooverTextsCopy = new ArrayList<>();
    private String hooverStatusText = "";
    protected int guiLeft = 0;
    protected int guiTop = 0;

    public <T extends Screen> GuiScrollingListOf(T gui, int entryHeight, int width, int height, int top, int bottom, int left)
    {
        super(gui.mc, width, height, top, bottom, left, entryHeight, gui.width, gui.height);
        this.gui = gui;
        this.mc = gui.mc;
        this.entryHeight = entryHeight;
    }

    public <T extends Screen> GuiScrollingListOf(T gui)
    {
        super(gui.mc);
        this.gui = gui;
        this.mc = gui.mc;
    }

    public void setLayout(int entryHeight, int width, int height, int top, int bottom, int left)
    {
        super.setLayout(entryHeight, width, height, top, bottom, left, gui.width, gui.height);
        this.entryHeight = entryHeight;
    }

    /**
     * Used to the set the guiLeft and guiTop of a guiContainer based gui.
     * @param guiLeft guiLeft from the parent gui.
     * @param guiTop guiTop from the parent gui.
     */
    public void setGui(int guiLeft, int guiTop)
    {
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
    }

    public void resetScroll()
    {
        this.scrollDistance = keepSelectionInViewableArea();
    }

    private float keepSelectionInViewableArea()
    {
        int listHeight = this.getContentHeight() - (this.bottom - this.top - 4);
        float scrollDistance = (float) (selectedIndex * entryHeight);

        // NPE prevention!
        if (listHeight < 0)
        {
            listHeight = entryHeight;
        }

        if (scrollDistance < 0.0F)
        {
            scrollDistance = 0.0F;
        }
        if (scrollDistance > listHeight)
        {
            scrollDistance = listHeight;
        }
        return scrollDistance;
    }

    public void scrollToEnd()
    {
        synchronized (arrayList)
        {
            setSelectedIndex(arrayList.size());
            resetScroll();
        }
    }

    public void scrollToTop()
    {
        setSelectedIndex(0);
        resetScroll();
    }

    public void keyTyped(char typedChar, int keyCode)
    {
        if (isPointInRegion())
        {
            int pageSize = (bottom - top) / entryHeight;
            switch (keyCode)
            {
                case Keyboard.KEY_HOME:
                    scrollToTop();
                    break;
                case Keyboard.KEY_END:
                    scrollToEnd();
                    break;
                case Keyboard.KEY_DOWN:
                    int next = selectedIndex + 1;
                    setSelectedIndex(next < getSize() ? next : getSize());
                    resetScroll();
                    break;
                case Keyboard.KEY_UP:
                    int prev = selectedIndex - 1;
                    setSelectedIndex(prev > 0 ? prev : 0);
                    resetScroll();
                    break;
                case Keyboard.KEY_NEXT:
                    next = selectedIndex + pageSize - 1;
                    setSelectedIndex(next < getSize() ? next : getSize());
                    resetScroll();
                    break;
                case Keyboard.KEY_PRIOR:
                    prev = selectedIndex - pageSize + 1;
                    setSelectedIndex(prev > 0 ? prev : 0);
                    resetScroll();
                    break;
                case Keyboard.KEY_DELETE:
                    deleteAction(selectedIndex);
                    break;
                default:
            }
            if (selectedIndex >= 0 && selectedIndex < arrayList.size())
                selectedClickedCallback(selectedIndex);
        }
    }

    @Override
    public boolean isMouseOverElement(int guiLeft, int guiTop, int mouseX, int mouseY)
    {
        return ModGuiUtils.isPointInRegion(left, top, bottom - top, listWidth, guiLeft, guiTop, mouseX, mouseY);
    }

    protected boolean isPointInRegion()
    {
        return ModGuiUtils.isPointInRegion(left, top, bottom - top, listWidth, guiLeft, guiTop, mouseX, mouseY);
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

    @Override
    public void addHooverTexts(String hooverText) {hooverTexts.add(hooverText);}

    private boolean isEnableHighlightSelected()
    {
        return this.highlightSelected;
    }

    public int getSelectedIndex() { return selectedIndex; }

    public void setSelectedIndex(int index)
    {
        synchronized (arrayList)
        {
            if (index < 0)
                selectedIndex = -1;
            else if (index >= arrayList.size())
                selectedIndex = arrayList.size() - 1;
            else
                selectedIndex = index;
        }
    }

    @Nullable
    public E get()
    {
        synchronized (arrayList)
        {
            if (this.isSelected(selectedIndex))
                return arrayList.get(selectedIndex);
            else
                return null;
        }
    }

    @Override
    protected int getSize()
    {
        synchronized (arrayList)
        {
            return arrayList.size();
        }
    }

    public List<E> getList()
    {
        synchronized (arrayList)
        {
            return arrayList;
        }
    }

    public void elementClicked(int index)
    {
        elementClicked(index, false);
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick)
    {
        if (isEnableHighlightSelected() && index == selectedIndex && !doubleClick) return;
        setSelectedIndex(index);

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

    protected void deleteAction(int index) {/* NOP */}

    @Override
    public boolean isSelected(int index)
    {
        return index == selectedIndex && index > -1 && index < arrayList.size();
    }

    @Override
    protected void drawBackground()
    {
        AbstractGui.drawRect(left - 1, top - 1, left + listWidth + 1, top + listHeight + 1, -6250336);
        AbstractGui.drawRect(left, top, left + listWidth, top + listHeight, -16777216);
    }

    // Wrap ArrayList<E>
    @Override
    public boolean isEmpty()
    {
        synchronized (arrayList)
        {
            return arrayList.isEmpty();
        }
    }

    @Override
    public boolean contains(Object o)
    {
        synchronized (arrayList)
        {
            return arrayList.contains(o);
        }
    }

    @Override
    @Nonnull
    public Iterator<E> iterator()
    {
        synchronized (arrayList)
        {
            return arrayList.iterator();
        }
    }

    @Override
    @Nonnull
    public Object[] toArray()
    {
        synchronized (arrayList)
        {
            return arrayList.toArray();
        }
    }

    @Override
    @Nonnull
    public <T> T[] toArray(@Nonnull T[] a)
    {
        synchronized (arrayList)
        {
            //noinspection SuspiciousToArrayCall
            return arrayList.toArray(a);
        }
    }

    @Override
    public boolean remove(Object o)
    {
        synchronized (arrayList)
        {
            return arrayList.remove(o);
        }
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c)
    {
        synchronized (arrayList)
        {
            return arrayList.containsAll(c);
        }
    }

    @Override
    public boolean addAll(int index, @Nonnull Collection<? extends E> c)
    {
        synchronized (arrayList)
        {
            return arrayList.addAll(index, c);
        }
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator)
    {
        synchronized (arrayList)
        {
            arrayList.replaceAll(operator);
        }
    }

    @Override
    public Spliterator<E> spliterator()
    {
        synchronized (arrayList)
        {
            return arrayList.spliterator();
        }
    }

    @Override
    public int size()
    {
        synchronized (arrayList)
        {
            return arrayList.size();
        }
    }

    @Override
    public boolean add(E e)
    {
        synchronized (arrayList)
        {
            return arrayList.add(e);
        }
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends E> c)
    {
        synchronized (arrayList)
        {
            return arrayList.addAll(c);
        }
    }

    @Override
    public void clear()
    {
        synchronized (arrayList)
        {
            arrayList.clear();
        }
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> c)
    {
        synchronized (arrayList)
        {
            return arrayList.removeAll(c);
        }
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> c)
    {
        synchronized (arrayList)
        {
            return arrayList.retainAll(c);
        }
    }

    @Override
    public E get(int index)
    {
        synchronized (arrayList)
        {
            return arrayList.get(index);
        }
    }

    @Override
    public E set(int index, E element)
    {
        synchronized (arrayList)
        {
            return arrayList.set(index, element);
        }
    }

    @Override
    public void add(int index, E element)
    {
        synchronized (arrayList)
        {
            arrayList.add(element);
        }
    }

    @Override
    public E remove(int index)
    {
        synchronized (arrayList)
        {
            return arrayList.remove(index);
        }
    }

    @Override
    public int indexOf(Object o)
    {
        synchronized (arrayList)
        {
            return arrayList.indexOf(o);
        }
    }

    @Override
    public int lastIndexOf(Object o)
    {
        synchronized (arrayList)
        {
            return arrayList.lastIndexOf(o);
        }
    }

    @Override
    @Nonnull
    public ListIterator<E> listIterator()
    {
        synchronized (arrayList)
        {
            return arrayList.listIterator();
        }
    }

    @Override
    @Nonnull
    public ListIterator<E> listIterator(int index)
    {
        synchronized (arrayList)
        {
            return arrayList.listIterator(index);
        }
    }

    @Override
    @Nonnull
    public List<E> subList(int fromIndex, int toIndex)
    {
        synchronized (arrayList)
        {
            return arrayList.subList(fromIndex, toIndex);
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter)
    {
        synchronized (arrayList)
        {
            return arrayList.removeIf(filter);
        }
    }

    @Override
    public Stream<E> stream()
    {
        synchronized (arrayList)
        {
            return arrayList.stream();
        }
    }

    @Override
    public Stream<E> parallelStream()
    {
        synchronized (arrayList)
        {
            return arrayList.parallelStream();
        }
    }

    @Override
    public void forEach(Consumer<? super E> action)
    {
        synchronized (arrayList)
        {
            arrayList.forEach(action);
        }
    }

    @Override
    public void sort(Comparator<? super E> c)
    {
        synchronized (arrayList)
        {
            arrayList.sort(c);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        synchronized (arrayList)
        {
            return obj instanceof List<?> && arrayList.equals(obj);
        }
    }

    @Override
    public int hashCode()
    {
        synchronized (arrayList)
        {
            return arrayList.hashCode();
        }
    }
}
