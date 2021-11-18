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

package aeronicamc.mods.mxtune.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@SuppressWarnings("unused")
public class SoundFontProxyManager
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String RESOURCE = "/assets/mxtune/synth/soundfont_proxy.json";
    public static ImmutableMap<Integer, SoundFontProxy> soundFontProxyMapByIndex;
    public static ImmutableMap<String, SoundFontProxy> soundFontProxyMapById;
    public static final String INSTRUMENT_DEFAULT_ID = "piano";

    public static SoundFontProxy getSoundFontProxyDefault()
    {
        return SOUND_FONT_PROXY_DEFAULT;
    }

    private static final SoundFontProxy SOUND_FONT_PROXY_DEFAULT;

    static
    {
        // default 50: piano_acoustic
        SOUND_FONT_PROXY_DEFAULT = new SoundFontProxy();
        SOUND_FONT_PROXY_DEFAULT.index = 50;
        SOUND_FONT_PROXY_DEFAULT.packed_preset = 0;
        SOUND_FONT_PROXY_DEFAULT.id = INSTRUMENT_DEFAULT_ID;
        SOUND_FONT_PROXY_DEFAULT.general_midi = true;
        SOUND_FONT_PROXY_DEFAULT.maple_story_2 = true;

        ImmutableMap.Builder<Integer, SoundFontProxy> builderByIndex = ImmutableMap.builder();
        ImmutableMap.Builder<String, SoundFontProxy> builderById = ImmutableMap.builder();

        try
        {
            LOGGER.debug("Loading {}", RESOURCE);
            InputStreamReader reader = new InputStreamReader(SoundFontProxyManager.class.getResourceAsStream(RESOURCE), StandardCharsets.UTF_8);
            JsonParser parser = new JsonParser();
            JsonArray elements = parser.parse(reader).getAsJsonArray();
            reader.close();

            Gson gson = new Gson();

            for (JsonElement element : elements)
            {
                SoundFontProxy soundFontProxy = gson.fromJson(element, new TypeToken<SoundFontProxy>() {}.getType());
                builderByIndex.put(soundFontProxy.index, soundFontProxy);
                builderById.put(soundFontProxy.id, soundFontProxy);
            }

            soundFontProxyMapByIndex = builderByIndex.build();
            soundFontProxyMapById = builderById.build();

            if (soundFontProxyMapByIndex.isEmpty())
            {
                throw new MXTuneException("Failure to load soundfont_proxy json!");
            }
        } catch (Exception e)
        {
            LOGGER.error(e.getMessage(), e);
            throw new MXTuneRuntimeException(e);
        }
        LOGGER.debug("Loaded {} records from {}", soundFontProxyMapByIndex.size(), RESOURCE);
    }

    public SoundFontProxyManager INSTANCE = new SoundFontProxyManager();

    public static SoundFontProxy getProxy(int index)
    {
        // default 49: piano
        return soundFontProxyMapByIndex.getOrDefault(index, SOUND_FONT_PROXY_DEFAULT);
    }

    public static SoundFontProxy getProxy(String id)
    {
        // default 49: piano
        return soundFontProxyMapById.getOrDefault(id.toLowerCase(Locale.ROOT).trim(), SOUND_FONT_PROXY_DEFAULT);
    }

    public static int getPackedPreset(int index)
    {
        return getProxy(index).packed_preset;
    }

    public static String getName(int index)
    {
        return getProxy(index).id;
    }

    public static boolean hasTransform(int index)
    {
        return !getProxy(index).transform.isEmpty();
    }

    public static String getTransform(int index)
    {
        return getProxy(index).transform;
    }

    public static int getPackedPreset(String id)
    {
        return getProxy(id).packed_preset;
    }

    public static String getName(String id)
    {
        return getProxy(id).id;
    }

    public static String getLangKeyName(int index)
    {
        return String.format("item.mxtune.%s", getProxy(index).id);
    }

    public static String getLangKeyName(String id)
    {
        return String.format("item.mxtune.%s", getProxy(id).id);
    }

    public static boolean hasTransform(String id)
    {
        return !getProxy(id).transform.isEmpty();
    }

    public static String getTransform(String id)
    {
        return getProxy(id).transform;
    }

    public static int getIndexById(String id)
    {

        return getProxy(id).index;
    }

    public static boolean hasPackedPreset(int packedPreset)
    {
        boolean result = false;
        for (SoundFontProxy sp : soundFontProxyMapByIndex.values())
        {
            result = sp.packed_preset == packedPreset;
            if (result) break;
        }
        return result;
    }

    public static int getIndexForFirstMatchingPackedPreset(int packedPreset)
    {
        for (SoundFontProxy sp : soundFontProxyMapByIndex.values())
        {
            if (sp.packed_preset == packedPreset)
                return sp.index;
        }
        return 53; // default piano_mabinogi
    }
}
