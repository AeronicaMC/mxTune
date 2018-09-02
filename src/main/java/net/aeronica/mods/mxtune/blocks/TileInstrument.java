/**
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

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class TileInstrument extends TileEntity
{
    protected ItemStackHandler inventory = new StackHandler(1);
    protected EnumFacing facing = EnumFacing.NORTH;

    public TileInstrument() {}

    public TileInstrument(EnumFacing facing) {this.facing = facing;}

    public EnumFacing getFacing() {return facing;}

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        inventory = new StackHandler(1); // ItemStackHandler
        inventory.deserializeNBT(tag);
        facing = EnumFacing.byIndex(tag.getInteger("facing"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        tag.merge(inventory.serializeNBT());
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
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound tag = super.getUpdateTag();
        return this.writeToNBT(tag);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound cmp = new NBTTagCompound();
        writeToNBT(cmp);
        return new SPacketUpdateTileEntity(pos, 1, cmp);
    }

    @Override
    public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet)
    {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public boolean hasCapability(Capability<?> cap, EnumFacing side)
    {
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(cap, side);
    }

    @Override
    public <T> T getCapability(Capability<T> cap, EnumFacing side)
    {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) { return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory); }
        return super.getCapability(cap, side);
    }

    public void syncToClient()
    {
        markDirty();
        if (world != null)
        {
            if (!world.isRemote && !this.isInvalid())
            {
                IBlockState state = world.getBlockState(getPos());
                /**
                 * Sets the block state at a given location. Flag 1 will cause a
                 * block update. Flag 2 will send the change to clients (you
                 * almost always want this). Flag 4 prevents the block from
                 * being re-rendered, if this is a client world. Flags can be
                 * added together.
                 */
                world.notifyBlockUpdate(getPos(), state, state, 3);
            }
        }
    }

    class StackHandler extends ItemStackHandler
    {
        protected StackHandler(int size) {super(size);}

        @Override
        protected void onLoad()
        {
            super.onLoad();
            syncToClient();
        }

        @Override
        public void onContentsChanged(int slot) {syncToClient();}
    }
    
    public IItemHandlerModifiable getInventory() {return inventory;}
}
