/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.network.client;

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class StopPlayMessage extends AbstractClientMessage<StopPlayMessage>
{

    private int playID;

    @SuppressWarnings("unused")
    public StopPlayMessage() {/* Required by the PacketDispatcher */}

    public StopPlayMessage(Integer playID)
    {
        if(playID != null)
            this.playID = playID;
        else
            this.playID = -1;
    }

    @Override
    protected void read(PacketBuffer buffer)
    {
        playID = buffer.readInt();
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeInt(playID);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        ClientAudio.stop(playID);
    }

}
