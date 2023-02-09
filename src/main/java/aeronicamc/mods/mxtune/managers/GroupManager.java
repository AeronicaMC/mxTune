package aeronicamc.mods.mxtune.managers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class GroupManager
{
    private static final Map<Integer, Group> groups = new HashMap<>();

    /**
     * Any player can be a leader or in a group. A player who makes a group is
     * the leader of the group. A player in a group can't join another group.
     * If the leader leaves a group another member will be promoted to group
     * leader automatically.
     *
     * @param leader of a group
     */
    public static void addGroup(int leader)
    {
        Group group = new Group(leader);
        groups.put(group.getGroupId(), group);
    }

    public static void addMember(int groupId, @Nullable PlayerEntity member)
    {
        Group group = groups.getOrDefault(groupId, Group.EMPTY);
        if (groups.isEmpty() || group.isEmpty() || member == null) return;

        PlayerEntity leader = (PlayerEntity) member.level.getEntity(group.getLeader());

        if (leader != null && isNotMemberOfAnyGroup(member.getId()))
        {
            if (group.notFull())
            {
                group.addMember(member.getId());
                member.sendMessage(new TranslationTextComponent("mxtune.chat.groupManager.you_joined_players_group", leader.getDisplayName()), member.getUUID());
                leader.sendMessage(new TranslationTextComponent("mxtune.chat.groupManager.player_joined_the_group", member.getDisplayName()), leader.getUUID());
            }
            else
            {
                member.sendMessage(new TranslationTextComponent("mxtune.chat.groupManager.cannot_join_too_many", leader.getDisplayName()), member.getUUID());
                leader.sendMessage(new TranslationTextComponent("mxtune.chat.groupManager.player_cannot_join_too_many", member.getDisplayName()), leader.getUUID());
            }
        }
        else
        {
            member.sendMessage(new TranslationTextComponent("mxtune.chat.groupManager.cannot_join_if_group_member" ), member.getUUID());
        }
    }

    /**
     * Search all groups for the memberID.
     *
     * @param memberId member to search for
     * @return true if the memberID is not found.
     */
    private static boolean isNotMemberOfAnyGroup(int memberId)
    {
        boolean result = true;
        for (Group group : groups.values())
            if (group.getMembers().contains(memberId))
                {
                    result = false;
                    break;
                }
        return result;
    }
}
