package aeronicamc.mods.mxtune.managers;

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

    public static void clear()
    {
        synchronized (groupMap)
        {
            groupMap.clear();
            memberState.clear();
        }
    }

    public static void setGroupMap(Map<Integer, Group> pGroupMap)
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
    }

    public static void setMemberState(Map<Integer, Integer> memberStateIn)
    {
        synchronized (memberState)
        {
            LOGGER.debug("-----");
            LOGGER.debug("Apply memberState Server/Client: {}/{}", memberStateIn.size(), memberState.size());
            memberState.forEach((id, state) -> LOGGER.debug("  Before  {}", state));
            memberState.forEach((member, state) -> LOGGER.debug("    member: {} state: {}", member, state));
            Set<Integer> serverKeys = memberStateIn.keySet();
            Set<Integer> removeKeys = new HashSet<>();
            memberStateIn.forEach((kS, vS) -> {
                if (memberState.get(kS) != null && !memberState.get(kS).equals(memberStateIn.get(kS)))
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
    }

    /**
     * @param memberId search all groups for thia member.
     * @return the Group or the Group.EMPTY.
     */
    public static Group getMembersGroup(int memberId)
    {
        return groupMap.values().stream().filter(group -> group.isMember(memberId)).findFirst().orElse(Group.EMPTY);
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
        int state = memberState.getOrDefault(memberId, 0);
        return ((isLeader(memberId) ? 4 : 0) + state);
    }
}
