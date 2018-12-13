package net.aeronica.mods.mxtune.world;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LockableHelper
{
    private LockableHelper() {/* NOP */}

    public static boolean isLocked(EntityPlayer playerIn, World worldIn, BlockPos pos)
    {
        return isLocked(playerIn, worldIn, pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isLocked(EntityPlayer playerIn, World worldIn, int x, int y, int z)
    {
        TileEntity tileEntity = worldIn.getTileEntity(new BlockPos(x, y, z));
        boolean isLocked = false;
        if (tileEntity instanceof IModLockableContainer)
        {
            IModLockableContainer lockableContainer = (IModLockableContainer) tileEntity;
            isLocked = canOpen(playerIn, lockableContainer);
        }
        return isLocked;
    }

    private static boolean canOpen(EntityPlayer playerIn, IModLockableContainer lockableContainer)
    {
        return lockableContainer.isLocked() && !playerIn.getUniqueID().toString().equals(lockableContainer.getLockCode().getLock()) && !playerIn.isSpectator();
    }

    public static boolean isBreakable(EntityPlayer playerIn, World worldIn, BlockPos pos)
    {
        return isBreakable(playerIn, worldIn, pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isBreakable(EntityPlayer playerIn, World worldIn, int x, int y, int z)
    {
        TileEntity tileEntity = worldIn.getTileEntity(new BlockPos(x, y, z));
        boolean isBreakable = false;
        if (tileEntity instanceof IModLockableContainer)
        {
            IModLockableContainer lockableContainer = (IModLockableContainer) tileEntity;
            isBreakable = canBreak(playerIn, lockableContainer);
        }
        return isBreakable;
    }

    private static boolean canBreak(EntityPlayer playerIn, IModLockableContainer lockableContainer)
    {
        OwnerUUID ownerUUID = new OwnerUUID(playerIn.getPersistentID().toString());
        return !(lockableContainer.isOwner(ownerUUID)) && !playerIn.isSpectator();
    }
}
