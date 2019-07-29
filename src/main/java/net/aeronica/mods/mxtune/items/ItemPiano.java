/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese a.k.a. Aeronica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.aeronica.mods.mxtune.items;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.blocks.BlockPiano;
import net.aeronica.mods.mxtune.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemPiano extends Item
{
    public ItemPiano()
    {
        setMaxStackSize(1);
        setCreativeTab(MXTune.TAB);
    }

    /** Called when a Block is right-clicked with this Item */
    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onItemUse(PlayerEntity playerIn, World worldIn, BlockPos posIn, Hand handIn, Direction facingIn, float hitX, float hitY, float hitZ)
    {
        BlockPos pos = posIn;
        if (worldIn.isRemote)
        {
            /* Client side so just return */
            return ActionResultType.SUCCESS;
        } else if (facingIn != Direction.UP)
        {
            /* Can't place the blocks this way */
            return ActionResultType.FAIL;
        } else
        {
            BlockState iblockstate = worldIn.getBlockState(pos);
            Block block = iblockstate.getBlock();
            ItemStack stack = playerIn.getHeldItem(handIn);
            /* Looking at the ground or a replaceable block like grass. */
            boolean flag = block.isReplaceable(worldIn, pos);
            if (!flag) pos = pos.up();

            /* determine the direction the player is facing */
            int i = MathHelper.floor((double) (playerIn.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            Direction enumfacing = Direction.byHorizontalIndex(i);
            /* get the next block in line. */
            BlockPos blockpos = pos.offset(enumfacing);

            /* pos = block at cursor; blockpos = one block beyond the cursor in direction player is facing */
            if (playerIn.canPlayerEdit(pos, facingIn, stack) && playerIn.canPlayerEdit(blockpos, facingIn, stack))
            {
                boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
                boolean flag2 = flag || worldIn.isAirBlock(pos);
                boolean flag3 = flag1 || worldIn.isAirBlock(blockpos);

                /* Disallow placing blocks on water or other unstable blocks */
                if (flag2 && flag3 && worldIn.getBlockState(pos.down()).isFullCube() && worldIn.getBlockState(blockpos.down()).isFullCube())
                {
                    BlockState iBlockState01 = ModBlocks.SPINET_PIANO.getDefaultState().withProperty(BlockPiano.OCCUPIED, Boolean.FALSE).withProperty(BlockPiano.FACING, enumfacing)
                            .withProperty(BlockPiano.PART, BlockPiano.EnumPartType.LEFT);

                    if (worldIn.setBlockState(pos, iBlockState01, 11))
                    {
                        BlockState iBlockState02 = iBlockState01.withProperty(BlockPiano.PART, BlockPiano.EnumPartType.RIGHT);
                        worldIn.setBlockState(blockpos, iBlockState02, 11);
                    }

                    SoundType soundtype = iBlockState01.getBlock().getSoundType();
                    worldIn.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    stack.setCount(stack.getCount()-1);
                    return ActionResultType.SUCCESS;
                } else
                {
                    return ActionResultType.FAIL;
                }
            } else
            {
                return ActionResultType.FAIL;
            }
        }
    }
}
