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
package net.aeronica.mods.mxtune.world;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.concurrent.Immutable;

@Immutable
public class OwnerUUID
{
    public static final OwnerUUID EMPTY_UUID = new OwnerUUID("");
    private static final String UUID_KEY = "OwnerUUID";
    private final String uuid;

    public OwnerUUID(String uuid) { this.uuid = uuid; }

    public boolean isEmpty() { return this.uuid == null || this.uuid.isEmpty(); }

    public String getUUID() { return this.uuid; }

    public void toNBT(NBTTagCompound nbt) { nbt.setString(UUID_KEY, this.uuid); }

    public static OwnerUUID fromNBT(NBTTagCompound nbt)
    {
        if (nbt.hasKey(UUID_KEY, 8))
        {
            String s = nbt.getString(UUID_KEY);
            return new OwnerUUID(s);
        }
        else
        {
            return EMPTY_UUID;
        }
    }
}
