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

import net.aeronica.mods.mxtune.network.PacketDispatcher;
import net.aeronica.mods.mxtune.network.client.SyncStatusMessage;
import net.aeronica.mods.mxtune.util.ModLogger;

public class PlayManager
{
    /** Don't allow any other class to instantiate the PlayManager */
    private PlayManager() {}

    private static class PlayManagerHolder {public static final PlayManager INSTANCE = new PlayManager();}

    public static PlayManager getInstance() {return PlayManagerHolder.INSTANCE;}

    private static Map<String, String> membersMML = new HashMap<String, String>();
    private static Map<String, String> groupsMembers = new HashMap<String, String>();
    private static Map<String, String> playStatus = new HashMap<String, String>();

    public void setPlaying(String playerName) {playStatus.put(playerName, GROUPS.PLAYING.name());}

    public void setQueued(String playerName) {playStatus.put(playerName, GROUPS.QUEUED.name());}

    public void setDone(String playerName) {if (playStatus.containsKey(playStatus)) playStatus.remove(playerName);}

    public void syncStatus()
    {
        String buildStatus = " ";
        try
        {
            Set<String> keys = playStatus.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext())
            {
                String playerName = (String) it.next();
                buildStatus = buildStatus + playerName + "=" + playStatus.get(playerName) + " ";
            }

        } catch (Exception e)
        {
            ModLogger.logError(e.getLocalizedMessage());
            e.printStackTrace();
        }
        PacketDispatcher.sendToAll(new SyncStatusMessage(buildStatus.trim()));
    }

    public void queue(String groupID, String memberName, String mml)
    {
        try
        {
            membersMML.put(memberName, mml);
            groupsMembers.put(memberName, groupID);
            setQueued(memberName);
            syncStatus();
        } catch (Exception e)
        {
            ModLogger.logError(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns a string in Map ready format. e.g.
     * "mamberName=MML@... memberName=MML@..."
     * 
     * @param groupID
     * @return
     */
    public String getMML(String groupID)
    {
        String buildMML = " ";
        try
        {
            Set<String> keys = groupsMembers.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext())
            {
                String member = (String) it.next();
                String group = groupsMembers.get(member);
                if (group.equalsIgnoreCase(groupID))
                {
                    buildMML = buildMML + member + "=" + membersMML.get(member) + " ";
                    it.remove();
                    membersMML.remove(member);
                    setPlaying(member);
                }
            }
        } catch (Exception e)
        {
            ModLogger.logError(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return buildMML.trim();
    }

    /**
     * Used by the GroupMananger to purge unused/aborted Jam data
     * 
     * @param memberName
     */
    public void dequeueMember(String memberName)
    {
        if (membersMML != null && !membersMML.isEmpty() && membersMML.containsKey(memberName))
        {
            membersMML.remove(memberName);
        }
        if (groupsMembers != null && !groupsMembers.isEmpty() && groupsMembers.containsKey(memberName))
        {
            groupsMembers.remove(memberName);
        }
        if (playStatus != null && !playStatus.isEmpty() && playStatus.containsKey(memberName))
        {
            playStatus.remove(memberName);
            syncStatus();
        }
    }
}
