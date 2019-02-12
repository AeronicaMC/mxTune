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

import javax.annotation.Nullable;
import java.nio.file.Path;

public class ActionGet implements ISelectorAction
{
    public static final ActionGet INSTANCE = new ActionGet();

    private Path path;
    private String title;
    private String mml;

    @Override
    public void select(Path path) { this.path = path; }

    @Override
    public void select(String title, String mml)
    {
        this.title = title;
        this.mml = mml;
    }

    @Nullable
    Path getFileName() { return path != null ? path.getFileName() : null; }

    @Nullable
    String getFileNameString() { return path != null ? (path.getFileName().toString()) : null; }

    @Nullable
    public Path getPath() { return path; }

    @Nullable
    public String getTitle() { return title; }

    @Nullable
    public String getMml() { return mml; }

    enum SELECTOR
    {
        FILE,
        PASTE,
        CANCEL;
    }
}
