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

    public static boolean isGrouped(int memberId)
    {
        boolean[] isGrouped = new boolean[1];
        groupMap.values().forEach(group -> {
            if (group.isMember(memberId))
                isGrouped[0] = true;
        });
        return isGrouped[0];
    }
}
