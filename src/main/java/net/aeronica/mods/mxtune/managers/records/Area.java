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

import net.minecraft.nbt.NBTTagCompound;

public class Area
{
    private static final  String TAG_NAME = "name";
    private static final String TAG_PLAY_LIST = "play_list";

    private String name = "";
    private PlayList songs;

    public Area() { }

    public static Area build(NBTTagCompound compound)
    {
        String title = compound.getString(TAG_NAME);
        Area mxTuneFile = new Area();
        mxTuneFile.name = title;
        return mxTuneFile;
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString(TAG_NAME, name);
    }
}
