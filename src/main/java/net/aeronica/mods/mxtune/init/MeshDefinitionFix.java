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
package net.aeronica.mods.mxtune.init;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;

/**
 * A hackish adapter that allows lambdas to be used as {@link ItemMeshDefinition} implementations without breaking ForgeGradle's
 * reobfuscation and causing {@link AbstractMethodError}s.
 * <p>
 * Written by diesieben07 in this thread:
 * http://www.minecraftforge.net/forum/index.php/topic,34034.0.html
 *
 * @author diesieben07
 */
interface MeshDefinitionFix extends ItemMeshDefinition {
    ModelResourceLocation getLocation(ItemStack stack);

    // Helper method to easily create lambda instances of this class
    static ItemMeshDefinition create(MeshDefinitionFix lambda) {
        return lambda;
    }

    default ModelResourceLocation getModelLocation(ItemStack stack) {
        return getLocation(stack);
    }
}
