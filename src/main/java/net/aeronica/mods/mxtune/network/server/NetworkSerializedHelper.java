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
    private static final int MAX_BUFFER = 32000;
    private static final String EMPTY_STRING = "";
    private static byte[] byteBuffer = null;

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
        int expectedBytes = buffer.readInt();
        int inStreamSize = 0;
        int bufferReadableBytes = buffer.readableBytes();
        int bufferMaxCapacity = buffer.maxCapacity();
        Serializable obj;

        // Deserialize data object from a byte array
        byteBuffer = buffer.readByteArray();

        try
        {
            ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer);
            ObjectInputStream in = new ObjectInputStream(bis);
            inStreamSize = in.available();
            obj = (Serializable) in.readObject();
            in.close();
            ModLogger.debug("");
            ModLogger.debug("ReadObjStats: bufferMaxCapacity: %d, expectedBytes: %d,bufferReadableBytes: %d, inStreamSize: %d", bufferMaxCapacity, expectedBytes, bufferReadableBytes, inStreamSize);
            return obj;
        }
        catch (ClassNotFoundException e)
        {
            ModLogger.error(e);
            ModLogger.debug("ClassNotFoundException: obj is null");
        }
        return null;
    }

    public static void writeBuffer(PacketBuffer buffer, Serializable obj) throws IOException
    {
        // Serialize data object to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject((Serializable) obj);
        out.close();

        int bosSize = bos.size();
        int bufferCapacity = buffer.maxCapacity();
        int maxWriteBytes = buffer.maxWritableBytes();
        int writableBytes = buffer.writableBytes();

        ModLogger.debug("");
        ModLogger.debug("WriteObjStats: bosSize: %s, bufferCapacity %d, maxWriteBytes %d, writableBytes %d", bosSize, bufferCapacity, maxWriteBytes, writableBytes);

        // bytes to write
        buffer.writeInt(bosSize);
        // Get the bytes of the serialized object
        byteBuffer = bos.toByteArray();
        buffer.writeByteArray(byteBuffer);
    }
}
