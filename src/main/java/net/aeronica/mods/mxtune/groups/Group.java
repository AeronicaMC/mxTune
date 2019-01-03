/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.groups;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Group
{
    public static final Group EMPTY = new Group();
    private Integer groupID;
    private Integer playID;
    private int longestDuration;
    private Integer leaderEntityID;
    private Set<Member> members;

    // The EMPTY group
    private Group() { this.members = Collections.unmodifiableSet(Collections.emptySet()); }

    public Group(Integer groupID, Integer leaderEntityID)
    {
        this.groupID = groupID;
        this.leaderEntityID = leaderEntityID;
        this.members = new HashSet<>(GroupHelper.MAX_MEMBERS);
        this.longestDuration = 0;
    }

    public Integer getGroupID() { return groupID; }

    void setLeaderEntityID(Integer leaderEntityID)
    {
        this.leaderEntityID = leaderEntityID;
    }

    Integer getLeaderEntityID() { return leaderEntityID; }

    Set<Member> getMembers() { return members; }

    void addMember(Member member) { members.add(member); }

    public Integer getPlayID() { return playID; }

    public void setPlayID(Integer playID) { this.playID = playID; }

    void inputPartDuration(int duration) { if (duration > longestDuration) longestDuration = duration; }

    int getLongestDuration() { return longestDuration; }

    boolean isEmpty() { return this == EMPTY; }
}