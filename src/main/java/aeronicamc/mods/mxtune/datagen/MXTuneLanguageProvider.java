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
        add("gui.mxtune.gui_test.title", "Gui Test Screen");

        add(ModEntities.MUSIC_SOURCE.get(), "[MusicSource]");

        addBlocks();
        addChats();
        addCommands();
        addConfigs();
        addEntities();
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
        add(ModBlocks.MUSIC_BLOCK.get(), "Music Block");
        add("container.mxtune.block_music.more", "%s More");
        addTooltip("block_music.help", "When placed in the world: SHIFT+Right-Click to OPEN. Right-Click to play. Add up to 16 instruments loaded with sheet music.");
    }

    private void addItems()
    {
        add(ModItems.SCRAP_ITEM.get(), "Paper Scraps");
        add(ModItems.SHEET_MUSIC.get(), "Sheet Music");
        add(ModItems.MUSIC_SCORE.get(), "Music Score");
        add(ModItems.MUSIC_PAPER.get(), "Music Paper");
        add(ModItems.PLACARD_ITEM.get(), "Placard State");
        add(ModItems.MUSIC_VENUE_INFO.get(), "Music Venue Info Panel");
        add(ModItems.WRENCH.get(), "Wooden Wrench (mxTune)");
        addTooltip("music_paper.help", "Right-Click to OPEN");
        addTooltip("music_score.shift_parts_01", "Hold SHIFT to see parts");
        addTooltip("music_score.n_part_score", "%s Part Score");
        addTooltip("music_score.parts_error", "No parts found");
        addTooltip("sheet_music.days_left", "Days left: %s");
        addTooltip("sheet_music.duration_error", "h:mm:ss");
        addTooltip("sheet_music.empty", " - Empty - ");
        addTooltip("sheet_music.n_of_m_instrument_name", "%s of %s %s");
        addInstrumentNames(this);
        addTooltip("instrument_item.shift_help_01", "Hold SHIFT for HELP");
        addTooltip("instrument_item.shift_help_02", "SHIFT+Right Click to OPEN");
        addTooltip("instrument_item.shift_help_03", "Right Click to PLAY");
        add(ModItems.MUSIC_VENUE_TOOL.get(), "Music Venue Tool");
        addTooltip("music_venue_tool_item.shift_help_02", "Right Click two blocks to define a Venue. Minimum size 2x2x2. Venues cannot intersect.");
        addTooltip("music_venue_tool_item.shift_help_03", "SHIFT+Right Click block to Reset back to Start.");
        addTooltip("music_venue_tool_item.shift_help_04", "To REMOVE a venue, stand *inside* it and Right Click any block. *(Head and Body)*");
        addTooltip("music_venue_tool_item.shift_help_05", "Survival: Owners may remove their own Venues. OP/Creative players can remove any Venue.");
        addTooltip("music_venue_tool_item.shift_help_06", "Venues are only visible when this tool is on the HotBar.");
        addTooltip("wrench_item.shift_help_01","Music Block:");
        addTooltip("wrench_item.shift_help_02","  Right-Click rotates front to the clicked face.");
        addTooltip("wrench_item.shift_help_03","  SHIFT-Right-Click picks up into inventory.");


        addTooltip("music_venue_tool_block.help_01", "Used to manage mxTune Venues. mxTunes played within a Venue are only heard by players while inside. mxTunes played Outside the Venue are not heard by players within.");
    }

    private void addInstrumentNames(LanguageProvider provider)
    {
        SoundFontProxyManager.getProxyMapByIndex().forEach(
            (key, value) -> provider.add("item." + Reference.MOD_ID + "." +value.id,
                                         convertSnakeCaseToTitleCase(value.id)));
    }

    private String convertSnakeCaseToTitleCase(String input)
    {
        //noinspection deprecation
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

        // ModGuiHelper, GuiHelpButton, generic texts
        addGuiText("button.help.help01", "Help");
        addGuiText("button.help.help02", "Hoover over buttons for tips.");
        addGuiText("button.help.help03", "SHIFT-Hoover over buttons for tips.");
        addGuiText("button.enabled", "Enabled");
        addGuiText("button.disabled", "Disabled");

        // GuiMXT, GuiMXTPartTab, GuiFileImporter, GuiMusicLibrary
        addGuiText("button.import", "Import");
        addGuiText("button.import.help01", "Supports 3MLE .mml, Maple Story 2 .ms2mml, and zipped .ms2mml multipart files.");
        addGuiText("button.minus", "-");
        addGuiText("button.minus.help01", "Hides the last right most Instrument Part Tab.");
        addGuiText("button.minus.help02", "Hidden tabs do not lose their contents. Hidden tabs are not saved or played.");
        addGuiText("button.minus_chord", "-");
        addGuiText("button.minus_chord.help01", "Hides the bottom most chord line.");
        addGuiText("button.minus_chord.help02", "Hidden lines do not lose their contents. Hidden lines are not saved, played or copied.");
        addGuiText("button.new", "New");
        addGuiText("button.new.help01", "Clears all text and MML. Will ask to save.");
        addGuiText("button.open_folder", "Open Folder");
        addGuiText("button.open_folder.help01","Place MML files into this folder to be imported. Recognized types are: ");
        addGuiText("button.open_folder.help02"," .mml, .ms2mml, .zip (contains multiple .ms2mml files)");
        addGuiText("button.library", "Library");
        addGuiText("button.library.help01", "Select and load a saved .mxt file");
        addGuiText("button.plus", "+");
        addGuiText("button.plus.help01", "Add an Instrument Part Tab at the end (right most).");
        addGuiText("button.plus_chord", "+");
        addGuiText("button.plus_chord.help01", "Add an Instrument chord line at the bottom.");
        addGuiText("button.refresh", "Refresh");
        addGuiText("button.refresh.help01", "Click if the list doesn't refresh automatically after placing files into the folder.");
        addGuiText("button.clipboard_copy_to", "Copy");
        addGuiText("button.clipboard_copy_to.help01", "Copy MML to the clipboard in Mabinogi Format.");
        addGuiText("button.clipboard_paste_from", "Paste");
        addGuiText("button.clipboard_paste_from.help01", "Paste MML in Mabinogi Clipboard Format.");

        addGuiText("button_order.normal", "Off");
        addGuiText("button_order.normal.help01", "Sort off.");
        addGuiText("button_order.a_to_z", "A-Z");
        addGuiText("button_order.a_to_z.help01", "Ascending order.");
        addGuiText("button_order.z_to_a", "Z-A");
        addGuiText("button_order.z_to_a.help01", "Descending order.");

        addGuiText("button.play", "Play");
        addGuiText("button.play_all", "Play All");
        addGuiText("button.play_part", "Play Part");
        addGuiText("button.save", "Save");
        addGuiText("button.save.help01", "Save a new file or overwrite existing.");
        addGuiText("button.save.help02", "The actual filename is derived from the Title field with the .mxt extension appended.");
        addGuiText("button.save.help03", "A Title and at least one instrument tab must have a melody line. Empty instrument tabs are not allowed.");
        addGuiText("button.stop", "Stop");
        addGuiText("button.select", "Select");
        addGuiText("button.upload", "Upload");
        addGuiText("button.write_sheet_music", "Make Sheet Music");
        addGuiText("button.write_sheet_music.help01", "Consumes 1 sheet of Music Paper per instrument tab. A stack of 16 Music Paper is needed to make 16 parts.");
        addGuiText("button.write_sheet_music.help02", "The title, numerical part of parts, instrument name, and part duration is written on the Sheet Music.");
        addGuiText("button.write_music_score", "Make Music Score");

        // MusicBlockScreen, GuiLockButton
        addGuiText("button.lock.help01", "Lock");
        addGuiText("button.lock.help02", "[Unlocked]: Anyone can modify contents. View contents by RIGHT-CLICK-ing the block.");
        addGuiText("button.lock.help03", "[Locked]: Non-owners can view contents. View contents by SHIFT-RIGHT-CLICK-ing the block.");
        addGuiText("button.lock.help04", "Play Activation: [Unlocked] SHIFT-RIGHT-CLICK / [Locked] RIGHT-CLICK.");
        addGuiText("button.lock.help05", "Owners may break the block. Settings and instruments are saved! The block item may be passed to another person. They become owner upon placing in the world.");
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

        addGuiText("button.multi_inst_screen.instrument_chooser.help01", "Instrument Chooser");
        addGuiText("button.multi_inst_screen.instrument_chooser.help02", "Choose the instrument in to use for playing sheet music.");
        addGuiText("button.multi_inst_screen.instrument_chooser.help03", "Disabled when Auto Select is on.");
        addGuiText("button.multi_inst_screen.instrument_chooser.open_group", "Open Group");
        addGuiText("switch.multi_inst_screen.auto_select_on", "Auto Select on");
        addGuiText("switch.multi_inst_screen.auto_select_off", "Auto Select off");
        addGuiText("switch.multi_inst_screen.auto_select.help01", "Auto Select Instrument");
        addGuiText("switch.multi_inst_screen.auto_select.help02", "On: Changes to the instrument specified by the Sheet Music in the inventory slot.");
        addGuiText("switch.multi_inst_screen.auto_select.help03", "Off: Uses the instrument shown on the selector button.");

        // GuiPin
        addGuiText("gui_pin.group_disbanded","Group Disbanded");
        addGuiText("gui_pin.leaders_group", "%s's Group");
        addGuiText("gui_pin.unexpected_error", "*** Unexpected Error ***");

        // GuiGroup
        addGuiText("button.make_group", "Make Group");
        addGuiText("button.disband", "Disband");
        addGuiText("label.pin", "Pin:");
        addGuiText("label.mode", "Access:");
        addGuiText("button.make_group.help01", "Make Group");
        addGuiText("button.make_group.help02", "Creates a new group with you as the leader.");
        addGuiText("button.make_group.help03", "Button disappears when clicked.");
        addGuiText("button.new_pin.help01", "Enabled: Click to generate a new pin. The server ensures no active groups will have the same pin.");
        addGuiText("button.new_pin.help02", "Disabled: When the Access mode is Open.");
        addGuiText("button.disband.help01",  "Disband");
        addGuiText("button.disband.help02",  "Click to remove all members and close the group.");
        addGuiText("button.disband.help03",  "Disabled: If your group does not exist or if you are not the leader.");

        addGuiText("button.member_promote.help01", "Promote: ");
        addGuiText("button.member_promote.help02", "Promotes the member to leader of the group.");
        addGuiText("button.member_promote.help03", "Only the leader may promote another member.");
        addGuiText("button.member_remove.help01", "Remove: ");
        addGuiText("button.member_remove.help02", "Remove the member from the group.");
        addGuiText("button.member_remove.help03", "Only the member themselves or the leader may remove. If the leader is the only member, then if the leader removes themself the group is removed too.");
    }

    private void addChats()
    {
        addChat("groupManager.you_joined_players_group", "You joined %s's group.");
        addChat("groupManager.player_joined_the_group", "%s joined the group.");
        addChat("groupManager.cannot_join_too_many", "Cannot join %s group. It is full.");
        addChat("groupManager.player_cannot_join_too_many", "%s cannot join since group is full");
        addChat("groupManager.invalid_leader", "Leader left, was demoted or group disbanded.");
        addChat("groupManager.member_left_group", "%s has left the group.");
    }

    private void addCommands()
    {
        addCommand("music.dump", "Wrote %s records");
        addCommand("music.load", "Read %s records");
        addCommand("music.convert", "Dump converted to %s files");
    }

    private void addConfigs()
    {
        addConfig("client.double_click_time_ms", "Double-click time in milliseconds for GUI widgets");
        addConfig("client.mml_Link", "MML Site Link");
        addConfig("server.listener_range", "Listener Range");
        addConfig("server.sheet_music_expires", "Sheet Music Expires");
        addConfig("server.sheet_music_life_in_days", "Sheet Music Life in Days.");
    }

    private void addEntities()
    {
        addEntity("music_venue_info", "Music Venue Info Panel");
    }

    private void addEnums()
    {
        addEnum("tool_state.type.start", "Start");
        addEnum("tool_state.type.end", "End");
        addEnum("tool_state.type.done", "Done");
        addEnum("tool_state.type.remove", "Remove");

        addEnum("group.mode.pin", "Pin");
        addEnum("group.mode.pin.help01", "Players must know the pin to join your group.");
        addEnum("group.mode.pin.help02", "Click to toggle access mode.");
        addEnum("group.mode.open", "Open");
        addEnum("group.mode.open.help01", "Anyone can join your group.");
        addEnum("group.mode.open.help02", "Click to toggle access mode.");
    }

    private void addErrors()
    {
        addError("midi_system_util.no_sound_bank_loaded","No SoundBank Loaded");
        addError("mml_server_side_validation_failure", "Server side music validation error");
        addError("sheet_music_too_old", "The Sheet Music is unreadable!");
        addError("sheet_music_not_present", "No instrument(s) with sheet music present in Music Block!");
        addError("sheet_music_write_failure", "Unable to write Sheet Music!");
    }

    private void addKeys()
    {
        addKey("open_group", "Open Group");
        addKey("open_music_options", "Music Options");
    }

    private void addMessages()
    {
        addMessage("master_record_sound_off","mxTune Music Plays: Records and/or Master volume(s) are off." );
        addMessage("existing_venue_error", "Cannot choose a position in an existing venue.");
        addMessage("intersects_venue", "Intersects existing venue.");
        addMessage("same_block_error", "Same block not allowed.");
        addMessage("venue_too_small", "Too small. Must be at least 2x2x2.");
        addMessage("not_owner_of_venue", "Only owner, admin or player in creative Mode can remove a venue.");
        addMessage("groupManager.cannot_sleep_when_grouped", "Can't sleep when in a group.");
        addMessage("groupManager.created_group", "You created a group.");
        addMessage("groupManager.cannot_create_group", "You're already in a group.");
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
     * prepends chat.[mod_id].
     * @param identifier the unique help id
     * @param helpText help text
     */
    private void addChat(String identifier, String helpText)
    {
        add(String.format("chat.%s.%s", Reference.MOD_ID, identifier), helpText);
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
    @SuppressWarnings("unused")
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
     * prepends entity.[mod_id].
     * @param identifier the unique error id
     * @param text of the entity
     */
    private void addEntity(String identifier, String text)
    {
        add(String.format("entity.%s.%s", Reference.MOD_ID, identifier), text);
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
