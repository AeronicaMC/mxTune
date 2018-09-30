package net.aeronica.mods.mxtune.blocks;

import com.google.common.collect.Maps;
import net.aeronica.mods.mxtune.MXTuneMain;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

import java.util.Map;

public class TileIdFixer implements IFixableData
{
    private static final Map<String, String> OLD_TO_NEW_ID_MAP = Maps.newHashMap();
    static
    {
        OLD_TO_NEW_ID_MAP.put("minecraft:tile_piano", "mxtune:tile_piano");
    }
    @Override
    public int getFixVersion()
    {
        return MXTuneMain.MXTUNE_DATA_FIXER_VERSION;
    }

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound)
    {
        String s = OLD_TO_NEW_ID_MAP.get(compound.getString("id"));
        if (s != null)
        {
            compound.setString("id", s);
        }
        return compound;
    }
}
