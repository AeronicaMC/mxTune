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

import net.aeronica.mods.mxtune.MXTuneMain;
import net.aeronica.mods.mxtune.blocks.BlockPiano;
import net.aeronica.mods.mxtune.blocks.RendererPiano;
import net.aeronica.mods.mxtune.blocks.TilePiano;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class BlockModels
{
    private static final String modid = MXTuneMain.MODID.toLowerCase();
    private static final int DEFAULT_ITEM_SUBTYPE = 0;

    public static void register()
    {
        /*
         * http://www.minecraftforge.net/forum/index.php/topic,28997.msg149446.
         * html#msg149446 (from above link by Bedrock_Miner) I did a bit of
         * search and read in the minecraft code and figured out the following:
         * The excluding magic in Vanilla happens in the method
         * BlockModelShapes.registerAllBlocks(). There, the blocks are
         * registered with the corresponding blockstate names and a StateMap.
         * The exclusion happens in the StateMap. The properties to exclude are
         * saved in the field listProperties. The StateMap can also map
         * different blockstates to different files. The property which controls
         * the mapping is saved in the property field. The value of this
         * property (if existent) is used as the name for the blockstates file,
         * if property is null, the block name is used. The value of the field
         * suffix is then appended to the name. The property of the mapping is
         * also excluded from the final property map. FINALLY: Doing the
         * exclusion for modded Blocks: It's relatively easy as well... During
         * the preInit phase (maybe before the addVariantName method call) you
         * need to execute this method: BlockModelShapes.registerAllBlocks()
         * this.registerBlockWithStateMapper((Blocks.bed, (new
         * StateMap.Builder()).ignore(new IProperty[]
         * {BlockBed.OCCUPIED}).build()); This lets us IGNORE specified
         * properties in our blockstates JSON. It reduces the number of states
         * you need to account for. The builder class provides the necessary
         * methods to create a StateMap which can exclude properties or name the
         * blockstate files after one property.
         */
        ModelLoader.setCustomStateMapper(StartupBlocks.block_piano, new StateMap.Builder().ignore(new IProperty[]
        {
                BlockPiano.OCCUPIED
        }).build());

        ModelLoader.setCustomModelResourceLocation(StartupBlocks.item_piano, DEFAULT_ITEM_SUBTYPE, new ModelResourceLocation(modid + ":" + "block_piano", "inventory"));

        ClientRegistry.bindTileEntitySpecialRenderer(TilePiano.class, new RendererPiano());
    }
}
