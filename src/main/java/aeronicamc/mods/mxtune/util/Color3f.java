package aeronicamc.mods.mxtune.util;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Color3f implements Comparable<Color3f>
{
    protected static final float[][] rainbow =
            {{0F, 0F, 1F},
             {0F, 1F, 0F},
             {0F, 1F, 1F},
             {1F, 0F, 0F},
             {1F, 0F, 1F},
             {1F, 1F, 0F}};

    protected final float r;
    protected final float g;
    protected final float b;

    public Color3f(float r, float g, float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static Color3f rainbowFactory()
    {
        int row = RandomUtils.nextInt(0, rainbow.length/3);
        return new Color3f(rainbow[row][0], rainbow[row][1], rainbow[row][2]);
    }

    public float getR()
    {
        return r;
    }

    public float getG()
    {
        return g;
    }

    public float getB()
    {
        return b;
    }

    @Override
    public String toString()
    {
        int shift = RandomUtils.nextInt(1, 10);
        return String.format("Stage: %s", StringUtils.rotate("0123456789", shift));
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(r)
                .append(g)
                .append(b)
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
        Color3f color3f = (Color3f) o;
        return new EqualsBuilder()
                .append(r, color3f.getR())
                .append(g, color3f.getG())
                .append(b, color3f.getB())
                .isEquals();
    }

    @Override
    public int compareTo(Color3f o)
    {
        return new CompareToBuilder()
                .append(r, o.getR())
                .append(g, o.getG())
                .append(b, o.getB())
                .toComparison();
    }
}
