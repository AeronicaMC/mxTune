package aeronicamc.mods.mxtune.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class LockableHelper
{
    private LockableHelper() { /* NOP */ }

    /**
     * Test if a block entity is locked for the {@link PlayerEntity}
     * @param player that is tested for ownership
     * @param level the current world
     * @param blockPos of the {@link TileEntity}
     * @return
     */
    public static boolean isLocked(PlayerEntity player, World level, BlockPos blockPos)
    {
        TileEntity blockEntity = level.getBlockEntity(blockPos);
        boolean lock = false;
        if (blockEntity instanceof ILockable)
            lock = ((ILockable) blockEntity).isLocked() ; //canManage(player, (ILockable) blockEntity);

        return lock;
    }

    /**
     * Test if the {@link PlayerEntity} can lock/unlock the block entity or item.
     * @param player that is tested for ownership
     * @param lockable the {@link TileEntity} or {@link net.minecraft.item.ItemStack} that implements {@link ILockable} interface
     * @return true if the player can lock/unlock the block entity or item.
     */
    public static boolean canLock(PlayerEntity player, ILockable lockable)
    {
        return lockable.isOwner(player.getUUID()) && !player.isSpectator();
    }

    /**
     * Test if the {@link PlayerEntity} can manage the block entity or item.
     * @param player that is tested for ownership
     * @param lockable the {@link TileEntity} or {@link net.minecraft.item.ItemStack} that implements {@link ILockable} interface
     * @return true if the block and its contents and settings can be changed or removed from the world
     */
    public static boolean canManage(PlayerEntity player, ILockable lockable)
    {
        return (lockable.isOwner(player.getUUID()) && !player.isSpectator()) || !lockable.isLocked() ;
    }

    public static boolean cannotBreak(PlayerEntity playerIn, World worldIn, BlockPos pos)
    {
        TileEntity tileEntity = worldIn.getBlockEntity(pos);
        boolean cannotBreak = false;
        if (tileEntity instanceof ILockable)
            cannotBreak = cannotBreak(playerIn, (ILockable) tileEntity);

        return cannotBreak;
    }

    private static boolean cannotBreak(PlayerEntity player, ILockable lockable)
    {
        return !lockable.isOwner(player.getUUID());
    }

}
