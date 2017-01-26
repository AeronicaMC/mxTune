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

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPlacedInstrument
{
    /**
     * Get the TE associated with the block
     * @param worldIn
     * @param pos
     * @return
     */
    @SuppressWarnings("unchecked")
    default public <T extends TileInstrument> T getTE(World worldIn, BlockPos pos) {return (T) worldIn.getTileEntity(pos);}
    
    /**
     * Get the patch for this placed instrument
     * @return GM 1 Patch in the range of 1-127
     */
    public int getPatch();
}
