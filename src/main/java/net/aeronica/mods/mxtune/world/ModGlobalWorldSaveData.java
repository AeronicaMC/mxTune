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

package net.aeronica.mods.mxtune.world;

import net.aeronica.mods.mxtune.Reference;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class ModGlobalWorldSaveData extends WorldSavedData
{
    private static final String MOD_WORLD_GLOBAL_DATA = Reference.MOD_ID + "_data";
    private static final String KEY_NAME = "name";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_IS_SOMETHING = "isSomething";

    private String name;
    private int number;
    private boolean isSomething;

    private ModGlobalWorldSaveData()
    {
        super(MOD_WORLD_GLOBAL_DATA);
        name = "";
        number = 0;
        isSomething = false;
    }

    public ModGlobalWorldSaveData(String s)
    {
        super(s);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
        markDirty();
    }

    public int getNumber()
    {
        return number;
    }

    public void setNumber(int number)
    {
        this.number = number;
        markDirty();
    }

    public boolean isSomething()
    {
        return isSomething;
    }

    public void setSomething(boolean something)
    {
        isSomething = something;
       markDirty();
    }

    @Override
    public void readFromNBT(CompoundNBT nbt)
    {
            name = nbt.getString(KEY_NAME);
            number = nbt.getInteger(KEY_NUMBER);
            isSomething = nbt.getBoolean(KEY_IS_SOMETHING);
    }

    // Remember to use markDirty()
    @Override
    public CompoundNBT writeToNBT(CompoundNBT compound)
    {
        compound.setString(KEY_NAME, name);
        compound.setInteger(KEY_NUMBER, number);
        compound.setBoolean(KEY_IS_SOMETHING, isSomething);
        return compound;
    }

    public static ModGlobalWorldSaveData get(World world)
    {
        MapStorage storage = world.getMapStorage();
        if (storage == null) return null;

        ModGlobalWorldSaveData instance = (ModGlobalWorldSaveData) storage.getOrLoadData(ModGlobalWorldSaveData.class, MOD_WORLD_GLOBAL_DATA);

        if (instance == null)
        {
            instance = new ModGlobalWorldSaveData();
            storage.setData(MOD_WORLD_GLOBAL_DATA, instance);
        }
        return instance;
    }
}
