package aeronicamc.mods.mxtune.gui;

import net.minecraft.util.text.TextFormatting;

/*
 * Foreground colors useful with the FontRenderer#draw methods, and AbstractGui drawing methods.
 */
@SuppressWarnings({"unused", "ConstantConditions"})
public class TextColorFg
{
    private TextColorFg() { /* NOOP */}
    public static final int BLACK =         TextFormatting.BLACK.getColor();        // 0x000000
    public static final int DARK_BLUE =     TextFormatting.DARK_BLUE.getColor();    // 0x0000aa
    public static final int DARK_GREEN =    TextFormatting.DARK_GREEN.getColor();   // 0x00aa00
    public static final int DARK_AQUA =     TextFormatting.DARK_AQUA.getColor();    // 0x00aaaa
    public static final int DARK_RED =      TextFormatting.DARK_RED.getColor();     // 0xaa0000
    public static final int DARK_PURPLE =   TextFormatting.DARK_PURPLE.getColor();  // 0xaa00aa
    public static final int GOLD =          TextFormatting.DARK_GREEN.getColor();   // 0xffaa00
    public static final int GRAY =          TextFormatting.GRAY.getColor();         // 0xaaaaaa
    public static final int DARK_GRAY =     TextFormatting.DARK_GRAY.getColor();    // 0x555555
    public static final int BLUE =          TextFormatting.BLUE.getColor();         // 0x5555ff
    public static final int GREEN =         TextFormatting.GREEN.getColor();        // 0x55ff55
    public static final int AQUA =          TextFormatting.AQUA.getColor();         // 0x55ffff
    public static final int RED =           TextFormatting.RED.getColor();          // 0xff5555
    public static final int LIGHT_PURPLE =  TextFormatting.LIGHT_PURPLE.getColor(); // 0xff55ff
    public static final int YELLOW =        TextFormatting.YELLOW.getColor();       // 0xffff55
    public static final int WHITE =         TextFormatting.WHITE.getColor();        // 0xffffff
}
