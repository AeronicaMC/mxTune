/*
 * Aeronica's mxTune MOD
 * Copyright 2018, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.aeronica.mods.mxtune.init;

import net.aeronica.mods.mxtune.MXTune;
import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.entity.EntitySittableBlock;
import net.aeronica.mods.mxtune.entity.living.EntityGoldenSkeleton;
import net.aeronica.mods.mxtune.entity.living.EntityTimpani;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public enum ModEntities
{
    ;
    protected static int entityID = 0;
    
    private static int getEntityID() {return entityID++;}

    public static void init()
    {
        EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "mountableblock"), EntitySittableBlock.class, "mountableblock", getEntityID(), MXTune.instance,80, 1, false);
        EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "mob_golden_skeleton"), EntityGoldenSkeleton.class, "mxtune:mob_golden_skeleton", getEntityID(), MXTune.instance, 64, 1, true, 0x000000, 0xE6BA50);
        EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "mob_timpani"), EntityTimpani.class, "mxtune:mob_timpani", getEntityID(), MXTune.instance, 64, 1, true, 0x000000, 0xFF5121);
    }
}
