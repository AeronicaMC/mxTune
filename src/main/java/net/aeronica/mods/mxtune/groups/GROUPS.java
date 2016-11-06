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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Splitter;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.entity.player.EntityPlayer;
import paulscode.sound.Vector3D;

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
    private static HashMap<Integer, String> clientPlayStatuses;
    private static HashMap<Integer, Integer> playIDMembers;
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
    
    public static Map<Integer, String> getClientPlayStatuses()
    {
        return clientPlayStatuses;
    }

    public static List<Integer> getMembersByPlayID(Integer playID) 
    {
        List<Integer> members = new ArrayList<Integer>();
        for(Integer someMember: GROUPS.playIDMembers.keySet())
        {
            if(GROUPS.playIDMembers.get(someMember).equals(playID))
            {
                members.add(GROUPS.playIDMembers.get(someMember));
            }
        }
        return members;
    }
    
    public static Vector3D getMedianPos(Integer playID)
    {
        int x, y, z, count; x = y = z = count = 0;
        Vector3D pos;
        ModLogger.logInfo("getMedianPos");

        for(Integer member: getMembersByPlayID(playID))
        {   
            EntityPlayer player = (EntityPlayer)  MXTuneMain.proxy.getClientPlayer().getEntityWorld().getEntityByID(member);
            x = x + player.getPosition().getX();
            y = y + player.getPosition().getY();
            z = z + player.getPosition().getZ();
            count++;
            ModLogger.logInfo("  getMedianPos player:" + player + ", x:" + x + ", count: " + count);
        }            

        if (count == 0) return new Vector3D(0,0,0);
        x/=count;
        y/=count;
        z/=count;
        pos = new Vector3D(x,y,z);
        ModLogger.logInfo("" + pos);
        return pos;
    }

    public static boolean isClientPlaying(Integer playID)
    {
        List<Integer> members = getMembersByPlayID(playID);
        return ((members!=null) && members.isEmpty())?members.contains(MXTuneMain.proxy.getClientPlayer().getEntityId()):false;
    }
    
    
    public static boolean isPlaying(Integer playID) { return activePlayIDs != null ? activePlayIDs.contains(playID) : false; }

    public static void setClientPlayStatuses(String clientPlayStatuses)
    {
        GROUPS.clientPlayStatuses = deserializeIntStrMap(clientPlayStatuses);
    }
        
    public static Map<Integer, Integer> getPlayIDMembers()
    {
        return playIDMembers;
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

    public static String serializeIntIntMap(HashMap<Integer, Integer> mapIntInt)
    {
        String serializedIntIntMap = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try
        {
            os = new ObjectOutputStream(bos);
            os.writeObject(mapIntInt);
            serializedIntIntMap = bos.toString();
            os.close();
            bos.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return serializedIntIntMap;
    }
    
    @SuppressWarnings("unchecked")
    public static HashMap<Integer, Integer> deserializeIntIntMap(String mapIntInt)
    {
        ModLogger.logInfo("deserializeIntIntMap: "+mapIntInt);
        ByteArrayInputStream bis = new ByteArrayInputStream(mapIntInt.getBytes(Charset.defaultCharset()));
        ObjectInputStream oInputStream = null;
        HashMap<Integer, Integer> deserializedIntIntMap = null;
        try
        {
            oInputStream = new ObjectInputStream(bis);
            deserializedIntIntMap = (HashMap<Integer, Integer>) oInputStream.readObject();
            bis.close();
            oInputStream.close();
        } catch (ClassNotFoundException | IOException e)
        {
            e.printStackTrace();
        }        
        return deserializedIntIntMap;
    }
    
    public static String serializeIntStrMap(HashMap<Integer, String> mapIntStr)
    {
        String serializedIntStrMap = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try
        {
            os = new ObjectOutputStream(bos);
            os.writeObject(mapIntStr);
            serializedIntStrMap = bos.toString();
            os.close();
            bos.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return serializedIntStrMap;
    }
   
    @SuppressWarnings("unchecked")
    public static HashMap<Integer, String> deserializeIntStrMap(String mapIntStr)
    {
        ModLogger.logInfo("deserializeIntStrMap: "+mapIntStr);
        ByteArrayInputStream bis = new ByteArrayInputStream(mapIntStr.getBytes(Charset.defaultCharset()));
        ObjectInputStream oInputStream = null;
        HashMap<Integer, String> deserializedIntStrMap = null;
        try
        {
            oInputStream = new ObjectInputStream(bis);
            deserializedIntStrMap = (HashMap<Integer, String>) oInputStream.readObject();
        } catch (ClassNotFoundException | IOException e)
        {
            e.printStackTrace();
        }        
        return deserializedIntStrMap;
    }
        
    @SuppressWarnings("unchecked")
    public static Set<Integer> deserializeIntegerSet(String setIntString)
    {
        ModLogger.logInfo("deserializeIntegerSet: "+setIntString);
        ByteArrayInputStream bis = new ByteArrayInputStream(setIntString.getBytes(Charset.defaultCharset()));
        ObjectInputStream oInputStream = null;
        Set<Integer> deserializedSet = null;
        try
        {
            oInputStream = new ObjectInputStream(bis);
            deserializedSet = (Set<Integer>) oInputStream.readObject();
            bis.close();
            oInputStream.close();
        } catch (ClassNotFoundException | IOException e)
        {
            e.printStackTrace();
        }        
        return deserializedSet;
    }

    public static String serializeIntegerSet(Set<Integer> setIntegers)
    {
        String serializedSet = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try
        {
            os = new ObjectOutputStream(bos);
            os.writeObject(setIntegers);
            serializedSet = bos.toString();
            os.close();
            bos.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        ModLogger.logInfo("serializeIntegerSet: "+serializedSet);
        return serializedSet;        
    }
    
}
