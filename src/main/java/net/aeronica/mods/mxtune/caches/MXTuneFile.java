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

package net.aeronica.mods.mxtune.caches;

import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

public class MXTuneFile
{
    private static final  String TAG_TITLE = "title";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_SOURCE = "source";
    private static final String TAG_PART_PREFIX = "part";
    private static final String TAG_PART_COUNT = "partCount";

    private String title = "";
    private String author = "";
    private String source = "";
    private List<MXTunePart> parts;

    public MXTuneFile()
    {
        parts = new ArrayList<>();
    }

    public static MXTuneFile build(NBTTagCompound compound)
    {
        String title = compound.getString(TAG_TITLE);
        String author = compound.getString(TAG_AUTHOR);
        String source = compound.getString(TAG_SOURCE);
        int partCount = compound.getInteger(TAG_PART_COUNT);

        List<MXTunePart> parts = new ArrayList<>();
        for (int i = 0; i < partCount; i++)
        {
            NBTTagCompound compoundPart = compound.getCompoundTag(TAG_PART_PREFIX + i);
            parts.add(new MXTunePart(compoundPart));
        }

        MXTuneFile mxTuneFile = new MXTuneFile();
        mxTuneFile.title = title;
        mxTuneFile.author = author;
        mxTuneFile.source = source;
        mxTuneFile.parts = parts;
        return mxTuneFile;
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString(TAG_TITLE, title);
        compound.setString(TAG_AUTHOR, author);
        compound.setString(TAG_SOURCE, source);
        compound.setInteger(TAG_PART_COUNT, parts.size());
        int i = 0;

        for (MXTunePart part : parts)
        {
            NBTTagCompound compoundPart = new NBTTagCompound();
            part.writeToNBT(compoundPart);

            compound.setTag(TAG_PART_PREFIX + i, compoundPart);
            i++;
        }
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public List<MXTunePart> getParts()
    {
        return parts;
    }

    @SuppressWarnings("unused")
    public void setParts(List<MXTunePart> parts) { this.parts = parts != null ? parts : new ArrayList<>(); }
}
