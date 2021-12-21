package aeronicamc.mods.mxtune.gui.mml;

import aeronicamc.mods.mxtune.mxt.MXTuneFile;

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

    @Nullable
    public MXTuneFile getMxTuneFile() { return mxTuneFile; }

    public void setCancel() { selector = SELECTOR.CANCEL; }

    public void setDone() { selector = SELECTOR.DONE; }

    public void setFileImport() { selector = SELECTOR.FILE_IMPORT; }

    public void setFileOpen() { selector = SELECTOR.FILE_OPEN; }

    public void setFileSave() { selector = SELECTOR.FILE_SAVE; }

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
        PASTE,
    }
}
