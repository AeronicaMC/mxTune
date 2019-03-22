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
    // TODO: Refactor to use NBTHelper methods and make a data fixer to update exiting worlds.
    public static final OwnerUUID EMPTY_UUID = new OwnerUUID(0L, 0L);
    private static final String OWNER_UUID_KEY_MSB = "OwnerUUIDKeyMSB";
    private static final String OWNER_UUID_KEY_LSB = "OwnerUUIDKeyLSB";
    private final UUID uuid;

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

    public void toNBT(NBTTagCompound nbt)
    {
        nbt.setLong(OWNER_UUID_KEY_MSB, uuid.getMostSignificantBits());
        nbt.setLong(OWNER_UUID_KEY_LSB, uuid.getLeastSignificantBits());
    }

    public static OwnerUUID fromNBT(NBTTagCompound nbt)
    {
        if (nbt.hasKey(OWNER_UUID_KEY_MSB, Constants.NBT.TAG_LONG) && nbt.hasKey(OWNER_UUID_KEY_LSB, Constants.NBT.TAG_LONG))
        {
            long msb = nbt.getLong(OWNER_UUID_KEY_MSB);
            long lsb = nbt.getLong(OWNER_UUID_KEY_LSB);
            return new OwnerUUID(msb, lsb);
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
        long highLow = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
        return ((int) (highLow >> 32)) ^ (int) highLow;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((null == obj) || (obj.getClass() != OwnerUUID.class))
            return false;
        UUID id = ((OwnerUUID) obj).getUUID();
        return (uuid.getMostSignificantBits() == id.getMostSignificantBits() &&
                        uuid.getLeastSignificantBits() == id.getLeastSignificantBits());
    }
}
