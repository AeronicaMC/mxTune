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

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.capabilities.IJamPlayer;
import net.aeronica.mods.mxtune.network.AbstractMessage.AbstractClientMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 
 * A packet to send ALL data stored in your extended properties to the client.
 * This is handy if you only need to send your data once per game session or all
 * of your data needs to be synchronized together; it's also handy while first
 * starting, since you only need one packet for everything - however, you should
 * NOT use such a packet in your final product!!!
 * 
 * Each packet should handle one thing and one thing only, in order to minimize
 * network traffic as much as possible. There is no point sending 20+ fields'
 * worth of data when you just need the current mana amount; conversely, it's
 * foolish to send 20 packets for all the data when the player first loads, when
 * you could send it all in one single packet.
 * 
 * TL;DR - make separate packets for each piece of data, and one big packet for
 * those times when you need to send everything.
 *
 */
public class SyncPlayerPropsMessage extends AbstractClientMessage<SyncPlayerPropsMessage>
{
    // Previously, we've been writing each field in our properties one at a
    // time,
    // but that is really annoying, and we've already done it in the save and
    // load
    // NBT methods anyway, so here's a slick way to efficiently send all of your
    // extended data, and no matter how much you add or remove, you'll never
    // have
    // to change the packet / synchronization of your data.

    // this will store our ExtendedPlayer data, allowing us to easily read and
    // write
    private NBTTagCompound data;

    // The basic, no-argument constructor MUST be included to use the new
    // automated handling
    public SyncPlayerPropsMessage() {}

    // We need to initialize our data, so provide a suitable constructor:
    public SyncPlayerPropsMessage(EntityPlayer player)
    {
        /** create a new tag compound */
        data = new NBTTagCompound();
        /** and save our player's data into it */
        if (player.hasCapability(MXTuneMain.JAM_PLAYER, null))
        {
            IJamPlayer inst = player.getCapability(MXTuneMain.JAM_PLAYER, null);
            data = (NBTTagCompound) MXTuneMain.JAM_PLAYER.writeNBT(inst, null);
        }
    }

    @Override
    protected void read(PacketBuffer buffer) throws IOException
    {
        data = buffer.readNBTTagCompoundFromBuffer();
    }

    @Override
    protected void write(PacketBuffer buffer) throws IOException
    {
        buffer.writeNBTTagCompoundToBuffer(data);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        /** Now we can just load the NBTTagCompound data directly; one and done folks */
        ModLogger.logInfo("Synchronizing extended properties data on CLIENT");
        if (player.hasCapability(MXTuneMain.JAM_PLAYER, null))
        {
            IJamPlayer inst = player.getCapability(MXTuneMain.JAM_PLAYER, null);
            MXTuneMain.JAM_PLAYER.readNBT(inst, null, data);
        }
    }
}
