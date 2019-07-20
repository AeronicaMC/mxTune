/*
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

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.gui.GuiGuid;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class JoinGroupMessage extends AbstractClientMessage<JoinGroupMessage>
{
    private int groupID;

    @SuppressWarnings("unused")
    public JoinGroupMessage() { /* Required by the PacketDispatcher */ }

    public JoinGroupMessage( Integer groupID) {this.groupID = groupID; }

    @Override
    protected void read(PacketBuffer buffer) { groupID = buffer.readInt(); }

    @Override
    protected void write(PacketBuffer buffer) { buffer.writeInt(groupID); }

    @Override
    public void process(PlayerEntity player, Side side)
    {
        player.openGui(MXTune.instance, GuiGuid.GUI_GROUP_JOIN, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
    }
}
