package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.util.IGroupClientChangedCallback;
import aeronicamc.mods.mxtune.util.IGroupClientChangedCallback.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GroupClient
{
    private static final Logger LOGGER = LogManager.getLogger(GroupClient.class);
    private static final Map<Integer, Group> groupMap = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> memberState = new ConcurrentHashMap<>();
    private static IGroupClientChangedCallback callback;

    public static void clear()
    {
        synchronized (groupMap) { groupMap.clear(); }
        synchronized (memberState) { memberState.clear(); }
    }

    public static void setGroups(Map<Integer, Group> pGroupMap)
    {
        synchronized (groupMap)
        {
            LOGGER.debug("-----");
            LOGGER.debug("Apply GroupMap Server/Client: {}/{}", pGroupMap.size(), groupMap.size());
            groupMap.forEach((id, group) -> LOGGER.debug("  Before {}", group));
            Set<Integer> serverKeys = pGroupMap.keySet();
            Set<Integer> removeKeys = new HashSet<>();
            pGroupMap.forEach((kS, vS) -> {
                if (groupMap.get(kS) != null && !groupMap.get(kS).equals(pGroupMap.get(kS)))
                    groupMap.replace(kS, vS);
                else
                    groupMap.putIfAbsent(kS, vS);
            });
            groupMap.keySet().forEach(kC -> {
                if (!serverKeys.contains(kC))
                    removeKeys.add(kC);
            });
            removeKeys.forEach(groupMap::remove);
            LOGGER.debug("Final GroupMap Server/Client: {}/{}", pGroupMap.size(), groupMap.size());
            groupMap.forEach((id, group) -> LOGGER.debug("  After  {}", group));
        }
        if (callback != null)
            callback.onGroupClientChanged(Type.Group);
    }

    public static void setMemberStates(Map<Integer, Integer> pMemberState)
    {
        synchronized (memberState)
        {
            LOGGER.debug("-----");
            LOGGER.debug("Apply memberState Server/Client: {}/{}", pMemberState.size(), memberState.size());
            memberState.forEach((id, state) -> LOGGER.debug("  Before  {}", state));
            memberState.forEach((member, state) -> LOGGER.debug("    member: {} state: {}", member, state));
            Set<Integer> serverKeys = pMemberState.keySet();
            Set<Integer> removeKeys = new HashSet<>();
            pMemberState.forEach((kS, vS) -> {
                if (memberState.get(kS) != null && !memberState.get(kS).equals(pMemberState.get(kS)))
                    memberState.replace(kS, vS);
                else
                    memberState.putIfAbsent(kS, vS);
            });
            memberState.keySet().forEach(kC -> {
                if (!serverKeys.contains(kC))
                    removeKeys.add(kC);
            });
            removeKeys.forEach(memberState::remove);
            memberState.forEach((id, state) -> LOGGER.debug("  After  {}", state));
            memberState.forEach((member, state) -> LOGGER.debug("    member: {} state: {}", member, state));
        }
        if (callback != null)
            callback.onGroupClientChanged(Type.Member);
    }

    public static void setCallback(IGroupClientChangedCallback pCallback)
    {
        callback = pCallback;
    }

    public static void removeCallback()
    {
        callback = null;
    }

    /**
     * @param memberId search all groups for this member.
     * @return the Group or the Group.EMPTY.
     */
    public static Group getGroup(int memberId)
    {
        return groupMap.values().stream().filter(group -> group.isMember(memberId)).findFirst().orElse(Group.EMPTY);
    }

    public synchronized static Group getGroupById(int groupId)
    {
        return groupMap.values().stream().filter(group -> group.getGroupId() == groupId).findFirst().orElse(Group.EMPTY);
    }

    public static boolean isGrouped(int memberId)
    {
        return groupMap.values().stream().anyMatch(group -> group.isMember(memberId));
    }

    public static boolean isLeader(int memberId)
    {
        return groupMap.values().stream().anyMatch(group -> group.getLeader() == memberId);
    }

    public static int getPlacardState(int memberId)
    {
        return ((isLeader(memberId) ? 4 : 0) + memberState.getOrDefault(memberId, 0));
    }
}
