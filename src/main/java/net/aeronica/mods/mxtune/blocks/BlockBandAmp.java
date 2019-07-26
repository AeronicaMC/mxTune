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

/*
package net.aeronica.mods.mxtune.blocks;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.gui.GuiGuid;
import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.managers.PlayManager;
import net.aeronica.mods.mxtune.util.EnumRelativeSide;
import net.aeronica.mods.mxtune.world.LockableHelper;
import net.aeronica.mods.mxtune.world.OwnerUUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

import static net.aeronica.mods.mxtune.blocks.BlockPiano.spawnEntityItem;

@SuppressWarnings("deprecation")
public class BlockBandAmp extends HorizontalBlock implements IMusicPlayer
{
    private static final PropertyBool PLAYING = PropertyBool.create("playing");
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    public static final PropertyInteger UPDATE_COUNT = PropertyInteger.create("update_count", 0, 15);

    public BlockBandAmp()
    {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, Direction.NORTH).withProperty(PLAYING, Boolean.FALSE).withProperty(POWERED, Boolean.FALSE).withProperty(UPDATE_COUNT, 0));
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

    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) { return ModItems.ITEM_BAND_AMP; }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, float hitX, float hitY, float hitZ)
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
                playerIn.openGui(MXTune.instance, GuiGuid.GUI_BAND_AMP, worldIn, pos.getX(), pos.getY(), pos.getZ());
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
                Integer playID = PlayManager.playMusic(worldIn, pos);
                tileBandAmp.setPlayID(playID);
            }
        }
        return true;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, BlockState state, Random rand)
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
                    {
                        setPlayingState(worldIn, pos, state, false);
                        tileBandAmp.setPlayID(-1);
                    }

                    worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
                } else
                    onePulseOutputState(worldIn, pos, state, tileBandAmp);
            }
        }
    }

    @Override
    public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if(!worldIn.isBlockLoaded(pos)) return;

        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (tileEntity instanceof TileBandAmp)
        {
            TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
            tileBandAmp.clientSideNotify();
        }
    }

    // Pulse the output power state once when playing stops and only for a valid playID. i.e. playID > 0
    private void onePulseOutputState(World worldIn, BlockPos pos, BlockState state, TileBandAmp tileBandAmp)
    {
        if(!state.getValue(POWERED) && tileBandAmp.lastPlayIDSuccess())
        {
            setOutputPowerState(worldIn, pos, state, true);
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
            tileBandAmp.clearLastPlayID();
        } else if (state.getValue(POWERED))
            setOutputPowerState(worldIn, pos, state, false);
    }

    // React to a redstone powered neighbor block
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
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

    private void setPlayingState(World worldIn, BlockPos posIn, BlockState state, boolean playing)
    {
        boolean currentPlayingState = state.getValue(PLAYING);
        if (currentPlayingState != playing)
        {
            worldIn.setBlockState(posIn, worldIn.getBlockState(posIn).withProperty(PLAYING, playing), 2);
            worldIn.markBlockRangeForRenderUpdate(posIn, posIn);
        }
    }

    private void setOutputPowerState(World worldIn, BlockPos posIn, BlockState state, boolean outputPowerState)
    {
        boolean currentOutputPowerState = state.getValue(POWERED);
        if (currentOutputPowerState != outputPowerState)
        {
            worldIn.setBlockState(posIn, worldIn.getBlockState(posIn).withProperty(POWERED, outputPowerState), 3);
            worldIn.markBlockRangeForRenderUpdate(posIn, posIn);
        }
    }

    // TODO: Complete Emissive blockstate and models for BandAmp
    @SideOnly(Side.CLIENT)
    @Override
    public int getLightValue(BlockState state)
    {
        // return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.SOLID && state.getValue(PLAYING) ? 15 : super.getLightValue(state);
        return super.getLightValue(state);
    }

    @Override
    public BlockState withRotation(BlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState withMirror(BlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, PLAYING, POWERED, UPDATE_COUNT);
    }

    @Override
    public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(PLAYING, Boolean.FALSE).withProperty(POWERED, Boolean.FALSE).withProperty(UPDATE_COUNT, 0);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
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
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity tileEntity = worldIn instanceof ChunkCache ? ((ChunkCache)worldIn).getTileEntity(pos, CreateEntityType.CHECK) : worldIn.getTileEntity(pos);

        int count = 0;
        if(tileEntity instanceof TileBandAmp)
        {
            TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
            count = tileBandAmp.getUpdateCount();
        }
        return super.getActualState(state, worldIn, pos).withProperty(UPDATE_COUNT, count);
    }

    @Override
    public BlockState getStateFromMeta(int meta)
    {
        Direction enumfacing = Direction.byHorizontalIndex(meta & 3);
        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(PLAYING, (meta & 8) > 0).withProperty(POWERED, (meta & 4) > 0);
    }

    @Override
    public int getMetaFromState(BlockState state)
    {
        int i = 0;
        i = i | state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(POWERED)) i |= 4;
        if (state.getValue(PLAYING)) i |= 8;
        return i;
    }

    @Override
    public boolean hasTileEntity(BlockState state) { return true; }

    @Override
    public TileEntity createTileEntity(World world, BlockState state)
    {
        return new TileBandAmp(state.getValue(FACING));
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, BlockState state)
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


     // Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
     // Block.removedByPlayer. If the te has a custom name the returned itemBlock will receive the custom name.

    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack)
    {
        if (te instanceof INameable && ((INameable)te).hasCustomName())
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
            itemstack.setStackDisplayName(((INameable)te).getName());
            spawnAsEntity(worldIn, pos, itemstack);
        }
        else
        {
            super.harvestBlock(worldIn, player, pos, state, null, stack);
        }
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockState state, BlockPos pos, Direction face)
    {
        EnumRelativeSide relativeSide = EnumRelativeSide.getRelativeSide(face, state.getValue(FACING));
        return relativeSide == EnumRelativeSide.LEFT || relativeSide == EnumRelativeSide.RIGHT ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockAccess world, BlockPos pos, @Nullable Direction side)
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
    public boolean canProvidePower(BlockState state)
    {
        return true;
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side)
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
    public int getStrongPower(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side)
    {
        return blockState.getWeakPower(blockAccess, pos, side);
    }
}

*/
