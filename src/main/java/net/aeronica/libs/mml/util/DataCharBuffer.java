package net.aeronica.libs.mml.util;

/**
 */
public class DataCharBuffer
{
    public char[] data = null;
    public int length = 0;

    public DataCharBuffer()
    {
        /* NOP */
    }

    public DataCharBuffer(char[] data)
    {
        this.data = data;
    }

    public DataCharBuffer(int capacity)
    {
        this.data = new char[capacity];
    }
}
