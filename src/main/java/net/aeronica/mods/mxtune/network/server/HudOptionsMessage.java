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
package net.aeronica.mods.mxtune.network.server;

import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractServerMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

public class HudOptionsMessage extends AbstractServerMessage<HudOptionsMessage>
{

    int positionHud;
    boolean disableHud;
    float sizeHud;
    
    public HudOptionsMessage() {/* Required by the PacketDispacher */}
    
    public HudOptionsMessage(int positionHud, boolean disableHud, float sizeHud)
    {
        this.positionHud = positionHud;
        this.disableHud = disableHud;
        this.sizeHud = sizeHud;
    }
    
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        positionHud = buffer.readInt();
        disableHud = buffer.readBoolean();
        sizeHud = buffer.readFloat();
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        buffer.writeInt(positionHud);
        buffer.writeBoolean(disableHud);
        buffer.writeFloat(sizeHud);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        MusicOptionsUtil.setHudOptions(player, disableHud, positionHud, sizeHud);
    }

}
