package aeronicamc.mods.mxtune.blocks;

import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.managers.PlayManager;
import aeronicamc.mods.mxtune.sound.ClientAudio;
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
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Random;


@SuppressWarnings("deprecation")
public class MusicBlock extends Block implements IMusicPlayer
{
    public static final BooleanProperty PLAYING = BooleanProperty.create("playing");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final Random rand = new Random();

    public MusicBlock()
    {
        super(Properties.of(Material.METAL)
             .sound(SoundType.METAL)
             .strength(2.0F));
        this.registerDefaultState(this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(PLAYING, Boolean.FALSE)
                .setValue(POWERED, Boolean.FALSE));
    }

    @Override
    public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand)
    {
        TileEntity tileEntity = pLevel.getBlockEntity(pPos);
        if (tileEntity instanceof MusicBlockTile)
            {
            double d0 = (double)pPos.getX() + 0.5D;
            double d1 = (double)pPos.getY() + 1.0625D;
            double d2 = (double)pPos.getZ() + 0.5D;
            double noteColor = rand.nextDouble();
            double d4 = pRand.nextDouble() * 0.4D - 0.2D;
            double d5 = 1D * 0D;
            double d6 = pRand.nextDouble() * 6.0D / 16.0D;
            double d7 = 1D * 0D;

            // TODO: Convert to use block state properties so server-side invoked changes are used to
            // TODO: control the particles/animations. Property<Boolean> PLAYING...
            MusicBlockTile musicBlockTile = (MusicBlockTile) tileEntity;
            if (ClientAudio.getActivePlayIDs().contains(musicBlockTile.getPlayId()))
            {
                // TODO: come up with out own particles for the BandAmp :D
                pLevel.addParticle(ParticleTypes.NOTE, d0 + d4, d1 + d6, d2 + d4, noteColor, 0.0D, 0.0D);
                pLevel.addParticle(ParticleTypes.ASH, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom)
    {
        super.randomTick(pState, pLevel, pPos, pRandom);
    }

    /**
     * Returns whether or not this block is of a type that needs random ticking. Called for ref-counting purposes by
     * ExtendedBlockStorage in order to broadly cull a chunk from the random chunk update list for efficiency's sake.
     *
     * @param pState
     */
    @Override
    public boolean isRandomlyTicking(BlockState pState)
    {
        return true;
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
                        boolean isPlaying = canPlayOrStopMusic(worldIn, pos, false);
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

    private boolean canPlayOrStopMusic(World worldIn, BlockPos pos, Boolean stop)
    {
        TileEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof MusicBlockTile)
        {
            MusicBlockTile musicBlockTile = (MusicBlockTile) tileEntity;
            if (PlayManager.isActivePlayId(musicBlockTile.getPlayId()))
            {
                PlayManager.stopPlayId(musicBlockTile.getPlayId());
                musicBlockTile.setPlayId(PlayIdSupplier.INVALID);
                return false;
            }
            if (!stop)
            {
                int playId = PlayManager.playMusic(worldIn, pos);
                musicBlockTile.setPlayId(playId);
            }
        }
        return true;
    }

    private void setPlayingState(World worldIn, BlockPos posIn, BlockState state, boolean playing)
    {
        //worldIn.setBlock(posIn, state.setValue(PLAYING, playing), Constants.BlockFlags.BLOCK_UPDATE);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rot.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite())
                .setValue(PLAYING, Boolean.FALSE)
                .setValue(POWERED, Boolean.FALSE);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, PLAYING, POWERED);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        LOGGER.debug("Created TE {}", state);
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

    @Override
    public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving)
    {
        if (!pLevel.isClientSide())
        {
            TileEntity tileEntity = pLevel.getBlockEntity(pPos);
            if (tileEntity instanceof MusicBlockTile)
            {
                MusicBlockTile musicBlockTile = (MusicBlockTile) tileEntity;
                if (PlayManager.isActivePlayId(musicBlockTile.getPlayId()))
                {
                    PlayManager.stopPlayId(musicBlockTile.getPlayId());
                    musicBlockTile.setPlayId(PlayIdSupplier.INVALID);
                }
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}
