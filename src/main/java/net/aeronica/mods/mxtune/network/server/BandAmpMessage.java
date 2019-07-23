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
import net.aeronica.mods.mxtune.options.MusicOptionsUtil;
import net.aeronica.mods.mxtune.sound.SoundRange;
import net.aeronica.mods.mxtune.world.IModLockableContainer;
import net.aeronica.mods.mxtune.world.OwnerUUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static net.aeronica.mods.mxtune.util.Miscellus.audiblePingPlayer;

public class BandAmpMessage
{
    private final boolean lockContainer;
    private final BlockPos pos;
    private final boolean rearRedstoneInputEnabled;
    private final boolean leftRedstoneOutputEnabled;
    private final boolean rightRedstoneOutputEnabled;
    private final SoundRange soundRange;

    public BandAmpMessage(BlockPos posIn, boolean lockContainerIn, boolean rearRedstoneInputEnabledIn, boolean leftRedstoneOutputEnabledIn, boolean rightRedstoneOutputEnabledIn, SoundRange soundRangeIn)
    {
        this.pos = posIn;
        this.lockContainer = lockContainerIn;
        this.rearRedstoneInputEnabled = rearRedstoneInputEnabledIn;
        this.leftRedstoneOutputEnabled = leftRedstoneOutputEnabledIn;
        this.rightRedstoneOutputEnabled = rightRedstoneOutputEnabledIn;
        this.soundRange = soundRangeIn;
    }

    public static BandAmpMessage decode(final PacketBuffer buffer)
    {
        final BlockPos pos = buffer.readBlockPos();
        final boolean lockContainer = buffer.readBoolean();
        final boolean rearRedstoneInputEnabled = buffer.readBoolean();
        final boolean leftRedstoneOutputEnabled = buffer.readBoolean();
        final boolean rightRedstoneOutputEnabled = buffer.readBoolean();
        final SoundRange soundRange = buffer.readEnumValue(SoundRange.class);
        return new BandAmpMessage(pos, lockContainer, rearRedstoneInputEnabled, leftRedstoneOutputEnabled, rightRedstoneOutputEnabled, soundRange);
    }

    public static void encode(final BandAmpMessage message, final PacketBuffer buffer)
    {
        buffer.writeBlockPos(message.pos);
        buffer.writeBoolean(message.lockContainer);
        buffer.writeBoolean(message.rearRedstoneInputEnabled);
        buffer.writeBoolean(message.leftRedstoneOutputEnabled);
        buffer.writeBoolean(message.rightRedstoneOutputEnabled);
        buffer.writeEnumValue(message.soundRange);
    }

    public static void handle(final BandAmpMessage message, final Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
            {
                final ServerPlayerEntity player = ctx.get().getSender();
                if (player == null) return;
                final World world = player.world;
                if (world.isRemote) return;

                if(world.isAreaLoaded(message.pos, 1))
                {
                    TileEntity tileEntity = world.getTileEntity(message.pos);
                    processLockButton(message, player, tileEntity);
                    processButtons(message, player, tileEntity);
                }
            });
        ctx.get().setPacketHandled(true);
    }

    private static void processLockButton(BandAmpMessage message, PlayerEntity player, TileEntity tileEntity)
    {
        if(tileEntity instanceof IModLockableContainer)
        {
            IModLockableContainer lockableContainer = (IModLockableContainer) tileEntity;
            OwnerUUID ownerUUID = new OwnerUUID(player.getUniqueID());

            if(lockableContainer.isOwner(ownerUUID))
            {
                if(message.lockContainer)
                {
                    LockCode lockCode = new LockCode("Locked by Owner");
                    lockableContainer.setLockCode(lockCode);
                }
                else
                    lockableContainer.setLockCode(LockCode.EMPTY_CODE);
            }
        }
    }

    private static void processButtons(BandAmpMessage message, PlayerEntity entityPlayer, TileEntity tileEntity)
    {
        // Process updates only for the owner
        if(tileEntity instanceof TileBandAmp && ((TileBandAmp) tileEntity).getOwner().getUUID().equals(entityPlayer.getUniqueID()))
        {
            TileBandAmp tileBandAmp = (TileBandAmp) tileEntity;
            tileBandAmp.setRearRedstoneInputEnabled(message.rearRedstoneInputEnabled);
            tileBandAmp.setLeftRedstoneOutputEnabled(message.leftRedstoneOutputEnabled);
            tileBandAmp.setRightRedstoneOutputEnabled(message.rightRedstoneOutputEnabled);
            if (message.soundRange == SoundRange.INFINITY && !MusicOptionsUtil.isSoundRangeInfinityAllowed(entityPlayer))
            {
                tileBandAmp.setSoundRange(SoundRange.NORMAL);
                entityPlayer.sendMessage(new TranslationTextComponent("mxtune.gui.bandAmp.soundRangeInfinityNotAllowed"));
                audiblePingPlayer(entityPlayer, SoundEvents.BLOCK_NOTE_BLOCK_PLING);
            }
            else {
                tileBandAmp.setSoundRange(message.soundRange);
            }
        }
    }
}
