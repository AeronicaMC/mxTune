/**
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
package net.aeronica.mods.mxtune.advancements;

import java.lang.reflect.Method;

import net.aeronica.mods.mxtune.advancements.criterion.PlayInstrumentTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ModCriteriaTriggers
{

    public static final PlayInstrumentTrigger PLAY_INSTRUMENT = new PlayInstrumentTrigger();

    public static void init()
    {
        //TODO: Remove this once Forge supports registering your own criteria
        Method registerMethod = ReflectionHelper.findMethod(CriteriaTriggers.class, "register", "func_192118_a", ICriterionTrigger.class);
        try
        {
            registerMethod.invoke(null, PLAY_INSTRUMENT);
        }
        catch(ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        }
    }

}
