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
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;

public class ChunkToolMessage extends AbstractServerMessage<ChunkToolMessage>
{
    private boolean reset;

    @SuppressWarnings("unused")
    public ChunkToolMessage() {/* Required by the PacketDispatcher */}

    public ChunkToolMessage(boolean reset)
    {
        this.reset = reset;
    }
    
    @Override
    protected void read(PacketBuffer buffer)
    {
        reset = buffer.readBoolean();
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeBoolean(reset);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (MusicOptionsUtil.isMxTuneServerUpdateAllowed(player))
        {
            World world = player.world;
            Chunk chunk = world.getChunk(player.getPosition());
            Operation operation = reset ? Operation.RESET : MusicOptionsUtil.getChunkToolOperation(player);
            if (chunk.isLoaded())
            {
                switch (operation)
                {
                    case START:
                        MusicOptionsUtil.setChunkStart(player, chunk);
                        MusicOptionsUtil.setChunkToolOperation(player, Operation.END);
                        break;
                    case END:
                        MusicOptionsUtil.setChunkEnd(player, chunk);
                        MusicOptionsUtil.setChunkToolOperation(player, Operation.DO_IT);
                        break;
                    case DO_IT:

                    case RESET:
                    default:
                        MusicOptionsUtil.setChunkToolOperation(player, Operation.START);
                        MusicOptionsUtil.setChunkStart(player, null);
                        MusicOptionsUtil.setChunkEnd(player, null);
                }
            }
        }
    }

    public enum Operation
    {
        START, END, RESET, DO_IT
    }
}
