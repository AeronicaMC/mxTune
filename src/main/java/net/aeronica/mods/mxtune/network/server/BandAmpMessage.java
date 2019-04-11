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
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.sound.SoundRange;
import net.aeronica.mods.mxtune.world.IModLockableContainer;
import net.aeronica.mods.mxtune.world.OwnerUUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.LockCode;
import net.minecraftforge.fml.relauncher.Side;

import static net.aeronica.mods.mxtune.util.Miscellus.audiblePingPlayer;

public class BandAmpMessage extends AbstractMessage.AbstractServerMessage<BandAmpMessage>
{
    private boolean lockContainer;
    private BlockPos pos;
    private boolean rearRedstoneInputEnabled;
    private boolean leftRedstoneOutputEnabled;
    private boolean rightRedstoneOutputEnabled;
    private SoundRange soundRange;

    @SuppressWarnings("unused")
    public BandAmpMessage() {/* NOP */}

    public BandAmpMessage(BlockPos pos, boolean lockContainer, boolean rearRedstoneInputEnabled, boolean leftRedstoneOutputEnabled, boolean rightRedstoneOutputEnabled, SoundRange soundRange)
    {
        this.pos = pos;
        this.lockContainer = lockContainer;
        this.rearRedstoneInputEnabled = rearRedstoneInputEnabled;
        this.leftRedstoneOutputEnabled = leftRedstoneOutputEnabled;
        this.rightRedstoneOutputEnabled = rightRedstoneOutputEnabled;
        this.soundRange = soundRange;
    }

    @Override
    protected void read(PacketBuffer buffer)
    {
        pos = buffer.readBlockPos();
        lockContainer = buffer.readBoolean();
        rearRedstoneInputEnabled = buffer.readBoolean();
        leftRedstoneOutputEnabled = buffer.readBoolean();
        rightRedstoneOutputEnabled = buffer.readBoolean();
        soundRange = buffer.readEnumValue(SoundRange.class);
    }

    @Override
    protected void write(PacketBuffer buffer)
    {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(lockContainer);
        buffer.writeBoolean(rearRedstoneInputEnabled);
        buffer.writeBoolean(leftRedstoneOutputEnabled);
        buffer.writeBoolean(rightRedstoneOutputEnabled);
        buffer.writeEnumValue(soundRange);
    }

    @Override
    public void process(EntityPlayer player, Side side)
    {
        if(player.world.isBlockLoaded(pos))
        {
            TileEntity tileEntity = player.world.getTileEntity(pos);
            processLockButton(player, tileEntity);
            processButtons(player, tileEntity);
        }
    }

    private void processLockButton(EntityPlayer player, TileEntity tileEntity)
    {
        if(tileEntity instanceof IModLockableContainer)
        {
            IModLockableContainer lockableContainer = (IModLockableContainer) tileEntity;
            OwnerUUID ownerUUID = new OwnerUUID(player.getPersistentID());

            if(lockableContainer.isOwner(ownerUUID))
            {
                if(lockContainer)
                {
                    LockCode lockCode = new LockCode("Locked by Owner");
                    lockableContainer.setLockCode(lockCode);
                }
                else
                    lockableContainer.setLockCode(LockCode.EMPTY_CODE);
            }
        }
    }

    private void processButtons(EntityPlayer entityPlayer, TileEntity tileEntity)
    {
        // Process updates only for the owner
        if(tileEntity instanceof TileBandAmp && ((TileBandAmp) tileEntity).getOwner().getUUID().equals(entityPlayer.getPersistentID()))
        {
            TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
            tileBandAmp.setRearRedstoneInputEnabled(rearRedstoneInputEnabled);
            tileBandAmp.setLeftRedstoneOutputEnabled(leftRedstoneOutputEnabled);
            tileBandAmp.setRightRedstoneOutputEnabled(rightRedstoneOutputEnabled);
            if (soundRange == SoundRange.INFINITY && !MusicOptionsUtil.isSoundRangeInfinityAllowed(entityPlayer))
            {
                tileBandAmp.setSoundRange(SoundRange.NORMAL);
                entityPlayer.sendMessage(new TextComponentTranslation("mxtune.gui.bandAmp.soundRangeInfinityNotAllowed"));
                audiblePingPlayer(entityPlayer, SoundEvents.BLOCK_NOTE_PLING);
            }
            else {
                tileBandAmp.setSoundRange(soundRange);
            }
        }
    }
}
