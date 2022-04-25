package aeronicamc.mods.mxtune.blocks;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.managers.PlayManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Random;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;


@SuppressWarnings("deprecation")
public class MusicBlock extends Block implements IMusicPlayer
{
    public static final BooleanProperty PLAYING = BooleanProperty.create("playing");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final Logger LOGGER = LogManager.getLogger(MusicBlock.class);
    private static final Random rand = new Random();

    public MusicBlock()
    {
        super(Properties.of(Material.METAL)
             .sound(SoundType.METAL)
             .strength(2.0F));
        this.registerDefaultState(this.defaultBlockState()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(PLAYING, Boolean.FALSE)
                .setValue(POWERED, Boolean.FALSE));
    }

    @Override
    public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand)
    {
        if (pState.getValue(PLAYING))
            {
            double d0 = (double)pPos.getX() + 0.5D;
            double d1 = (double)pPos.getY() + 1.0625D;
            double d2 = (double)pPos.getZ() + 0.5D;
            double noteColor = rand.nextDouble();
            double d4 = pRand.nextDouble() * 0.4D - 0.2D;
            double d5 = 1D * 0D;
            double d6 = pRand.nextDouble() * 6.0D / 16.0D;
            double d7 = 1D * 0D;
            // TODO: come up with out own particles for the BandAmp :D
            pLevel.addParticle(ParticleTypes.NOTE, d0 + d4, d1 + d6, d2 + d4, noteColor, 0.0D, 0.0D);
            pLevel.addParticle(ParticleTypes.ASH, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
            }
    }

    @Override
    public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
        if (!pLevel.isClientSide())
        {
            TileEntity tileEntity = pLevel.getBlockEntity(pPos);
            if (tileEntity instanceof MusicBlockTile)
            {
                MusicBlockTile musicBlockTile = (MusicBlockTile) tileEntity;
                if (pState.getValue(PLAYING))
                {
                    pLevel.getBlockTicks().scheduleTick(pPos, this, 20);
                    if (PlayManager.getActiveBlockPlayId(pPos) == PlayIdSupplier.INVALID)
                    {
                        setPlayingState(pLevel, pPos, pState, false);
                        onePulseOutputState(pLevel, pPos, pState, musicBlockTile);
                    }
                }
                else
                    onePulseOutputState(pLevel, pPos, pState, musicBlockTile);
            }
        }
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        if (!worldIn.isClientSide())
        {
            if (!player.isShiftKeyDown())
            {
                TileEntity tileEntity = worldIn.getBlockEntity(pos);
                if (tileEntity instanceof MusicBlockTile)
                {
                    MusicBlockTile musicBlockTile = (MusicBlockTile) tileEntity;
                    // Server side: prevent runaway activation.
                    // Limits activation to a single use even if held.
                    // It's a shame to use ITickableTileEntity#ticks for this,
                    // but I have not found another solution yet.
                    if (!musicBlockTile.isUseHeld())
                    {
                        boolean isPlaying = canPlayOrStopMusic(worldIn, state, pos, false);
                        if (isPlaying)
                            musicBlockTile.setLastPlay(true);
                        setPlayingState(worldIn, pos, state, isPlaying);
                    }
                    musicBlockTile.useHeldCounterUpdate(true);
                }
            }
            else
            {
                TileEntity tileEntity = worldIn.getBlockEntity(pos);
                if (tileEntity instanceof INamedContainerProvider)
                {
                    NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, tileEntity.getBlockPos());
                }
                else
                {
                    throw new IllegalStateException("Our named container provider is missing!");
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    private boolean canPlayOrStopMusic(World pLevel, BlockState pState, BlockPos pPos, Boolean noPlay)
    {
        int playId = PlayManager.getActiveBlockPlayId(pPos);
            if (PlayManager.isActivePlayId(playId) || pState.getValue(PLAYING))
            {
                LOGGER.warn("STOP canPlayOrStopMusic playId {}", playId);
                PlayManager.stopPlayId(playId);
                return false;
            }
            if (!noPlay)
            {
                playId = PlayManager.playMusic(pLevel, pPos);
                LOGGER.warn("PLAY canPlayOrStopMusic playId {}", playId);
                return playId != PlayIdSupplier.INVALID && !pState.getValue(PLAYING);
            }
        return false;
    }

    private void onePulseOutputState(World pLevel, BlockPos pPos, BlockState pState, MusicBlockTile musicBlockTile)
    {
        if (!pState.getValue(POWERED) && musicBlockTile.isLastPlay())
        {
            setOutputPowerState(pLevel, pPos, pState, true);
            musicBlockTile.setLastPlay(false);
        } else if (pState.getValue(POWERED))
            setOutputPowerState(pLevel, pPos, pState, false);
    }


    private void setPlayingState(World pLevel, BlockPos pPos, BlockState pState, boolean pIsPlaying)
    {
        pLevel.setBlock(pPos, pState.setValue(PLAYING, pIsPlaying), Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.UPDATE_NEIGHBORS);
        pLevel.getBlockTicks().scheduleTick(pPos, this, 10);
    }

    private void setOutputPowerState(World pLevel, BlockPos pPos, BlockState pState, boolean pIsPowered)
    {
        pLevel.setBlock(pPos, pState.setValue(POWERED, pIsPowered), 3);
        pLevel.getBlockTicks().scheduleTick(pPos, this, 10);
    }

    @Override
    public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!pLevel.isClientSide)
        {
            TileEntity tileEntity = pLevel.getBlockEntity(pPos);

            if (tileEntity instanceof MusicBlockTile)
            {
                // get redStone input from the rear side
                boolean isSidePowered = pLevel.hasSignal(pPos.relative(pState.getValue(HORIZONTAL_FACING).getOpposite()), pState.getValue(HORIZONTAL_FACING));
                MusicBlockTile musicBlockTile = (MusicBlockTile) tileEntity;
                if ((musicBlockTile.getPreviousInputState() != isSidePowered) && musicBlockTile.isRearRedstoneInputEnabled())
                {
                    if (isSidePowered)
                    {
                        boolean isPlaying = canPlayOrStopMusic(pLevel, pState, pPos, false);
                        if (isPlaying)
                            musicBlockTile.setLastPlay(true);
                        setPlayingState(pLevel, pPos, pState, isPlaying);
                    }
                    musicBlockTile.setPreviousInputState(isSidePowered);
                }
            }
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side)
    {
        TileEntity tileEntity = world.getBlockEntity(pos);
        if (side != null && (tileEntity instanceof MusicBlockTile))
        {
            MusicBlockTile musicBlockTile = (MusicBlockTile) tileEntity;
            Direction direction = state.getValue(HORIZONTAL_FACING);
            boolean canConnectBack = musicBlockTile.isRearRedstoneInputEnabled() && direction == side;
            boolean canConnectLeft = musicBlockTile.isLeftRedstoneOutputEnabled() && direction.getCounterClockWise() == side;
            boolean canConnectRight = musicBlockTile.isRightRedstoneOutputEnabled() && direction.getClockWise() == side;
            return canConnectBack || canConnectLeft || canConnectRight;
        }
        return false;
    }

    @Override
    public int getSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide)
    {
        TileEntity tileEntity = pBlockAccess.getBlockEntity(pPos);
        if (tileEntity instanceof MusicBlockTile)
        {
            MusicBlockTile musicBlockTile = (MusicBlockTile) tileEntity;
            Direction direction = pBlockState.getValue(HORIZONTAL_FACING);
            boolean canConnectLeft = musicBlockTile.isLeftRedstoneOutputEnabled() && direction.getCounterClockWise() == pSide;
            boolean canConnectRight = musicBlockTile.isRightRedstoneOutputEnabled() && direction.getClockWise() == pSide;
            return pBlockState.getValue(POWERED) && (canConnectLeft || canConnectRight) ? 15 :0;
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return super.getSignal(pBlockState, pBlockAccess, pPos, pSide);
    }

    // This prevents this block from conducting redstone signals.
    @Override
    public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side)
    {
        return false; //state.isRedstoneConductor(world, pos);
    }

    @Override
    public boolean isSignalSource(BlockState pState)
    {
        return true;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(HORIZONTAL_FACING, rot.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.defaultBlockState()
                .setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite())
                .setValue(PLAYING, Boolean.FALSE)
                .setValue(POWERED, Boolean.FALSE);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, PLAYING, POWERED);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new MusicBlockTile();
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof MusicBlockTile) {
                ((MusicBlockTile)tileentity).setCustomName(stack.getHoverName());
            }
        }
    }
}
