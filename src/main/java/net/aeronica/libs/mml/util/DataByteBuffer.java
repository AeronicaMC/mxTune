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


public class DataByteBuffer
{
    private final byte[] data;
    private final int length;

    public DataByteBuffer(byte[] data)
    {
        this.data = data;
        this.length = data.length;
    }

    public byte getByte(int index) throws ArrayIndexOutOfBoundsException
    {
        if (index > this.length-1 || index < 0)
            throw new ArrayIndexOutOfBoundsException();
        return data[index];
    }

    public byte[] getData()
    {
        return data;
    }

    public int getLength()
    {
        return length;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (byte datum : data)
        {
            sb.append(((char) datum));
        }
        return sb.toString();
    }
}
