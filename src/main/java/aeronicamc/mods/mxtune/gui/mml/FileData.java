package aeronicamc.mods.mxtune.gui.mml;

import aeronicamc.mods.mxtune.caches.FileHelper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

    public Path getPath()
    {
        return path;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public int compareTo(FileData o)
    {
        return this.name.compareTo(o.name);
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
        return new EqualsBuilder()
                .append(path, ((FileData) o).path)
                .append(name, ((FileData) o).path)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(path)
                .append(name)
                .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("path", path)
                .append("name", name)
                .toString();
    }
}
