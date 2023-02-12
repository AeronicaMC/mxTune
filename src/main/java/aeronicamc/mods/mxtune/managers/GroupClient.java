package aeronicamc.mods.mxtune.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupClient
{
    private static final Logger LOGGER = LogManager.getLogger(GroupClient.class);
    private static final Map<Integer, Group> groupMap = new ConcurrentHashMap<>();

    public static void setGroupMap(Map<Integer, Group> pGroupMap)
    {
        synchronized (groupMap)
        {
            groupMap.clear();
            groupMap.putAll(pGroupMap);
        }
        LOGGER.debug("setGroupMap {}", groupMap.size());
    }
}
