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
package net.aeronica.mods.mxtune.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class TileInstrument extends TileEntity
{
    protected ItemStackHandler inventory;
    protected Direction facing = Direction.NORTH;

    public TileInstrument() {}

    public TileInstrument(Direction facing) {this.facing = facing;}

    public Direction getFacing() {return facing;}

    @Override
    public void readFromNBT(CompoundNBT tag)
    {
        super.readFromNBT(tag);
        facing = Direction.byIndex(tag.getInteger("facing"));
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tag)
    {
        tag.setInteger("facing", facing.getIndex());
        return super.writeToNBT(tag);
    }

    /**
     * 1.9.4 TE Syncing
     * https://gist.github.com/williewillus/7945c4959b1142ece9828706b527c5a4
     * 
     * When the chunk/block data is sent:
     * 
     * - getUpdateTag() called to get compound to sync - this tag must include
     * coordinate and id tags - vanilla TE's write ALL data into this tag by
     * calling writeToNBT
     * 
     * When TE is resynced:
     * 
     * - getUpdatePacket() called to get a SPacketUpdateTileEntity (this is more
     * limited than it used to) - the packet itself holds the pos, compound
     * itself need not include coordinates - compound can contain whatever you'd
     * like, since it just comes back to you in onDataPacket() - vanilla just
     * delegates to getUpdateTag(), writing ALL te data, coordinates, and id
     * into the packet, and reading it all out on the other side - but mods
     * don't have to
     * 
     */
    @Override
    public CompoundNBT getUpdateTag()
    {
        CompoundNBT tag = super.getUpdateTag();
        return this.writeToNBT(tag);
    }

    /*
     Needed when block states can change
     */
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, BlockState oldState, BlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT cmp = new CompoundNBT();
        writeToNBT(cmp);
        return new SUpdateTileEntityPacket(pos, 1, cmp);
    }

    @Override
    public void onDataPacket(NetworkManager manager, SUpdateTileEntityPacket packet)
    {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public <T> T getCapability(Capability<T> cap, @Nullable Direction side)
    {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) { return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory); }
        return super.getCapability(cap, side);
    }
    
    public IItemHandlerModifiable getInventory() {return inventory;}
}
