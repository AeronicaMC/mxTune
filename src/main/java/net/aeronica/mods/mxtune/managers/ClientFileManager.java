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

package net.aeronica.mods.mxtune.managers;

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.ModLogger;

import java.util.UUID;

public class ClientFileManager
{
    private static UUID cachedServerID;

    // TODO: Client side server cache folders must be per server and player (integrated AND dedicated)
    // This must be done to avoid potential naming conflicts.
    private ClientFileManager() { /* NOP*/ }

    public static void setCachedServerID(long msb, long lsb)
    {
        cachedServerID = new UUID(msb, lsb);
        ModLogger.debug("Cached Server ID received: %s", cachedServerID.toString());
    }

    private static UUID getCachedServerID()
    {
        return cachedServerID;
    }

    // Uses Playlist for now. Area will be used comes once I get all the basic mechanics working
    // This is called every tick so after at least one to two ticks a song should be available
    // If no local cache files exist then the area/playlist will get DL and cached. On subsequent ticks
    // the song itself will be chosen and made available
    static boolean songAvailable(UUID uuidPlayList)
    {
        // META: What to do?

        // Check local cache for area/playlist
        // if area/playlist not exists
        //     DL area/playlist and cache to collection and disk (async threaded flush)
        // else
        //     choose song based on <Area> settings and <playlist>
        //     if song not cached
        //         DL and cache
        //     else
        //         get song from cache
        // queue song for next play and set songAvailable to true.
        return !Reference.EMPTY_UUID.equals(uuidPlayList);
    }
}
