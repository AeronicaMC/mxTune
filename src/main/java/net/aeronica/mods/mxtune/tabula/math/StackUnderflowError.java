package net.aeronica.mods.mxtune.tabula.math;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author pau101
 * @since 1.0.0
 */
@SuppressWarnings("serial")
@SideOnly(Side.CLIENT)
public class StackUnderflowError extends Error {
    public StackUnderflowError() {
        super();
    }

    public StackUnderflowError(String s) {
        super(s);
    }
}
