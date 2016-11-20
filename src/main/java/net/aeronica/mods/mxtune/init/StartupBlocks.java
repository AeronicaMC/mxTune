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
package net.aeronica.mods.mxtune.init;

import net.aeronica.mods.mxtune.blocks.BlockPiano;
import net.aeronica.mods.mxtune.blocks.ItemPiano;
import net.aeronica.mods.mxtune.blocks.TileInstrument;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class StartupBlocks
{
    public static BlockPiano block_piano;
    public static ItemPiano item_piano;

    public static void register()
    {
        // each instance of your block should have a name that is unique within
        // your mod. use lower case
        // you must register the block and item for the block separately.
        GameRegistry.register(block_piano = (BlockPiano) new BlockPiano("block_piano"));
        GameRegistry.register(item_piano = (ItemPiano) new ItemPiano("block_piano"));

        GameRegistry.registerTileEntityWithAlternatives(TileInstrument.class, "TileInstrument", "PianoTile");
    }
}
