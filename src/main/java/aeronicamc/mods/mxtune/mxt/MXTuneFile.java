
package aeronicamc.mods.mxtune.mxt;

import net.minecraft.nbt.CompoundNBT;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MXTuneFile implements Serializable
{
    private static final long serialVersionUID = -76044260522231311L;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MXT_VERSION = "2.0.0";
    private static final String TAG_TITLE = "title";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_SOURCE = "source";
    private static final String TAG_DURATION = "duration";
    private static final String TAG_PART_PREFIX = "part";
    private static final String TAG_PART_COUNT = "partCount";
    private static final String TAG_MXT_VERSION = "mxtVersion";
    private static final String ERROR_MSG_MXT_VERSION = "Unsupported mxTune file version! expected {}, found {}, Title: {}";

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

    public static MXTuneFile build(CompoundNBT compound)
    {
        MXTuneFile mxTuneFile = new MXTuneFile();
        mxTuneFile.readFromNBT(compound);
        return mxTuneFile;
    }

    public void readFromNBT(CompoundNBT compound)
    {
        mxtVersion = compound.getString(TAG_MXT_VERSION);
        title = compound.getString(TAG_TITLE);
        author = compound.getString(TAG_AUTHOR);
        source = compound.getString(TAG_SOURCE);
        duration = compound.getInt(TAG_DURATION);
        int partCount = compound.getInt(TAG_PART_COUNT);

        parts = new ArrayList<>();
        for (int i = 0; i < partCount; i++)
        {
            CompoundNBT compoundPart = (CompoundNBT) compound.get(TAG_PART_PREFIX + i);
            if (compoundPart != null)
                parts.add(new MXTunePart(compoundPart));
            else
                break;
        }

        if (MXT_VERSION.compareTo(mxtVersion) < 0 || mxtVersion.isEmpty())
            LOGGER.warn(ERROR_MSG_MXT_VERSION, MXT_VERSION, mxtVersion.isEmpty() ? "No Version" : mxtVersion, title);
    }

    public void writeToNBT(CompoundNBT compound)
    {
        compound.putString(TAG_MXT_VERSION, MXT_VERSION);
        compound.putString(TAG_TITLE, title);
        compound.putString(TAG_AUTHOR, author);
        compound.putString(TAG_SOURCE, source);
        compound.putInt(TAG_DURATION, duration);
        compound.putInt(TAG_PART_COUNT, parts.size());

        int i = 0;
        for (MXTunePart part : parts)
        {
            CompoundNBT compoundPart = new CompoundNBT();
            part.writeToNBT(compoundPart);

            compound.put(TAG_PART_PREFIX + i, compoundPart);
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

    public void setParts(List<MXTunePart> parts) { this.parts = parts; }

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
