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

package net.aeronica.mods.mxtune.network.server;

import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.network.PacketBuffer;

import java.io.*;

public class NetworkSerializedHelper
{
    private static final int MAX_BUFFER = 8192;

    public NetworkSerializedHelper() { /* NOP */ }

//    public void writeLongString(PacketBuffer buffer, Serializable obj)
//    {
//        int totalLength = obj;
//        int index = 0;
//        int start;
//        int end;
//
//        buffer.writeInt(totalLength);
//        buffer.writeInt(obj.hashCode());
//        do
//        {
//            start = (MAX_BUFFER * index);
//            end = Math.min((MAX_BUFFER * (index + 1)), totalLength);
//            buffer.writeString(obj.substring(start, end));
//            index++;
//        } while (end < totalLength);
//    }
//
//    public String readLongString(PacketBuffer buffer)
//    {
//        StringBuilder buildString = new StringBuilder();
//
//        int expectedLength = buffer.readInt();
//        int expectedHashCode = buffer.readInt();
//        do
//            buildString.append(buffer.readString(MAX_BUFFER));
//        while (buildString.length() < expectedLength);
//
//        String receivedString = buildString.toString();
//
//        if ((expectedHashCode == receivedString.hashCode()) && (expectedLength == receivedString.length()))
//        {
//            return receivedString;
//        }
//        else
//        {
//            ModLogger.error("StringHelper#readLongString received data error: expected length: %d, actual: %d", expectedLength, receivedString.length());
//            ModLogger.error("StringHelper#readLongString received data error: expected hash: %d, actual: %d", expectedHashCode, receivedString.hashCode());
//            return EMPTY_STRING;
//        }
//    }

    public static Serializable readBuffer(PacketBuffer buffer) throws IOException
    {
        // bytes to read
        int inStreamSize;
        int bufferReadableBytes = buffer.readableBytes();

        Serializable obj;

        // Deserialize data object from a byte array
        int expectedLength = buffer.readInt();
        int expectedHashCode = buffer.readInt();
        byte[] byteBuffer = new byte[0];
        do
        {
            byte[] readBuffer;
            readBuffer = buffer.readByteArray(MAX_BUFFER);
            byteBuffer = appendByteArrays(byteBuffer, readBuffer, readBuffer.length);
        }
        while (byteBuffer.length < expectedLength);

        try
        {
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer);
            ObjectInputStream in = new ObjectInputStream(bis);
            inStreamSize = in.available();
            obj = (Serializable) in.readObject();
            in.close();
            ModLogger.debug("");
            ModLogger.debug("ReadObjStats: expectedLength: %d, expectedHashCode: %d, bufferReadableBytes: %d, inStreamSize: %d", expectedLength, expectedHashCode, bufferReadableBytes, inStreamSize);
        }
        catch (ClassNotFoundException e)
        {
            ModLogger.error(e);
            ModLogger.debug("ClassNotFoundException: obj is null");
            return null;
        }
        if ((obj != null && expectedHashCode == obj.hashCode()))
        {
            return obj;
        }
        else
        {
            ModLogger.error("StringHelper#readLongString received data error: expected hash: %d, actual: %d", expectedHashCode, obj.hashCode());
            return null;
        }
    }

    public static void writeBuffer(PacketBuffer buffer, Serializable obj) throws IOException
    {
        byte[] byteBuffer = null;
        int totalLength = 0;
        int index = 0;
        int start;
        int end;
        // Serialize data object to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject((Serializable) obj);
        out.flush();

        byteBuffer = bos.toByteArray();

        int bufferCapacity = buffer.maxCapacity();
        int maxWriteBytes = buffer.maxWritableBytes();
        int writableBytes = buffer.writableBytes();

        totalLength = byteBuffer.length;
        buffer.writeInt(totalLength);
        buffer.writeInt(obj.hashCode());
        do
        {
            byte[] writeBuffer = new byte[MAX_BUFFER];
            start = (MAX_BUFFER * index);
            end = Math.min((MAX_BUFFER * (index + 1)), totalLength);
            System.arraycopy(byteBuffer, start, writeBuffer, 0, end - start);
            buffer.writeByteArray(writeBuffer);
            index++;
        } while (end < totalLength);

        ModLogger.debug("");
        ModLogger.debug("WriteObjStats: byteBuffer[] length: %s, bufferCapacity: %d, maxWriteBytes: %d, writableBytes: %d, obj.hash: %d", totalLength, bufferCapacity, maxWriteBytes, writableBytes, obj.hashCode());

        // bytes to write
//        buffer.writeInt(bosSize);
//        // Get the bytes of the serialized object
//        byteBuffer = bos.toByteArray();
//        buffer.writeByteArray(byteBuffer);
    }

    /**
     * Creates a new array with the second array appended to the end of the
     * first array.
     *
     * @param arrayOne The first array.
     * @param arrayTwo The second array.
     * @param length   How many bytes to append from the second array.
     * @return Byte array containing information from both arrays.
     */
    private static byte[] appendByteArrays(byte[] arrayOne, byte[] arrayTwo, int length)
    {
        byte[] newArray;
        if (arrayOne == null && arrayTwo == null)
        {
            // no data, just return
            return new byte[0];
        }
        else if (arrayOne == null)
        {
            // create the new array, same length as arrayTwo:
            newArray = new byte[length];
            // fill the new array with the contents of arrayTwo:
            System.arraycopy(arrayTwo, 0, newArray, 0, length);
        }
        else if (arrayTwo == null)
        {
            // create the new array, same length as arrayOne:
            newArray = new byte[arrayOne.length];
            // fill the new array with the contents of arrayOne:
            System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
        }
        else
        {
            // create the new array large enough to hold both arrays:
            newArray = new byte[arrayOne.length + length];
            System.arraycopy(arrayOne, 0, newArray, 0, arrayOne.length);
            // fill the new array with the contents of both arrays:
            System.arraycopy(arrayTwo, 0, newArray, arrayOne.length, length);
        }

        return newArray;
    }
}
