package net.aeronica.libs.mml.util;

/**
 */
public class DataByteBuffer
{
    public byte[] data = null;
    public int length = 0;

    public DataByteBuffer()
    {
        /* NOP */
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++)
        {
            sb.append(((char)data[i]));
        }
        return sb.toString();
    }

    public DataByteBuffer(byte[] data)
    {
        this.data = data;
    }

    public DataByteBuffer(int capacity)
    {
        this.data = new byte[capacity];
    }
}
