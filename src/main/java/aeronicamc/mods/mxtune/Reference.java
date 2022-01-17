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
package aeronicamc.mods.mxtune;

public class Reference
{
    private Reference() {/* NOP */}

    public static final String MOD_ID = "mxtune";
    public static final String MOD_NAME = "mxTune";
    public static final String VERSION = "{@VERSION}";
    static final String CERTIFICATE_FINGERPRINT = "999640c365a8443393a1a21df2c0ede9488400e9";

    // Mabinogi:        1200 max per line? https://wiki.mabinogiworld.com/view/Compose
    // Maple Story 2:   10000 characters per score or 1000 per line? https://docs.google.com/document/d/1HGTZtftOn6eMfMTlOBC9cxcmO5C8ZDd4xdd8vLDxnSM/edit
    // Archeage:        2000 characters total, https://archeage.fandom.com/wiki/Artistry_(Proficiency)
    // TODO: Show limits per game in terms of total chars and by game limits.
    // TODO: number for a given line type: Mabinogi:    melody, harmony 1, harmony 2, song.
    // TODO: number for a given line type: MS2 melody:  chord 1 thru chord 9, or 10 lines total (Max chars for all line 10,000 chars).
    // TODO: number for a given line type: ArcheAge:    200 to 2000 characters depending on proficiency.
    public static final int MAX_MML_PART_LENGTH = 25000;
    public static final int MXT_SONG_TITLE_LENGTH = 80;
    public static final int MXT_SONG_AUTHOR_LENGTH = 80;
    public static final int MXT_SONG_SOURCE_LENGTH = 320;
    public static final int MXT_TAG_NAME_LENGTH = 25;
    public static final int MXT_TAG_DISPLAY_NAME_LENGTH = 50;
}