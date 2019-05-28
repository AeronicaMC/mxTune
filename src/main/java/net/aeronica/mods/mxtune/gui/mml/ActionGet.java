/*
 * Aeronica's mxTune MOD
 * Copyright 2019, Paul Boese a.k.a. Aeronica
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.aeronica.mods.mxtune.gui.mml;

import net.aeronica.mods.mxtune.mxt.MXTuneFile;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ActionGet implements ISelectorAction
{
    public static final ActionGet INSTANCE = new ActionGet();

    private Path path;
    private MXTuneFile mxTuneFile;
    private SELECTOR selector = SELECTOR.CANCEL;

    @Override
    public void select(Path path)
    {
        this.path = path;
    }

    @Override
    public void select(MXTuneFile mxTuneFile)
    {
        this.mxTuneFile = mxTuneFile;
    }

    public void clear()
    {
        path = null;
        mxTuneFile = null;
    }

    public Path getFileName() { return path != null ? path.getFileName() : Paths.get(""); }

    public String getFileNameString() { return path != null ? (path.getFileName().toString()) : ""; }

    @Nullable
    public Path getPath() { return path; }

    public MXTuneFile getMxTuneFile() { return mxTuneFile; }

    public void setCancel() { selector = SELECTOR.CANCEL; }

    public void setDone() { selector = SELECTOR.DONE; }

    public void setFileImport() { selector = SELECTOR.FILE_IMPORT; }

    public void setFileOpen() { selector = SELECTOR.FILE_OPEN; }

    public void setFileSave() { selector = SELECTOR.FILE_SAVE; }

    public void setFileSaveAs() { selector = SELECTOR.FILE_SAVE_AS; }

    public void setNewFile() { selector = SELECTOR.FILE_NEW; }

    public void setPaste() { selector = SELECTOR.PASTE; }

    public SELECTOR getSelector() { return selector; }

    public enum SELECTOR
    {
        CANCEL,
        DONE,
        FILE_IMPORT,
        FILE_NEW,
        FILE_OPEN,
        FILE_SAVE,
        FILE_SAVE_AS,
        PASTE,
    }
}
