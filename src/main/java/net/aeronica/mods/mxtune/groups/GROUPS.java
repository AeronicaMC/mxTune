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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

public enum GROUPS
{
    GROUP_ADD, MEMBER_ADD, MEMBER_REMOVE, MEMBER_PROMOTE, QUEUED, PLAYING;

    public static final int MAX_MEMBERS = 8;

    /** Server side, Client side is sync'd with packets */
    /* GroupManager */
    private static Map<Integer, Integer> clientGroups;
    private static Map<Integer, Integer> clientMembers;
    private static ListMultimap<Integer, Integer> groupsMembers;
    /* PlayManager */
    private static Map<Integer, String> membersQueuedStatus;
    private static Map<Integer, Integer> membersPlayID;
    private static Set<Integer> activePlayIDs;


    /* GroupManager Client Status Methods */
    public static Integer getLeaderOfGroup(Integer integer)
    {
        return GROUPS.clientGroups != null ? GROUPS.clientGroups.get(integer) : null;
    }

    public static Integer getMembersGroupLeader(Integer memberID){
        return getLeaderOfGroup(getMembersGroupID(memberID));
    }
    
    public static Integer getMembersGroupID(Integer memberID)
    {
        return GROUPS.clientMembers != null ? GROUPS.clientMembers.get(memberID) : null;
    }

    public static Set<Integer> getPlayersGroupMembers(EntityPlayer playerIn)
    {
        Integer groupID = GROUPS.getMembersGroupID(playerIn.getEntityId());
        if(groupID != null)
        {
            Set<Integer> members = Sets.newHashSet();
            for (Integer group: groupsMembers.keySet())
            {
                if(groupID == group)
                    members.addAll(groupsMembers.get(group));
            }       
            return members;
        }
        return null;
    }
    
    private static boolean isLeader(Integer memberID)
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
    
    public static void setGroupsMembers(String members)
    {
        GROUPS.groupsMembers = deserializeIntIntListMultimapSwapped(members);
    }
    
    public static ListMultimap<Integer, Integer> getGroupsMembers()
    {
        return groupsMembers;
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

     /* PlayManager Client Status Methods */
    public static Set<Integer> getMembersByPlayID(Integer playID) 
    {
        Set<Integer> members = Sets.newHashSet();
        if (membersPlayID != null)
        {
            for(Integer someMember: GROUPS.membersPlayID.keySet())
            {
                if(GROUPS.membersPlayID.get(someMember).equals(playID))
                {
                    members.add(someMember);
                }
            }
        }
        return members;
    }
    
    public static Vec3d getMedianPos(int playID)
    {
        double x, y, z;
        x = y = z = 0;
        int count = 0;
        Vec3d pos;
        for(int member: getMembersByPlayID(playID))
        {   
            EntityPlayer player = (EntityPlayer)  MXTuneMain.proxy.getClientPlayer().getEntityWorld().getEntityByID(member);
            if(player == null)
                continue;
            x = x + player.getPositionVector().xCoord;
            y = y + player.getPositionVector().yCoord;
            z = z + player.getPositionVector().zCoord;
            count++;
        }            

        if (count == 0)
            return Vec3d.ZERO;
        x/=count;
        y/=count;
        z/=count;
        pos = new Vec3d(x,y,z);
        return pos;
    }

    /**
     * Called by client tick once every two seconds to calculate the distance between
     * group members in relation to a maximum after which the server will stop any music the
     * group is performing.
     * 
     * @param stopDistance
     * #return 0-1D, where 1 represents the critical stop.
     */
    public static double getGroupMembersScaledDistance(EntityPlayer playerIn)
    {
        Set<Integer> members = getPlayersGroupMembers(playerIn);
        double abortDistance = ModConfig.getGroupPlayAbortDistance();
        double distance = 0D;
        double maxDistance = 0D;
        if (members != null && !members.isEmpty())
        {  
            for (Integer memberA: members)
            {
                for (Integer memberB:  members )
                {
                    if (memberA != memberB)
                    {
                        double playerDistance = getMemberVector(memberA).distanceTo(getMemberVector(memberB));
                        if (playerDistance > maxDistance) maxDistance = playerDistance;
                        distance = Math.min(1.0D, scaleBetween(maxDistance, 0, 1D, 0D, abortDistance));
                    }
                }
            }
        }
        return distance;
    }

    private static double scaleBetween(double unscaledNum, double minAllowed, double maxAllowed, double min, double max) {
        return (maxAllowed - minAllowed) * (unscaledNum - min) / (max - min) + minAllowed;
      }
    
    private static Vec3d getMemberVector(Integer entityID)
    {
        Vec3d v3d;
        EntityPlayer player = (EntityPlayer) MXTuneMain.proxy.getClientPlayer().getEntityWorld().getEntityByID(entityID);
        if (player != null)
            v3d = new Vec3d(player.posX, player.prevPosY, player.posZ);
        else
            v3d = new Vec3d(0,0,0);
        return v3d;
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
        return membersPlayID;
    }
    
    public static Integer getSoloMemberByPlayID(Integer playID)
    {
        for(Integer someMember: GROUPS.membersPlayID.keySet())
        {
            if(GROUPS.membersPlayID.get(someMember).equals(playID))
            {
                return someMember;
            }
        }
        return null;
    }
    
    public static void setPlayIDMembers(String playIDMembers)
    {
        GROUPS.membersPlayID = deserializeIntIntMap(playIDMembers);
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
            for (Map.Entry<String,String> entry: inStringString.entrySet())
            {
                outIntInt.put(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
            }
            return outIntInt;
        } catch (IllegalArgumentException e)
        {
            ModLogger.error(e);
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
            ModLogger.error(e);
        }
        return serializedIntIntMap.toString();
    }
    
    /**
     * This was created specifically to make the groupsMembers ListMultimap
     * without duplicating network traffic to send a complementary structure.
     * @param hashTableString
     * @return a ListMultimap where the keys and values have been swapped.
     */
    public static ListMultimap<Integer, Integer> deserializeIntIntListMultimapSwapped(String hashTableString)
    {
        try
        {
            Map<String, String> inStringString =  (Map<String, String>) Splitter.on('|').omitEmptyStrings().withKeyValueSeparator("=").split(hashTableString);
            ListMultimap<Integer, Integer> outListMultimapIntInt = ArrayListMultimap.create();
            for (Map.Entry<String,String> entry: inStringString.entrySet())
            {
                outListMultimapIntInt.put(Integer.valueOf(entry.getValue()), Integer.valueOf(entry.getKey()));
            }
            return outListMultimapIntInt;
        } catch (IllegalArgumentException e)
        {
            ModLogger.error(e);
            return null;
        }

    }
    
    public static Map<Integer, String> deserializeIntStrMap(String mapIntString)
    {       
        try
        {
            Map<String, String> inStringString =  (Map<String, String>) Splitter.on('|').omitEmptyStrings().withKeyValueSeparator("=").split(mapIntString);
            Map<Integer, String> outIntString = new HashMap<Integer, String>();
            for (Map.Entry<String,String> entry: inStringString.entrySet())
            {
                outIntString.put(Integer.valueOf(entry.getKey()), entry.getValue());
            }
            return outIntString;
        } catch (IllegalArgumentException e)
        {
            ModLogger.error(e);
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
            ModLogger.error(e);
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
            ModLogger.error(e);
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
            ModLogger.error(e);
        }
        return serializedSet.toString();        
    }
    
}
