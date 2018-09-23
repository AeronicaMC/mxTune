package net.aeronica.mods.mxtune.util;

import net.minecraft.util.EnumFacing;

public enum EnumRelativeSide
{
    BACK(0, "back"),
    RIGHT(1, "right"),
    FRONT(2, "front"),
    LEFT(3, "left"),
    TOP(4, "top"),
    BOTTOM(5, "bottom"),
    ERROR(6, "error");

    private int index;
    private String name;

    EnumRelativeSide(int index, String name)
    {
        this.index = index;
        this.name = name;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    public String getName()
    {
        return this.name;
    }

    public static final EnumRelativeSide[] ENUM_RELATIVE_SIDES = new EnumRelativeSide[]{
            FRONT, LEFT, BACK, RIGHT,
            RIGHT, FRONT, LEFT, BACK,
            BACK, RIGHT, FRONT, LEFT,
            LEFT, BACK, RIGHT, FRONT,
            };

    /**
     * Given the capability EnumFacing side and EnumFacing facing direction of the block return the side relative to
     * the front of block in terms of BACK, RIGHT, FRONT, LEFT, TOP, and BOTTOM. This makes ie easier to keep track of
     * the sides of a block relative to its front for automated slot access.
     * @param side getCapability EnumFacing side
     * @param facing horizontal EnumFacing property facing of the block
     * @return BACK, RIGHT, FRONT, LEFT, TOP, BOTTOM or ERROR if side and/or facing are null
     */
    public static EnumRelativeSide getRelativeSide(EnumFacing side, EnumFacing facing)
    {
        EnumRelativeSide enumRelativeSide;
        int index = -1;

        if (side == null || facing == null)
            enumRelativeSide = ERROR;
        else if (side == EnumFacing.UP)
            enumRelativeSide = TOP;
        else if (side == EnumFacing.DOWN)
            enumRelativeSide = BOTTOM;
        else
        {
            index = facing.getHorizontalIndex() * 4 + side.getHorizontalIndex();
            enumRelativeSide = ENUM_RELATIVE_SIDES[index];
        }
        ModLogger.info("getRelativeSide index: %d, location: %s", index, enumRelativeSide);
        return enumRelativeSide;
    }
}
