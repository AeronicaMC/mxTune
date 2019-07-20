/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
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

package net.aeronica.mods.mxtune.util;

import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

@SuppressWarnings("unchecked")
public class ResultMessage extends Tuple
{
    public static final ResultMessage NO_ERROR = new ResultMessage(false, new TranslationTextComponent("mxtune.no_error"));

    public ResultMessage(Boolean errorResult, ITextComponent message)
    {
        super(errorResult, message);
    }

    public Boolean hasError()
    {
        return (Boolean) super.getFirst();
    }

    public ITextComponent getMessage()
    {
        return ((ITextComponent) super.getSecond());
    }
}
