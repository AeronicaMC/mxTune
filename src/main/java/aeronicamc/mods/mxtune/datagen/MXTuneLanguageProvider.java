package aeronicamc.mods.mxtune.datagen;

import aeronicamc.mods.mxtune.Reference;
import aeronicamc.mods.mxtune.init.ModBlocks;
import aeronicamc.mods.mxtune.init.ModEntities;
import aeronicamc.mods.mxtune.init.ModItems;
import aeronicamc.mods.mxtune.util.SoundFontProxyManager;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

public class MXTuneLanguageProvider extends LanguageProvider
{
    public MXTuneLanguageProvider(DataGenerator gen)
    {
        super(gen, Reference.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
        // TODO: remove raw testing translations
        add("block.mxtune.inv_test_block", "Inventory Test Block");
        add("container.mxtune.inv_test_block.more", "More...");
        add("gui.mxtune.gui_test.title", "Gui Test Screen");
        add("item.mxtune.music_item", "Music Item");
        add("item.mxtune.gui_test_item", "Wrench Test Item");

        add(ModEntities.MUSIC_SOURCE.get(), "[MusicSource]");

        addBlocks();
        addCommands();
        addConfigs();
        addEnums();
        addErrors();
        addGuiTexts();
        addItems();
        addKeys();
        addMessages();
        addSubtitles();
    }

    @Override
    public String getName()
    {
        return  Reference.MOD_NAME + " " +super.getName();
    }

    private void addBlocks()
    {
        // TODO
        add(ModBlocks.MUSIC_BLOCK.get(), "Music Block");
        add("container.mxtune.block_music.more", "%s More");
        addTooltip("block_music.help", "When placed in the world: SHIFT+Right-Click to OPEN. Right-Click to play. Add up to 16 instruments loaded with sheet music.");
        add(ModBlocks.MUSIC_VENUE_TOOL_BLOCK.get(), "Music Venue Marker");
    }

    private void addItems()
    {
        // TODO
        add(ModItems.SCRAP_ITEM.get(), "Paper Scraps");
        add(ModItems.SHEET_MUSIC.get(), "Sheet Music");
        add(ModItems.MUSIC_PAPER.get(), "Music Paper");
        addTooltip("music_paper.help", "Right-Click to OPEN");
        addTooltip("sheet_music.days_left", "Days left: %s");
        addTooltip("sheet_music.days_left_error", "Days left: nn");
        addTooltip("sheet_music.duration_error", "h:mm:ss");
        addTooltip("sheet_music.empty", " - Empty - ");
        addInstrumentNames(this);
        addTooltip("instrument_item.shift_help_01", "Hold SHIFT for HELP");
        addTooltip("instrument_item.shift_help_02", "SHIFT+Right Click to OPEN");
        addTooltip("instrument_item.shift_help_03", "Right Click to PLAY");
        add(ModItems.MUSIC_VENUE_TOOL.get(), "Music Venue Tool");
        addTooltip("music_venue_tool_item.shift_help_02", "Right Click two blocks to define an area");
        addTooltip("music_venue_tool_item.shift_help_03", "SHIFT+Right Click block to Reset back to Start");
        addTooltip("music_venue_tool_item.shift_help_04", "Makes Pretty boxes. Useless at this point. Work-in-progress! You cannot edit or delete them!");
        addTooltip("music_venue_tool_block.help_01", "WIP: Does nothing at this time :P");
    }

    private void addInstrumentNames(LanguageProvider provider)
    {
        SoundFontProxyManager.getProxyMapByIndex().forEach(
            (key, value) -> provider.add("item." + Reference.MOD_ID + "." +value.id,
                                         convertSnakeCaseToTitleCase(value.id)));
    }

    private String convertSnakeCaseToTitleCase(String input)
    {
        String temp = WordUtils.capitalizeFully(StringUtils.replace(input, "_", " "));
        return StringUtils.replace(temp,"Mabinogi", "(Mabi)");
    }

    private void addGuiTexts()
    {
        add("itemGroup.mxtune", "mxTune");
        addGuiText("gui_music_library.title", "Music Library");
        addGuiText("gui_file_importer.title", "File Importer");
        addGuiText("confirm.cancel.text01", "Any unsaved changes will be lost.");
        addGuiText("confirm.cancel.text02", "No to go back and Save. Yes to return to game.");
        addGuiText("confirm.new.text01", "This will clear the current music.");
        addGuiText("confirm.new.text02", "There is no undo feature. Are you sure?");
        addGuiText("label.author", "Author:");
        addGuiText("label.chord", "chord %s");
        addGuiText("label.melody", "melody %s");
        addGuiText("label.filename", "Filename: ");
        addGuiText("label.instruments", "Instruments");
        addGuiText("label.search", "Search");
        addGuiText("label.source", "Source URL:");
        addGuiText("label.status", "Status/Meta");
        addGuiText("label.title", "Title:");
        addGuiText("label.metadata", "Chars: %s, Duration: %s, Meta: %s");
        addGuiText("label.duration_value_total", "Duration All: ");

        addGuiText("button.help.help01", "Help");
        addGuiText("button.help.help02", "Hoover over buttons for tips.");
        addGuiText("button.help.help03", "SHIFT-Hoover over buttons for tips.");

        addGuiText("button.import", "Import");
        addGuiText("button.minus", "-");
        addGuiText("button.new", "New");
        addGuiText("button.open_folder", "Open Folder");
        addGuiText("button.open_folder.help01","Place MML files into this folder to be imported. Recognized types are: ");
        addGuiText("button.open_folder.help02"," .mml, .ms2mml, .zip (contains multiple .ms2mml files)");
        addGuiText("button.library", "Library");
        addGuiText("button.plus", "+");
        addGuiText("button.refresh", "Refresh");
        addGuiText("button.refresh.help01", "Only needed it the file list fails to refresh automatically after placing files into the folder.");
        addGuiText("button.clipboard_Copy_to", "Copy");
        addGuiText("button.clipboard_paste_from", "Paste");
        addGuiText("button.play", "Play");
        addGuiText("button.play_all", "Play All");
        addGuiText("button.play_part", "Play Part");
        addGuiText("button.save", "Save");
        addGuiText("button.stop", "Stop");
        addGuiText("button.select", "Select");
        addGuiText("button.upload", "Upload");
        addGuiText("button.write", "Write");

        addGuiText("button.lock.help01", "Lock");
        addGuiText("button.lock.help02", "[Unlocked]: Anyone can modify contents. View contents by RIGHT-CLICK-ing the block.");
        addGuiText("button.lock.help03", "[Locked]: Non-owners can view contents. View contents by SHIFT-RIGHT-CLICK-ing the block.");
        addGuiText("button.lock.help04", "Play Activation: [Unlocked] SHIFT-RIGHT-CLICK / [Locked] RIGHT-CLICK.");
        addGuiText("button.lock.help05", "Owners may break the block or. Settings and instruments are saved! The block item may be passed to another person. They become owner upon placing in the world.");
        addGuiText("button.lock.help06", "Owners may lock or unlock the contents. Owners can use a standard mod wrench to rotate or pick up the block.");
        addGuiText("button.lock.locked", "Locked");
        addGuiText("button.lock.unlocked", "Unlocked");

        addGuiText("button.back_rs_in.help01", "Back Side Redstone Input Toggle");
        addGuiText("button.back_rs_in.help02", "Enables or Disables Redstone input from the Backside.");
        addGuiText("button.back_rs_in.help03", "Plays when the input changes state from off to on.");
        addGuiText("button.back_rs_in.disabled", "Input Disabled");
        addGuiText("button.back_rs_in.enabled", "Input Enabled");

        addGuiText("button.left_rs_out.help01", "Left Side Redstone Output Toggle");
        addGuiText("button.left_rs_out.help02", "Enables or Disables Redstone output from the Left Side.");
        addGuiText("button.left_rs_out.help03", "Outputs a single 10 tick pulse at the end of a song/cancel.");
        addGuiText("button.left_rs_out.disabled", "Output Disabled");
        addGuiText("button.left_rs_out.enabled", "Output Enabled");

        addGuiText("button.right_rs_out.help01", "Right Side Redstone Output Toggle");
        addGuiText("button.right_rs_out.help02", "Enables or Disables Redstone output from the Right Side.");
        addGuiText("button.right_rs_out.help03", "Outputs a single 10 tick pulse at the end of a song/cancel.");
        addGuiText("button.right_rs_out.disabled", "Output Disabled");
        addGuiText("button.right_rs_out.enabled", "Output Enabled");

    }

    private void addCommands()
    {
        addCommand("music.dump", "Wrote %s records");
        addCommand("music.load", "Read %s records");
    }

    private void addConfigs()
    {
        addConfig("client.double_click_time_ms", "Double-click time in milliseconds for GUI widgets");
        addConfig("client.mml_Link", "MML Site Link");
        addConfig("server.listener_range", "Listener Range");
        addConfig("server.sheet_music_life_in_days", "Sheet Music Life in Days");
    }

    private void addEnums()
    {
        addEnum("tool_state.type.start", "Start");
        addEnum("tool_state.type.end", "End");
        addEnum("tool_state.type.done", "Done");
    }

    private void addErrors()
    {
        addError("midi_system_util.no_sound_bank_loaded","No SoundBank Loaded");
        addError("mml_server_side_validation_failure", "Server side music validation error");
        addError("sheet_music_too_old", "The Sheet Music is unreadable!");
        addError("sheet_music_write_failure", "Unable to write Sheet Music!");
    }

    private void addKeys()
    {
        addKey("open_party", "Open Party");
        addKey("open_music_options", "Music Options");
    }

    private void addMessages()
    {
        /* unused */
        addMessage("master_record_sound_off","mxTune Music Plays: Records and/or Master volume(s) are off." );
    }

    private void addSubtitles()
    {
        addSubtitle("pcm-proxy", "Music Plays");
        addSubtitle("failure", "Oops");
        addSubtitle("crumple_paper", "Crumple Paper");
        addSubtitle("rotate_block", "Rotate Block");
        addSubtitle("rotate_block_failed", "Rotate Block Failed");
    }

    /**
     * prepends tooltip.[mod_id].
     * @param identifier the unique help id
     * @param helpText help text
     */
    private void addTooltip(String identifier, String helpText)
    {
        add(String.format("tooltip.%s.%s", Reference.MOD_ID, identifier), helpText);
    }

    /**
     * prepends gui.[mod_id].
     * @param identifier the unique help id
     * @param text of the gui element
     */
    private void addGuiText(String identifier, String text)
    {
        add(String.format("gui.%s.%s", Reference.MOD_ID, identifier), text);
    }

    /**
     * prepends commands.[mod_id].
     * @param identifier the unique command id
     * @param text of the command
     */
    private void addCommand(String identifier, String text)
    {
        add(String.format("commands.%s.%s", Reference.MOD_ID, identifier), text);
    }

    /**
     * prepends block.[mod_id].
     * @param identifier the unique block id
     * @param text of the command
     */
    private void addBlock(String identifier, String text)
    {
        add(String.format("block.%s.%s", Reference.MOD_ID, identifier), text);
    }

    /**
     * prepends config.[mod_id].
     * @param identifier the unique command id
     * @param text of the config item
     */

    private void addConfig(String identifier, String text)
    {
        add(String.format("config.%s.%s", Reference.MOD_ID, identifier), text);
    }

    /**
     * prepends enum.[mod_id].
     * @param identifier the unique error id
     * @param text of the enum
     */
    private void addEnum(String identifier, String text)
    {
        add(String.format("enum.%s.%s", Reference.MOD_ID, identifier), text);
    }

    /**
     * prepends errors.[mod_id].
     * @param identifier the unique error id
     * @param text of the error
     */
    private void addError(String identifier, String text)
    {
        add(String.format("errors.%s.%s", Reference.MOD_ID, identifier), text);
    }

    /**
     * prepends key.[mod_id].
     * @param identifier the unique error id
     * @param text of the error
     */
    private void addKey(String identifier, String text)
    {
        add(String.format("key.%s.%s", Reference.MOD_ID, identifier), text);
    }

    /**
     * prepends message.[mod_id].
     * @param identifier the unique error id
     * @param text of the error
     */
    private void addMessage(String identifier, String text)
    {
        add(String.format("message.%s.%s", Reference.MOD_ID, identifier), text);
    }

    /**
     * prepends subtitle.[mod_id].
     * @param identifier the unique error id
     * @param text of the subtitle
     */
    private void addSubtitle(String identifier, String text)
    {
        add(String.format("subtitle.%s.%s", Reference.MOD_ID, identifier), text);
    }
}
