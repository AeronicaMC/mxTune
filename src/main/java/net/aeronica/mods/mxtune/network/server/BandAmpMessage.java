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

import net.aeronica.mods.mxtune.blocks.TileBandAmp;
import net.aeronica.mods.mxtune.network.AbstractMessage;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.world.OwnerUUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LockCode;
import net.minecraftforge.fml.relauncher.Side;

public class BandAmpMessage extends AbstractMessage.AbstractServerMessage<BandAmpMessage>
{
    private boolean lockBandAmp;
    private BlockPos bandAmpPosition;

    @SuppressWarnings("unused")
    public BandAmpMessage() {/* NOP */}

    public BandAmpMessage(BlockPos bandAmpPosition, boolean lockBandAmp)
    {
        this.bandAmpPosition = bandAmpPosition;
        this.lockBandAmp = lockBandAmp;
    }

    @Override
    protected void read(PacketBuffer buffer)
    {
        bandAmpPosition = buffer.readBlockPos();
        lockBandAmp = buffer.readBoolean();
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeBlockPos(bandAmpPosition);
        buffer.writeBoolean(lockBandAmp);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        ModLogger.info("BandAmpMessage, %s, %s", bandAmpPosition, lockBandAmp);
        if(player.world.isBlockLoaded(bandAmpPosition))
        {
            TileEntity tileEntity = player.world.getTileEntity(bandAmpPosition);
            if(tileEntity instanceof TileBandAmp)
            {
                TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
                OwnerUUID ownerUUID = new OwnerUUID(player.getPersistentID().toString());

                if(tileBandAmp.isOwner(ownerUUID))
                {
                    LockCode lockCode = new LockCode(ownerUUID.getUUID());
                    if(lockBandAmp)
                        tileBandAmp.setLockCode(lockCode);
                    else
                        tileBandAmp.setLockCode(LockCode.EMPTY_CODE);

                    tileBandAmp.markDirty();
                    ModLogger.info("  lockCode  %s", tileBandAmp.getLockCode().getLock());
                    ModLogger.info("  ownerCode %s", tileBandAmp.getOwner().getUUID());
                }
            }
        }
    }
}
