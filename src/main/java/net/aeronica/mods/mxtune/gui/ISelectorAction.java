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

package net.aeronica.mods.mxtune.gui;

import java.nio.file.Path;

/**
 * <p>Scarfed from MineTunes by Vazkii</p>
 * <p>https://github.com/Vazkii/MineTunes</p>
 * <p><a ref="https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB">
 * Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0)</a></p>
 */
public interface ISelectorAction
{
    void select(Path path);

    void select(String title, String mml);
}
