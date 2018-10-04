package net.aeronica.mods.mxtune.datafixers;

import com.google.common.collect.ImmutableSet;
import net.aeronica.mods.mxtune.util.ModLogger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

public class CapInventoryWalker implements IDataWalker
{
    private final Set<ResourceLocation> ids;

    public CapInventoryWalker(Class<? extends TileEntity> te)
    {
        this(ImmutableSet.of(te));
    }

    public CapInventoryWalker(Set<Class<? extends TileEntity>> teTypes)
    {
        this.ids = teTypes.stream().map(TileEntity::getKey).collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public NBTTagCompound process(@Nonnull IDataFixer fixer, @Nonnull NBTTagCompound te, int version)
    {
        if (ids.contains(new ResourceLocation(te.getString("id"))))
        {
            DataFixesManager.processInventory(fixer, te, version, "Items");
            ModLogger.debug("CapInventoryWalker Walked inventory of TE {}, containing {} items", te.getString("id"), te.getInteger("Size"));
        }

        return te;
    }
}
