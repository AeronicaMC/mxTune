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

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.*;

public class MapListHelper
{
    /* Serialization and deserialization methods */
    @SuppressWarnings("UnstableApiUsage")
    public static Map<Integer, Integer> deserializeIntIntMap(String mapIntString)
    {
        try
        {
            Map<String, String> inStringString = Splitter.on('|').omitEmptyStrings().withKeyValueSeparator("=").split(mapIntString);
            Map<Integer, Integer> outIntInt = new HashMap<>();
            for (Map.Entry<String,String> entry: inStringString.entrySet())
            {
                outIntInt.put(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
            }
            return outIntInt;
        } catch (IllegalArgumentException e)
        {
            ModLogger.error(e);
            return Collections.emptyMap();
        }
    }

    public static String serializeIntIntMap(HashMap<Integer, Integer> mapIntInt)
    {
        StringBuilder serializedIntIntMap = new StringBuilder();
        try
        {
            Set<Integer> keys = mapIntInt.keySet();
            for (Integer integer : keys)
            {
                serializedIntIntMap.append(integer).append("=").append(mapIntInt.get(integer)).append("|");
            }
        } catch (Exception e)
        {
            ModLogger.error(e);
        }
        return serializedIntIntMap.toString();
    }

    /**
     * This was created specifically to make the groupsMembers ListMultimap
     * without duplicating network traffic to send a complementary structure.
     * @param hashTableString string to deserialize
     * @return a ListMultimap where the keys and values have been swapped.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static ListMultimap<Integer, Integer> deserializeIntIntListMultimapSwapped(String hashTableString)
    {
        try
        {
            Map<String, String> inStringString = Splitter.on('|').omitEmptyStrings().withKeyValueSeparator("=").split(hashTableString);
            ListMultimap<Integer, Integer> outListMultimapIntInt = ArrayListMultimap.create();
            for (Map.Entry<String,String> entry: inStringString.entrySet())
            {
                outListMultimapIntInt.put(Integer.valueOf(entry.getValue()), Integer.valueOf(entry.getKey()));
            }
            return outListMultimapIntInt;
        } catch (IllegalArgumentException e)
        {
            ModLogger.error(e);
            return ArrayListMultimap.create();
        }
    }

    @SuppressWarnings({"unused", "UnstableApiUsage"})
    public static Map<Integer, String> deserializeIntStrMap(String mapIntString)
    {
        try
        {
            Map<String, String> inStringString = Splitter.on('|').omitEmptyStrings().withKeyValueSeparator("=").split(mapIntString);
            Map<Integer, String> outIntString = new HashMap<>();
            for (Map.Entry<String,String> entry: inStringString.entrySet())
            {
                outIntString.put(Integer.valueOf(entry.getKey()), entry.getValue());
            }
            return outIntString;
        } catch (IllegalArgumentException e)
        {
            ModLogger.error(e);
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unused")
    public static String serializeIntStrMap(HashMap<Integer, String> mapIntStr)
    {
        StringBuilder serializedIntStrMap = new StringBuilder();
        try
        {
            Set<Integer> keys = mapIntStr.keySet();
            for (Integer integer : keys)
            {
                serializedIntStrMap.append(integer).append("=").append(mapIntStr.get(integer)).append("|");
            }
        } catch (Exception e)
        {
            ModLogger.error(e);
        }
        return serializedIntStrMap.toString();
    }

    public static Set<Integer> deserializeIntegerSet(String setIntString)
    {
        Iterable<String> inString = Splitter.on(',').omitEmptyStrings().split(setIntString);
        Set<Integer> deserializedSet = new HashSet<>();
        try
        {
            for (String id: inString)
            {
                if (id != null && !id.isEmpty())
                    deserializedSet.add(Integer.valueOf(id));
            }
        } catch (Exception e)
        {
            ModLogger.error(e);
        }
        return deserializedSet;
    }

    public static String serializeIntegerSet(Set<Integer> setIntegers)
    {
        StringBuilder serializedSet = new StringBuilder();
        try
        {
            for (Integer integer : setIntegers)
            {
                serializedSet.append(integer).append(",");
            }
        } catch (Exception e)
        {
            ModLogger.error(e);
        }
        return serializedSet.toString();
    }
}
