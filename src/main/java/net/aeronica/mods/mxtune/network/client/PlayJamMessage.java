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
import net.aeronica.mods.mxtune.mml.MMLManager;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

public class PlayJamMessage extends AbstractClientMessage<PlayJamMessage>
{
    private String jamMML;
    private String groupID;

    public PlayJamMessage() {}

    public PlayJamMessage(String jamMML, String groupID)
    {
        this.jamMML = jamMML;
        this.groupID = groupID;
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        jamMML = ByteBufUtils.readUTF8String(buffer);
        groupID = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        ByteBufUtils.writeUTF8String(buffer, jamMML);
        ByteBufUtils.writeUTF8String(buffer, groupID);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if (MusicOptionsUtil.getMuteResult(player, player.worldObj.getPlayerEntityByName(GROUPS.getLeaderOfGroup(groupID))) == false)
        {
            MMLManager.getInstance().mmlPlay(jamMML, groupID, true, MusicOptionsUtil.getMidiVolume(player));
        }
    }
}
