/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.groups;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Splitter;

// Notes: For saving to disk use UUIDs. For client-server communication use getEntityID. Done.
// UUID does not work on the client.
public enum GROUPS
{
    GROUP_ADD, MEMBER_ADD, MEMBER_REMOVE, MEMBER_PROMOTE, QUEUED, PLAYING;

    public static final int MAX_MEMBERS = 8;

    /** Server side, Client side is sync'd with packets */
    private static Map<Integer, Integer> clientGroups;
    private static Map<Integer, Integer> clientMembers;
    private static Map<Integer, String> clientPlayStatuses;

    public static Integer getLeaderOfGroup(Integer integer)
    {
        if (GROUPS.clientGroups != null) { return GROUPS.clientGroups.get(integer); }
        return null;
    }

    public static Integer getMembersGroupID(Integer memberID)
    {
        if (GROUPS.clientMembers != null) { return GROUPS.clientMembers.get(memberID); }
        return null;
    }

    public static boolean isLeader(Integer memberID)
    {
        return memberID.equals(getLeaderOfGroup(getMembersGroupID(memberID)));
    }

    public static void setClientPlayStatuses(String status)
    {
        GROUPS.clientPlayStatuses = splitToIntStrMap(status);
    }
    
    public static Map<Integer, Integer> getClientMembers()
    {
        return GROUPS.clientMembers;
    }
    
    public static void setClientMembers(String members)
    {
        GROUPS.clientMembers = splitToIntIntMap(members);
    }
    
    public static Map<Integer, Integer> getClientGroups()
    {
        return GROUPS.clientGroups;
    }
    
    public static void setClientGroups(String groups)
    {
        GROUPS.clientGroups = splitToIntIntMap(groups);
    }
    /**
     * getIndex(String playerName)
     * 
     * This is used to return a the index for the playing status icons
     * 
     * @param playerID
     * @return int
     */
    public static int getIndex(Integer playerID)
    {
        int result = 0;
        if (GROUPS.clientPlayStatuses != null && GROUPS.clientPlayStatuses.containsKey(playerID))
        {
            switch (GROUPS.valueOf(GROUPS.clientPlayStatuses.get(playerID)))
            {
            case QUEUED:
                result = 1;
                break;
            case PLAYING:
                result = 2;
            default:
            }
        }
        return result + (GROUPS.isLeader(playerID) ? 8 : 0);
    }

    public static Map<Integer, String> splitToIntStrMap(String mapIntString)
    {       
        try
        {
            Map<String, String> inStringString =  (Map<String, String>) Splitter.on(" ").withKeyValueSeparator("=").split(mapIntString);
            Map<Integer, String> outIntString = new HashMap<Integer, String>();
            for (String id: inStringString.keySet())
            {
                outIntString.put(Integer.valueOf(id), inStringString.get(id));
            }
            return outIntString;
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }
    
    public static Map<Integer, Integer> splitToIntIntMap(String mapIntString)
    {       
        try
        {
            Map<String, String> inStringString =  (Map<String, String>) Splitter.on(" ").withKeyValueSeparator("=").split(mapIntString);
            Map<Integer, Integer> outIntInt = new HashMap<Integer, Integer>();
            for (String id: inStringString.keySet())
            {
                outIntInt.put(Integer.valueOf(id), Integer.valueOf(inStringString.get(id)));
            }
            return outIntInt;
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}
