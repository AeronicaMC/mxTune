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
package net.aeronica.mods.mxtune.gui;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.aeronica.libs.mml.core.MMLAllowedCharacters;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiMMLBox extends Gui
{
    private final int id;
    private final FontRenderer fontRendererInstance;
    public final int xPosition;
    public final int yPosition;
    public boolean blockCursor;
    /* Keep track of the displayed lines, cursor and selection highlighting */
    private int topTextBox;
    private int fontHeight;
    private int displayLineStartIdx;
    private int scrollAmount;
    private int maxDisplayLines;
    private List<String> lines;
    private HashMap<Integer, Integer> lineWidths;
    private boolean loadingLists;
    /* The width of this text field. */
    public final int width;
    public final int height;
    /* Has the current text being edited on the textbox. */
    private String text;
    private int maxStringLength = 32;
    private int cursorCounter;
    private boolean enableBackgroundDrawing = true;
    /**
     * if true the textbox can lose focus by clicking elsewhere on the screen
     */
    private boolean canLoseFocus = true;
    /**
     * If this value is true along with isEnabled, keyTyped will process the
     * keys.
     */
    private boolean isFocused;
    /**
     * If this value is true along with isFocused, keyTyped will process the
     * keys.
     */
    private boolean isEnabled = true;
    /**
     * The current character index that should be used as start of the rendered
     * text.
     */
    private int lineScrollOffset;
    private int cursorPosition;
    /** other selection position, maybe the same as the cursor */
    private int selectionEnd;
    private int enabledColor = 14737632;
    private int disabledColor = 7368816;
    /** True if this textbox is visible */
    private boolean visible = true;
    private GuiPageButtonList.GuiResponder guiResponder;
    private Predicate<String> validator = Predicates.<String> alwaysTrue();

    public GuiMMLBox(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height)
    {
        this.id = componentId;
        this.fontRendererInstance = fontrendererObj;
        this.xPosition = x;
        this.yPosition = y;
        this.width = width;
        this.height = height;

        this.text = "";
        this.fontHeight = this.fontRendererInstance.FONT_HEIGHT;
        this.topTextBox = this.yPosition - (this.height / 2) + fontHeight;
        this.displayLineStartIdx = this.scrollAmount = 0;
        this.lineWidths = new HashMap<>();
        this.lines = new ArrayList<>();
        this.maxDisplayLines = this.height / fontHeight;
        this.loadingLists = true;
    }

    /**
     * Sets the GuiResponder associated with this text box.
     */
    public void setGuiResponder(GuiPageButtonList.GuiResponder guiResponderIn)
    {
        this.guiResponder = guiResponderIn;
    }

    /**
     * Increments the cursor counter
     */
    public void updateCursorCounter()
    {
        ++this.cursorCounter;
    }

    public boolean isEmpty()
    {
        return this.text.isEmpty();
    }
    
    /**
     * Sets the text of the textbox, and moves the cursor to the end.
     */
    public void setText(String textIn)
    {
        if (this.validator.apply(textIn))
        {
            if (textIn.length() > this.maxStringLength)
            {
                this.text = textIn.substring(0, this.maxStringLength);
            } else
            {
                this.text = textIn;
            }

            this.setCursorPositionEnd();
        }
    }

    /**
     * Returns the contents of the textbox
     */
    public String getText()
    {
        return this.text;
    }
    
    /**
     * Returns the contents of the textbox
     */
    public String getTextToParse()
    {
        /* ArcheAge Semi-Compatibility Adjustments and fixes for stupid MML */
        String copy = this.text.toString();
        
        // remove any remaining "MML@" and ";" tokens
        copy = copy.replaceAll("(MML\\@)|;", "");
        StringBuilder sb = new StringBuilder(copy);
        // Add the required MML BEGIN and END tokens
        if (!copy.regionMatches(true, 0, "MML@", 0, 4) && copy.length() > 0)
            sb.insert(0, "MML@");
        if (!copy.endsWith(";") && copy.length() > 0)
            sb.append(";");
        return sb.toString();
    }

    /**
     * returns the text between the cursor and selectionEnd
     */
    public String getSelectedText()
    {
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return this.text.substring(i, j);
    }

    public void setValidator(Predicate<String> theValidator)
    {
        this.validator = theValidator;
    }

    /**
     * Adds the given text after the cursor, or replaces the currently selected
     * text if there is a selection.
     */
    public void writeText(String textToWrite)
    {
        String s = "";
        String s1 = MMLAllowedCharacters.filterAllowedCharacters(textToWrite);
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        int k = this.maxStringLength - this.text.length() - (i - j);
        int l;

        if (!this.text.isEmpty())
        {
            s = s + this.text.substring(0, i);
        }

        if (k < s1.length())
        {
            s = s + s1.substring(0, k);
            l = k;
        } else
        {
            s = s + s1;
            l = s1.length();
        }

        if (!this.text.isEmpty() && j < this.text.length())
        {
            s = s + this.text.substring(j);
        }

        if (this.validator.apply(s))
        {
            this.text = s;
            this.moveCursorBy(i - this.selectionEnd + l);

            if (this.guiResponder != null)
            {
                this.guiResponder.setEntryValue(this.id, this.text);
            }
        }
    }

    /**
     * Deletes the given number of words from the current cursor's position,
     * unless there is currently a selection, in which case the selection is
     * deleted instead.
     */
    public void deleteWords(int num)
    {
        if (!this.text.isEmpty())
        {
            if (this.selectionEnd != this.cursorPosition)
            {
                this.writeText("");
            } else
            {
                this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
            }
        }
    }

    /**
     * Deletes the given number of characters from the current cursor's
     * position, unless there is currently a selection, in which case the
     * selection is deleted instead.
     */
    public void deleteFromCursor(int num)
    {
        if (!this.text.isEmpty())
        {
            if (this.selectionEnd != this.cursorPosition)
            {
                this.writeText("");
            } else
            {
                boolean flag = num < 0;
                int i = flag ? this.cursorPosition + num : this.cursorPosition;
                int j = flag ? this.cursorPosition : this.cursorPosition + num;
                String s = "";

                if (i >= 0)
                {
                    s = this.text.substring(0, i);
                }

                if (j < this.text.length())
                {
                    s = s + this.text.substring(j);
                }

                if (this.validator.apply(s))
                {
                    this.text = s;

                    if (flag)
                    {
                        this.moveCursorBy(num);
                    }

                    if (this.guiResponder != null)
                    {
                        this.guiResponder.setEntryValue(this.id, this.text);
                    }
                }
            }
        }
    }

    public int getId()
    {
        return this.id;
    }

    /**
     * Gets the starting index of the word at the specified number of words away
     * from the cursor position.
     */
    public int getNthWordFromCursor(int numWords)
    {
        return this.getNthWordFromPos(numWords, this.getCursorPosition());
    }

    /**
     * Gets the starting index of the word at a distance of the specified number
     * of words away from the given position.
     */
    public int getNthWordFromPos(int n, int pos)
    {
        return this.getNthWordFromPosWS(n, pos, true);
    }

    /**
     * Like getNthWordFromPos (which wraps this), but adds option for skipping
     * consecutive spaces
     */
    public int getNthWordFromPosWS(int n, int pos, boolean skipWs)
    {
        int i = pos;
        boolean flag = n < 0;
        int j = Math.abs(n);

        for (int k = 0; k < j; ++k)
        {
            if (!flag)
            {
                int l = this.text.length();
                i = this.text.indexOf(32, i);

                if (i == -1)
                {
                    i = l;
                } else
                {
                    while (skipWs && i < l && this.text.charAt(i) == 32)
                    {
                        ++i;
                    }
                }
            } else
            {
                while (skipWs && i > 0 && this.text.charAt(i - 1) == 32)
                {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != 32)
                {
                    --i;
                }
            }
        }

        return i;
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the
     * selection
     */
    public void moveCursorBy(int num)
    {
        this.setCursorPosition(this.selectionEnd + num);
    }

    /**
     * Sets the current position of the cursor.
     */
    public void setCursorPosition(int pos)
    {
        this.cursorPosition = pos;
        int i = this.text.length();
        this.cursorPosition = MathHelper.clamp(this.cursorPosition, 0, i);
        this.setSelectionPos(this.cursorPosition);
    }

    /**
     * Moves the cursor to the very start of this text box.
     */
    public void setCursorPositionZero()
    {
        this.setCursorPosition(0);
    }

    /**
     * Moves the cursor to the very end of this text box.
     */
    public void setCursorPositionEnd()
    {
        this.setCursorPosition(this.text.length());
    }

    /**
     * Call this method from your GuiScreen to process the keys into the textbox
     */
    public boolean textboxKeyTyped(char typedChar, int keyCode)
    {
        if (!this.isFocused)
        {
            return false;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode))
        {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        } else if (GuiScreen.isKeyComboCtrlC(keyCode))
        {
            GuiScreen.setClipboardString(this.getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode))
        {
            if (this.isEnabled)
            {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode))
        {
            GuiScreen.setClipboardString(this.getSelectedText());

            if (this.isEnabled)
            {
                this.writeText("");
            }

            return true;
        } else
        {
            switch (keyCode)
            {
            case Keyboard.KEY_BACK:

                if (GuiScreen.isCtrlKeyDown())
                {
                    if (this.isEnabled)
                    {
                        this.deleteWords(-1);
                    }
                } else if (this.isEnabled)
                {
                    this.deleteFromCursor(-1);
                }

                return true;
            case Keyboard.KEY_HOME:

                if (GuiScreen.isShiftKeyDown())
                {
                    this.setSelectionPos(0);
                } else
                {
                    this.setCursorPositionZero();
                }

                return true;
            case Keyboard.KEY_UP:
                int offSet = this.getCountforSelectionOffset(-1);
                if (GuiScreen.isShiftKeyDown())
                {
                    if (GuiScreen.isCtrlKeyDown())
                    {
                        this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                    } else
                    {
                        this.setSelectionPos(this.getSelectionEnd() + offSet);
                    }
                } else if (GuiScreen.isCtrlKeyDown())
                {
                    this.setCursorPosition(-1);
                } else
                {
                    this.moveCursorBy(offSet);
                }

                return true;
            case Keyboard.KEY_DOWN:
                offSet = this.getCountforSelectionOffset(1);
                if (GuiScreen.isShiftKeyDown())
                {
                    if (GuiScreen.isCtrlKeyDown())
                    {
                        this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                    } else
                    {
                        this.setSelectionPos(this.getSelectionEnd() + offSet);
                    }
                } else if (GuiScreen.isCtrlKeyDown())
                {
                    this.setCursorPosition(1);
                } else
                {
                    this.moveCursorBy(offSet);
                }

                return true;
            case Keyboard.KEY_PRIOR:
                offSet = this.getCountforSelectionOffset(-this.maxDisplayLines);
                if (GuiScreen.isShiftKeyDown())
                {
                    if (GuiScreen.isCtrlKeyDown())
                    {
                        this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                    } else
                    {
                        this.setSelectionPos(this.getSelectionEnd() + offSet);
                    }
                } else if (GuiScreen.isCtrlKeyDown())
                {
                    this.setCursorPosition(-1);
                } else
                {
                    this.moveCursorBy(offSet);
                }

                return true;
            case Keyboard.KEY_NEXT:
                offSet = this.getCountforSelectionOffset(this.maxDisplayLines);
                if (GuiScreen.isShiftKeyDown())
                {
                    if (GuiScreen.isCtrlKeyDown())
                    {
                        this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                    } else
                    {
                        this.setSelectionPos(this.getSelectionEnd() + offSet);
                    }
                } else if (GuiScreen.isCtrlKeyDown())
                {
                    this.setCursorPosition(1);
                } else
                {
                    this.moveCursorBy(offSet);
                }

                return true;
            case Keyboard.KEY_LEFT:

                if (GuiScreen.isShiftKeyDown())
                {
                    if (GuiScreen.isCtrlKeyDown())
                    {
                        this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                    } else
                    {
                        this.setSelectionPos(this.getSelectionEnd() - 1);
                    }
                } else if (GuiScreen.isCtrlKeyDown())
                {
                    this.setCursorPosition(this.getNthWordFromCursor(-1));
                } else
                {
                    this.moveCursorBy(-1);
                }

                return true;
            case Keyboard.KEY_RIGHT:

                if (GuiScreen.isShiftKeyDown())
                {
                    if (GuiScreen.isCtrlKeyDown())
                    {
                        this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                    } else
                    {
                        this.setSelectionPos(this.getSelectionEnd() + 1);
                    }
                } else if (GuiScreen.isCtrlKeyDown())
                {
                    this.setCursorPosition(this.getNthWordFromCursor(1));
                } else
                {
                    this.moveCursorBy(1);
                }

                return true;
            case Keyboard.KEY_END:

                if (GuiScreen.isShiftKeyDown())
                {
                    this.setSelectionPos(this.text.length());
                } else
                {
                    this.setCursorPositionEnd();
                }

                return true;
            case Keyboard.KEY_DELETE:

                if (GuiScreen.isCtrlKeyDown())
                {
                    if (this.isEnabled)
                    {
                        this.deleteWords(1);
                    }
                } else if (this.isEnabled)
                {
                    this.deleteFromCursor(1);
                }

                return true;
            default:

                if (MMLAllowedCharacters.isAllowedCharacter(typedChar))
                {
                    if (this.isEnabled)
                    {
                        this.writeText(Character.toString(typedChar));
                    }

                    return true;
                } else
                {
                    return false;
                }
            }
        }
    }

    /**
     * Called when mouse is clicked, regardless as to whether it is over this
     * button or not.
     */
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        boolean flag = mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height;

        if (this.canLoseFocus)
        {
            this.setFocused(flag);
        }

        if (this.isFocused && flag && mouseButton == 0)
        {
            if (!this.lines.isEmpty() && !this.lineWidths.isEmpty() && !this.loadingLists)
            {
                int i = mouseX - this.xPosition;
                int j = ((mouseY - (this.yPosition + (this.fontHeight / 2))) / this.fontHeight) + this.displayLineStartIdx;
                if (j > getNumLines()) j = getNumLines() - 1;
                if (j < 0) j = 0;
                int k = getLineWidths(j) - getLineLength(j);

                if (this.enableBackgroundDrawing)
                {
                    i -= 4;
                }
                //ModLogger.debug("i,j,k (" + i + ", " + j + ", " + k + ") lineScrollOffset:" + this.lineScrollOffset);
                if (!text.isEmpty() && (k >= 0) && (i >= 0))
                {
                    int kk = Math.abs(k);
                    String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(kk), this.getWidth());
                    this.setCursorPosition(this.fontRendererInstance.trimStringToWidth(s, i).length() + kk);
                }
            }
        }
    }

    private int getNumLines()
    {
        if (this.lines.isEmpty())
            return 0;
        else
            return this.lines.size();
    }

    private int getLineLength(int line)
    {
        if (this.lines.isEmpty())
            return 0;
        else if (line >= 0) if (this.lines.size() > line) return lines.get(line).length();
        return 0;
    }

    private int getLineWidths(int line)
    {
        if (this.lineWidths.isEmpty())
            return 0;
        else if (line >= 0) if (this.lineWidths.size() > line) return lineWidths.get(line);
        return 0;
    }

    private void scrollDown()
    {
        scrollAmount = 1;
    }

    private void scrollUp()
    {
        scrollAmount = -1;
    }

    private int getCountforSelectionOffset(int lineOffset)
    {
        int start;
        int end;
        if (lineOffset >= 0)
        {
            /** DOWN Positive Offset */
            start = getLineFromSelectionEnd(this.getSelectionEnd());
            end = (start + lineOffset) >= this.getNumLines() ? this.getNumLines() - 1 : start + lineOffset;
            return getLineWidths(end) - (getLineWidths(start));
        } else
        {
            /** UP Negative offset */
            start = getLineFromSelectionEnd(this.getSelectionEnd());
            end = (start + lineOffset) > 0 ? start + lineOffset : 0;
            return -(getLineWidths(start) - getLineWidths(end));
        }
    }

    private int getLineFromSelectionEnd(int selectionEnd)
    {
        int line = displayLineStartIdx; // The fall back
        for (int q = 0; q < lineWidths.size(); q++)
        {
            int runningLength = getLineWidths(q);
            if (selectionEnd <= runningLength) { return q; }
        }
        return line;
    }

    @SuppressWarnings("unused")
    private void updateDisplayWindow(int lineDelta)
    {
        if ((this.displayLineStartIdx + this.maxDisplayLines) <= getLineFromSelectionEnd(this.getSelectionEnd())) scrollAmount = lineDelta;
        if ((this.displayLineStartIdx) > getLineFromSelectionEnd(this.getSelectionEnd())) scrollAmount = -lineDelta;
    }

    private int getLineFromPosition(int currentLine, int position)
    {
        int line = currentLine;
        @SuppressWarnings("unused")
        int difference;

        if ((currentLine + this.maxDisplayLines) <= getLineFromSelectionEnd(position))
        {
            // difference = Math.abs(currentLine -
            // getLineFromSelectionEnd(position));
            // ModLogger.logInfo("diff:"+difference);
            // scrollAmount = difference;
            scrollDown();
        } else if ((currentLine) > getLineFromSelectionEnd(position))
        {
            // difference = Math.abs(currentLine -
            // getLineFromSelectionEnd(position));
            // ModLogger.logInfo("diff:"+difference);
            // scrollAmount = -difference;
            scrollUp();
        }
        return line;
    }

    /**
     * Draws the textbox
     */
    public void drawTextBox()
    {
        if (this.getVisible())
        {
            this.loadingLists = true;

            if (this.getEnableBackgroundDrawing())
            {
                drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1, -6250336);
                drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, -16777216);
            }
            
            /** Collect current text and format it for a list view, then squirrel away some data about our list */
            lines = this.fontRendererInstance.listFormattedStringToWidth(this.text, this.getWidth());

            int accumulateLineLengths = 0;
            for (int i = 0; i < lines.size(); i++)
            {
                accumulateLineLengths += lines.get(i).length();
                lineWidths.put(i, accumulateLineLengths);
            }
            int totalBufferLines = lines.size();
            int numDisplayLines = totalBufferLines;
            int charCount;

            /* Handle any vertical scroll requests */
            if (scrollAmount != 0)
            {
                displayLineStartIdx += scrollAmount;
                scrollAmount = 0;
            }

            /*
             * we want to maintain a scrolling view and keep the cursorPostition
             * within the view
             */
            /* so display line wise that's between 0 and maxLines */
            getLineFromPosition(displayLineStartIdx, this.getSelectionEnd());

            if (displayLineStartIdx > totalBufferLines - maxDisplayLines) displayLineStartIdx = totalBufferLines - maxDisplayLines;
            if (displayLineStartIdx < 0) displayLineStartIdx = 0;

            if (numDisplayLines + displayLineStartIdx > maxDisplayLines) numDisplayLines = maxDisplayLines;

            this.loadingLists = false;
            for (int displayIndex = 0; displayIndex < numDisplayLines; displayIndex++)
            {
                String sLine = lines.get(displayIndex + displayLineStartIdx);
                charCount = lineWidths.get(displayIndex + displayLineStartIdx);
                boolean cursorInLine = ((this.cursorPosition >= charCount - sLine.length()) && (this.cursorPosition <= charCount));

                int cursorPos = this.cursorPosition - (charCount - sLine.length());
                if (cursorPos < 0) cursorPos = 0;
                if (cursorPos > sLine.length()) cursorPos = sLine.length();
                int selectEnd = this.selectionEnd - (charCount - sLine.length());
                if (selectEnd < 0) selectEnd = 0;
                if (selectEnd > sLine.length()) selectEnd = sLine.length();

                int statusColor = this.isEnabled ? this.enabledColor : this.disabledColor;
                boolean cursorBlink = this.isFocused && this.cursorCounter / 6 % 2 == 0 && cursorInLine;
                // ModLogger.logInfo("i:"+displayIndex+",
                // cursorPosition:"+cursorPosition+", charCount:"+charCount+",
                // sLen:"+sLine.length()+", cursorInLine:"+cursorInLine+",
                // cursorPos:"+cursorPos+", selectEnd:"+selectEnd);
                int xBackDraw = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
                int yBackDraw = this.enableBackgroundDrawing ? this.topTextBox + displayIndex * fontHeight + (this.height - 8) / 2
                        : this.topTextBox + displayIndex * fontHeight + (this.height - 16) / 2;
                int j1 = xBackDraw;

                // ModLogger.logInfo("i:"+displayIndex+",
                // cursorPosition:"+cursorPosition+", charCount:"+charCount+",
                // sLen:"+sLine.length()+", cursorInLine:"+cursorInLine+",
                // cursorPos:"+cursorPos+", selectEnd:"+selectEnd);

                /* Draw the line before the cursor */
                if (!sLine.isEmpty())
                {
                    String s1 = cursorInLine ? sLine.substring(0, cursorPos) : sLine;
                    j1 = this.fontRendererInstance.drawStringWithShadow(s1, (float) xBackDraw, (float) yBackDraw, statusColor);
                }

                /* Determine cursor type */
                boolean isLineCursor = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
                int k1 = j1;

                if (!cursorInLine)
                {
                    k1 = cursorPos > 0 ? xBackDraw + this.width : xBackDraw;
                } else if (!isLineCursor)
                {
                    k1 = j1 - 1;
                    --j1;
                }

                /* Draw the rest of the line after the cursor */
                if (!sLine.isEmpty() && cursorInLine && cursorPos < sLine.length())
                {
                   j1 = this.fontRendererInstance.drawStringWithShadow(sLine.substring(cursorPos), (float) j1, (float) yBackDraw, statusColor);
                }

                /* Draw the cursor */
                if (cursorBlink && cursorInLine)
                {
                    if (isLineCursor)
                    {
                        if (blockCursor)
                        {
                            int charWidth = this.fontRendererInstance.getStringWidth(sLine.substring(cursorPos, cursorPos + 1));
                            this.drawCursorVertical(k1 - 1, yBackDraw - 1, k1 + charWidth, yBackDraw + this.fontRendererInstance.FONT_HEIGHT);
                        } else
                            Gui.drawRect(k1 - 1, yBackDraw - 1, k1, yBackDraw + 1 + this.fontRendererInstance.FONT_HEIGHT, -3092272);
                    } else
                    {
                        this.fontRendererInstance.drawStringWithShadow("_", (float) k1, (float) yBackDraw, statusColor);
                    }
                }

                /* Draw the selection */
                if (selectEnd != cursorPos)
                {
                    int l1 = xBackDraw + this.fontRendererInstance.getStringWidth(sLine.substring(0, selectEnd));
                    this.drawCursorVertical(k1, yBackDraw, l1-1, yBackDraw + this.fontRendererInstance.FONT_HEIGHT);
                }
            }
        }
    }

    /**
     * Draws the current selection and a vertical line cursor in the text box.
     */
    private void drawCursorVertical(int startXIn, int startYIn, int endXIn, int endYIn)
    {
        int startX = startXIn;
        int startY = startYIn;
        int endX = endXIn;
        int endY = endYIn;
        
        if (startX < endX)
        {
            int i = startX;
            startX = endX;
            endX = i;
        }

        if (startY < endY)
        {
            int j = startY;
            startY = endY;
            endY = j;
        }

        if (endX > this.xPosition + this.width)
        {
            endX = this.xPosition + this.width;
        }

        if (startX > this.xPosition + this.width)
        {
            startX = this.xPosition + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferBuilder.pos((double) startX, (double) endY, 0.0D).endVertex();
        bufferBuilder.pos((double) endX, (double) endY, 0.0D).endVertex();
        bufferBuilder.pos((double) endX, (double) startY, 0.0D).endVertex();
        bufferBuilder.pos((double) startX, (double) startY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    /**
     * Sets the maximum length for the text in this text box. If the current
     * text is longer than this length, the current text will be trimmed.
     */
    public void setMaxStringLength(int length)
    {
        this.maxStringLength = length;

        if (this.text.length() > length)
        {
            this.text = this.text.substring(0, length);
        }
    }

    /**
     * returns the maximum number of character that can be contained in this text box
     */
    public int getMaxStringLength()
    {
        return this.maxStringLength;
    }

    /**
     * returns the current position of the cursor
     */
    public int getCursorPosition()
    {
        return this.cursorPosition;
    }

    /**
     * Gets whether the background and outline of this text box should be drawn
     * (true if so).
     */
    public boolean getEnableBackgroundDrawing()
    {
        return this.enableBackgroundDrawing;
    }

    /**
     * Sets whether or not the background and outline of this text box should be
     * drawn.
     */
    public void setEnableBackgroundDrawing(boolean enableBackgroundDrawingIn)
    {
        this.enableBackgroundDrawing = enableBackgroundDrawingIn;
    }

    /**
     * Sets the color to use when drawing this text box's text. A different
     * color is used if this text box is disabled.
     */
    public void setTextColor(int color)
    {
        this.enabledColor = color;
    }

    /**
     * Sets the color to use for text in this text box when this text box is
     * disabled.
     */
    public void setDisabledTextColour(int color)
    {
        this.disabledColor = color;
    }

    /**
     * Sets focus to this gui element
     */
    public void setFocused(boolean isFocusedIn)
    {
        if (isFocusedIn && !this.isFocused)
        {
            this.cursorCounter = 0;
        }

        this.isFocused = isFocusedIn;
    }

    /**
     * Getter for the focused field
     */
    public boolean isFocused()
    {
        return this.isFocused;
    }

    /**
     * Sets whether this text box is enabled. Disabled text boxes cannot be
     * typed in.
     */
    public void setEnabled(boolean enabled)
    {
        this.isEnabled = enabled;
    }

    /**
     * the side of the selection that is not the cursor, may be the same as the
     * cursor
     */
    public int getSelectionEnd()
    {
        return this.selectionEnd;
    }

    /**
     * returns the width of the textbox depending on if background drawing is
     * enabled
     */
    public int getWidth()
    {
        return this.getEnableBackgroundDrawing() ? this.width - 8 : this.width;
    }

    /**
     * Sets the position of the selection anchor (the selection anchor and the
     * cursor position mark the edges of the selection). If the anchor is set
     * beyond the bounds of the current text, it will be put back inside.
     */
    public void setSelectionPos(int positionIn)
    {
        int position = positionIn;
        int i = this.text.length();

        if (position > i)
        {
            position = i;
        }

        if (position < 0)
        {
            position = 0;
        }

        this.selectionEnd = position;

        if (this.fontRendererInstance != null)
        {
            if (this.lineScrollOffset > i)
            {
                this.lineScrollOffset = i;
            }

            int j = this.getWidth();
            String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), j);
            int k = s.length() + this.lineScrollOffset;

            if (position == this.lineScrollOffset)
            {
                this.lineScrollOffset -= this.fontRendererInstance.trimStringToWidth(this.text, j, true).length();
            }

            if (position > k)
            {
                this.lineScrollOffset += position - k;
            } else if (position <= this.lineScrollOffset)
            {
                this.lineScrollOffset -= this.lineScrollOffset - position;
            }

            this.lineScrollOffset = MathHelper.clamp(this.lineScrollOffset, 0, i);
        }
    }

    /**
     * Sets whether this text box loses focus when something other than it is
     * clicked.
     */
    public void setCanLoseFocus(boolean canLoseFocusIn)
    {
        this.canLoseFocus = canLoseFocusIn;
    }

    /**
     * returns true if this textbox is visible
     */
    public boolean getVisible()
    {
        return this.visible;
    }

    /**
     * Sets whether or not this textbox is visible
     */
    public void setVisible(boolean isVisible)
    {
        this.visible = isVisible;
    }
}
