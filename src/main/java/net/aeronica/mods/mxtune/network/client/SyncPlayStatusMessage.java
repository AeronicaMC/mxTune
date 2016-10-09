/**
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
package net.aeronica.mods.mxtune.network.client;

import java.io.IOException;

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.sound.PlayStatusUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class SyncPlayStatusMessage extends AbstractClientMessage<SyncPlayStatusMessage>
{
    
    boolean playing;
    int entityID;
    public SyncPlayStatusMessage() {}
    
    public SyncPlayStatusMessage(EntityPlayer playerIn, boolean playing) 
    {
        this.playing = playing;
        this.entityID = playerIn.getEntityId();
    }
    
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {  
        playing = buffer.readInt() == 1;
        entityID = buffer.readInt();
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        buffer.writeInt(this.playing ? 1 : 0);        
        buffer.writeInt(this.entityID);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        EntityPlayer target = (EntityPlayer) player.getEntityWorld().getEntityByID(entityID);
        target.getCapability(PlayStatusUtil.PLAY_STATUS, null).setPlaying(null, playing);
    }

}
