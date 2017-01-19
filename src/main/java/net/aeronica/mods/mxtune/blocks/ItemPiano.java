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
package net.aeronica.mods.mxtune.blocks;

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemPiano extends Item
{

    public ItemPiano()
    {
        setMaxStackSize(1);
        setCreativeTab(MXTuneMain.TAB_MUSIC);
    }

    /** Called when a Block is right-clicked with this Item */
    @SuppressWarnings("deprecation")
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            /** Client side so just return */
            return EnumActionResult.SUCCESS;
        } else if (facing != EnumFacing.UP)
        {
            /** Can't place the blocks this way */
            return EnumActionResult.FAIL;
        } else
        {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            Block block = iblockstate.getBlock();

            /** Looking at the ground or a replaceable block like grass. */
            boolean flag = block.isReplaceable(worldIn, pos);
            if (!flag) pos = pos.up();

            /**determine the direction the player is facing */
            int i = MathHelper.floor_double((double) (playerIn.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            EnumFacing enumfacing = EnumFacing.getHorizontal(i);
            /**get the next block in line. */
            BlockPos blockpos = pos.offset(enumfacing);

            /** pos = block at cursor; blockpos = one block beyond the cursor in direction player is facing */
            if (playerIn.canPlayerEdit(pos, facing, stack) && playerIn.canPlayerEdit(blockpos, facing, stack))
            {
                boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
                boolean flag2 = flag || worldIn.isAirBlock(pos);
                boolean flag3 = flag1 || worldIn.isAirBlock(blockpos);

                /** Disallow placing blocks on water or other unstable blocks */
                if (flag2 && flag3 && worldIn.getBlockState(pos.down()).isFullyOpaque() && worldIn.getBlockState(blockpos.down()).isFullyOpaque())
                {
                    IBlockState iblockstate1 = ModBlocks.BLOCK_PIANO.getDefaultState().withProperty(BlockPiano.OCCUPIED, Boolean.valueOf(false)).withProperty(BlockPiano.FACING, enumfacing)
                            .withProperty(BlockPiano.PART, BlockPiano.EnumPartType.LEFT);

                    if (worldIn.setBlockState(pos, iblockstate1, 11))
                    {
                        IBlockState iblockstate2 = iblockstate1.withProperty(BlockPiano.PART, BlockPiano.EnumPartType.RIGHT);
                        worldIn.setBlockState(blockpos, iblockstate2, 11);
                    }

                    SoundType soundtype = iblockstate1.getBlock().getSoundType();
                    worldIn.playSound((EntityPlayer) null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    --stack.stackSize;
                    return EnumActionResult.SUCCESS;
                } else
                {
                    return EnumActionResult.FAIL;
                }
            } else
            {
                return EnumActionResult.FAIL;
            }
        }
    }
}
