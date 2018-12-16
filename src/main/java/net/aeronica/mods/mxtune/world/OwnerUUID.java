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
import net.minecraftforge.common.util.Constants;

import javax.annotation.concurrent.Immutable;
import java.util.UUID;

@Immutable
public class OwnerUUID
{
    public static final OwnerUUID EMPTY_UUID = new OwnerUUID(0L, 0L);
    private static final String UUID_KEY = "OwnerUUID";
    private final UUID uuid;

    private OwnerUUID(String uuidString)
    {
        this.uuid = UUID.fromString(uuidString);
    }

    public OwnerUUID(UUID uuid)
    {
        this.uuid = uuid;
    }

    private OwnerUUID(long msb, long lsb)
    {
        this.uuid = new UUID(msb, lsb);
    }

    public boolean isEmpty() { return this.uuid == null || EMPTY_UUID.uuid.equals(uuid); }

    public UUID getUUID() { return this.uuid; }

    public void toNBT(NBTTagCompound nbt) { nbt.setString(UUID_KEY, uuid.toString()); }

    public static OwnerUUID fromNBT(NBTTagCompound nbt)
    {
        if (nbt.hasKey(UUID_KEY, Constants.NBT.TAG_STRING))
        {
            String s = nbt.getString(UUID_KEY);
            return new OwnerUUID(s);
        }
        else
        {
            return EMPTY_UUID;
        }
    }

    @Override
    public String toString()
    {
        return uuid.toString();
    }

    @Override
    public int hashCode()
    {
        long hilo = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
        return ((int)(hilo >> 32)) ^ (int) hilo;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((null == obj) || (obj.getClass() != OwnerUUID.class))
            return false;
        UUID id = ((OwnerUUID)obj).getUUID();
        return (uuid.getMostSignificantBits() == id.getMostSignificantBits() &&
                        uuid.getLeastSignificantBits() == id.getLeastSignificantBits());
    }
}
