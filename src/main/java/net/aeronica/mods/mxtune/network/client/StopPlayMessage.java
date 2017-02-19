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

import java.io.IOException;

import net.aeronica.mods.mxtune.groups.PlayManager;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StopPlayMessage extends AbstractClientMessage<StopPlayMessage>
{

    private Integer playID;

    public StopPlayMessage() {}

    public StopPlayMessage(Integer playID) {this.playID = playID;}

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        playID = ByteBufUtils.readVarInt(buffer, 5);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeVarInt(buffer, playID, 5);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (side.isClient())
        {
            handleClientSide(player);
        } else
        {
            handleServerSide(player);
        }
    }

    @SideOnly(Side.CLIENT)
    public void handleClientSide(EntityPlayer playerSP)
    {
        // TODO: More Cleanup for playing - resetting all the players PlayStatuses
        ClientAudio.stop(playID);
    }

    public void handleServerSide(EntityPlayer playerMP)
    {
        //PlayManager.stopPlayID(playID);
    }

}
