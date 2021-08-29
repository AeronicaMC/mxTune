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

package aeronicamc.libs.mml.readers.ms2mml;

import com.google.common.collect.ImmutableMap;

import java.util.Locale;

public class MapMS2Instruments
{
    private static final ImmutableMap<String, String> ms2name2ID;
    private MapMS2Instruments() {/* NOP */}

    static
    {
        ImmutableMap.Builder<String, String> builderByIndex = ImmutableMap.builder();

        // Yes this is ugly. I should read a json in.
        builderByIndex.put("acousticbass","bass_acoustic");
        builderByIndex.put("acousticbassguitar","bass_acoustic");
        builderByIndex.put("acousticguitar","guitar_nylon");
        builderByIndex.put("bassdrum","drum_bass_mabinogi");
        builderByIndex.put("bassguitar","bass_fingered");
        builderByIndex.put("celesta","celesta");
        builderByIndex.put("cello","cello");
        builderByIndex.put("clarinet","clarinet");
        builderByIndex.put("cymbals","cymbals_mabinogi");
        builderByIndex.put("eguitar","guitar_overdrive");
        builderByIndex.put("electricguitar","guitar_overdrive");
        builderByIndex.put("electricpiano","piano_electric");
        builderByIndex.put("epiano","piano_electric");
        builderByIndex.put("guitar","guitar_nylon");
        builderByIndex.put("harmonica","harmonica");
        builderByIndex.put("harp","harp");
        builderByIndex.put("harpsichord","harpsichord");
        builderByIndex.put("oboe","oboe");
        builderByIndex.put("ocarina","ocarina");
        builderByIndex.put("panflute","flute_pan");
        builderByIndex.put("piano","piano");
        builderByIndex.put("pickbassguitar","bass_pick");
        builderByIndex.put("pizzicativiolin","pizzicato_string");
        builderByIndex.put("pizzicatoviolin","pizzicato_string");
        builderByIndex.put("pizziviolin","pizzicato_string");
        builderByIndex.put("recorder","recorder");
        builderByIndex.put("saxophone","sax_alto");
        builderByIndex.put("snaredrum","drum_snare_mabinogi");
        builderByIndex.put("steeldrum","drums_steel");
        builderByIndex.put("timpani","timpani");
        builderByIndex.put("tomtom","drums_melodic_tom");
        builderByIndex.put("trombone","trombone");
        builderByIndex.put("trumpet","trumpet");
        builderByIndex.put("vibraphone","vibraphone");
        builderByIndex.put("violin","violin");
        builderByIndex.put("xylophone","xylophone");

        ms2name2ID = builderByIndex.build();
    }

    public static String getSoundFontProxyNameFromMeta(String metaName)
    {
        return ms2name2ID.getOrDefault(cleanupMeta(metaName), "piano");
    }

    private static String cleanupMeta(String metaName)
    {
        String working = metaName.toLowerCase(Locale.ENGLISH);
        working = filterAllowedMetaCharacters(working);
        return working.replaceAll("(([A-Za-z\\-\\d]*).-)|([\\d].)|([.].+)", "");
    }

    private static final String META_CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789.-";

    public static boolean isAllowedMetaCharacter(char character)
    {
        char[] ca = META_CHARACTERS.toCharArray();
        for (char c : ca)
        {
            if (character == c) return true;
        }
        return false;
    }

    /** Filter string by only keeping those characters for which isAllowedCharacter() returns true. */
    public static String filterAllowedMetaCharacters(String input)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (char c0 : input.toCharArray())
        {
            if (isAllowedMetaCharacter(c0))
            {
                stringbuilder.append(c0);
            }
        }
        return stringbuilder.toString();
    }
}
