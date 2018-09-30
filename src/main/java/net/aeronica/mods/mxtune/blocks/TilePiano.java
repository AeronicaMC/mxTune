/**
 * Aeronica's mxTune MOD
 * Copyright {2016} Paul Boese aka Aeronica
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
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.ItemStackHandler;

public class TilePiano extends TileInstrument
{
    @SuppressWarnings("unused")
    public TilePiano() {/* Needed for vanilla processing */}

    public TilePiano(EnumFacing facing)
    {
        this.inventory = new StackHandler(1);
        this.facing = facing;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        inventory = new StackHandler(1);
        inventory.deserializeNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        tag.merge(inventory.serializeNBT());
        return super.writeToNBT(tag);
    }

    public void syncToClient()
    {
        markDirty();
        if (world != null)
        {
            if (!world.isRemote && !this.isInvalid())
            {
                IBlockState state = world.getBlockState(getPos());
                /*
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
}
