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
import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.util.EnumRelativeSide;
import net.aeronica.mods.mxtune.world.LockableHelper;
import net.aeronica.mods.mxtune.world.OwnerUUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
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
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.Random;

import static net.aeronica.mods.mxtune.blocks.BlockPiano.spawnEntityItem;

@SuppressWarnings("deprecation")
public class BlockBandAmp extends BlockHorizontal implements IMusicPlayer
{
    private static final PropertyBool PLAYING = PropertyBool.create("playing");
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final PropertyInteger UPDATE_COUNT = PropertyInteger.create("update_count", 0, 15);

    public BlockBandAmp()
    {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(PLAYING, Boolean.FALSE).withProperty(POWERED, Boolean.FALSE).withProperty(UPDATE_COUNT, 0));
        this.setSoundType(SoundType.WOOD);
        this.setHardness(2.0F);
        this.disableStats();
        this.needsRandomTick = true;
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
            if (playerIn.isSneaking() || LockableHelper.isLocked(playerIn, worldIn, pos))
            {
                boolean isPlaying = canPlayOrStopMusic(worldIn, pos, false);
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

    private boolean canPlayOrStopMusic(World worldIn, BlockPos pos, Boolean stop)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileBandAmp)
        {
            TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
            if (PlayManager.isActivePlayID(tileBandAmp.getPlayID()))
            {
                PlayManager.stopPlayID(tileBandAmp.getPlayID());
                tileBandAmp.setPlayID(-1);
                return false;
            }
            if (!stop)
            {
                Integer playID = PlayManager.playMusic(worldIn, pos, tileBandAmp.getSoundRange());
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
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if ((tileEntity instanceof TileBandAmp))
            {
                TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
                if (state.getValue(PLAYING))
                {
                    if (!PlayManager.isActivePlayID(tileBandAmp.getPlayID()))
                        setPlayingState(worldIn, pos, state, false);

                    worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
                } else
                    onePulseOutputState(worldIn, pos, state, tileBandAmp);
            }
        }
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if(!worldIn.isBlockLoaded(pos)) return;

        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof TileBandAmp)
        {
            TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
            tileBandAmp.clientSideNotify();
        }
    }

    /** Pulse the output power state once when playing stops and only for a valid playID. i.e. playID > 0 */
    private void onePulseOutputState(World worldIn, BlockPos pos, IBlockState state, TileBandAmp tileBandAmp)
    {
        if(!state.getValue(POWERED) && tileBandAmp.lastPlayIDSuccess())
        {
            setOutputPowerState(worldIn, pos, state, true);
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
            tileBandAmp.clearLastPlayID();
        } else if (state.getValue(POWERED))
            setOutputPowerState(worldIn, pos, state, false);
    }

    /**
     * React to a redstone powered neighbor block
     */
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        // get redstone input from the back side
        boolean inSidePowered = worldIn.isSidePowered(pos.offset(state.getValue(FACING).getOpposite()), state.getValue(FACING).getOpposite());
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof TileBandAmp)
        {
            TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
            boolean inputEnabled = tileBandAmp.isRearRedstoneInputEnabled();
            if ((tileBandAmp.getPreviousInputState() != inSidePowered) && inputEnabled)
            {
                if (inSidePowered)
                {
                    boolean isPlaying = canPlayOrStopMusic(worldIn, pos, false);
                    setPlayingState(worldIn, pos, state, isPlaying);
                    worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
                }
                tileBandAmp.setPreviousInputState(inSidePowered);
            }
        }
    }

    private void setPlayingState(World worldIn, BlockPos posIn, IBlockState state, boolean playing)
    {
        boolean currentPlayingState = state.getValue(PLAYING);
        if (currentPlayingState != playing)
        {
            worldIn.setBlockState(posIn, worldIn.getBlockState(posIn).withProperty(PLAYING, playing), 2);
            worldIn.markBlockRangeForRenderUpdate(posIn, posIn);
        }
    }

    private void setOutputPowerState(World worldIn, BlockPos posIn, IBlockState state, boolean outputPowerState)
    {
        boolean currentOutputPowerState = state.getValue(POWERED);
        if (currentOutputPowerState != outputPowerState)
        {
            worldIn.setBlockState(posIn, worldIn.getBlockState(posIn).withProperty(POWERED, outputPowerState), 3);
            worldIn.markBlockRangeForRenderUpdate(posIn, posIn);
        }
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, PLAYING, POWERED, UPDATE_COUNT);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(PLAYING, Boolean.FALSE).withProperty(POWERED, Boolean.FALSE).withProperty(UPDATE_COUNT, 0);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(PLAYING, Boolean.FALSE).withProperty(POWERED, Boolean.FALSE).withProperty(UPDATE_COUNT, 0);

        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileBandAmp) {
            TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
            OwnerUUID ownerUUID = new OwnerUUID(placer.getPersistentID());
            tileBandAmp.setOwner(ownerUUID);

            if (stack.hasDisplayName())
                tileBandAmp.setCustomInventoryName(stack.getDisplayName());
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity tileEntity = worldIn instanceof ChunkCache ? ((ChunkCache)worldIn).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : worldIn.getTileEntity(pos);

        int count = 0;
        if(tileEntity instanceof TileBandAmp)
        {
            TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
            count = tileBandAmp.getUpdateCount();
        }
        return super.getActualState(state, worldIn, pos).withProperty(UPDATE_COUNT, count);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.byHorizontalIndex(meta & 3);
        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(PLAYING, (meta & 8) > 0).withProperty(POWERED, (meta & 4) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(POWERED)) i |= 4;
        if (state.getValue(PLAYING)) i |= 8;
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
        canPlayOrStopMusic(worldIn, pos, true);
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof TileBandAmp)
        {
            TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
            for (int slot = 0; slot < tileBandAmp.getInventory().getSlots(); slot++)
            {
                spawnEntityItem(worldIn, tileBandAmp.getInventory().getStackInSlot(slot), pos);
            }
            tileBandAmp.invalidate();
        }
        worldIn.notifyNeighborsOfStateChange(pos, this, false);
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

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
    {
        EnumRelativeSide relativeSide = EnumRelativeSide.getRelativeSide(face, state.getValue(FACING));
        return relativeSide == EnumRelativeSide.LEFT || relativeSide == EnumRelativeSide.RIGHT ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side)
    {
        boolean canConnect = false;
        if (side != null)
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof TileBandAmp)
            {
                TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
                EnumRelativeSide relativeSide = EnumRelativeSide.getRelativeSide(side.getOpposite(), state.getValue(FACING));
                boolean canConnectBack = tileBandAmp.isRearRedstoneInputEnabled() && relativeSide == EnumRelativeSide.BACK;
                boolean canConnectLeft = tileBandAmp.isLeftRedstoneOutputEnabled() && relativeSide == EnumRelativeSide.LEFT;
                boolean canConnectRight = tileBandAmp.isRightRedstoneOutputEnabled() && relativeSide == EnumRelativeSide.RIGHT;
                canConnect = canConnectBack || canConnectLeft || canConnectRight;
            }
        }
        return canConnect;
    }

    @Override
    public boolean canProvidePower(IBlockState state)
    {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        int weakPower = 0;
        TileEntity tileEntity = blockAccess.getTileEntity(pos);
        if(tileEntity instanceof TileBandAmp)
        {
            TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
            EnumRelativeSide relativeSide = EnumRelativeSide.getRelativeSide(side.getOpposite(), blockState.getValue(FACING));
            boolean canConnectLeft = tileBandAmp.isLeftRedstoneOutputEnabled() && relativeSide == EnumRelativeSide.LEFT;
            boolean canConnectRight = tileBandAmp.isRightRedstoneOutputEnabled() && relativeSide == EnumRelativeSide.RIGHT;
            weakPower = blockState.getValue(POWERED) && (canConnectLeft || canConnectRight) ? 15 : 0;
        }
        return weakPower;
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return blockState.getWeakPower(blockAccess, pos, side);
    }
}
