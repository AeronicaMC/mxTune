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

package net.aeronica.mods.mxtune.mxt;

import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MXTuneFile implements Serializable
{
    private static final long serialVersionUID = -76044260522231311L;
    private static final String MXT_VERSION = "2.0.0";
    private static final String TAG_TITLE = "title";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_SOURCE = "source";
    private static final String TAG_DURATION = "duration";
    private static final String TAG_PART_PREFIX = "part";
    private static final String TAG_PART_COUNT = "partCount";
    private static final String TAG_MXT_VERSION = "mxtVersion";
    private static final String ERROR_MSG_MXT_VERSION = "Unsupported mxTune file version! expected %s, found %s, Title: %s";

    private String mxtVersion;
    private String title;
    private String author;
    private String source;
    private int duration;
    private List<MXTunePart> parts = new ArrayList<>();

    public MXTuneFile()
    {
        mxtVersion = "";
        title = "";
        author = "";
        source = "";

    }

    public static MXTuneFile build(NBTTagCompound compound)
    {
        MXTuneFile mxTuneFile = new MXTuneFile();
        mxTuneFile.readFromNBT(compound);
        return mxTuneFile;
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        mxtVersion = compound.getString(TAG_MXT_VERSION);
        title = compound.getString(TAG_TITLE);
        author = compound.getString(TAG_AUTHOR);
        source = compound.getString(TAG_SOURCE);
        duration = compound.getInteger(TAG_DURATION);
        int partCount = compound.getInteger(TAG_PART_COUNT);

        parts = new ArrayList<>();
        for (int i = 0; i < partCount; i++)
        {
            NBTTagCompound compoundPart = compound.getCompoundTag(TAG_PART_PREFIX + i);
            parts.add(new MXTunePart(compoundPart));
        }

        if (MXT_VERSION.compareTo(mxtVersion) < 0 || mxtVersion.equals(""))
            ModLogger.warn(ERROR_MSG_MXT_VERSION, MXT_VERSION, mxtVersion.equals("") ? "No Version" : mxtVersion, title);
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString(TAG_MXT_VERSION, MXT_VERSION);
        compound.setString(TAG_TITLE, title);
        compound.setString(TAG_AUTHOR, author);
        compound.setString(TAG_SOURCE, source);
        compound.setInteger(TAG_DURATION, duration);
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

    public String getMxtVersion()
    {
        return mxtVersion;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title.trim();
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

    public int getDuration()
    {
        return duration;
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public List<MXTunePart> getParts()
    {
        return parts;
    }

    public void setParts(List<MXTunePart> parts) { this.parts = parts != null ? parts : new ArrayList<>(); }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(mxtVersion)
                .append(title)
                .append(author)
                .append(source)
                .append(duration)
                .append(parts)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MXTuneFile mxTuneFile = (MXTuneFile) o;
        return new EqualsBuilder()
                .append(mxtVersion, mxTuneFile.getMxtVersion())
                .append(title, mxTuneFile.getTitle())
                .append(author, mxTuneFile.getAuthor())
                .append(source, mxTuneFile.getSource())
                .append(duration, mxTuneFile.getDuration())
                .append(parts, mxTuneFile.getParts())
                .isEquals();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("mxtVersion", mxtVersion)
                .append("title", title)
                .append("author", author)
                .append("source", source)
                .append("duration", duration)
                .append("parts", parts)
                .toString();
    }
}
