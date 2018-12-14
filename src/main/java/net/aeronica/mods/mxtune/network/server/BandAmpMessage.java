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
package net.aeronica.mods.mxtune.network.server;

import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.world.IModLockableContainer;
import net.aeronica.mods.mxtune.world.OwnerUUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LockCode;
import net.minecraftforge.fml.relauncher.Side;

public class BandAmpMessage extends AbstractMessage.AbstractServerMessage<BandAmpMessage>
{
    private boolean lockContainer;
    private BlockPos pos;

    @SuppressWarnings("unused")
    public BandAmpMessage() {/* NOP */}

    public BandAmpMessage(BlockPos pos, boolean lockContainer)
    {
        this.pos = pos;
        this.lockContainer = lockContainer;
    }

    @Override
    protected void read(PacketBuffer buffer)
    {
        pos = buffer.readBlockPos();
        lockContainer = buffer.readBoolean();
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(lockContainer);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        ModLogger.info("BandAmpMessage, %s, %s", pos, lockContainer);
        if(player.world.isBlockLoaded(pos))
        {
            TileEntity tileEntity = player.world.getTileEntity(pos);
            if(tileEntity instanceof IModLockableContainer)
            {
                IModLockableContainer lockableContainer = (IModLockableContainer) tileEntity;
                OwnerUUID ownerUUID = new OwnerUUID(player.getPersistentID());

                if(lockableContainer.isOwner(ownerUUID))
                {
                    if(lockContainer)
                    {
                        LockCode lockCode = new LockCode(ownerUUID.toString());
                        lockableContainer.setLockCode(lockCode);
                    }
                    else
                        lockableContainer.setLockCode(LockCode.EMPTY_CODE);

                    ModLogger.info("  lockCode  %s", lockableContainer.getLockCode().getLock());
                    ModLogger.info("  ownerCode %s", lockableContainer.getOwner());
                }
            }
        }
    }
}
