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

import net.aeronica.mods.mxtune.Reference;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.aeronica.mods.mxtune.util.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MXTuneFile
{
    private static final String MXT_VERSION = "1.0.0";
    private static final String TAG_TITLE = "title";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_SOURCE = "source";
    private static final String TAG_PART_PREFIX = "part";
    private static final String TAG_PART_COUNT = "partCount";
    private static final String TAG_CREATED_ON = "createdOn";
    private static final String TAG_MODIFIED_ON = "modifiedOn";
    private static final String TAG_CREATED_BY = "createdBy";
    private static final String TAG_MODIFIED_BY = "modifiedBy";
    private static final String TAG_MXT_VERSION = "mxtVersion";

    private String title = "";
    private String author = "";
    private String source = "";
    private List<MXTunePart> parts;
    private ZonedDateTime createdOn;
    private ZonedDateTime modifiedOn;
    private UUID createdBy;
    private UUID modifiedBy;

    public MXTuneFile()
    {
        parts = new ArrayList<>();
        createdBy = Reference.EMPTY_UUID;
        modifiedBy = Reference.EMPTY_UUID;
        LocalDateTime ldtNow = LocalDateTime.MIN;
        createdOn = ZonedDateTime.of(ldtNow, ZoneId.of("UTC"));
        modifiedOn = createdOn;
    }

    public MXTuneFile(UUID createdBy, LocalDateTime createdOn)
    {
        this();
        this.createdBy = createdBy;
        this.createdOn = ZonedDateTime.of(createdOn, ZoneId.of("UTC"));
    }

    public static MXTuneFile build(NBTTagCompound compound)
    {
        String mxtVersion = compound.getString(TAG_MXT_VERSION);
        if (MXT_VERSION.compareTo(mxtVersion) < 0 || mxtVersion.equals(""))
            ModLogger.warn("Unsupported mxTune file version! expected %s, found %s", MXT_VERSION, mxtVersion);
        String title = compound.getString(TAG_TITLE);
        String author = compound.getString(TAG_AUTHOR);
        String source = compound.getString(TAG_SOURCE);
        ZonedDateTime createdOn;
        ZonedDateTime modifiedOn;
        try
        {
            createdOn = ZonedDateTime.parse(compound.getString(TAG_CREATED_ON), DateTimeFormatter.ISO_ZONED_DATE_TIME);
            modifiedOn = ZonedDateTime.parse(compound.getString(TAG_MODIFIED_ON), DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }
        catch (DateTimeParseException e)
        {
            ModLogger.error("Invalid data. Re-initializing createdOn and modifiedOn dates");
            ModLogger.error(e);
            LocalDateTime ldtNow = LocalDateTime.MIN;
            createdOn = ZonedDateTime.of(ldtNow, ZoneId.of("UTC"));
            modifiedOn = ZonedDateTime.now();
        }
        UUID createdBy = NBTHelper.getUuidFromTag(compound, TAG_CREATED_BY);
        UUID modifiedBy = NBTHelper.getUuidFromTag(compound, TAG_MODIFIED_BY);
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
        mxTuneFile.createdOn = createdOn;
        mxTuneFile.modifiedOn = modifiedOn;
        mxTuneFile.createdBy = createdBy;
        mxTuneFile.modifiedBy = modifiedBy;
        return mxTuneFile;
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString(TAG_MXT_VERSION, MXT_VERSION);
        applyUserDateTime(false);
        compound.setString(TAG_TITLE, title);
        compound.setString(TAG_AUTHOR, author);
        compound.setString(TAG_SOURCE, source);
        compound.setInteger(TAG_PART_COUNT, parts.size());
        compound.setString(TAG_CREATED_ON, createdOn.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        compound.setString(TAG_MODIFIED_ON, modifiedOn.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        NBTHelper.setUuidToTag(createdBy, compound, TAG_CREATED_BY);
        NBTHelper.setUuidToTag(modifiedBy, compound, TAG_MODIFIED_BY);

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

    public ZonedDateTime getCreatedOn()
    {
        return createdOn;
    }

    public void setCreatedOn(ZonedDateTime createdOn)
    {
        this.createdOn = createdOn;
    }

    public ZonedDateTime getModifiedOn()
    {
        return modifiedOn;
    }

    public void setModifiedOn(ZonedDateTime modifiedOn)
    {
        this.modifiedOn = modifiedOn;
    }

    public UUID getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy)
    {
        this.createdBy = createdBy;
    }

    public UUID getModifiedBy()
    {
        return modifiedBy;
    }

    public void setModifiedBy(UUID modifiedBy)
    {
        this.modifiedBy = modifiedBy;
    }

    @SideOnly(Side.CLIENT)
    public void applyUserDateTime(boolean setAll)
    {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null)
        {
            modifiedBy = player.getPersistentID();
            modifiedOn = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC"));
            if (setAll)
            {
               createdBy = modifiedBy;
               createdOn = modifiedOn;
            }
        }
    }
}
