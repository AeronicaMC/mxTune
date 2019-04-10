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

package net.aeronica.libs.mml.readers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractMmlFileReader
{
    private Set<String> collectedErrors = new HashSet<>();

    protected String title = "";
    protected String author = "";
    protected String url = "";
    protected String mml = "";
    protected String[] meta = new String[0];

    public abstract boolean parseFile(Path path);

    public abstract String getFileExtension();

    public String getTitle() { return title; }

    public String getAuthor() { return author; }

    public String getUrl() { return url; }

    public String[] getMeta() { return meta; }

    public abstract String getMML();

    public boolean hasErrors()
    {
        return !collectedErrors.isEmpty();
    }

    public String[] getErrorMessages()
    {
        return (String[]) collectedErrors.toArray();
    }

    protected void addError(String message)
    {
        collectedErrors.add(message);
    }

    protected FileInputStream getFile(Path path)
    {
        FileInputStream is = null;
        try
        {
            is = new FileInputStream(path.toFile());
        } catch (FileNotFoundException e)
        {
            collectedErrors.add(e.getLocalizedMessage());
        }
        return is;
    }
}
