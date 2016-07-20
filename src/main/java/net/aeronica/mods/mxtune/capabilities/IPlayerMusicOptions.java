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

import net.minecraft.entity.player.EntityPlayer;

public interface IPlayerMusicOptions
{   
    public void clearAll(EntityPlayer playerIn);

    /** range 0.0 - 1.0 */
    public void setMidiVolume(EntityPlayer playerIn, float volumeIn);

    public void setMidiVolume(float volumeIn);

    /** range 0.0 - 1.0 */
    public float getMidiVolume();
    
    public int getMuteOption();
    
    public void setMuteOption(EntityPlayer playerIn, int muteOptionIn);

    public void setMuteOption(int muteOptionIn);

    public void setSParams(EntityPlayer playerIn, String sParam1, String sParam2, String sParam3);
    
    public void setSParams(String sParam1, String sParam2, String sParam3);

    public String getSParam1();

    public String getSParam2();

    public String getSParam3();
    
    public void syncAll(EntityPlayer playerIn);
    
    public void sync(EntityPlayer playerIn, byte propertyID);

   // public NBTTagCompound serializeNBT();

    //public void deserializeNBT(NBTTagCompound nbt);
}
