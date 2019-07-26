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
import net.minecraft.tileentity.TileEntityType;
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


    public TileInstrument(TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public TileInstrument(Direction facing)
    {
        this.facing = facing;
    }

    public Direction getFacing() {return facing;}

    @Override
    public void readFromNBT(CompoundNBT tag)
    {
        super.read(tag);
        facing = Direction.byIndex(tag.getInt("facing"));
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tag)
    {
        tag.putInt("facing", facing.getIndex());
        return super.write(tag);
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        CompoundNBT tag = super.getUpdateTag();
        return this.writeToNBT(tag);
    }

     // Needed when block states can change
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
