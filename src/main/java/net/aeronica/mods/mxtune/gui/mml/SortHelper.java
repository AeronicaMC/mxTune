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

package net.aeronica.mods.mxtune.gui.mml;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import net.aeronica.mods.mxtune.managers.records.PlayList;
import net.aeronica.mods.mxtune.managers.records.Song;
import net.aeronica.mods.mxtune.managers.records.SongProxy;
import net.minecraft.util.StringUtils;

import java.util.Comparator;
import java.util.Locale;

public final class SortHelper
{
    private SortHelper() { /* NOP */ }

    public static final Ordering<PlayList> PLAYLIST_ORDERING = Ordering.from(new PlaylistComparator());
    public static final Ordering<SongProxy> SONG_PROXY_ORDERING = Ordering.from(new SongProxyComparator());
    public static final Ordering<Song> SONG_ORDERING = Ordering.from(new SongComparator());

    static class PlaylistComparator implements Comparator<PlayList>
    {
        @Override
        public int compare(PlayList o1, PlayList o2)
        {
            return ComparisonChain.start().compare(stripCase(o1.getName()), stripCase(o2.getName())).result();
        }
    }

    static class SongProxyComparator implements Comparator<SongProxy>
    {
        @Override
        public int compare(SongProxy o1, SongProxy o2)
        {
            return ComparisonChain.start().compare(stripCase(o1.getTitle()), stripCase(o2.getTitle())).result();
        }
    }

    static class SongComparator implements Comparator<Song>
    {
        @Override
        public int compare(Song o1, Song o2)
        {
            return ComparisonChain.start().compare(stripCase(o1.getTitle()), stripCase(o2.getTitle())).result();
        }
    }

    private static String stripCase(String s)
    {
        return StringUtils.stripControlCodes(s).toLowerCase(Locale.ROOT);
    }
}
