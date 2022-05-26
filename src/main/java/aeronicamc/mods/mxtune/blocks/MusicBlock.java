package aeronicamc.mods.mxtune.blocks;

import aeronicamc.mods.mxtune.init.ModSoundEvents;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.managers.PlayManager;
import aeronicamc.mods.mxtune.util.IInstrument;
import aeronicamc.mods.mxtune.util.IWrenchAble;
import aeronicamc.mods.mxtune.util.SheetMusicHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;
import static net.minecraftforge.common.util.Constants.BlockFlags;
import static net.minecraftforge.common.util.Constants.NBT;


@SuppressWarnings("deprecation")
public class MusicBlock extends Block implements IMusicPlayer, IWrenchAble
{
    public static final BooleanProperty PLAYING = BooleanProperty.create("playing");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape BOTTOM_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    private static final Logger LOGGER = LogManager.getLogger(MusicBlock.class);
    private static final Random rand = new Random();

    public MusicBlock()
    {
        super(Properties.of(Material.NETHER_WOOD, MaterialColor.COLOR_BROWN)
                      .sound(SoundType.WOOD)
                      .strength(2.0F)
                      .lightLevel(state -> state.getValue(PLAYING) ? 14 : 0)
                      .noOcclusion());
        this.registerDefaultState(this.defaultBlockState()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(PLAYING, Boolean.FALSE)
                .setValue(POWERED, Boolean.FALSE));
    }

    @Override
    public VoxelShape getVisualShape(BlockState pState, IBlockReader pReader, BlockPos pPos, ISelectionContext pContext) {
        return BOTTOM_AABB;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, IBlockReader pReader, BlockPos pPos) {
        return false;
    }

