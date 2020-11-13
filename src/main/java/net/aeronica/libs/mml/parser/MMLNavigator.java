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
        return this.elementIndex < this.elementBuffer.count;
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
        return this.elementBuffer.position[this.elementIndex];
    }

    public int length()
    {
        return this.elementBuffer.length[this.elementIndex];
    }

    public byte type()
    {
        return this.elementBuffer.type[this.elementIndex];
    }

    // Data extraction support methods

    public String asString()
    {
        byte elementType = this.elementBuffer.type[this.elementIndex];
        if (elementType == ElementTypes.MML_BEGIN)
        {
            try
            {
                return new String(this.buffer.data, this.elementBuffer.position[this.elementIndex], this.elementBuffer.length[this.elementIndex], StandardCharsets.US_ASCII.name());
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
        byte numberType = this.elementBuffer.type[this.elementIndex];
        if (numberType == ElementTypes.MML_NUMBER)
        {
            try
            {
                String number = new String(this.buffer.data, this.elementBuffer.position[this.elementIndex], this.elementBuffer.length[this.elementIndex], StandardCharsets.US_ASCII.name());
                int length = number.length();
                if (length >= 1 & length <= 5)
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
            return (char) this.buffer.data[this.elementBuffer.position[this.elementIndex]];
        return 0;
    }

    /**
     * For collecting characters for debugging purposes.
     * @return the source data character.
     */
    public char anyChar()
    {
        return (char) this.buffer.data[this.elementBuffer.position[this.elementIndex]];
    }
}
