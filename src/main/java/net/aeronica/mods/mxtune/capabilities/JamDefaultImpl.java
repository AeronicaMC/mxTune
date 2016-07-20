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

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.SyncPlayerPropsMessage;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;

public class JamDefaultImpl implements IJamPlayer
{

    private final EntityLivingBase player;
    /** Music Options*/
    private int muteOption;
    private float midiVolume;
    /** Generic string for passing params from server to client */
    private String sParam1, sParam2, sParam3;

    public JamDefaultImpl(EntityLivingBase entity)
    {
        this.player = entity;
        this.midiVolume = 0.70F;
        this.muteOption = 0;
        this.sParam1 = this.sParam2 = this.sParam3 = new String("");
    }

    @Override
    public void clearAll()
    {
        this.midiVolume = 0.70F;
        this.muteOption = 0;
        this.sParam1 = this.sParam2 = this.sParam3 = new String("");
        this.syncAll();
    }

    @Override
    public void setSParams(String sParam1, String sParam2, String sParam3)
    {
        this.sParam1 = sParam1;
        this.sParam2 = sParam2;
        this.sParam3 = sParam3;
        this.sync(SYNC_SPARAMS);
    }

    @Override
    public String getSParam1() {return sParam1;}

    @Override
    public String getSParam2() {return sParam2;}

    @Override
    public String getSParam3() {return sParam3;}

    @Override
    public void setMidiVolume(float volumeIn) {this.midiVolume = Math.min(1.0F, Math.max(0.0F, volumeIn)); sync(SYNC_MIDI_VOLUME);}

    @Override
    public float getMidiVolume() {return this.midiVolume;}

    @Override
    public void setMuteOption(int muteOptionIn) {this.muteOption = muteOptionIn; sync(SYNC_MUTE_OPTION);}

    @Override
    public int getMuteOption() {return muteOption;}
    
    public static final byte SYNC_ALL = 0;
    public static final byte SYNC_MIDI_VOLUME = 1;
    public static final byte SYNC_MUTE_OPTION = 2;
    public static final byte SYNC_SPARAMS = 3;
    
    public final void syncAll() {sync(SYNC_ALL);}
    
    public final void sync(byte propertyID)
    {
        if (player != null && !player.worldObj.isRemote)
        {
            PacketDispatcher.sendTo(new SyncPlayerPropsMessage((EntityPlayer) player, propertyID), (EntityPlayerMP) player);
        }
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound properties = new NBTTagCompound();
        properties.setFloat("midiVolume", this.midiVolume);
        properties.setInteger("muteOption", this.muteOption);
        properties.setString("sParam1", this.sParam1);
        properties.setString("sParam2", this.sParam2);
        properties.setString("sParam3", this.sParam3);
        return properties; // compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.midiVolume = nbt.getFloat("midiVolume");
        this.muteOption = nbt.getInteger("muteOption");
        this.sParam1 = nbt.getString("sParam1");
        this.sParam2 = nbt.getString("sParam2");
        this.sParam3 = nbt.getString("sParam3");
    }
    
    public static enum EnumMuteOptions implements IStringSerializable
    {
        OFF(0, "mxtune.gui.musicOptions.muteOption.off"),
        OTHERS(1, "mxtune.gui.musicOptions.muteOption.others"),
        BLACKLIST(2, "mxtune.gui.musicOptions.muteOption.blacklist"),
        WHITELIST(3, "mxtune.gui.musicOptions.muteOption.whitelist"),
        ALL(4, "mxtune.gui.musicOptions.muteOption.all");

        private final int meta;
        private final String translateKey;
        private static final EnumMuteOptions[] META_LOOKUP = new EnumMuteOptions[values().length];

        private EnumMuteOptions(int meta, String translateKey)
        {
            this.meta = meta;
            this.translateKey = translateKey;
        }
        
        public int getMetadata() {return this.meta;}
        
        static
        {
            for (EnumMuteOptions value : values())
            {
                META_LOOKUP[value.getMetadata()] = value;
            }
        }

        public static EnumMuteOptions byMetadata(int meta)
        {
            if (meta < 0 || meta >= META_LOOKUP.length)
            {
                meta = 0;
            }
            return META_LOOKUP[meta];
        }
        
        @Override
        public String toString(){return I18n.format(this.translateKey);}  

        @Override
        public String getName() {return this.translateKey;}        
    }
}
