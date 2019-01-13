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
package net.aeronica.mods.mxtune.datafixers;

import com.google.common.collect.Maps;
import net.aeronica.mods.mxtune.Reference;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

import javax.annotation.Nullable;
import java.util.Map;

public class TileIdFixer implements IFixableData
{
    private static final Map<String, String> OLD_TO_NEW_ID_MAP = Maps.newHashMap();
    static
    {
        OLD_TO_NEW_ID_MAP.put("minecraft:tile_piano", "mxtune:tile_piano");
    }
    @Override
    public int getFixVersion()
    {
        return Reference.MXTUNE_DATA_FIXER_VERSION;
    }

    @Nullable
    @Override
    public NBTTagCompound fixTagCompound(@SuppressWarnings("NullableProblems") NBTTagCompound compound)
    {
        String s = OLD_TO_NEW_ID_MAP.get(compound.getString("id"));
        if (s != null)
        {
            compound.setString("id", s);
        }
        return compound;
    }
}
