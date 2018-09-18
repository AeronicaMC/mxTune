package net.aeronica.mods.mxtune.blocks;

import net.aeronica.libs.mml.core.MMLUtil;
import net.aeronica.libs.mml.core.TestData;
import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.groups.PlayManager;
import net.aeronica.mods.mxtune.gui.GuiBandAmp;
import net.aeronica.mods.mxtune.init.ModItems;
import net.aeronica.mods.mxtune.items.ItemInstrument;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.SheetMusicUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import static net.aeronica.mods.mxtune.blocks.BlockPiano.spawnEntityItem;

public class BlockBandAmp extends BlockHorizontal implements IMusicPlayer
{
    public BlockBandAmp()
    {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setHardness(0.0F);
        this.setSoundType(SoundType.WOOD);
        this.disableStats();
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
        if (!worldIn.isRemote && playerIn.capabilities.allowEdit)
        {
            if (!playerIn.isSneaking())
            {
                PlayManager.playMusic(worldIn, pos);
            } else
            {
                playerIn.openGui(MXTuneMain.instance, GuiBandAmp.GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    /**
     * React to a redstone powered neighbor block
     */
    @Deprecated
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        boolean powered = worldIn.isBlockPowered(pos);
        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof TileBandAmp)
        {
            TileBandAmp tileBandAmp = (TileBandAmp) te;
            if (tileBandAmp.getPreviousRedStoneState() != powered)
            {
                if (powered)
                    tileBandAmp.setPowered(state, worldIn, pos, blockIn, fromPos);

                tileBandAmp.setPreviousRedStoneState(powered);
            }
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
        return new BlockStateContainer(this, new IProperty[] {FACING});
    }

    @Deprecated
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) { return state.getValue(FACING).getHorizontalIndex(); }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileBandAmp(state.getValue(FACING));
    }

    public TileBandAmp getTileEntity(IBlockAccess world, BlockPos pos) {
        return (TileBandAmp) world.getTileEntity(pos);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
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

    @Override
    public String getMML(World worldIn, BlockPos blockPos)
    {
        StringBuilder buildMML = new StringBuilder("");
        TileEntity te = worldIn.getTileEntity(blockPos);

        if (te instanceof TileBandAmp)
        {
            try
            {
                for(int slot = 0; slot < ((TileBandAmp) te).getInventory().getSlots(); slot++)
                {
                    ItemStack instrument = ((TileBandAmp) te).getInventory().getStackInSlot(slot);
                    if (!instrument.isEmpty())
                    {
                        ItemInstrument ii = (ItemInstrument) instrument.getItem();
                        int patch = ii.getPatch(instrument.getMetadata());
                        ItemStack sheetMusic = SheetMusicUtil.getSheetMusic(instrument);
                        if (!sheetMusic.isEmpty())
                        {
                            NBTTagCompound contents = (NBTTagCompound) sheetMusic.getTagCompound().getTag("MusicBook");
                            if (contents != null)
                            {
                                String mml = contents.getString("MML");
                                mml = mml.replace("MML@", "MML@I" + patch);
                                buildMML.append(slot).append("=").append(mml).append("|");
                            }
                        }
                    }
                }
            } catch (Exception e)
            {
                ModLogger.error(e);
            }
        }
        return buildMML.toString();
    }

    //    @Deprecated
//    @Override
//    public boolean isFullCube(IBlockState state) { return false; }
//
//    @Deprecated
//    @Override
//    public boolean isOpaqueCube(IBlockState state) { return false; }
}
