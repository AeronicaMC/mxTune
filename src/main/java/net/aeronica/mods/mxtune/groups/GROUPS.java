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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

// Notes: For saving to disk use UUIDs. For client-server communication use getEntityID. Done.
// UUID does not work on the client.
public enum GROUPS
{
    GROUP_ADD, MEMBER_ADD, MEMBER_REMOVE, MEMBER_PROMOTE, QUEUED, PLAYING;

    public static final int MAX_MEMBERS = 8;

    /** Server side, Client side is sync'd with packets */
    /* GroupManager */
    private static Map<Integer, Integer> clientGroups;
    private static Map<Integer, Integer> clientMembers;
    /* PlayManager */
    private static Map<Integer, String> membersQueuedStatus;
    private static Map<Integer, Integer> playIDMembers;
    private static Set<Integer> activePlayIDs;


    /* GroupManager statuses */
    public static Integer getLeaderOfGroup(Integer integer)
    {
        if (GROUPS.clientGroups != null) { return GROUPS.clientGroups.get(integer); }
        return null;
    }

    public static Integer getMembersGroupLeader(Integer memberID){
        return getLeaderOfGroup(getMembersGroupID(memberID));
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

    public static Map<Integer, Integer> getClientMembers()
    {
        return GROUPS.clientMembers;
    }
    
    public static void setClientMembers(String members)
    {
        GROUPS.clientMembers = deserializeIntIntMap(members);
    }
    
    public static Map<Integer, Integer> getClientGroups()
    {
        return GROUPS.clientGroups;
    }
    
    public static void setClientGroups(String groups)
    {
        GROUPS.clientGroups = deserializeIntIntMap(groups);
    }
    
    /**
     * getIndex(Integer playerID)
     * 
     * This is used to return a the index for the playing status icons
     * 
     * @param playerID (EntityID)
     * @return int
     */
    public static int getIndex(Integer playerID)
    {
        int result = 0;
        if (GROUPS.membersQueuedStatus != null && GROUPS.membersQueuedStatus.containsKey(playerID))
        {
            switch (GROUPS.valueOf(GROUPS.membersQueuedStatus.get(playerID)))
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
    
    public static Map<Integer, String> getClientPlayStatuses()
    {
        return membersQueuedStatus;
    }

    public static Set<Integer> getMembersByPlayID(Integer playID) 
    {
        Set<Integer> members = Sets.newHashSet();
        if (playIDMembers != null)
        {
            for(Integer someMember: GROUPS.playIDMembers.keySet())
            {
                if(GROUPS.playIDMembers.get(someMember).equals(playID))
                {
                    members.add(someMember);
                }
            }
        }
        return members;
    }
    
    public static Vec3d getMedianPos(Integer playID)
    {
        double x, y, z, count; x = y = z = count = 0;
        Vec3d pos;
        for(Integer member: getMembersByPlayID(playID))
        {   
            EntityPlayer player = (EntityPlayer)  MXTuneMain.proxy.getClientPlayer().getEntityWorld().getEntityByID(member);
            x = x + player.getPositionVector().xCoord;
            y = y + player.getPositionVector().yCoord;
            z = z + player.getPositionVector().zCoord;
            count++;
        }            

        if (count == 0) return new Vec3d(0,0,0);
        x/=count;
        y/=count;
        z/=count;
        pos = new Vec3d(x,y,z);
        return pos;
    }

    public static boolean isClientPlaying(Integer playID)
    {
        Set<Integer> members = GROUPS.getMembersByPlayID(playID);
        return ((members!=null) && !members.isEmpty()) ? members.contains(MXTuneMain.proxy.getClientPlayer().getEntityId()) : false;
    }
    
    public static boolean playerHasPlayID(Integer entityID, Integer playID)
    {
        Set<Integer> members = GROUPS.getMembersByPlayID(playID);
        return (members != null && !members.isEmpty()) ? members.contains(entityID) : false;
    }
    
    public static boolean isPlayIDPlaying(Integer playID) { return activePlayIDs != null ? activePlayIDs.contains(playID) : false; }

    public static void setClientPlayStatuses(String clientPlayStatuses)
    {
        GROUPS.membersQueuedStatus = deserializeIntStrMap(clientPlayStatuses);
    }
        
    public static Map<Integer, Integer> getPlayIDMembers()
    {
        return playIDMembers;
    }
    
    public static Integer getSoloMemberByPlayID(Integer playID)
    {
        for(Integer someMember: GROUPS.playIDMembers.keySet())
        {
            if(GROUPS.playIDMembers.get(someMember).equals(playID))
            {
                return someMember;
            }
        }
        return null;
    }

    public static void setPlayIDMembers(String playIDMembers)
    {
        GROUPS.playIDMembers = deserializeIntIntMap(playIDMembers);
    }

    public static Set<Integer> getActivePlayIDs()
    {
        return activePlayIDs;
    }

    public static void setActivePlayIDs(String setIntString)
    {
        activePlayIDs = deserializeIntegerSet(setIntString);
    }
    
    /* Serialization and deserialization methods */
    public static Map<Integer, Integer> deserializeIntIntMap(String mapIntString)
    {       
        try
        {
            Map<String, String> inStringString =  (Map<String, String>) Splitter.on('|').omitEmptyStrings().withKeyValueSeparator("=").split(mapIntString);
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

    public static String serializeIntIntMap(HashMap<Integer, Integer> mapIntInt)
    {
        StringBuilder serializedIntIntMap = new StringBuilder();
        try
        {
            Set<Integer> keys = mapIntInt.keySet();
            Iterator<Integer> it = keys.iterator();
            while (it.hasNext())
            {
                Integer integer = (Integer) it.next();
                serializedIntIntMap.append(integer).append("=").append(mapIntInt.get(integer)).append("|");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return serializedIntIntMap.toString();
    }
    
    public static Map<Integer, String> deserializeIntStrMap(String mapIntString)
    {       
        try
        {
            Map<String, String> inStringString =  (Map<String, String>) Splitter.on('|').omitEmptyStrings().withKeyValueSeparator("=").split(mapIntString);
            Map<Integer, String> outIntString = new HashMap<Integer, String>();
            for (String id: inStringString.keySet())
            {
                outIntString.put(Integer.valueOf(id), inStringString.get(id));
            }
            return outIntString;
        } catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String serializeIntStrMap(HashMap<Integer, String> mapIntStr)
    {
        StringBuilder serializedIntStrMap = new StringBuilder();
        try
        {
            Set<Integer> keys = mapIntStr.keySet();
            Iterator<Integer> it = keys.iterator();
            while (it.hasNext())
            {
                Integer integer = (Integer) it.next();
                serializedIntStrMap.append(integer).append("=").append(mapIntStr.get(integer)).append("|");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return serializedIntStrMap.toString();
    }
            
    public static Set<Integer> deserializeIntegerSet(String setIntString)
    {
        Iterable<String> inString = Splitter.on(',').omitEmptyStrings().split(setIntString);
        Set<Integer> deserializedSet = null;
        try
        {
            deserializedSet =  Sets.newHashSet();
            for (String id: inString)
            {
                if (id != null && !id.isEmpty())
                    deserializedSet.add(Integer.valueOf(id));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return deserializedSet;
    }

    public static String serializeIntegerSet(Set<Integer> setIntegers)
    {
        StringBuilder serializedSet = new StringBuilder();
        try
        {
            Iterator<Integer> it = setIntegers.iterator();
            while (it.hasNext())
            {
                Integer integer = (Integer) it.next();
                serializedSet.append(integer).append(",");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return serializedSet.toString();        
    }
    
}
