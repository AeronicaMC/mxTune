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

import net.minecraft.nbt.NBTTagCompound;

public interface IJamPlayer
{

    public void clearAll();

    public void setJam(boolean value);

    public boolean getJam();

    public void setLeader(boolean value);

    public boolean getLeader();

    public void setPlaying(boolean value);

    public boolean getPlaying();

    public void setSParams(String sParam1, String sParam2, String sParam3);

    public String getSParam1();

    public String getSParam2();

    public String getSParam3();

    /**
     * The values returned are an index to icons stored in the
     * instrument_inventory.png texture.
     * 
     * @return A collection of the active properties. that is those that are
     *         "true".
     */
    public Collection<Integer> getActiveProps();

    public NBTTagCompound serializeNBT();

    public void deserializeNBT(NBTTagCompound nbt);
}
