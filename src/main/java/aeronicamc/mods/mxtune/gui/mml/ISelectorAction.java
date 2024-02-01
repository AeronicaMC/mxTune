package aeronicamc.mods.mxtune.gui.mml;

import aeronicamc.mods.mxtune.mxt.MXTuneFile;

import java.nio.file.Path;

/**
 * <p>Scarfed from MineTunes by Vazkii</p>
 * <p><a href="https://github.com/Vazkii/MineTunes">MineTunes</a></p>
 * <p><a ref="https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB">
 * Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0)</a></p>
 */
public interface ISelectorAction
{
    void select(Path path);

    void select(MXTuneFile mxTuneFile);
}
