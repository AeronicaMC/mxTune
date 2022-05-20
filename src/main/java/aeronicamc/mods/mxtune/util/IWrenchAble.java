package aeronicamc.mods.mxtune.util;

import aeronicamc.mods.mxtune.init.ModTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * Allow wrenches to interact with a Block.
 * <p>
 * Apply to EntityBlock (Block) where applicable.
 */
public interface IWrenchAble
{
    /**
     * Convenience method to check if the item used by a player is a wrench
     * @param player using the wrench
     * @param handIn the active hand
     * @return true if the item has the standard forge item tag for wrenches
     */
    default boolean hasWrench(PlayerEntity player, Hand handIn)
    {
        return (!player.getItemInHand(handIn).isEmpty() && player.getItemInHand(handIn).getItem().is(ModTags.Items.TOOLS_WRENCH));
    }

    /**
     * Convenience method that returns whether the block is WrenchAble or not.
     * @return true if WrenchAble
     */
    default boolean canWrench()
    {
        return true;
    }
}
