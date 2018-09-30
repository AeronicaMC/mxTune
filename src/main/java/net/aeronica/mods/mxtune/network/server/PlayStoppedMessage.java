/*
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
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
package net.aeronica.mods.mxtune.network.server;

import net.aeronica.mods.mxtune.groups.PlayManager;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class PlayStoppedMessage extends AbstractServerMessage<PlayStoppedMessage>
{

    private int playID;
    private boolean isBlockEntity;
    
    public PlayStoppedMessage() {/* Required by the PacketDispatcher */}
    
    public PlayStoppedMessage(int playID)
    {
        this.playID = playID;
        this.isBlockEntity = false;
    }

    public PlayStoppedMessage(int playID, boolean isBlockEntity)
    {
        this.playID = playID;
        this.isBlockEntity = isBlockEntity;
    }

    protected void read(PacketBuffer buffer) throws IOException
    {
        playID = buffer.readInt();
        isBlockEntity = buffer.readBoolean();
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        buffer.writeInt(playID);
        buffer.writeBoolean(isBlockEntity);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (isBlockEntity)
            PlayManager.stopPlayID(playID);
        else
            PlayManager.playingEnded(player, playID);
    }

}
