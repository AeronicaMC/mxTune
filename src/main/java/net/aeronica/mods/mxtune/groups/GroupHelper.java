/*
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

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.config.ModConfig;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class GroupHelper
{
    public static final int GROUP_ADD = 1;
    public static final int MEMBER_ADD = 2;
    public static final int MEMBER_REMOVE =3;
    public static final int MEMBER_PROMOTE = 4;
    static final int QUEUED = 5;
    static final int PLAYING = 6;

    public static final int MAX_MEMBERS = 8;

    /* Server side, Client side is sync'd with packets */
    /* GroupManager */
    private static Map<Integer, Integer> clientGroups = Collections.emptyMap();
    private static Map<Integer, Integer> clientMembers = Collections.emptyMap();
    private static ListMultimap<Integer, Integer> groupsMembers = ArrayListMultimap.create();
    /* PlayManager */
    private static Map<Integer, Integer> membersQueuedStatus = Collections.emptyMap();
    private static Map<Integer, Integer> membersPlayID = Collections.emptyMap();
    private static Set<Integer> activeServerManagedPlayIDs = new ConcurrentSkipListSet<>();
    private static Set<Integer> activeClientManagedPlayIDs = new ConcurrentSkipListSet<>();

    private GroupHelper() { /* NOP */ }

    /* GroupManager Client Status Methods */
    @Nullable
    public static Integer getLeaderOfGroup(Integer leaderID)
    {
        return GroupHelper.clientGroups.get(leaderID);
    }

    public static int getMembersGroupLeader(@Nullable Integer memberID){
        Integer leaderId = getLeaderOfGroup(getMembersGroupID(memberID));
        return leaderId != null ? leaderId : -1;
    }

    @Nullable
    public static Integer getMembersGroupID(@Nullable Integer memberID)
    {
        return GroupHelper.clientMembers.get(memberID);
    }

    private static Set<Integer> getPlayersGroupMembers(EntityPlayer playerIn)
    {
        Integer groupID = GroupHelper.getMembersGroupID(playerIn.getEntityId());
        if(groupID != null)
        {
            Set<Integer> members = Sets.newHashSet();
            for (Integer group: groupsMembers.keySet())
            {
                if(groupID.intValue() == group)
                    members.addAll(groupsMembers.get(group));
            }       
            return members;
        }
        return Collections.emptySet();
    }
    
    private static boolean isLeader(Integer memberID)
    {
        return memberID.equals(getLeaderOfGroup(getMembersGroupID(memberID)));
    }

    public static Map<Integer, Integer> getClientMembers()
    {
        return GroupHelper.clientMembers;
    }
    
    public static void setClientMembers(String members)
    {
        GroupHelper.clientMembers = deserializeIntIntMap(members);
    }
    
    public static Map<Integer, Integer> getClientGroups()
    {
        return GroupHelper.clientGroups;
    }
    
    public static void setClientGroups(String groups)
    {
        GroupHelper.clientGroups = deserializeIntIntMap(groups);
    }
    
    public static void setGroupsMembers(String members)
    {
        GroupHelper.groupsMembers = deserializeIntIntListMultimapSwapped(members);
    }

    // This is a workaround to force the playID into the active list on the client side presuming a network order
    // incident occurred and the playID is either not present, AND/OR a race condition, or threading issue made it not
    // available.
    @SideOnly(Side.CLIENT)
    public static void addServerManagedActivePlayID(int playId)
    {
        if (playId != PlayIdSupplier.PlayType.INVALID)
            activeServerManagedPlayIDs.add(playId);
    }

    @SideOnly(Side.CLIENT)
    public static void addClientManagedActivePlayID(int playId)
    {
        if (playId != PlayIdSupplier.PlayType.INVALID)
            activeClientManagedPlayIDs.add(playId);
    }

    public static Set<Integer> getAllPlayIDs()
    {
        return mergeSets(activeServerManagedPlayIDs, activeClientManagedPlayIDs);
    }

    private static<T> Set<T> mergeSets(Set<T> a, Set<T> b)
    {
        Set<T> set = new ConcurrentSkipListSet<>(a);
        set.addAll(b);
        return set;
    }

    public static void removeClientManagedPlayID(int playId)
    {
        activeClientManagedPlayIDs.remove(playId);
    }

    public static void clearClientManagedPlayIDs()
    {
        activeClientManagedPlayIDs.clear();
    }

    public static Set<Integer> getClientManagedPlayIDs()
    {
        return activeClientManagedPlayIDs;
    }

    public static Set<Integer> getServerManagedPlayIDS()
    {
        return activeServerManagedPlayIDs;
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
        if (GroupHelper.membersQueuedStatus != null && GroupHelper.membersQueuedStatus.containsKey(playerID))
        {
            switch (GroupHelper.membersQueuedStatus.get(playerID))
            {
            case QUEUED:
                result = 1;
                break;
            case PLAYING:
                result = 2;
                break;
            default:
            }
        }
        return result + (GroupHelper.isLeader(playerID) ? 8 : 0);
    }
    
    public static Map<Integer, Integer> getClientPlayStatuses()
    {
        return membersQueuedStatus;
    }

     /* PlayManager Client Status Methods */
    private static Set<Integer> getMembersByPlayID(Integer playID)
    {
        Set<Integer> members = Sets.newHashSet();
        if (membersPlayID != null)
        {
            for(Integer someMember: GroupHelper.membersPlayID.keySet())
            {
                if(GroupHelper.membersPlayID.get(someMember).equals(playID))
                {
                    members.add(someMember);
                }
            }
        }
        return members;
    }

    /**
     * Returns the median position of a group or player by playID.
     * Client side only until fixed. Probably need to take into account the dimension.
     *
     * @param playID for solo or JAM
     * @return the median position for a group, or the position of a solo player
     */
    public static Vec3d getMedianPos(int playID)
    {
        double x;
        double y;
        double z;
        x = y = z = 0;
        int count = 0;
        Vec3d pos;
        for(int member: getMembersByPlayID(playID))
        {
            EntityPlayer player = MXTune.proxy.getPlayerByEntityID(member);
            if(player == null)
                continue;
            x = x + player.getPositionVector().x;
            y = y + player.getPositionVector().y;
            z = z + player.getPositionVector().z;
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
     * @param playerIn get the scaled distance for this player
     * #return 0-1D, where 1 represents the critical stop.
     */
    public static double getGroupMembersScaledDistance(EntityPlayer playerIn)
    {
        Set<Integer> members = getPlayersGroupMembers(playerIn);
        double abortDistance = ModConfig.getGroupPlayAbortDistance();
        double distance = 0D;
        double maxDistance = 0D;
        for (Integer memberA : members)
            for (Integer memberB : members)
                if (memberA.intValue() != memberB)
                {
                    double playerDistance = getMemberVector(memberA).distanceTo(getMemberVector(memberB));
                    if (playerDistance > maxDistance) maxDistance = playerDistance;
                    distance = Math.min(1.0D, (maxDistance / abortDistance));
                }
        return distance;
    }

    private static Vec3d getMemberVector(Integer entityID)
    {
        Vec3d v3d;
        EntityPlayer player = (EntityPlayer) MXTune.proxy.getClientPlayer().getEntityWorld().getEntityByID(entityID);
        if (player != null)
            v3d = new Vec3d(player.posX, player.prevPosY, player.posZ);
        else
            v3d = new Vec3d(0,0,0);
        return v3d;
    }
    
    public static boolean isClientPlaying(Integer playID)
    {
        Set<Integer> members = GroupHelper.getMembersByPlayID(playID);
        return ((members != null) && !members.isEmpty()) && members.contains(MXTune.proxy.getClientPlayer().getEntityId());
    }

    @SuppressWarnings("unused")
    public static boolean playerHasPlayID(Integer entityID, Integer playID)
    {
        Set<Integer> members = GroupHelper.getMembersByPlayID(playID);
        return (members != null && !members.isEmpty()) && members.contains(entityID);
    }

    @SuppressWarnings("unused")
    public static boolean isPlayIDPlaying(Integer playID) { return activeServerManagedPlayIDs != null && activeServerManagedPlayIDs.contains(playID); }

    public static void setClientPlayStatuses(String clientPlayStatuses)
    {
        GroupHelper.membersQueuedStatus = deserializeIntIntMap(clientPlayStatuses);
    }
        
    public static Map<Integer, Integer> getPlayIDMembers()
    {
        return membersPlayID;
    }

    @Nullable
    public static Integer getSoloMemberByPlayID(Integer playID)
    {
        for(Integer someMember: GroupHelper.membersPlayID.keySet())
        {
            if(GroupHelper.membersPlayID.get(someMember).equals(playID))
            {
                return someMember;
            }
        }
        return null;
    }
    
    public static void setPlayIDMembers(String playIDMembers)
    {
        GroupHelper.membersPlayID = deserializeIntIntMap(playIDMembers);
    }

    /**
     * Update the active play IDs without replacing the collection instance.
     * @param setIntString serialized set
     */
    public static void setActiveServerManagedPlayIDs(String setIntString)
    {
        Set<Integer> receivedSet = deserializeIntegerSet(setIntString);

        activeServerManagedPlayIDs.addAll(receivedSet);

        for (Integer integer : activeServerManagedPlayIDs)
            if (!receivedSet.contains(integer))
                activeServerManagedPlayIDs.remove(integer);
    }
    
    /* Serialization and deserialization methods */
    private static Map<Integer, Integer> deserializeIntIntMap(String mapIntString)
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

    @SuppressWarnings("unused")
    static String serializeIntIntMap(HashMap<Integer, Integer> mapIntInt)
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
    private static ListMultimap<Integer, Integer> deserializeIntIntListMultimapSwapped(String hashTableString)
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

    @SuppressWarnings("unused")
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
    static String serializeIntStrMap(HashMap<Integer, String> mapIntStr)
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

    private static Set<Integer> deserializeIntegerSet(String setIntString)
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

    @SuppressWarnings("all")
    static String serializeIntegerSet(Set<Integer> setIntegers)
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
