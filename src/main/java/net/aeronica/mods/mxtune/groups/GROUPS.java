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

import java.util.Map;

import com.google.common.base.Splitter;

// Notes: For saving to disk use UUIDs. For client-server communication use getEntityID. Done.
// UUID does not work on the client.
public enum GROUPS
{
    GROUP_ADD, MEMBER_ADD, MEMBER_REMOVE, MEMBER_PROMOTE, QUEUED, PLAYING;

    public static final int MAX_MEMBERS = 8;

    /** Server side, Client side is sync'd with packets */
    private static Map<String, String> clientGroups;
    private static Map<String, String> clientMembers;
    private static Map<String, String> clientPlayStatuses;

    public static String getLeaderOfGroup(String groupID)
    {
        if (GROUPS.clientGroups != null) { return GROUPS.clientGroups.get(groupID); }
        return null;
    }

    public static String getMembersGroupID(String memberName)
    {
        if (GROUPS.clientMembers != null) { return GROUPS.clientMembers.get(memberName); }
        return null;
    }

    public static boolean isLeader(String memberName)
    {
        return memberName.equalsIgnoreCase(getLeaderOfGroup(getMembersGroupID(memberName)));
    }

    public static void setClientPlayStatuses(String status)
    {
        GROUPS.clientPlayStatuses = splitToHashMap(status);
    }
    
    public static Map<String, String> getClientMembers()
    {
        return GROUPS.clientMembers;
    }
    
    public static void setClientMembers(String members)
    {
        GROUPS.clientMembers = splitToHashMap(members);
    }
    
    public static Map<String, String> getClientGroups()
    {
        return GROUPS.clientGroups;
    }
    
    public static void setClientGroups(String groups)
    {
        GROUPS.clientGroups = splitToHashMap(groups);
    }
    /**
     * getIndex(String playerName)
     * 
     * This is used to return a the index for the playing status icons
     * 
     * @param playerName
     * @return int
     */
    public static int getIndex(String playerName)
    {
        int result = 0;
        if (GROUPS.clientPlayStatuses != null && GROUPS.clientPlayStatuses.containsKey(playerName))
        {
            switch (GROUPS.valueOf(GROUPS.clientPlayStatuses.get(playerName)))
            {
            case QUEUED:
                result = 1;
                break;
            case PLAYING:
                result = 2;
            default:
            }
        }
        return result + (GROUPS.isLeader(playerName) ? 8 : 0);
    }

    public static Map<String, String> splitToHashMap(String in)
    {
        try
        {
            return (Map<String, String>) Splitter.on(" ").withKeyValueSeparator("=").split(in);
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}
