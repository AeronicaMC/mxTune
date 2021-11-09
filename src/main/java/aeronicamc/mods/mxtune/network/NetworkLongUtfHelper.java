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

package aeronicamc.mods.mxtune.network;

import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkLongUtfHelper
{
    private static final Logger LOGGER = LogManager.getLogger(NetworkLongUtfHelper.class);
    private static final int MAX_STRING_BUFFER = 16384;
    private static final String EMPTY_STRING = "";

    public NetworkLongUtfHelper() { /* NOP */ }

    /**
     * For Server to Client use a network buffer is limited to about 1MB (1048576).
     * <p>For Client to Server use a network buffer is limited to about 32KB (32767).
     *
     * @param buffer The vanilla netty packet wrapper.
     * @param stringIn The string to be transferred.
     */
    public void writeLongUtf(PacketBuffer buffer, String stringIn)
    {
        int totalLength = stringIn.length();
        int index = 0;
        int start;
        int end;

        buffer.writeInt(totalLength);
        buffer.writeInt(stringIn.hashCode());
        do
        {
            start = (MAX_STRING_BUFFER * index);
            end = Math.min((MAX_STRING_BUFFER * (index + 1)), totalLength);
            buffer.writeUtf(stringIn.substring(start, end));
            index++;
        } while (end < totalLength);
    }

    public String readLongUtf(PacketBuffer buffer)
    {
        StringBuilder buildString = new StringBuilder();

        int expectedLength = buffer.readInt();
        int expectedHashCode = buffer.readInt();
        do
            buildString.append(buffer.readUtf(MAX_STRING_BUFFER));
        while (buildString.length() < expectedLength);

        String receivedString = buildString.toString();

        if ((expectedHashCode == receivedString.hashCode()) && (expectedLength == receivedString.length()))
        {
            return receivedString;
        }
        else
        {
            LOGGER.error("StringHelper#readLongString received data error: expected length: {}, actual: {}", expectedLength, receivedString.length());
            LOGGER.error("StringHelper#readLongString received data error: expected hash: {}, actual: {}", expectedHashCode, receivedString.hashCode());
            return EMPTY_STRING;
        }
    }
}