package aeronicamc.mods.mxtune.items;

import aeronicamc.mods.mxtune.entity.MusicVenueInfoEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MusicVenueInfoItem extends Item
{

    public MusicVenueInfoItem(Properties pProperties)
    {
        super(pProperties);
    }

    /**
     * Called when this item is used when targetting a Block
     */
    public ActionResultType useOn(ItemUseContext pContext) {
        BlockPos blockpos = pContext.getClickedPos();
        Direction direction = pContext.getClickedFace();
        BlockPos blockpos1 = blockpos.relative(direction);
        PlayerEntity playerentity = pContext.getPlayer();
        ItemStack itemstack = pContext.getItemInHand();
        if (playerentity != null && !this.mayPlace(playerentity, direction, itemstack, blockpos1)) {
            return ActionResultType.FAIL;
        } else {
            World world = pContext.getLevel();
            HangingEntity hangingentity;
            hangingentity = new MusicVenueInfoEntity(world, blockpos1, direction);
            CompoundNBT compoundnbt = itemstack.getTag();
            if (compoundnbt != null) {
                EntityType.updateCustomEntityTag(world, playerentity, hangingentity, compoundnbt);
            }

            if (hangingentity.survives()) {
                if (!world.isClientSide) {
                    hangingentity.playPlacementSound();
                    world.addFreshEntity(hangingentity);
                }

                itemstack.shrink(1);
                return ActionResultType.sidedSuccess(world.isClientSide);
            } else {
                return ActionResultType.CONSUME;
            }
        }
    }

    protected boolean mayPlace(PlayerEntity pPlayer, Direction pDirection, ItemStack pHangingEntityStack, BlockPos pPos) {
        return !pDirection.getAxis().isVertical() && pPlayer.mayUseItemAt(pPos, pDirection, pHangingEntityStack);
    }
}
