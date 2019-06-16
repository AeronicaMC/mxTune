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

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class ChunkToolMessage extends AbstractServerMessage<ChunkToolMessage>
{
    private Operation operation;

    @SuppressWarnings("unused")
    public ChunkToolMessage() {/* Required by the PacketDispatcher */}

    public ChunkToolMessage(Operation operation)
    {
        this.operation = operation;
    }
    
    @Override
    protected void read(PacketBuffer buffer)
    {
        operation = buffer.readEnumValue(Operation.class);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeEnumValue(operation);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (MusicOptionsUtil.isMxTuneServerUpdateAllowed(player))
        {
            switch (operation)
            {
                case NOP:
                    break;
                case START:
                    break;
                case END:
                    break;
                case DO_IT:
                    break;
                default:
            }
        }
    }

    public enum Operation
    {
        NOP, START, END, DO_IT
    }
}
