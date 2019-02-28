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
package net.aeronica.mods.mxtune.network.client;

import net.aeronica.mods.mxtune.groups.GroupHelper;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.sound.ClientAudio;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

public class StopPlayIDMessage extends AbstractClientMessage<StopPlayIDMessage>
{
    private Integer playID;

    @SuppressWarnings("unused")
    public StopPlayIDMessage() {/* Required by the PacketDispatcher */}

    public StopPlayIDMessage(Integer playID)
    {
        this.playID = playID;
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
        ModLogger.debug("Remove Managed playID: &d", playID);
        GroupHelper.removeClientManagedPlayID(playID);
        ClientAudio.queueAudioDataRemoval(playID);
    }
}
