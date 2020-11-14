/*
 * Aeronica's mxTune MOD
 * Copyright 2020, Paul Boese a.k.a. Aeronica
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

package net.aeronica.libs.mml.readers.mml3mle;

import com.google.common.collect.ImmutableMap;

public class Map3MLEInstruments
{
    private static final ImmutableMap<Integer, String> program2ID;

    private Map3MLEInstruments() { /* NOP */ }

    static
    {
        ImmutableMap.Builder<Integer, String> builderByIndex = ImmutableMap.builder();

        // Yes this is ugly. I should read a json in.
        builderByIndex.put(0, "lute_mabinogi");
        builderByIndex.put(1, "ukulele_mabinogi");
        builderByIndex.put(2, "mandolin_mabinogi");
        builderByIndex.put(3, "whistle_mabinogi");
        builderByIndex.put(4, "roncadora_mabinogi");
        builderByIndex.put(5, "flute_mabinogi");
        builderByIndex.put(6, "chalumeau_mabinogi"); // Chalumeau
        builderByIndex.put(7, "ocarina"); // C-Bottle
        builderByIndex.put(8, "ocarina"); // D-Bottle
        builderByIndex.put(9, "ocarina"); // E-Bottle
        builderByIndex.put(10, "ocarina"); // F-Bottle
        builderByIndex.put(11, "ocarina"); // G-Bottle
        builderByIndex.put(13, "ocarina"); // A-Bottle
        builderByIndex.put(12, "ocarina"); // B-Bottle
        builderByIndex.put(14, "flute_pan"); // GreenFinch
        builderByIndex.put(15, "flute_pan"); // FairyPitta
        builderByIndex.put(16, "flute_pan"); // KingFisher
        builderByIndex.put(17, "flute_pan"); // GreenFinch Short
        builderByIndex.put(18, "tuba_mabinogi"); // Physis Tuba
        builderByIndex.put(19, "lyre_mabinogi"); // lyre
        builderByIndex.put(20, "guitar_electric_mabinogi"); //Electric Guitar
        builderByIndex.put(21, "piano_mabinogi"); // Piano
        builderByIndex.put(22, "violin_mabinogi"); // Violin
        builderByIndex.put(23, "cello_mabinogi"); // Cello
        builderByIndex.put(24, "harp_mabinogi"); // Harp
        // Percussion
        builderByIndex.put(66, "drum_bass_mabinogi"); // Bass Drum
        builderByIndex.put(67, "drum_snare_mabinogi"); // Snare Drum
        builderByIndex.put(68, "cymbals_mabinogi"); // Cymbals
        builderByIndex.put(69, "celesta"); // C-Handbell
        builderByIndex.put(70, "celesta"); // D-Handbell
        builderByIndex.put(71, "celesta"); // E-Handbell
        builderByIndex.put(72, "celesta"); // F-Handbell
        builderByIndex.put(73, "celesta"); // G-Handbell
        builderByIndex.put(74, "celesta"); // A-Handbell
        builderByIndex.put(75, "celesta"); // B-Handbell
        builderByIndex.put(76, "celesta"); // C-Handbell
        builderByIndex.put(77, "hand_chimes_mabinogi"); //Hand Chimes
        // Song Male
        builderByIndex.put(80, "concert_choir"); // Male Sone
        builderByIndex.put(81, "concert_choir"); // Song
        builderByIndex.put(82, "concert_choir"); // Song
        builderByIndex.put(83, "concert_choir"); // Song
        builderByIndex.put(84, "concert_choir"); // Song
        // Song Female
        builderByIndex.put(90, "concert_choir"); // Female Song
        builderByIndex.put(91, "concert_choir"); // Song
        builderByIndex.put(92, "concert_choir"); // Song
        builderByIndex.put(93, "concert_choir"); // Song
        builderByIndex.put(94, "concert_choir"); // Song
        // Song Chorus
        builderByIndex.put(100, "concert_choir"); // Male Chorus
        builderByIndex.put(101, "concert_choir"); // Song
        builderByIndex.put(102, "concert_choir"); // Song
        builderByIndex.put(110, "concert_choir"); // Female Chorus
        builderByIndex.put(111, "concert_choir"); // Song
        builderByIndex.put(112, "concert_choir"); // Song
        //Voice
        builderByIndex.put(120, "concert_choir"); // Male Voice
        builderByIndex.put(121, "concert_choir"); // Female Voice
        // 10th
        builderByIndex.put(30, "lute_mabinogi"); // 10th Lute
        builderByIndex.put(31, "ukulele_mabinogi"); // 10th Ukulele
        builderByIndex.put(32, "mandolin_mabinogi"); // 10th Mandolin
        builderByIndex.put(33, "whistle_tuned_mabinogi"); // 10th Whistle
        builderByIndex.put(34, "flute_tuned_mabinogi"); // 10th Flute
        // Renew
        builderByIndex.put(50, "lute_mabinogi"); // Lute
        builderByIndex.put(51, "ukulele_mabinogi"); // Ukulele
        builderByIndex.put(52, "mandolin_mabinogi"); // Mandolin
        builderByIndex.put(53, "whistle_tuned_mabinogi"); // Tuned Whistle
        builderByIndex.put(54, "flute_tuned_mabinogi"); // Tuned Flute
        // Tuned
        builderByIndex.put(25, "violin"); //Tuned Violin
        builderByIndex.put(26, "cello"); //Tuned Cello
        builderByIndex.put(55, "flute_tuned_mabinogi"); //Tuned Flute
        builderByIndex.put(56, "whistle_tuned_mabinogi"); //Tuned Whistle
        // Drum Kit
        builderByIndex.put(27, "drum_set_mabinogi"); // Drum Kit

        program2ID = builderByIndex.build();
    }

    public static String getSoundFontProxyName(int program)
    {
        return program2ID.getOrDefault(program, "lute_mabinogi");
    }
}
