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

import com.google.common.collect.Lists;
import net.aeronica.mods.mxtune.advancements.ModCriteriaTriggers;
import net.aeronica.mods.mxtune.entity.EntitySittableBlock;
import net.aeronica.mods.mxtune.gui.hud.GuiJamOverlay;
import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.inventory.IMusic;
import net.aeronica.mods.mxtune.managers.PlayManager;
import net.aeronica.mods.mxtune.status.ServerCSDManager;
import net.aeronica.mods.mxtune.util.PlacedInstrumentUtil;
import net.aeronica.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockPiano extends BlockHorizontal implements IPlacedInstrument
{
    public static final PropertyEnum<BlockPiano.EnumPartType> PART = PropertyEnum.create("part", BlockPiano.EnumPartType.class);
    public static final PropertyBool OCCUPIED = PropertyBool.create("occupied");
    private static final AxisAlignedBB PIANO_BODY_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB MUSIC_RACK_AABB_NW = new AxisAlignedBB(0.0D, 1.0D, 0.0D, 0.5D, 1.5D, 0.5D);
    private static final AxisAlignedBB MUSIC_RACK_AABB_SW = new AxisAlignedBB(0.0D, 1.0D, 0.5D, 0.5D, 1.5D, 1.0D);
    private static final AxisAlignedBB MUSIC_RACK_AABB_NE = new AxisAlignedBB(0.5D, 1.0D, 0.0D, 1.0D, 1.5D, 0.5D);
    private static final AxisAlignedBB MUSIC_RACK_AABB_SE = new AxisAlignedBB(0.5D, 1.0D, 0.5D, 1.0D, 1.5D, 1.0D);

    public BlockPiano()
    {
        super(Material.WOOD);
        setDefaultState(this.blockState.getBaseState().withProperty(PART, BlockPiano.EnumPartType.LEFT).withProperty(OCCUPIED, Boolean.FALSE));
        this.setSoundType(SoundType.WOOD);
        this.setHardness(0.8F);
        this.disableStats();

    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos posIn, IBlockState stateIn, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        BlockPos pos = new BlockPos(posIn);
        IBlockState state = stateIn;

        if (state.getValue(PART) == BlockPiano.EnumPartType.RIGHT)
        {
            pos = pos.offset(state.getValue(FACING).getOpposite());
            state = worldIn.getBlockState(pos);
            if (state.getBlock() != this)
                return true;
        }

        if (!worldIn.isRemote)
        {
            /// SERVER SIDE
            TilePiano tile = getTE(worldIn, pos);
            if (tile.isInvalid())
                return true;
            boolean isOccupied = PlacedInstrumentUtil.isSomeoneSitting(worldIn, pos);
            boolean invHasItem = !tile.getInventory().getStackInSlot(0).isEmpty();
            boolean invIsMusic = invHasItem && (tile.getInventory().getStackInSlot(0).getItem() instanceof IMusic) &&
                    tile.getInventory().getStackInSlot(0).hasDisplayName();
            boolean canPlay = playerIn.isRiding() && invIsMusic && PlacedInstrumentUtil.isPlayerSitting(worldIn, playerIn, pos) && !PlayManager.isPlayerPlaying(playerIn);
            boolean playerHasItem = !playerIn.getHeldItem(hand).isEmpty();
            boolean playerHasMusic = playerHasItem && (playerIn.getHeldItem(hand).getItem() instanceof IMusic) && 
                    playerIn.getHeldItem(hand).hasDisplayName();

            if (playerIn.isSneaking() && !isOccupied)
            {
                /* Remove music from the piano */
                ItemStack itemStack = tile.getInventory().getStackInSlot(0);
                tile.getInventory().setStackInSlot(0, ItemStack.EMPTY);
                if (!playerIn.inventory.addItemStackToInventory(itemStack))
                {
                    /* Not possible. Throw item in the world */
                    if (!itemStack.isEmpty())
                        spawnEntityItem(worldIn, itemStack, pos);
                } else
                {
                    tile.syncToClient();
                    playerIn.openContainer.detectAndSendChanges();
                }
            } else if (!playerIn.isRiding() && invIsMusic && !isOccupied)
            {
                return sitPiano(worldIn, pos, state, playerIn);
            } else if (!playerIn.isRiding() && !invHasItem)
            {
                /* Place music on the piano */
                if (playerHasMusic)
                {
                    /*
                     * There is no item in the music rack and the player is
                     * holding an item. We move that item into the music rack
                     */
                    tile.getInventory().setStackInSlot(0, playerIn.getHeldItem(hand));
                    playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, ItemStack.EMPTY);

                    /*
                     * Make sure the client knows about the changes in the
                     * player inventory
                     */
                    tile.syncToClient();
                    playerIn.openContainer.detectAndSendChanges();
                }
            } else if (canPlay && !playerIn.isSneaking())
            {
                if (ServerCSDManager.canMXTunesPlay(playerIn))
                {
                    ((EntitySittableBlock) playerIn.getRidingEntity()).setPlayID(PlayManager.playMusic(playerIn, pos));
                    ModCriteriaTriggers.PLAY_INSTRUMENT.trigger((EntityPlayerMP) playerIn, "spinet_piano");
                }
                else
                    ServerCSDManager.sendErrorViaChat(playerIn);
            }

        }  else
        {
            // CLIENT SIDE
            if ((playerIn.getRidingEntity() instanceof EntitySittableBlock) && ((EntitySittableBlock) playerIn.getRidingEntity()).getBlockPos().equals(pos))
                GuiJamOverlay.hudTimerReset();
        }
        return true;
    }

    @Override
    public int getPatch()
    {
        return SoundFontProxyManager.getIndexById("piano_mabinogi");
    }

    private boolean sitPiano(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn)
    {
        double xOffset = 0D;
        double zOffset = 0D;
        float yaw = 0F;
        if (state.getValue(FACING).equals(EnumFacing.NORTH))
        {
            xOffset = 1.375D;
            zOffset = 0.0D;
            yaw = 90F;
        } else if (state.getValue(FACING).equals(EnumFacing.SOUTH))
        {
            xOffset = -0.375D;
            zOffset = 1.0D;
            yaw = 270F;
        } else if (state.getValue(FACING).equals(EnumFacing.EAST))
        {
            xOffset = 1.0D;
            zOffset = 1.375D;
            yaw = 180F;
        } else if (state.getValue(FACING).equals(EnumFacing.WEST))
        {
            xOffset = 0.0D;
            zOffset = -0.375D;
            yaw = 0F;
        }
        return PlacedInstrumentUtil.sitOnBlock(worldIn, pos, playerIn, xOffset, 4 * 0.0625, zOffset, yaw);
    }

    @Override
    public boolean isFullCube(IBlockState state) {return false;}

    // Used to determine ambient occlusion and culling when rebuilding chunks for render
    @Override
    public boolean isOpaqueCube(IBlockState state) {return false;}

    // Called when a neighboring block changes.
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        EnumFacing enumfacing = state.getValue(FACING);

        if (state.getValue(PART) == BlockPiano.EnumPartType.RIGHT)
        {
            if (worldIn.getBlockState(pos.offset(enumfacing.getOpposite())).getBlock() != this)
            {
                worldIn.setBlockToAir(pos);
            }
        }
        else if (worldIn.getBlockState(pos.offset(enumfacing)).getBlock() != this)
        {
            worldIn.setBlockToAir(pos);

            if (!worldIn.isRemote)
            {
                this.dropBlockAsItem(worldIn, pos, state, 0);
            }
        }
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_)
    {
        List<AxisAlignedBB> list = Lists.newArrayList();
        list.add(PIANO_BODY_AABB);
        if (state.getValue(PART) == BlockPiano.EnumPartType.LEFT)
        {
            if (state.getValue(FACING).equals(EnumFacing.NORTH))
            {
                list.add(MUSIC_RACK_AABB_NW);
            } else if (state.getValue(FACING).equals(EnumFacing.SOUTH))
            {
                list.add(MUSIC_RACK_AABB_SE);
            } else if (state.getValue(FACING).equals(EnumFacing.EAST))
            {
                list.add(MUSIC_RACK_AABB_NE);
            } else if (state.getValue(FACING).equals(EnumFacing.WEST))
            {
                list.add(MUSIC_RACK_AABB_SW);
            }
        } else
        {
            if (state.getValue(FACING).equals(EnumFacing.NORTH))
            {
                list.add(MUSIC_RACK_AABB_SW);
            } else if (state.getValue(FACING).equals(EnumFacing.SOUTH))
            {
                list.add(MUSIC_RACK_AABB_NE);
            } else if (state.getValue(FACING).equals(EnumFacing.EAST))
            {
                list.add(MUSIC_RACK_AABB_NW);
            } else if (state.getValue(FACING).equals(EnumFacing.WEST))
            {
                list.add(MUSIC_RACK_AABB_SE);
            }
        }
        for (AxisAlignedBB axisalignedbb : list)
        {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, axisalignedbb);
        }
    }

    protected static void addCollisionBoxToList(@Nonnull BlockPos pos, AxisAlignedBB aaBBIn, List<AxisAlignedBB> listAABB, @Nullable AxisAlignedBB addedAABB)
    {
        if (addedAABB != NULL_AABB)
        {
            AxisAlignedBB axisalignedbb = Objects.requireNonNull(addedAABB).offset(pos);

            if (aaBBIn.intersects(axisalignedbb))
            {
                listAABB.add(axisalignedbb);
            }
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {return PIANO_BODY_AABB;}

    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {return EnumPushReaction.DESTROY;}

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {return BlockRenderLayer.CUTOUT;}

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        return new ItemStack(ModItems.ITEM_SPINET_PIANO);
    }
    
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        if (player.capabilities.isCreativeMode && state.getValue(PART) == BlockPiano.EnumPartType.RIGHT)
        {
            BlockPos blockpos = pos.offset(state.getValue(FACING).getOpposite());

            if (worldIn.getBlockState(blockpos).getBlock() == this)
            {
                worldIn.setBlockToAir(blockpos);
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TilePiano tile = (TilePiano) worldIn.getTileEntity(pos);
        if (state.getValue(PART) == BlockPiano.EnumPartType.LEFT && tile != null && !tile.getInventory().getStackInSlot(0).isEmpty())
        {
            spawnEntityItem(worldIn, tile.getInventory().getStackInSlot(0).copy(), pos);
            tile.invalidate();
        }
        super.breakBlock(worldIn, pos, state);
    }

    // Get the Item that this Block should drop when harvested.
    @Nullable
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return state.getValue(PART) == BlockPiano.EnumPartType.RIGHT ? null : ModItems.ITEM_SPINET_PIANO;
    }

    // Spawns this Block's drops into the World as EntityItems.
    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        if (state.getValue(PART) == BlockPiano.EnumPartType.LEFT   )
        {
            super.dropBlockAsItemWithChance(worldIn, pos, state, chance, 0);
        }
    }

    // Convert the given metadata into a BlockState for this Block
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.byHorizontalIndex(meta);
        return (meta & 8) > 0 ? this.getDefaultState().withProperty(PART, BlockPiano.EnumPartType.RIGHT).withProperty(FACING, enumfacing).withProperty(OCCUPIED, (meta & 4) > 0)
                : this.getDefaultState().withProperty(PART, BlockPiano.EnumPartType.LEFT).withProperty(FACING, enumfacing);
    }

    /*
     * Get the actual Block state of this Block at the given position. This
     * applies properties not visible in the metadata, such as fence
     * connections.
     */
    @Override
    public IBlockState getActualState(IBlockState stateIn, IBlockAccess worldIn, BlockPos pos)
    {
        IBlockState stateOut = stateIn;
        if (stateIn.getValue(PART) == BlockPiano.EnumPartType.LEFT)
        {
            IBlockState iblockstate = worldIn.getBlockState(pos.offset(stateIn.getValue(FACING)));

            if (iblockstate.getBlock() == this)
            {
                stateOut = stateIn.withProperty(OCCUPIED, iblockstate.getValue(OCCUPIED));
            }
        }
        return stateOut;
    }

    //Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed blockstate.
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    // Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed blockstate.
    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    /* Convert the BlockState into the correct metadata value */
    @Override
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(PART) == BlockPiano.EnumPartType.RIGHT)
        {
            i |= 8;

            if (state.getValue(OCCUPIED))
            {
                i |= 4;
            }
        }
        return i;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, PART, OCCUPIED);
    }

    public enum EnumPartType implements IStringSerializable
    {
        RIGHT("right"),
        LEFT("left");

        private final String name;

        EnumPartType(String name) {this.name = name;}

        @Override
        public String toString() {return this.name;}

        @Override
        public String getName() {return this.name;}
    }

    /* TileEntity stuff */

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        /* We only need one Tile Entity per piano. We will reference the LEFT block only. */
        return state.getValue(PART) == BlockPiano.EnumPartType.LEFT;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TilePiano(state.getValue(FACING));
    }

    /*
     * Stuff for utility class e.g.
     * https://github.com/sinkillerj/ProjectE/blob/MC19/src/main/java/moze_intel
     * /projecte/utils/WorldHelper.java
     */
    static void spawnEntityItem(World world, ItemStack stack, BlockPos pos)
    {
        spawnEntityItem(world, stack, pos.getX(), pos.getY(), pos.getZ());
    }

    static void spawnEntityItem(World world, ItemStack stack, double x, double y, double z)
    {
        if (stack.isEmpty()) return;

        float f = world.rand.nextFloat() * 0.8F + 0.1F;
        float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
        float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
        EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, stack.copy());
        entityitem.motionX = world.rand.nextGaussian() * 0.05;
        entityitem.motionY = world.rand.nextGaussian() * 0.05 + 0.2;
        entityitem.motionZ = world.rand.nextGaussian() * 0.05;
        world.spawnEntity(entityitem);
    }
}