    @Override
    public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand)
    {
//        if (pState.getValue(PLAYING))
//            {
//            double d0 = (double)pPos.getX() + 0.5D;
//            double d1 = (double)pPos.getY() + 1.0625D;
//            double d2 = (double)pPos.getZ() + 0.5D;
//            double noteColor = rand.nextDouble();
//            double d4 = pRand.nextDouble() * 0.4D - 0.2D;
//            double d5 = 1D * 0D;
//            double d6 = pRand.nextDouble() * 6.0D / 16.0D;
//            double d7 = 1D * 0D;
//            // TODO: come up with out own particles for the BandAmp :D
//            pLevel.addParticle(ModParticles.getSpeaker(), d0 + d4, d1 + d6, d2 + d4, 0.0D, 0.1D, 0.0D);
//            pLevel.addParticle(ParticleTypes.ASH, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
//            }
    }

    @Override
    public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand)
    {
        if (!pLevel.isClientSide())
        {
            getMusicBlockEntity(pLevel, pPos).ifPresent(
                    musicBlockEntity ->
                    {
                        if (pState.getValue(PLAYING))
                        {
                            pLevel.getBlockTicks().scheduleTick(pPos, this, 20);
                            if (PlayManager.getActiveBlockPlayId(pPos) == PlayIdSupplier.INVALID)
                            {
                                setPlayingState(pLevel, pPos, pState, false);
                                onePulseOutputState(pLevel, pPos, pState, musicBlockEntity);
                            }
                        }
                        else
                            onePulseOutputState(pLevel, pPos, pState, musicBlockEntity);
                    });
        }
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        if (!worldIn.isClientSide())
        {
            if (handleWrenchAble(state, worldIn, pos, player, handIn, hit))
                return ActionResultType.SUCCESS;

            if (invertShiftIfLocked(player, worldIn, pos))
                getMusicBlockEntity(worldIn, pos).ifPresent(
                        musicBlockEntity ->
                        {
                            // Use spam prevention.
                            // Server side: prevent runaway activation.
                            // Limits activation to a single use even if held.
                            // It's a shame to use ITickableTileEntity#ticks for this,
                            // but I have not found another solution yet.
                            if (musicBlockEntity.notHeld())
                            {
                                boolean isPlaying = canPlayOrStopMusic(worldIn, state, pos, false);
                                if (isPlaying)
                                    musicBlockEntity.setLastPlay(true);
                                setPlayingState(worldIn, pos, state, isPlaying);
                            }
                            musicBlockEntity.useHeldCounterUpdate(true);
                        });
            else
            {
                TileEntity blockEntity = worldIn.getBlockEntity(pos);
                if (blockEntity instanceof INamedContainerProvider)
                    NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) blockEntity, blockEntity.getBlockPos());
                else
                    throw new IllegalStateException("Our named container provider is missing!");
            }
            return ActionResultType.SUCCESS;
        } else
            return ActionResultType.CONSUME;
    }

    private boolean invertShiftIfLocked(PlayerEntity player, World level, BlockPos blockPos)
    {
        return LockableHelper.isLocked(FakePlayerFactory.getMinecraft((ServerWorld) level), level, blockPos) != player.isShiftKeyDown();
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

    private boolean handleWrenchAble(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        MusicBlockEntity blockEntity = getMusicBlockEntity(level, pos).orElseThrow(() -> new IllegalStateException("Our Block Entity is missing!"));
        if (!level.isClientSide() && blockEntity.isOwner(player.getUUID()) && hasWrench(player, handIn))
        {
            BlockState newState;
            if (player.isShiftKeyDown())
            {
                // Pickup Music Block into player inventory. If inventory is full drop in the world.
                ItemStack itemStack = state.getBlock().getCloneItemStack(level, pos, state);
                if (!player.inventory.add(itemStack))
                {
                    ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), itemStack);
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                    level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                level.destroyBlock(pos, false, player);
            }
            else
            {
                // Rotate Block Horizontally to the side hit.
                if (!hit.getDirection().equals(Direction.UP ) && !hit.getDirection().equals(Direction.DOWN))
                {
                    newState = state.setValue(HORIZONTAL_FACING, hit.getDirection());
                    //newState = state.rotate(level, pos, Rotation.CLOCKWISE_90);
                    level.setBlockAndUpdate(pos, newState);
                    level.playSound(null, pos, ModSoundEvents.ROTATE_BLOCK.get(), SoundCategory.BLOCKS, 0.6F, 1.0F);
                } else
                    level.playSound(null, pos, ModSoundEvents.ROTATE_BLOCK_FAILED.get(), SoundCategory.BLOCKS, 0.2F, 1.0F);
            }
            return true;
        }
        return false;
    }

    private void onePulseOutputState(World pLevel, BlockPos pPos, BlockState pState, MusicBlockEntity musicBlockEntity)
    {
        if (!pState.getValue(POWERED) && musicBlockEntity.isLastPlay())
        {
            setOutputPowerState(pLevel, pPos, pState, true);
            musicBlockEntity.setLastPlay(false);
        } else if (pState.getValue(POWERED))
            setOutputPowerState(pLevel, pPos, pState, false);
    }

    private void setPlayingState(World pLevel, BlockPos pPos, BlockState pState, boolean pIsPlaying)
    {
        pLevel.setBlock(pPos, pState.setValue(PLAYING, pIsPlaying), BlockFlags.BLOCK_UPDATE | BlockFlags.NOTIFY_NEIGHBORS);
        pLevel.getBlockTicks().scheduleTick(pPos, this, 4);
    }

    private void setOutputPowerState(World pLevel, BlockPos pPos, BlockState pState, boolean pIsPowered)
    {
        pLevel.setBlock(pPos, pState.setValue(POWERED, pIsPowered), BlockFlags.BLOCK_UPDATE | BlockFlags.NOTIFY_NEIGHBORS);
        pLevel.getBlockTicks().scheduleTick(pPos, this, 4);
    }

    @Override
    public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving)
    {
        getMusicBlockEntity(pLevel, pPos).filter(p -> !pLevel.isClientSide()).ifPresent(
                musicBlockEntity ->
                {
                    // get redStone input from the rear side
                    boolean isSidePowered = pLevel.hasSignal(pPos.relative(pState.getValue(HORIZONTAL_FACING).getOpposite()), pState.getValue(HORIZONTAL_FACING));
                    // Lever spam prevention. see use method above for more details.
                    if (musicBlockEntity.notHeld())
                    {
                        if ((musicBlockEntity.getPreviousInputState() != isSidePowered) && musicBlockEntity.isRearRedstoneInputEnabled())
                        {
                            if (isSidePowered)
                            {
                                boolean isPlaying = canPlayOrStopMusic(pLevel, pState, pPos, false);
                                if (isPlaying)
                                    musicBlockEntity.setLastPlay(true);
                                setPlayingState(pLevel, pPos, pState, isPlaying);
                            }
                            musicBlockEntity.setPreviousInputState(isSidePowered);
                        }
                    }
                    musicBlockEntity.useHeldCounterUpdate(pState.getValue(PLAYING));
                });
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side)
    {
        return getMusicBlockEntity(world, pos).filter(p -> side != null).map(
                musicBlockEntity ->
                {
                    Direction direction = state.getValue(HORIZONTAL_FACING);
                    boolean canConnectBack = musicBlockEntity.isRearRedstoneInputEnabled() && direction == side;
                    boolean canConnectLeft = musicBlockEntity.isLeftRedstoneOutputEnabled() && direction.getCounterClockWise() == side;
                    boolean canConnectRight = musicBlockEntity.isRightRedstoneOutputEnabled() && direction.getClockWise() == side;
                    return canConnectBack || canConnectLeft || canConnectRight;
                }).orElse(false);
    }

    @Override
    public int getSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return getMusicBlockEntity(pBlockAccess, pPos).map(
                musicBlockEntity ->
                {
                    Direction direction = pBlockState.getValue(HORIZONTAL_FACING);
                    boolean canConnectLeft = musicBlockEntity.isLeftRedstoneOutputEnabled() && direction.getCounterClockWise() == pSide;
                    boolean canConnectRight = musicBlockEntity.isRightRedstoneOutputEnabled() && direction.getClockWise() == pSide;
                    return (pBlockState.getValue(POWERED) && (canConnectLeft || canConnectRight) ? 15 : 0);
                }).orElse(0);
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
        return false;
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
        return new MusicBlockEntity();
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack)
    {
        getMusicBlockEntity(world, pos).ifPresent(
                musicBlockEntity -> {
                    if (stack.hasCustomHoverName())
                        musicBlockEntity.setCustomName(stack.getHoverName());
                    if (entity != null) musicBlockEntity.setOwner(entity.getUUID());
                    }
                );
    }

    @Override
    public void playerWillDestroy(World pLevel, BlockPos pPos, BlockState pState, PlayerEntity pPlayer)
    {
        if(!pLevel.isClientSide() && !pPlayer.isCreative())
        {
            ItemStack itemStack = getCloneItemStack(pLevel, pPos, pState);
            ItemEntity itemEntity = new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), itemStack);
            itemEntity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itemEntity);
        }
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public ItemStack getCloneItemStack(IBlockReader pLevel, BlockPos pPos, BlockState pState)
    {
        ItemStack itemStack = super.getCloneItemStack(pLevel, pPos, pState);
        getMusicBlockEntity(pLevel, pPos).ifPresent(
                musicBlockEntity ->
                {
                    CompoundNBT compoundNBT = musicBlockEntity.save(new CompoundNBT());
                    if (!compoundNBT.isEmpty())
                    {
                        // Save Inventory only! No need to keep block position, button state or owner.
                        compoundNBT.remove("x");
                        compoundNBT.remove("y");
                        compoundNBT.remove("z");
                        compoundNBT.remove(MusicBlockEntity.KEY_BUTTON_STATE);
                        compoundNBT.remove(MusicBlockEntity.KEY_OWNER);
                        itemStack.addTagElement("BlockEntityTag", compoundNBT);
                    }

                    if (musicBlockEntity.hasCustomName())
                        itemStack.setHoverName(musicBlockEntity.getCustomName());
                });
        return itemStack;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable IBlockReader pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag)
    {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        CompoundNBT cNBT = pStack.getTagElement("BlockEntityTag");
        if (cNBT != null)
        {
            CompoundNBT inventoryNBT = cNBT.getCompound("Inventory");
            if (inventoryNBT.contains("Items", NBT.TAG_LIST))
            {
                int size = inventoryNBT.contains("Size", NBT.TAG_INT) ? inventoryNBT.getInt("Size") : 27;
                NonNullList<ItemStack> nonNullList = NonNullList.withSize(size, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(inventoryNBT, nonNullList);
                ItemStack instrumentStack = nonNullList.stream().findFirst().orElse(ItemStack.EMPTY);

                if (!instrumentStack.isEmpty())
                    pTooltip.add(SheetMusicHelper.getFormattedMusicTitle(SheetMusicHelper.getIMusicFromIInstrument(instrumentStack)));
                else pTooltip.add(SheetMusicHelper.getFormattedMusicTitle(ItemStack.EMPTY));

                long instrumentCount = nonNullList.stream().filter(p -> (p.getItem() instanceof IInstrument) && !SheetMusicHelper.getIMusicFromIInstrument(p).isEmpty()).count();
                if (instrumentCount > 1)
                    pTooltip.add(new StringTextComponent(new TranslationTextComponent("container.mxtune.block_music.more", instrumentCount - 1).getString() + (TextFormatting.ITALIC)));

                int duration =  cNBT.contains("Duration", NBT.TAG_INT) ? cNBT.getInt("Duration") : 0;
                if (duration > 0)
                    pTooltip.add(new StringTextComponent(SheetMusicHelper.formatDuration(duration)).withStyle(TextFormatting.YELLOW));
            }
        }
    }

    private Optional<MusicBlockEntity> getMusicBlockEntity(IBlockReader pLevel, BlockPos pPos)
    {
        return pLevel.getBlockEntity(pPos) instanceof MusicBlockEntity ? Optional.ofNullable(((MusicBlockEntity)(pLevel.getBlockEntity(pPos)))) : Optional.empty();
    }
}
