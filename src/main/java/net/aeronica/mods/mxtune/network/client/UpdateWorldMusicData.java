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

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.util.GUID;
import net.aeronica.mods.mxtune.world.caps.world.ModWorldPlaylistHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class UpdateWorldMusicData extends AbstractClientMessage<UpdateWorldMusicData>
{
    private GUID guid;
    private long ddddSigBits;
    private long ccccSigBits;
    private long bbbbSigBits;
    private long aaaaSigBits;

    @SuppressWarnings("unused")
    public UpdateWorldMusicData() {/* Required by the PacketDispatcher */}

    public UpdateWorldMusicData(GUID guid)
    {
        this.guid = guid;
        ddddSigBits = guid.getDdddSignificantBits();
        ccccSigBits = guid.getCcccSignificantBits();
        bbbbSigBits = guid.getBbbbSignificantBits();
        aaaaSigBits = guid.getAaaaSignificantBits();
    }

    @Override
    protected void read(PacketBuffer buffer)
    {
        ddddSigBits = buffer.readLong();
        ccccSigBits = buffer.readLong();
        bbbbSigBits = buffer.readLong();
        aaaaSigBits = buffer.readLong();
        guid = new GUID(ddddSigBits, ccccSigBits, bbbbSigBits, aaaaSigBits);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeLong(ddddSigBits);
        buffer.writeLong(ccccSigBits);
        buffer.writeLong(bbbbSigBits);
        buffer.writeLong(aaaaSigBits);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        World world = MXTune.proxy.getClientWorld();
        if (world != null)
        {
            if (world.hasCapability(ModWorldPlaylistHelper.MOD_WORLD_DATA, null))
                ModWorldPlaylistHelper.setPlaylistGuid(world, guid);
        }
    }
}
