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

import net.aeronica.mods.mxtune.groups.GROUPS;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.util.MIDISystemUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PlayJamMessage extends AbstractClientMessage<PlayJamMessage>
{
    private String jamMML;
    private Integer groupID;
    private BlockPos pos;
    private boolean isPlaced;

    public PlayJamMessage() {}

    public PlayJamMessage(String jamMML, Integer groupID)
    {
        this(jamMML, groupID, new BlockPos(0,0,0), false);
    }

    public PlayJamMessage(String jamMML, Integer groupID, BlockPos pos, boolean isPlaced)
    {
        this.jamMML = jamMML;
        this.groupID = groupID;
        this.pos = pos;
        this.isPlaced = isPlaced;
    }
    
    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        jamMML = ByteBufUtils.readUTF8String(buffer);
        groupID = ByteBufUtils.readVarInt(buffer, 5);
        pos = new BlockPos(ByteBufUtils.readVarInt(buffer, 5), ByteBufUtils.readVarInt(buffer, 5), ByteBufUtils.readVarInt(buffer, 5));
        isPlaced = (ByteBufUtils.readVarShort(buffer)==1);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeUTF8String(buffer, jamMML);
        ByteBufUtils.writeVarInt(buffer, groupID, 5);
        ByteBufUtils.writeVarInt(buffer, pos.getX(), 5);
        ByteBufUtils.writeVarInt(buffer, pos.getY(), 5);
        ByteBufUtils.writeVarInt(buffer, pos.getZ(), 5);
        ByteBufUtils.writeVarShort(buffer, isPlaced?1:0);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (MIDISystemUtil.getInstance().midiUnavailableWarn(player) == false)
        {
            if (MusicOptionsUtil.getMuteResult(player, (EntityPlayer) player.worldObj.getEntityByID(GROUPS.getMembersGroupLeader(groupID))) == false)
            {
                ClientAudio.play(groupID, jamMML, pos);
            }
        }
    }
}
