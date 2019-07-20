/*
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

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.items.ItemStackHandler;

public class TilePiano extends TileInstrument
{
    @SuppressWarnings("unused")
    public TilePiano() {/* NOP */}

    TilePiano(Direction facing)
    {
        this.inventory = new SheetMusicStackHandler(1);
        this.facing = facing;
    }

    @Override
    public void readFromNBT(CompoundNBT tag)
    {
        super.readFromNBT(tag);
        inventory = new SheetMusicStackHandler(1);
        inventory.deserializeNBT(tag);
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tag)
    {
        tag.merge(inventory.serializeNBT());
        return super.writeToNBT(tag);
    }

    void syncToClient()
    {
        markDirty();
        if (world != null && !world.isRemote && !this.isInvalid())
        {
            BlockState state = world.getBlockState(getPos());
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

    class SheetMusicStackHandler extends ItemStackHandler
    {
        private SheetMusicStackHandler(int size) {super(size);}

        @Override
        protected void onLoad()
        {
            super.onLoad();
            syncToClient();
        }
    }
}
