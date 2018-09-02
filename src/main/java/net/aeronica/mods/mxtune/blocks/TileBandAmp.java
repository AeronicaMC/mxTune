package net.aeronica.mods.mxtune.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class TileBandAmp extends TileInstrument
{
    private int outputSignal;

    public TileBandAmp() { /* NOP */ }

    public TileBandAmp(EnumFacing facing)
    {
        this.inventory =  new StackHandler(8);
        this.facing = facing;
    }

    public int getOutputSignal() { return outputSignal; }

    public void setOutputSignal(int i)
    {
        outputSignal = i;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        outputSignal = tag.getInteger("output_signal");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        tag.setInteger("output_signal", outputSignal);
        return super.writeToNBT(tag);
    }
}
