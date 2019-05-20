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

package net.aeronica.mods.mxtune.gui.mml;

import net.aeronica.mods.mxtune.caches.FileHelper;

import java.nio.file.Path;

public class FileData implements Comparable<FileData>
{
    final Path path;
    final String name;

    public FileData(Path path)
    {
        this.path = path;
        this.name = FileHelper.removeExtension(path.getFileName().toString());
    }

    @Override
    public int compareTo(FileData o)
    {
        return this.name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof FileData &&
                this.path != null && this.name != null &&
                this.path.equals(((FileData)obj).path) && this.name.equals(((FileData) obj).name);
    }

    @Override
    public int hashCode()
    {
        return path != null && name != null ? this.path.hashCode() * this.name.hashCode() : super.hashCode();
    }
}
