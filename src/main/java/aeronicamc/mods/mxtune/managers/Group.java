package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Group
{
    public static final Group EMPTY = new Group();
    private static final AtomicInteger nextGroup = new AtomicInteger(0);
    private final Set<Integer> members;
    private int groupId;
    private int playId;
    private int maxDuration;
    private int leader;

    private Group()
    {
        this.members = Collections.emptySet();
    }

    // For network messages
    public Group(int groupId, int leader)
    {
        this.members = new HashSet<>(Reference.MAX_MML_PARTS);
        this.members.add(leader);
        this.groupId = groupId;
        this.leader = leader;
    }

    public Group(int leader)
    {
        this.members = new HashSet<>(Reference.MAX_MML_PARTS);
        this.members.add(leader);
        this.groupId = nextGroup.incrementAndGet();
        this.leader = leader;
        this.maxDuration = 0;
        playId = PlayIdSupplier.INVALID;
    }

    public int getGroupId() {return groupId;}

    public int getLeader()
    {
        return leader;
    }

    public void setLeader(int leader)
    {
        this.leader = leader;
    }

    public boolean notFull()
    {
        return members.size() < Reference.MAX_MML_PARTS;
    }

    public Set<Integer> getMembers()
    {
        return members;
    }

    public int[] getMembersAsIntArray()
    {
        return Ints.toArray(members);
    }

    public void addMember(int member)
    {
        members.add(member);
    }

    public void removeMember(int member)
    {
        members.remove(member);
    }

    public boolean isMember(int entityId)
    {
        return this.members.contains(entityId);
    }

    public int getPlayId()
    {
        return playId;
    }

    public void setPlayId(int playId)
    {
        this.playId = playId;
    }

    public void setPartDuration(int duration)
    {
        if (duration > maxDuration) maxDuration = duration;
    }

    public int getMaxDuration()
    {
        return maxDuration;
    }

    public void resetMaxDuration()
    {
        maxDuration = 0;
    }

    public boolean isEmpty()
    {
        return this == EMPTY;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("groupId", groupId)
                .append("playId", playId)
                .append("maxDuration", maxDuration)
                .append("leader", leader)
                .append("members", members)
                .toString();
    }
}
