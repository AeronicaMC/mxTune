/**
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
package net.aeronica.mods.mxtune.network.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.status.CSDChatStatus;
import net.aeronica.mods.mxtune.status.ClientStateData;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class SendCSDChatMessage extends AbstractClientMessage<SendCSDChatMessage>
{

    private ClientStateData csd;
    private byte[] byteBuffer = null;
    
    public SendCSDChatMessage() {}
    
    public SendCSDChatMessage(ClientStateData csd) {this.csd = csd;}
    
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        // Deserialize data object from a byte array
        byteBuffer = buffer.readByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer) ;
        ObjectInputStream in = new ObjectInputStream(bis) ;
        try
        {
            csd = (ClientStateData) in.readObject();
        } catch (ClassNotFoundException e)
        {
            ModLogger.error(e.getMessage());
        }
        in.close();  
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        try{
            // Serialize data object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ObjectOutputStream out = new ObjectOutputStream(bos) ;
            out.writeObject(csd);
            out.close();

            // Get the bytes of the serialized object
            byteBuffer = bos.toByteArray();
        } catch (IOException e) {
            ModLogger.error(e.getMessage());
        }
        buffer.writeByteArray(byteBuffer);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        new CSDChatStatus(player, csd); 
    }

}
