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
package net.aeronica.libs.mml.util;

/**
 */
public class IndexBuffer
{
    private int[] position = null;
    private int[] length = null;
    private byte[] type = null; /* assuming there can be max 256 different token or element types (1 byte / type) */

    public int count = 0;  // the number of tokens / elements in the IndexBuffer.

    private IndexBuffer()
    {
        /* NOP */
    }

    public IndexBuffer(int capacity, boolean useTypeArray)
    {
        this.position = new int[capacity];
        this.length = new int[capacity];
        if (useTypeArray)
        {
            this.type = new byte[capacity];
        }
    }

    public int getPosition(int index)
    {
        return position[index];
    }

    public void setPosition(int index, int position)
    {
        this.position[index] = position;
    }

    public int getLength(int index)
    {
        return length[index];
    }

    public void setLength(int index, int length)
    {
        this.length[index] = length;
    }

    public byte getType(int index)
    {
        return type[index];
    }

    public void setType(int index, byte type)
    {
        this.type[index] = type;
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }
}
