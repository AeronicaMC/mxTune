/*
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

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.groups.PlayManager;
import net.aeronica.mods.mxtune.gui.GuiBandAmp;
import net.aeronica.mods.mxtune.handler.GUIHandler;
import net.aeronica.mods.mxtune.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

import static net.aeronica.mods.mxtune.blocks.BlockPiano.spawnEntityItem;

@SuppressWarnings("deprecation")
public class BlockBandAmp extends BlockHorizontal implements IMusicPlayer
{
    public static final PropertyBool PLAYING = PropertyBool.create("playing");

    public BlockBandAmp()
    {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(PLAYING, Boolean.valueOf(false)));
        this.setSoundType(SoundType.WOOD);
        this.setHardness(2.0F);
        this.disableStats();
        this.setCreativeTab(MXTune.TAB_MUSIC);
    }

    @Override
    public int tickRate(World worldIn)
    {
        return 10;
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) { return ModItems.ITEM_BAND_AMP; }

    @Deprecated
    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) { return new ItemStack(ModItems.ITEM_BAND_AMP); }

    /**
     * Called when the block is right clicked by a player.
     */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
        {
            if (playerIn.isSneaking() || GUIHandler.isLocked(playerIn, worldIn, pos))
            {
                boolean isPlaying = canPlayOrStopMusic(worldIn, pos, state, false);
                setPlayingState(worldIn, pos, state, isPlaying);
                worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
            }
            else if (!playerIn.isSneaking() && playerIn.capabilities.allowEdit)
            {
                playerIn.openGui(MXTune.instance, GuiBandAmp.GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    private boolean canPlayOrStopMusic(World worldIn, BlockPos pos, IBlockState state, Boolean stop)
    {
        TileBandAmp tileBandAmp = this.getTE(worldIn, pos);
        if (tileBandAmp != null)
        {
            if (PlayManager.isActivePlayID(tileBandAmp.getPlayID()))
            {
                PlayManager.stopPlayID(tileBandAmp.getPlayID());
                tileBandAmp.setPlayID(-1);
                return false;
            }
            if (!stop)
            {
                Integer playID = PlayManager.playMusic(worldIn, pos);
                tileBandAmp.setPlayID(playID);
            }
        }
        return true;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isRemote)
        {
            TileBandAmp tileBandAmp = this.getTE(worldIn, pos);
            if (tileBandAmp != null && state.getValue(PLAYING).booleanValue())
            {
                if (!PlayManager.isActivePlayID(tileBandAmp.getPlayID()))
                    setPlayingState(worldIn, pos, state, false);
                else
                    worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
            }
        }
    }

    /**
     * React to a redstone powered neighbor block
     */
    @Deprecated
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        boolean powered = worldIn.isBlockPowered(pos);
        TileBandAmp tileBandAmp = getTE(worldIn, pos);

        if ((tileBandAmp != null))
        {
            if (tileBandAmp.getPreviousRedStoneState() != powered)
            {
                if (powered)
                {
                    boolean isPlaying = canPlayOrStopMusic(worldIn, pos, state, false);
                    setPlayingState(worldIn, pos, state, isPlaying);
                    tileBandAmp.setPowered(state, worldIn, pos, blockIn, fromPos);
                    worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
                }
                tileBandAmp.setPreviousRedStoneState(powered);
            }
        }
    }

    private void setPlayingState(World worldIn, BlockPos posIn, IBlockState state, boolean playing)
    {
        boolean currentPlayingState = state.getValue(PLAYING).booleanValue();
        if (currentPlayingState != playing)
        {
            worldIn.setBlockState(posIn, worldIn.getBlockState(posIn).withProperty(PLAYING, Boolean.valueOf(playing)), 2);
            worldIn.markBlockRangeForRenderUpdate(posIn, posIn);
        }
    }

    @Deprecated
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
    }

    @Deprecated
    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {FACING, PLAYING});
    }

    @Deprecated
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(PLAYING, Boolean.valueOf(false));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(PLAYING, Boolean.valueOf(false));

        if (stack.hasDisplayName())
        {
            TileBandAmp tileBandAmp = getTE(worldIn, pos);
            if (tileBandAmp != null)
                tileBandAmp.setCustomInventoryName(stack.getDisplayName());
        }
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.byHorizontalIndex(meta & 3);
        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(PLAYING, Boolean.valueOf((meta & 8) > 0));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | ((EnumFacing) state.getValue(FACING)).getHorizontalIndex();

        if (state.getValue(PLAYING).booleanValue())
        {
            i |= 8;
        }
        return i;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileBandAmp(state.getValue(FACING));
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        canPlayOrStopMusic(worldIn, pos, state, true);

        TileBandAmp tile = (TileBandAmp) worldIn.getTileEntity(pos);
        if (tile != null)
        {
            for (int slot = 0; slot < tile.getInventory().getSlots(); slot++)
            {
                spawnEntityItem(worldIn, tile.getInventory().getStackInSlot(slot), pos);
            }
            tile.invalidate();
        }
        super.breakBlock(worldIn, pos, state);
    }

    /**
     * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
     * Block.removedByPlayer. If the te has a custom name the returned itemBlock will receive the custom name.
     */
    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack)
    {
        if (te instanceof IWorldNameable && ((IWorldNameable)te).hasCustomName())
        {
            player.addExhaustion(0.005F);

            if (worldIn.isRemote)
            {
                return;
            }

            int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
            Item item = this.getItemDropped(state, worldIn.rand, i);

            if (item == Items.AIR)
            {
                return;
            }

            ItemStack itemstack = new ItemStack(item, this.quantityDropped(worldIn.rand));
            itemstack.setStackDisplayName(((IWorldNameable)te).getName());
            spawnAsEntity(worldIn, pos, itemstack);
        }
        else
        {
            super.harvestBlock(worldIn, player, pos, state, null, stack);
        }
    }
//    @Deprecated
//    @Override
//    public boolean isFullCube(IBlockState state) { return false; }
//
//    @Deprecated
//    @Override
//    public boolean isOpaqueCube(IBlockState state) { return false; }
}
