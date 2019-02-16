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

package net.aeronica.mods.mxtune.caches;

import java.util.List;

public class MXTuneFile implements Comparable<MXTuneFile>
{
    private static final  String TAG_TITLE = "title";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_SOURCE = "source";
    private static final String TAG_PARTS = "parts";

    private String title = "";
    private String author;
    private String source;
    private List<MXTunePart> parts;

    public MXTuneFile() { /* NOP */ }



    private String getSortingKey()
    {
        return title.trim() + author.trim() + source.trim();
    }

    @Override
    public int compareTo(MXTuneFile o)
    {
        return getSortingKey().compareTo(o.getSortingKey());
    }
}
