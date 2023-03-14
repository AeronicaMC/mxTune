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

    public static void clear()
    {
        synchronized (groupMap)
        {
            groupMap.clear();
        }
    }

    public static void setGroupMap(Map<Integer, Group> pGroupMap)
    {
        synchronized (groupMap)
        {
            LOGGER.debug("Apply GroupMap Server/Client: {}/{}", pGroupMap.size(), groupMap.size());
            groupMap.forEach((id, group) -> LOGGER.debug("Before  {}", group));
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
            groupMap.forEach((id, group) -> LOGGER.debug("After  {}", group));
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

    public static byte getPlacardState(int memberId)
    {
        return (byte) ((isLeader(memberId) ? 4 : 0));
    }
}
