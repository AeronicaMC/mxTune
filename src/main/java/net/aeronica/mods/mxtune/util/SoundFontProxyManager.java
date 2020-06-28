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

package net.aeronica.mods.mxtune.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;

@SuppressWarnings("unused")
public class SoundFontProxyManager
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String RESOURCE = "/assets/mxtune/synth/soundfont_proxy.json";
    public static ImmutableMap<Integer, SoundFontProxy> soundFontProxyMapByIndex;
    public static ImmutableMap<String, SoundFontProxy> soundFontProxyMapById;

    static
    {
        try
        {
            LOGGER.debug("Loading {}", RESOURCE);
            Reader reader = new InputStreamReader(SoundFontProxy.class.getResourceAsStream(RESOURCE));
            JsonParser parser = new JsonParser();
            JsonArray elements = parser.parse(reader).getAsJsonArray();
            ImmutableMap.Builder<Integer, SoundFontProxy> builderByIndex = ImmutableMap.builder();
            ImmutableMap.Builder<String, SoundFontProxy> builderById = ImmutableMap.builder();
            Gson gson = new Gson();

            for (JsonElement element : elements)
            {
                SoundFontProxy soundFontProxy = gson.fromJson(element, new TypeToken<SoundFontProxy>() {}.getType());
                builderByIndex.put(soundFontProxy.index, soundFontProxy);
                builderById.put(soundFontProxy.id, soundFontProxy);
            }

            soundFontProxyMapByIndex = builderByIndex.build();
            soundFontProxyMapById = builderById.build();

            if (0 == soundFontProxyMapByIndex.size())
            {
                throw new MXTuneException("Failure to load soundfont_proxy json!");
            }
        } catch (Exception e)
        {
            throw new MXTuneRuntimeException(e);
        }
        LOGGER.debug("Loaded {} records from {}", soundFontProxyMapByIndex.size(), RESOURCE);
    }

    public SoundFontProxyManager INSTANCE = new SoundFontProxyManager();

    @Nullable
    public static SoundFontProxy getProxy(int index)
    {
        // default 49: piano
        return soundFontProxyMapByIndex.getOrDefault(index, soundFontProxyMapByIndex.get(49));
    }

    public static SoundFontProxy getProxy(String id)
    {
        // default 49: piano
        return soundFontProxyMapById.getOrDefault(id.toLowerCase(Locale.ROOT).trim(), soundFontProxyMapById.get("piano_acoustic"));
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
        return 52; // default piano_mabinogi
    }
}
