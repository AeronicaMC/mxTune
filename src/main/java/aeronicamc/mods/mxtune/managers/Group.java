package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Group
{
    public static final Group EMPTY = new Group();
    private static final AtomicInteger nextGroup = new AtomicInteger(0);
    private final Set<Integer> members;
    private int groupID;
    private int playID;
    private int maxDuration;
    private int leader;

    private Group() {this.members = Collections.emptySet();}

    public Group(int leader)
    {
        this.members = new HashSet<>(Reference.MAX_MML_PARTS);
        this.members.add(leader);
        this.groupID = nextGroup.incrementAndGet();
        this.leader = leader;
        this.maxDuration = 0;
        playID = PlayIdSupplier.INVALID;
    }

    public int getGroup() {return groupID;}

    int getLeader()
    {
        return leader;
    }

    void setLeader(int leader)
    {
        this.leader = leader;
    }

    Set<Integer> getMembers()
    {
        return members;
    }

    Integer[] getMembersAsIntegerArray()
    {
        return members.toArray(new Integer[0]);
    }

    void addMember(int member)
    {
        members.add(member);
    }

    boolean isMember(int entityId)
    {
        return this.members.contains(entityId);
    }

    public int getPlayID()
    {
        return playID;
    }

    public void setPlayID(int playID)
    {
        this.playID = playID;
    }

    void setPartDuration(int duration)
    {
        if (duration > maxDuration) maxDuration = duration;
    }

    int getMaxDuration()
    {
        return maxDuration;
    }

    boolean isEmpty()
    {
        return this == EMPTY;
    }
}
