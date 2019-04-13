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

package net.aeronica.mods.mxtune.world.caps.chunk;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.GUID;

public class ModChunkPlaylistImpl implements IModChunkPlaylist
{
    private GUID guid;

    public ModChunkPlaylistImpl()
    {
        guid = Reference.EMPTY_GUID;
    }

    @Override
    public GUID getPlaylistGuid()
    {
        return guid;
    }

    @Override
    public void setPlaylistGuid(GUID guid)
    {
        this.guid = guid;
    }
}
