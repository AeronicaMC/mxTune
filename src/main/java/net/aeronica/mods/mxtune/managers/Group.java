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

package net.aeronica.mods.mxtune.managers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Group
{
    public static final Group EMPTY = new Group();
    private int groupID;
    private int playID;
    private int maxDuration;
    private int leaderEntityID;
    private Set<Member> members;

    // The EMPTY group
    private Group() { this.members = Collections.unmodifiableSet(Collections.emptySet()); }

    public Group(int groupID, int leaderEntityID)
    {
        this.groupID = groupID;
        this.leaderEntityID = leaderEntityID;
        this.members = new HashSet<>(GroupHelper.MAX_MEMBERS);
        this.maxDuration = 0;
        playID = PlayIdSupplier.PlayType.INVALID;
    }

    public int getGroupID() { return groupID; }

    void setLeaderEntityID(Integer leaderEntityID)
    {
        this.leaderEntityID = leaderEntityID;
    }

    int getLeaderEntityID() { return leaderEntityID; }

    Set<Member> getMembers() { return members; }

    void addMember(Member member) { members.add(member); }

    public int getPlayID() { return playID; }

    public void setPlayID(int playID) { this.playID = playID; }

    void setPartDuration(int duration) { if (duration > maxDuration) maxDuration = duration; }

    int getMaxDuration() { return maxDuration; }

    boolean isEmpty() { return this == EMPTY; }
}