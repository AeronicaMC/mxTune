package aeronicamc.mods.mxtune.gui.mml;

import aeronicamc.mods.mxtune.gui.widget.MXButton;
import aeronicamc.mods.mxtune.gui.widget.MXLabel;
import aeronicamc.mods.mxtune.gui.widget.MXLink;
import aeronicamc.mods.mxtune.gui.widget.MXTextFieldWidget;
import aeronicamc.mods.mxtune.items.MXScreen;
import aeronicamc.mods.mxtune.managers.PlayIdSupplier;
import aeronicamc.mods.mxtune.mxt.MXTuneFile;
import aeronicamc.mods.mxtune.sound.ClientAudio;
import aeronicamc.mods.mxtune.sound.IAudioStatusCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiMXT extends MXScreen implements IAudioStatusCallback
{
    private final Screen parent;

    private boolean isStateCached;
    private MXLabel labelMXTFileName;
    private String cachedMXTFilename;
    private MXLabel labelTitle;
    private MXLabel labelAuthor;
    private MXLabel labelSource;
    private MXLabel labelDuration;
    private MXTextFieldWidget textTitle;
    private String cachedTitle = "";
    private MXTextFieldWidget textAuthor;
    private String cachedAuthor = "";
    private MXTextFieldWidget textSource;
    private String cachedSource = "";
    private MXButton buttonPlayStop;
    private MXButton buttonDoneMode;
    private int durationTotal;
    private int ticks;

    // Links
    private MXLink sourcesLink;
    private MXLink mmlLink;

    // Common data
    private MXTuneFile mxTuneFile;
    private boolean isPlaying = false;
    private boolean cachedIsPlaying;
    private MXButton buttonSave;
    private static final int PADDING = 4;
    private final Mode mode;

    /* MML Player */
    private int playId = PlayIdSupplier.INVALID;

    // Child tabs
    private static final int MAX_TABS = 16;
    private static final int MIN_TABS = 1;
    private static final int TAB_BTN_IDX = 200;
//    private final GuiMXTPartTab[] childTabs = new GuiMXTPartTab[MAX_TABS];
    private int activeChildIndex;
    private int cachedActiveChildIndex;
    private MXButton buttonAddTab;
    private MXButton buttonMinusTab;
    private final MXButton[] buttonNames = new MXButton[MAX_TABS];

    // Tab limits - allow limiting the viewable tabs
    private int viewableTabCount = MIN_TABS;
    private int cachedViewableTabCount;

    public GuiMXT(Screen parent, Mode mode)
    {
        super(new TranslationTextComponent("gui.mxtune.gui_mxt.title"));
        this.parent = parent;
        this.mode = mode;
    }

    @Override
    public void statusCallBack(ClientAudio.Status status, int playId)
    {

    }

    public enum Mode
    {
        CLIENT, SERVER, SHEET_MUSIC
    }
}
