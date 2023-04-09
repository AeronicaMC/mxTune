package aeronicamc.mods.mxtune.managers;

import aeronicamc.mods.mxtune.Reference;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
    private Mode mode;

    // Not shared with client.
    private String pin;

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

    public boolean isValid()
    {
        return this != EMPTY;
    }

    public Mode getMode()
    {
        return mode;
    }

    public void setMode(Mode mode)
    {
        this.mode = mode;
    }

    public String getPin()
    {
        return pin;
    }

    public void setPin(String pin)
    {
        this.pin = pin;
    }

    public enum Mode
    {
        Invite ("enum.mxtune.group.mode.invite", "enum.mxtune.group.mode.invite.help01", "enum.mxtune.group.mode.invite.help02", "enum.mxtune.group.mode.invite.help03"),
        Pin ("enum.mxtune.group.mode.pin", "enum.mxtune.group.mode.pin.help01", "enum.mxtune.group.mode.pin.help02", "enum.mxtune.group.mode.pin.help03"),
        Open("enum.mxtune.group.mode.open", "enum.mxtune.group.mode.open.help01", "enum.mxtune.group.mode.open.help02", "enum.mxtune.group.mode.open.help03");

        private final String modeKey;
        private final String help01Key;
        private final String help02Key;
        private final String help03Key;

        Mode(String modeKey, String help01Key, String help02Key, String help03Key)
        {
            this.modeKey = modeKey;
            this.help01Key = help01Key;
            this.help02Key = help02Key;
            this.help03Key = help03Key;
        }

        public String getModeKey()
        {
            return modeKey;
        }

        public String getHelp01Key()
        {
            return help01Key;
        }

        public String getHelp02Key()
        {
            return help02Key;
        }

        public String getHelp03Key()
        {
            return help03Key;
        }
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
                .append("Mode", mode)
                .append("Pin", pin)
                .toString();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(members)
                .append(groupId)
                .append(playId)
                .append(maxDuration)
                .append(leader)
                .append(mode)
                .append(pin)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Group group = (Group) o;
        return new EqualsBuilder()
                .append(members, group.members)
                .append(groupId, group.groupId)
                .append(playId, group.playId)
                .append(maxDuration, group.maxDuration)
                .append(leader, group.leader)
                .append(mode, group.mode)
                .append(pin, group.pin)
                .isEquals();
    }
}
