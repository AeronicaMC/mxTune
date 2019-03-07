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

package net.aeronica.mods.mxtune.managers.records;

import net.aeronica.mods.mxtune.caches.UUIDType5;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class Area extends BaseData
{
    private static final  String TAG_NAME = "name";
    private static final String TAG_PLAY_LIST = "play_list";

    private String name;
    private UUID playList;

    public Area(String name)
    {
        this.name = name;
        playList = UUIDType5.nameUUIDFromNamespaceAndString(UUIDType5.NAMESPACE_LIST, applyServerID(""));
        uuid = UUIDType5.nameUUIDFromNamespaceAndString(UUIDType5.NAMESPACE_AREA, applyServerID(this.name));
    }

    public Area(String name, UUID playList)
    {
        this.name = name;
        this.playList = playList;
        uuid = UUIDType5.nameUUIDFromNamespaceAndString(UUIDType5.NAMESPACE_AREA, applyServerID(this.name));
    }

    public static Area build(NBTTagCompound compound)
    {
        String name = compound.getString(TAG_NAME);
        NBTTagCompound compoundPlayList = compound.getCompoundTag(TAG_PLAY_LIST);
        UUID playList = getUuidFromCompound(compoundPlayList);
        return new Area(name, playList);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.name = compound.getString(TAG_NAME);
        NBTTagCompound compoundPlayList = compound.getCompoundTag(TAG_PLAY_LIST);
        playList = getUuidFromCompound(compoundPlayList);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString(TAG_NAME, name);
        NBTTagCompound compoundPlayList = new NBTTagCompound();
        setUuidToCompound(compoundPlayList, playList);
        compound.setTag(TAG_PLAY_LIST, compoundPlayList);
    }

    public String getName()
    {
        return name;
    }

    public UUID getPlayList()
    {
        return playList;
    }

    public void setPlayList(UUID playList)
    {
        this.playList = playList;
    }
}
