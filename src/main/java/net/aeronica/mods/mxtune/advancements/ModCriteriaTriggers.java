/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, 2015 Pokefenn, ljfa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.aeronica.mods.mxtune.advancements;

import java.lang.reflect.Method;

import net.aeronica.mods.mxtune.advancements.criterion.PlayInstrumentTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * Copied and adapted to my needs from code by:
 * @author Pokefenn, ljfa
 * </br><a href="https://github.com/TeamTotemic/Totemic">https://github.com/TeamTotemic/Totemic</a>
 */
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
