/*
 * MIT License
 *
 * Copyright (c) 2020 Paul Boese a.k.a. Aeronica
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.aeronica.libs.mml.parser;

import net.aeronica.libs.mml.util.DataByteBuffer;
import net.aeronica.libs.mml.util.IndexBuffer;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class MMLNavigator
{
    private DataByteBuffer buffer     = null;
    private IndexBuffer elementBuffer = null;
    private int         elementIndex  = 0;

    public MMLNavigator(DataByteBuffer buffer, IndexBuffer elementBuffer)
    {
        this.buffer = buffer;
        this.elementBuffer = elementBuffer;
    }

    // IndexBuffer (elementBuffer) navigation support methods

    public boolean hasNext()
    {
        return this.elementIndex < this.elementBuffer.getCount();
    }

    public void next()
    {
        this.elementIndex++;
    }

    public void previous()
    {
        this.elementIndex--;
    }

    // Parser element location methods

    /**
     * For getting the index of the raw data such that we can identify a single character in the source.
     * @return the data buffer index for this character/byte.
     */
    public int position()
    {
        return this.elementBuffer.getPosition(this.elementIndex);
    }

    public int length()
    {
        return this.elementBuffer.getLength(this.elementIndex);
    }

    public byte type()
    {
        return this.elementBuffer.getType(this.elementIndex);
    }

    // Data extraction support methods

    public String asString()
    {
        byte elementType = this.elementBuffer.getType(this.elementIndex);
        if (elementType == ElementTypes.MML_BEGIN)
        {
            try
            {
                return new String(this.buffer.getData(), this.elementBuffer.getPosition(this.elementIndex), this.elementBuffer.getLength(this.elementIndex), StandardCharsets.US_ASCII.name());
            }
            catch (UnsupportedEncodingException e)
            {
                return "";
            }
        }
        return "";
    }

    /**
     * Primitive integer bounded to 5 significant digits. -1 as invalid data.
     * @return -1 for invalid, 0<->99999
     */
    public int asInt()
    {
        byte numberType = this.elementBuffer.getType(this.elementIndex);
        if (numberType == ElementTypes.MML_NUMBER)
        {
            try
            {
                String number = new String(this.buffer.getData(), this.elementBuffer.getPosition(this.elementIndex), this.elementBuffer.getLength(this.elementIndex), StandardCharsets.US_ASCII.name());
                int length = number.length();
                if (length >= 1 && length <= 5)
                    return Integer.parseInt(number);
            } catch (NumberFormatException | UnsupportedEncodingException e)
            {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Only useful with regard to MML notes.
     * @return note characters only [CcDdEeFfGgAaBb] else null char.
     */
    public char asChar()
    {
        if (ElementTypes.MML_NOTE == type())
            return (char) this.buffer.getByte(this.elementBuffer.getPosition(this.elementIndex));
        return 0;
    }

    /**
     * For collecting characters for debugging purposes.
     * @return the source data character.
     */
    public char anyChar()
    {
        return (char) this.buffer.getByte(this.elementBuffer.getPosition(this.elementIndex));
    }
}
