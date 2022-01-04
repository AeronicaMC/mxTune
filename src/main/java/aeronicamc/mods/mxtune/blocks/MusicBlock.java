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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
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
    private static final Random rand = new Random();

    public MusicBlock()
    {
        super(Properties.of(Material.METAL)
             .sound(SoundType.METAL)
             .strength(2.0F));
    }

    @Override
    public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand)
    {
        super.animateTick(pState, pLevel, pPos, pRand);
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
                        worldIn.updateNeighborsAt(pos, this);
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
                musicBlockTile.setPlayId(PlayIdSupplier.PlayType.INVALID.getAsInt());
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
                    musicBlockTile.setPlayId(PlayIdSupplier.PlayType.INVALID.getAsInt());
                }
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}
