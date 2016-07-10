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
package net.aeronica.mods.mxtune.capabilities;

import java.util.Collection;
import java.util.HashMap;

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.SyncPlayerPropsMessage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class JamDefaultImpl implements IJamPlayer
{

    private final EntityLivingBase player;
    /** The players extended properties for playing music and jamming. */
    private boolean isLeader, inJam, isPlaying;
    /** Generic string for passing params from server to client */
    private String sParam1, sParam2, sParam3;

    public JamDefaultImpl(EntityLivingBase entity)
    {
        this.player = entity;
        this.isLeader = this.inJam = this.isPlaying = false;
        this.sParam1 = this.sParam2 = this.sParam3 = new String("");
    }

    @Override
    public void clearAll()
    {
        this.isLeader = this.inJam = this.isPlaying = false;
        this.sParam1 = this.sParam2 = this.sParam3 = new String("");
        this.sync();
    }

    @Override
    public void setJam(boolean value)
    {
        this.inJam = value;
        this.sync();
    }

    @Override
    public boolean getJam() {return this.inJam;}

    @Override
    public void setLeader(boolean value)
    {
        this.isLeader = value;
        this.sync();
    }

    @Override
    public boolean getLeader() {return this.isLeader;}

    @Override
    public void setPlaying(boolean value)
    {
        this.isPlaying = value;
        this.sync();
    }

    @Override
    public boolean getPlaying() {return this.isPlaying;}

    @Override
    public void setSParams(String sParam1, String sParam2, String sParam3)
    {
        this.sParam1 = sParam1;
        this.sParam2 = sParam2;
        this.sParam3 = sParam3;
        this.sync();
    }

    @Override
    public String getSParam1() {return sParam1;}

    @Override
    public String getSParam2() {return sParam2;}

    @Override
    public String getSParam3() {return sParam3;}

    /**
     * The values returned are an index to icons stored in the
     * instrument_inventory.png texture.
     * 
     * @return A collection of the active properties. that is those that are
     *         "true".
     */
    @Override
    public Collection<Integer> getActiveProps()
    {
        HashMap<String, Integer> activeProps = new HashMap<String, Integer>();
        if (this.inJam) activeProps.put("inJam", 2);
        if (this.isLeader) activeProps.put("isLeader", 3);
        if (this.isPlaying) activeProps.put("isPlaying", 1);
        if (activeProps.isEmpty()) activeProps.put("inactive", 0);
        return activeProps.values();
    }

    public final void sync()
    {
        if (player != null && !player.worldObj.isRemote)
        {
            PacketDispatcher.sendTo(new SyncPlayerPropsMessage((EntityPlayer) player), (EntityPlayerMP) player);
        }
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound properties = new NBTTagCompound();
        properties.setBoolean("inJam", this.inJam);
        properties.setBoolean("isLeader", this.isLeader);
        properties.setBoolean("isPlaying", this.isPlaying);
        properties.setString("sParam1", this.sParam1);
        properties.setString("sParam2", this.sParam2);
        properties.setString("sParam3", this.sParam3);
        return properties; // compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.inJam = nbt.getBoolean("inJam");
        this.isLeader = nbt.getBoolean("isLeader");
        this.isPlaying = nbt.getBoolean("isPlaying");
        this.sParam1 = nbt.getString("sParam1");
        this.sParam2 = nbt.getString("sParam2");
        this.sParam3 = nbt.getString("sParam3");
    }
}
